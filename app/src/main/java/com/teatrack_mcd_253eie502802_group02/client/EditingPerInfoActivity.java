package com.teatrack_mcd_253eie502802_group02.client;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.data.FirebaseProductRepository;
import com.teatrack_mcd_253eie502802_group02.databinding.ActivityEditingPerInfoBinding;
import com.teatrack_mcd_253eie502802_group02.model.User;
import com.teatrack_mcd_253eie502802_group02.util.UserProfileHelper;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;

public class EditingPerInfoActivity extends BaseActivity {

    private ActivityEditingPerInfoBinding binding;
    private DatabaseReference userRef;
    private String userId;
    private String loginUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEditingPerInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences(UserProfileHelper.PREF_NAME, MODE_PRIVATE);
        userId = prefs.getString(UserProfileHelper.KEY_USER_ID, null);
        loginUsername = prefs.getString(UserProfileHelper.KEY_USERNAME, "");
        if (userId != null && !userId.isEmpty()) {
            userRef = FirebaseDatabase.getInstance(FirebaseProductRepository.DB_URL)
                    .getReference("Users")
                    .child(userId);
        }

        binding.btnCancel.setOnClickListener(v -> finish());
        binding.btnSaveChanges.setOnClickListener(v -> saveUserInfo());

        setupInputControls();
        loadCurrentUserInfo();
    }

    private void setupInputControls() {
        String[] genderOptions = {
                getString(R.string.personal_info_gender_male),
                getString(R.string.personal_info_gender_female),
                getString(R.string.personal_info_gender_other)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genderOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spGender.setAdapter(adapter);

        binding.etDob.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
                String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                binding.etDob.setText(selectedDate);
            }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void loadCurrentUserInfo() {
        if (userRef == null) {
            Toast.makeText(this, R.string.personal_info_session_missing, Toast.LENGTH_SHORT).show();
            return;
        }

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(EditingPerInfoActivity.this, R.string.personal_info_load_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    User user = snapshot.getValue(User.class);
                    if (user == null) {
                        return;
                    }
                    binding.etFullName.setText(UserProfileHelper.resolveDisplayName(user));
                    binding.etEmail.setText(user.getEmail());
                    binding.etPhone.setText(user.getPhoneNumber());
                    binding.etDob.setText(user.getDob());
                    binding.etAddress.setText(user.getAddress());
                    selectGender(user.getGender());

                    if (user.getAvatarBase64() != null && !user.getAvatarBase64().isEmpty()) {
                        byte[] decodedBytes = Base64.decode(user.getAvatarBase64(), Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        binding.imgAvatar.setImageBitmap(bitmap);
                    }
                } catch (Exception e) {
                    Toast.makeText(EditingPerInfoActivity.this, R.string.personal_info_format_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditingPerInfoActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectGender(String gender) {
        if (gender == null || gender.isEmpty()) {
            return;
        }
        String normalized = gender.trim().toLowerCase();
        int position = 0;
        if (normalized.contains("female") || normalized.contains("nữ")) {
            position = 1;
        } else if (normalized.contains("other") || normalized.contains("khác")) {
            position = 2;
        }
        binding.spGender.setSelection(position);
    }

    private void saveUserInfo() {
        String name = binding.etFullName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String dob = binding.etDob.getText().toString().trim();
        String gender = binding.spGender.getSelectedItem().toString();
        String address = binding.etAddress.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            binding.etFullName.setError(getString(R.string.personal_info_name_required));
            binding.etFullName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            binding.etPhone.setError(getString(R.string.personal_info_phone_required));
            binding.etPhone.requestFocus();
            return;
        }

        if (userRef == null) {
            Toast.makeText(this, R.string.personal_info_session_missing, Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", name);
        if (loginUsername != null && !loginUsername.trim().isEmpty()) {
            updates.put("username", loginUsername.trim());
        }
        updates.put("email", email);
        updates.put("phone", phone);
        updates.put("phoneNumber", phone);
        updates.put("dob", dob);
        updates.put("gender", gender);
        updates.put("address", address);

        userRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    UserProfileHelper.cacheProfile(
                            EditingPerInfoActivity.this,
                            userId,
                            null,
                            name,
                            phone
                    );
                    setResult(RESULT_OK);
                    showSuccessDialog();
                })
                .addOnFailureListener(e -> Toast.makeText(
                        EditingPerInfoActivity.this,
                        getString(R.string.personal_info_update_failed, e.getMessage()),
                        Toast.LENGTH_SHORT).show());
    }

    private void showSuccessDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_success_profile_update);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        MaterialButton btnBackToProfile = dialog.findViewById(R.id.btnBackToProfile);
        if (btnBackToProfile != null) {
            btnBackToProfile.setOnClickListener(v -> {
                dialog.dismiss();
                finish();
            });
        }

        dialog.setCancelable(false);
        dialog.show();
    }
}
