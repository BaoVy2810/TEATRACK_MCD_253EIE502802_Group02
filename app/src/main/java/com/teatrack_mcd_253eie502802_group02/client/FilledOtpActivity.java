package com.teatrack_mcd_253eie502802_group02.client;

import android.graphics.Paint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.teatrack_mcd_253eie502802_group02.R;

import java.util.ArrayList;
import java.util.List;

public class FilledOtpActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "extra_email";

    private static final int OTP_LENGTH = 6;
    private static final long COUNTDOWN_MS = 60_000L;

    private TextView tvEmailMasked;
    private TextView tvCountdown;
    private TextView tvResendOtp;
    private Button btnVerifyOtp;

    private final List<EditText> otpFields = new ArrayList<>();
    private CountDownTimer countDownTimer;
    private boolean isPasting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filled_otp);

        tvEmailMasked = findViewById(R.id.tvEmailMasked);
        tvCountdown = findViewById(R.id.tvCountdown);
        tvResendOtp = findViewById(R.id.tvResendOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);

        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvBackLink = findViewById(R.id.tvBackLink);

        tvResendOtp.setPaintFlags(tvResendOtp.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        collectOtpFields();
        setupMaskedEmail();
        setupOtpInputs();
        setupActions(btnBack, tvBackLink);

        startCountdown();
        updateVerifyButtonState();
    }

    private void collectOtpFields() {
        otpFields.clear();
        otpFields.add(findViewById(R.id.etOtp1));
        otpFields.add(findViewById(R.id.etOtp2));
        otpFields.add(findViewById(R.id.etOtp3));
        otpFields.add(findViewById(R.id.etOtp4));
        otpFields.add(findViewById(R.id.etOtp5));
        otpFields.add(findViewById(R.id.etOtp6));
    }

    private void setupMaskedEmail() {
        String email = getIntent().getStringExtra(EXTRA_EMAIL);
        if (email == null || email.isEmpty()) {
            tvEmailMasked.setVisibility(View.GONE);
            return;
        }
        tvEmailMasked.setText(maskEmail(email));
    }

    private void setupActions(ImageButton btnBack, TextView tvBackLink) {
        View.OnClickListener backListener = v -> finish();
        btnBack.setOnClickListener(backListener);
        tvBackLink.setOnClickListener(backListener);

        tvResendOtp.setOnClickListener(v -> {
            clearOtpFields();
            startCountdown();
            otpFields.get(0).requestFocus();
        });

        btnVerifyOtp.setOnClickListener(v -> {
            // TODO: Gọi API xác thực OTP với getOtpCode()
        });
    }

    private void setupOtpInputs() {
        for (int i = 0; i < otpFields.size(); i++) {
            final int index = i;
            EditText field = otpFields.get(i);

            field.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (isPasting) {
                        return;
                    }

                    updateFieldState(field);

                    if (s.length() == 1 && index < otpFields.size() - 1) {
                        otpFields.get(index + 1).requestFocus();
                    }

                    if (s.length() > 1) {
                        handlePaste(s.toString(), index);
                        return;
                    }

                    updateVerifyButtonState();
                }
            });

            field.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL
                        && event.getAction() == KeyEvent.ACTION_DOWN
                        && field.getText().length() == 0
                        && index > 0) {
                    EditText previous = otpFields.get(index - 1);
                    previous.requestFocus();
                    previous.setText("");
                    updateFieldState(previous);
                    updateVerifyButtonState();
                    return true;
                }
                return false;
            });

            field.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    updateFieldState(field);
                }
            });
        }

        otpFields.get(0).requestFocus();
    }

    private void handlePaste(String pasted, int startIndex) {
        String digits = pasted.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return;
        }

        isPasting = true;
        int fieldIndex = startIndex;

        for (int i = 0; i < digits.length() && fieldIndex < otpFields.size(); i++, fieldIndex++) {
            otpFields.get(fieldIndex).setText(String.valueOf(digits.charAt(i)));
            updateFieldState(otpFields.get(fieldIndex));
        }

        if (fieldIndex < otpFields.size()) {
            otpFields.get(fieldIndex).requestFocus();
        } else {
            otpFields.get(otpFields.size() - 1).clearFocus();
        }

        isPasting = false;
        updateVerifyButtonState();
    }

    private void updateFieldState(EditText field) {
        boolean hasText = field.getText().length() > 0;
        field.setSelected(hasText && !field.isFocused());
    }

    private void updateVerifyButtonState() {
        boolean isComplete = getOtpCode().length() == OTP_LENGTH;
        btnVerifyOtp.setEnabled(isComplete);
        btnVerifyOtp.setAlpha(isComplete ? 1f : 0.5f);
    }

    private String getOtpCode() {
        StringBuilder code = new StringBuilder();
        for (EditText field : otpFields) {
            code.append(field.getText().toString().trim());
        }
        return code.toString();
    }

    private void clearOtpFields() {
        for (EditText field : otpFields) {
            field.setText("");
            field.setSelected(false);
            field.setActivated(false);
        }
        updateVerifyButtonState();
    }

    public void setOtpErrorState(boolean hasError) {
        for (EditText field : otpFields) {
            field.setActivated(hasError);
        }
    }

    private void startCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        tvResendOtp.setVisibility(View.GONE);
        tvCountdown.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(COUNTDOWN_MS, 1_000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1_000L;
                tvCountdown.setText(getString(R.string.otp_resend_countdown_format, seconds));
            }

            @Override
            public void onFinish() {
                tvCountdown.setVisibility(View.GONE);
                tvResendOtp.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@", 2);
        String local = parts[0];
        String domain = parts[1];

        if (local.isEmpty()) {
            return email;
        }

        String maskedLocal = local.charAt(0) + "***";
        return maskedLocal + "@" + domain;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
