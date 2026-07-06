package com.teatrack_mcd_253eie502802_group02.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PasswordResetManager {

    private static final String USERS_NODE = "Users";
    private static final String OTP_NODE = "otp";
    private static final String APP_CONFIG_NODE = "appConfig";
    private static final String MAIL_SERVER_BASE_URL_KEY = "mailServerBaseUrl";
    private static final String LOCAL_MAIL_SERVER_BASE_URL = "http://10.0.2.2:3000";
    private static final String SEND_OTP_EMAIL_PATH = "/api/auth/send-otp-email";
    private static final long OTP_TTL_MS = 10 * 60 * 1000L;
    private static final int MAIL_SERVER_TIMEOUT_MS = 90_000;

    private final Context context;
    private final DatabaseReference usersRef;
    private final DatabaseReference otpRef;
    private final DatabaseReference appConfigRef;
    private final SecureRandom secureRandom = new SecureRandom();
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public PasswordResetManager(Context context) {
        this.context = context.getApplicationContext();
        FirebaseDatabase database = FirebaseDatabase.getInstance(FirebaseProductRepository.DB_URL);
        usersRef = database.getReference(USERS_NODE);
        otpRef = database.getReference(OTP_NODE);
        appConfigRef = database.getReference(APP_CONFIG_NODE);
    }

    public interface Callback {
        void onSuccess();

        void onError(String message);
    }

    public void sendOtp(String email, Callback callback) {
        String cleanEmail = cleanEmail(email);
        if (cleanEmail.isEmpty()) {
            callback.onError(context.getString(R.string.forgot_password_error_empty));
            return;
        }

        Query query = usersRef.orderByChild("email").equalTo(cleanEmail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onError(context.getString(R.string.forgot_password_error_not_found));
                    return;
                }

                String otp = generateOtp();
                long now = System.currentTimeMillis();
                Map<String, Object> otpData = new HashMap<>();
                otpData.put("email", cleanEmail);
                otpData.put("to", cleanEmail);
                otpData.put("otp", otp);
                otpData.put("subject", context.getString(R.string.password_reset_email_subject));
                otpData.put("html", buildEmailHtml(otp));
                otpData.put("status", "pending");
                otpData.put("createdAt", now);
                otpData.put("expiresAt", now + OTP_TTL_MS);
                otpData.put("verified", false);

                otpRef.child(emailKey(cleanEmail)).setValue(otpData)
                        .addOnSuccessListener(unused -> {
                            // Chuyển màn hình ngay khi lưu DB xong để tránh bị timeout chặn UI
                            callback.onSuccess();
                            // Gửi email ngầm ở background
                            sendOtpEmail(cleanEmail, null);
                        })
                        .addOnFailureListener(e -> callback.onError(context.getString(R.string.forgot_password_server_error)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(context.getString(R.string.forgot_password_server_error));
            }
        });
    }

    public void verifyOtp(String email, String otp, Callback callback) {
        String cleanEmail = cleanEmail(email);
        String cleanOtp = otp == null ? "" : otp.trim();
        if (cleanEmail.isEmpty() || cleanOtp.isEmpty()) {
            callback.onError(context.getString(R.string.otp_error_missing));
            return;
        }

        otpRef.child(emailKey(cleanEmail)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onError(context.getString(R.string.otp_error_invalid_or_expired));
                    return;
                }

                String savedOtp = valueAsString(snapshot.child("otp"));
                long expiresAt = valueAsLong(snapshot.child("expiresAt"));
                if (expiresAt <= System.currentTimeMillis()) {
                    snapshot.getRef().removeValue();
                    callback.onError(context.getString(R.string.otp_error_expired));
                    return;
                }

                if (!cleanOtp.equals(savedOtp)) {
                    callback.onError(context.getString(R.string.otp_error_invalid_or_expired));
                    return;
                }

                Map<String, Object> updates = new HashMap<>();
                updates.put("verified", true);
                updates.put("verifiedAt", System.currentTimeMillis());
                snapshot.getRef().updateChildren(updates)
                        .addOnSuccessListener(unused -> callback.onSuccess())
                        .addOnFailureListener(e -> callback.onError(context.getString(R.string.forgot_password_server_error)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(context.getString(R.string.forgot_password_server_error));
            }
        });
    }

    public void requireVerifiedOtp(String email, Callback callback) {
        String cleanEmail = cleanEmail(email);
        if (cleanEmail.isEmpty()) {
            callback.onError(context.getString(R.string.reset_password_error_request_otp_first));
            return;
        }

        otpRef.child(emailKey(cleanEmail)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean verified = Boolean.TRUE.equals(snapshot.child("verified").getValue(Boolean.class));
                long expiresAt = valueAsLong(snapshot.child("expiresAt"));
                if (!snapshot.exists() || !verified) {
                    callback.onError(context.getString(R.string.reset_password_error_request_otp_first));
                    return;
                }

                if (expiresAt <= System.currentTimeMillis()) {
                    snapshot.getRef().removeValue();
                    callback.onError(context.getString(R.string.otp_error_expired));
                    return;
                }

                callback.onSuccess();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(context.getString(R.string.forgot_password_server_error));
            }
        });
    }

    public void clearOtp(String email) {
        String cleanEmail = cleanEmail(email);
        if (!cleanEmail.isEmpty()) {
            otpRef.child(emailKey(cleanEmail)).removeValue();
        }
    }

    private void sendOtpEmail(String email, Callback callback) {
        otpRef.child(emailKey(email)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String subject = valueAsString(snapshot.child("subject"));
                String html = valueAsString(snapshot.child("html"));
                if (!snapshot.exists() || subject.isEmpty() || html.isEmpty()) {
                    if (callback != null) callback.onError(context.getString(R.string.forgot_password_server_error));
                    return;
                }

                loadMailServerBaseUrl(new MailServerUrlCallback() {
                    @Override
                    public void onUrlLoaded(String baseUrl) {
                        networkExecutor.execute(() -> {
                            try {
                                postOtpEmail(baseUrl, email, subject, html);
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("status", "sent");
                                updates.put("sentAt", System.currentTimeMillis());
                                updates.put("updatedAt", System.currentTimeMillis());
                                otpRef.child(emailKey(email)).updateChildren(updates);
                                if (callback != null) mainHandler.post(callback::onSuccess);
                            } catch (Exception e) {
                                markEmailFailed(email, e.getMessage());
                                if (callback != null) mainHandler.post(() -> callback.onError(context.getString(R.string.forgot_password_server_error)));
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        markEmailFailed(email, message);
                        if (callback != null) callback.onError(context.getString(R.string.forgot_password_server_error));
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(context.getString(R.string.forgot_password_server_error));
            }
        });
    }

    private void loadMailServerBaseUrl(MailServerUrlCallback callback) {
        appConfigRef.child(MAIL_SERVER_BASE_URL_KEY).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String baseUrl = valueAsString(snapshot);
                if (baseUrl.isEmpty()) {
                    baseUrl = LOCAL_MAIL_SERVER_BASE_URL;
                }
                callback.onUrlLoaded(trimTrailingSlash(baseUrl));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    private void postOtpEmail(String baseUrl, String to, String subject, String html) throws Exception {
        String fullUrl = baseUrl + SEND_OTP_EMAIL_PATH;
        android.util.Log.d("PasswordResetManager", "Calling mail server: " + fullUrl);

        HttpURLConnection connection = (HttpURLConnection) new URL(fullUrl).openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(MAIL_SERVER_TIMEOUT_MS);
        connection.setReadTimeout(MAIL_SERVER_TIMEOUT_MS);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        // Sử dụng JSONObject để đảm bảo định dạng JSON chuẩn
        org.json.JSONObject jsonBody = new org.json.JSONObject();
        jsonBody.put("to", to);
        jsonBody.put("subject", subject);
        jsonBody.put("html", html);

        String body = jsonBody.toString();

        try (OutputStream os = connection.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = connection.getResponseCode();
        android.util.Log.d("PasswordResetManager", "Mail server response code: " + responseCode);

        if (responseCode < 200 || responseCode >= 300) {
            throw new IllegalStateException("Mail server returned HTTP " + responseCode);
        }
        connection.disconnect();
    }

    private void markEmailFailed(String email, String message) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "failed");
        updates.put("error", message);
        updates.put("updatedAt", System.currentTimeMillis());
        otpRef.child(emailKey(email)).updateChildren(updates);
    }

    private String trimTrailingSlash(String value) {
        String cleanValue = value == null ? "" : value.trim();
        while (cleanValue.endsWith("/")) {
            cleanValue = cleanValue.substring(0, cleanValue.length() - 1);
        }
        return cleanValue;
    }

    private String buildEmailHtml(String otp) {
        return "<div style=\"font-family:Arial,sans-serif;line-height:1.6;color:#1f2937;max-width:520px;margin:0 auto;padding:24px;\">"
                + "<h2 style=\"color:#0b86f6;margin:0 0 16px;\">" + escape(context.getString(R.string.password_reset_email_title)) + "</h2>"
                + "<p>" + escape(context.getString(R.string.password_reset_email_greeting)) + "</p>"
                + "<p>" + escape(context.getString(R.string.password_reset_email_intro)) + "</p>"
                + "<div style=\"font-size:30px;font-weight:700;letter-spacing:6px;color:#0b86f6;background:#f0f8ff;padding:18px;text-align:center;border-radius:10px;margin:20px 0;\">"
                + escape(otp)
                + "</div>"
                + "<p>" + escape(context.getString(R.string.password_reset_email_valid_minutes)) + " <strong>"
                + escape(context.getString(R.string.password_reset_email_minutes)) + "</strong>.</p>"
                + "<p>" + escape(context.getString(R.string.password_reset_email_do_not_share)) + "</p>"
                + "<p style=\"color:#6b7280;font-size:13px;\">" + escape(context.getString(R.string.password_reset_email_ignore)) + "</p>"
                + "</div>";
    }

    private String generateOtp() {
        return String.format(Locale.US, "%06d", secureRandom.nextInt(1_000_000));
    }

    private String emailKey(String email) {
        String normalized = cleanEmail(email).toLowerCase(Locale.US);
        return Base64.encodeToString(
                normalized.getBytes(StandardCharsets.UTF_8),
                Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING
        );
    }

    private String cleanEmail(String email) {
        return email == null ? "" : email.trim();
    }

    private String valueAsString(DataSnapshot snapshot) {
        Object value = snapshot.getValue();
        return value == null ? "" : String.valueOf(value);
    }

    private long valueAsLong(DataSnapshot snapshot) {
        Object value = snapshot.getValue();
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException ignored) {
                return 0L;
            }
        }
        return 0L;
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private interface MailServerUrlCallback {
        void onUrlLoaded(String baseUrl);

        void onError(String message);
    }
}
