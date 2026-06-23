package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.teatrack_mcd_253eie502802_group02.MainActivity;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;
import com.teatrack_mcd_253eie502802_group02.shared.LocaleHelper;
import com.teatrack_mcd_253eie502802_group02.shared.ui.ProfileBackHelper;

import java.util.Locale;

public class ChangeLanguageActivity extends BaseActivity {

    private RadioGroup rgLanguage;
    private RadioButton rbEnglish, rbVietnamese;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_language);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ProfileBackHelper.setupBackToProfile(this);
        initViews();
    }

    private void initViews() {
        rgLanguage = findViewById(R.id.rgLanguage);
        rbEnglish = findViewById(R.id.rbEnglish);
        rbVietnamese = findViewById(R.id.rbVietnamese);

        // Đánh dấu RadioButton dựa trên ngôn ngữ hiện tại
        String language = LocaleHelper.getLanguage(this);
        if (language.equals("vi")) {
            rbVietnamese.setChecked(true);
        } else {
            rbEnglish.setChecked(true);
        }

        findViewById(R.id.btnSaveLanguage).setOnClickListener(v -> {
            String selectedLang = "en";
            if (rbVietnamese.isChecked()) {
                selectedLang = "vi";
            }
            
            saveLanguage(selectedLang);
        });
    }

    private void saveLanguage(String lang) {
        LocaleHelper.setLocale(this, lang);

        Toast.makeText(this, R.string.personal_info_success_title, Toast.LENGTH_SHORT).show();

        // Khởi động lại app để áp dụng ngôn ngữ mới cho toàn bộ hệ thống
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}