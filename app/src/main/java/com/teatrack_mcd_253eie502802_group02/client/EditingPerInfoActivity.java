package com.teatrack_mcd_253eie502802_group02.client;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

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
    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_USER_ID = "userId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEditingPerInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // SỬA LỖI: Thay findViewById(R.id.main) bằng binding.getRoot() để tránh NullPointerException
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.btnCancel.setOnClickListener(v -> finish());

        binding.btnSaveChanges.setOnClickListener(v -> saveUserInfo());

        setupInputControls();
        loadCurrentUserInfo();
    }

    private void setupInputControls() {
        // Setup Gender Spinner
        String[] genderOptions = {"Male", "Female", "Others"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genderOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spGender.setAdapter(adapter);

        // Setup Date Picker for etDob
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
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        userId = sharedPreferences.getString(KEY_USER_ID, null);

        if (userId != null) {
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        try {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                binding.etFullName.setText(user.getName());
                                binding.etEmail.setText(user.getEmail());
                                binding.etPhone.setText(user.getPhone());
                                binding.etDob.setText(user.getDob());
                                binding.etAddress.setText(user.getAddress());

                                // Set Spinner Selection for Gender
                                String gender = user.getGender();
                                if (gender != null) {
                                    int position = 0;
                                    if (gender.equalsIgnoreCase("Female")) position = 1;
                                    else if (gender.equalsIgnoreCase("Others")) position = 2;
                                    binding.spGender.setSelection(position);
                                }
                                if (user.getAvatarBase64() != null && !user.getAvatarBase64().isEmpty()) {
                                    byte[] decodedBytes = Base64.decode(user.getAvatarBase64(), Base64.DEFAULT);
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                    binding.imgAvatar.setImageBitmap(bitmap);
                                }
                            }
                        } catch (Exception e) {
                            // Đề phòng trường hợp Class User thiếu Constructor không tham số
                            Toast.makeText(EditingPerInfoActivity.this, "Lỗi định dạng dữ liệu!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(EditingPerInfoActivity.this, "Error loading data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Không tìm thấy phiên đăng nhập!", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserInfo() {
        String name = binding.etFullName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String dob = binding.etDob.getText().toString().trim();
        String gender = binding.spGender.getSelectedItem().toString();
        String address = binding.etAddress.getText().toString().trim();

        if (userId != null && userRef != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("fullName", name);
            updates.put("name", name);
            updates.put("email", email);
            updates.put("phone", phone);
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
                        showSuccessDialog();
                    })
                    .addOnFailureListener(e -> Toast.makeText(EditingPerInfoActivity.this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID người dùng. Vui lòng đăng nhập lại!", Toast.LENGTH_LONG).show();
        }
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