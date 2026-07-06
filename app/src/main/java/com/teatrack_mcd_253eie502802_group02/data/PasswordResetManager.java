package com.teatrack_mcd_253eie502802_group02.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

    private static final String TAG = "PasswordResetManager";
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
        database.goOnline();
        usersRef = database.getReference(USERS_NODE);
        otpRef = database.getReference(OTP_NODE);
        appConfigRef = database.getReference(APP_CONFIG_NODE);
        usersRef.keepSynced(true);
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

        usersRef.get()
                .addOnSuccessListener(snapshot -> handleUsersSnapshotForOtp(snapshot, cleanEmail, callback))
                .addOnFailureListener(error -> {
                    Log.w(TAG, "Server read Users failed, falling back to cached listener", error);
                    usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            handleUsersSnapshotForOtp(snapshot, cleanEmail, callback);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            if (callback != null) {
                                callback.onError(context.getString(R.string.forgot_password_server_error));
                            }
                        }
                    });
                });
    }

    private void handleUsersSnapshotForOtp(@NonNull DataSnapshot snapshot, String cleanEmail, Callback callback) {
        Log.d(TAG, "Scanning Users for email=" + cleanEmail
                + ", exists=" + snapshot.exists()
                + ", children=" + snapshot.getChildrenCount());

        String matchedEmail = findExistingUserEmail(snapshot, cleanEmail);
        if (matchedEmail.isEmpty()) {
            Log.w(TAG, "No matching user email found for " + cleanEmail);
            callback.onError(context.getString(R.string.forgot_password_error_not_found));
            return;
        }

        Log.d(TAG, "Matched user email=" + matchedEmail);
        String otpEmail = cleanEmail(matchedEmail);
        String otp = generateOtp();
        long now = System.currentTimeMillis();
        Map<String, Object> otpData = new HashMap<>();
        otpData.put("email", otpEmail);
        otpData.put("to", otpEmail);
        otpData.put("otp", otp);
        otpData.put("subject", context.getString(R.string.password_reset_email_subject));
        otpData.put("html", buildEmailHtml(otp));
        otpData.put("status", "pending");
        otpData.put("createdAt", now);
        otpData.put("expiresAt", now + OTP_TTL_MS);
        otpData.put("verified", false);

        otpRef.child(emailKey(otpEmail)).setValue(otpData)
                .addOnSuccessListener(unused -> {
                    callback.onSuccess();
                    sendOtpEmail(otpEmail, null);
                })
                .addOnFailureListener(e -> callback.onError(context.getString(R.string.forgot_password_server_error)));
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
                if (callback != null) callback.onError(context.getString(R.string.forgot_password_server_error));
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
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "<style>"
                + "body{font-family:'Segoe UI',Arial,sans-serif;background:#eef5ff;margin:0;padding:24px 20px;}"
                + ".container{max-width:520px;margin:0 auto;background:#ffffff;border-radius:18px;box-shadow:0 10px 30px rgba(0,136,255,0.12);overflow:hidden;border:1px solid #0088ff;}"
                + ".header{background:#0088FF;color:#ffffff;padding:32px 24px;text-align:center;}"
                + ".header h2{margin:0;font-size:26px;font-weight:700;letter-spacing:1px;}"
                + ".content{padding:36px 30px;text-align:center;background:#ffffff;}"
                + ".message{color:#4b5563;font-size:16px;line-height:1.7;margin-bottom:18px;}"
                + ".otp-code{font-size:44px;font-weight:800;letter-spacing:12px;color:#0088FF;margin:28px 0;padding:20px 28px;background:#f0f7ff;border-radius:14px;display:inline-block;border:2px dashed #0088ff;}"
                + ".footer{background:#ffffff;padding:22px;text-align:center;color:#6b7280;font-size:14px;line-height:1.6;border-top:1px solid #0088ff;}"
                + ".footer a{color:#0088FF;text-decoration:none;font-weight:700;}"
                + ".footer a:hover{text-decoration:underline;}"
                + ".brand{font-size:13px;color:#9ca3af;letter-spacing:0.5px;}"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class=\"container\">"
                + "<div class=\"header\">"
                + "<h2>" + escape(context.getString(R.string.password_reset_email_title)) + "</h2>"
                + "</div>"
                + "<div class=\"content\">"
                + "<div class=\"message\">" + escape(context.getString(R.string.password_reset_email_greeting)) + "</div>"
                + "<div class=\"message\">" + escape(context.getString(R.string.password_reset_email_intro)) + "</div>"
                + "<div class=\"otp-code\">" + escape(otp) + "</div>"
                + "<div class=\"message\">"
                + escape(context.getString(R.string.password_reset_email_valid_minutes)) + " <strong>"
                + escape(context.getString(R.string.password_reset_email_minutes)) + "</strong>.<br>"
                + escape(context.getString(R.string.password_reset_email_do_not_share))
                + "</div>"
                + "<div class=\"message\">" + escape(context.getString(R.string.password_reset_email_ignore)) + "</div>"
                + "</div>"
                + "<div class=\"footer\">"
                + "&copy; 2026 Hồng Trà Ngô Gia<br>"
                + "<a href=\"https://teatrackcustomer-sable.vercel.app/\">hongtrangogia.vn</a>"
                + "<div class=\"brand\">Tinh hoa trà Việt - Đậm vị truyền thống</div>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
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

    private String normalizeEmail(String email) {
        return cleanEmail(email).toLowerCase(Locale.US);
    }

    private String findExistingUserEmail(DataSnapshot usersSnapshot, String inputEmail) {
        String target = normalizeEmail(inputEmail);
        if (target.isEmpty() || !usersSnapshot.exists()) {
            return "";
        }

        for (DataSnapshot userSnapshot : usersSnapshot.getChildren()) {
            String matchedEmail = matchingEmailFromUser(userSnapshot, target);
            if (!matchedEmail.isEmpty()) {
                return matchedEmail;
            }
        }
        return "";
    }

    private String matchingEmailFromUser(DataSnapshot userSnapshot, String normalizedTarget) {
        String[] emailFields = {"email", "Email", "userEmail", "emailAddress"};
        for (String field : emailFields) {
            String candidate = valueAsString(userSnapshot.child(field));
            if (!candidate.isEmpty()) {
                Log.d(TAG, "User " + userSnapshot.getKey() + " " + field + "=" + cleanEmail(candidate));
            }
            if (normalizeEmail(candidate).equals(normalizedTarget)) {
                return cleanEmail(candidate);
            }
        }

        String keyCandidate = userSnapshot.getKey();
        if (normalizeEmail(keyCandidate).equals(normalizedTarget)) {
            return cleanEmail(keyCandidate);
        }
        return "";
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
