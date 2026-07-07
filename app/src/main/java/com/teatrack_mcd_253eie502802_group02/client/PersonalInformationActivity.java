package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.data.FirebaseProductRepository;
import com.teatrack_mcd_253eie502802_group02.databinding.ActivityPersonalInformationBinding;
import com.teatrack_mcd_253eie502802_group02.model.User;
import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;
import com.teatrack_mcd_253eie502802_group02.shared.ui.HeaderClientHelper;
import com.teatrack_mcd_253eie502802_group02.shared.ui.ProfileBackHelper;
import com.teatrack_mcd_253eie502802_group02.util.AvatarBitmapHelper;
import com.teatrack_mcd_253eie502802_group02.util.CustomerSessionHelper;
import com.teatrack_mcd_253eie502802_group02.util.UserProfileHelper;

import java.io.IOException;

public class PersonalInformationActivity extends BaseActivity {

    private ActivityPersonalInformationBinding binding;
    private DatabaseReference userRef;
    private String userId;
    private static final int PICK_IMAGE_REQUEST = 1001;
    private static final int EDIT_INFO_REQUEST = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPersonalInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ✅ Gán userId sớm nhất có thể
        userId = CustomerSessionHelper.getCustomerUserId(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ProfileBackHelper.setupBackToProfile(this);

        binding.btnEditInfo.setOnClickListener(v ->
                startActivityForResult(new Intent(this, EditingPerInfoActivity.class), EDIT_INFO_REQUEST));

        binding.btnEditAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        loadUserInfo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_INFO_REQUEST && resultCode == RESULT_OK) {
            loadUserInfo();
            return;
        }

        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            Uri imageUri = data.getData();
            compressAndSaveAvatar(imageUri);
        }
    }

    private void compressAndSaveAvatar(Uri imageUri) {
        try {
            Bitmap resized = AvatarBitmapHelper.prepareFromUri(this, imageUri);
            if (resized == null) {
                Toast.makeText(this, "Lỗi xử lý ảnh!", Toast.LENGTH_SHORT).show();
                return;
            }

            String base64Image = AvatarBitmapHelper.toBase64Jpeg(resized);
            if (base64Image == null) {
                Toast.makeText(this, "Lỗi xử lý ảnh!", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.imgAvatar.setImageBitmap(resized);

            HeaderClientHelper.cacheAvatar(this, userId, base64Image);
            saveBase64ToDatabase(base64Image);

        } catch (IOException e) {
            Toast.makeText(this, "Lỗi xử lý ảnh!", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveBase64ToDatabase(String base64Image) {
        if (userId == null) {
            Toast.makeText(this, "Lỗi: không tìm thấy userId!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase.getInstance(FirebaseProductRepository.DB_URL)
                .getReference("Users")
                .child(userId)
                .child("avatarBase64")
                .setValue(base64Image)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Cập nhật ảnh thành công!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lưu ảnh thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserInfo();
        HeaderClientHelper.bindProfileAvatar(this);
    }

    private void loadUserInfo() {
        if (userId != null) {
            userRef = FirebaseDatabase.getInstance(FirebaseProductRepository.DB_URL)
                    .getReference("Users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            UserProfileHelper.cacheFromSnapshot(
                                    getSharedPreferences(UserProfileHelper.PREF_NAME, MODE_PRIVATE),
                                    snapshot);
                            binding.tvFullNameValue.setText(UserProfileHelper.resolveDisplayName(user));
                            binding.tvEmailValue.setText(user.getEmail());
                            binding.tvPhoneValue.setText(user.getPhone());
                            binding.tvDobValue.setText(user.getDob());
                            binding.tvGenderValue.setText(user.getGender());
                            binding.tvAddressValue.setText(user.getAddress());

                            if (user.getAvatarBase64() != null && !user.getAvatarBase64().isEmpty()) {
                                Bitmap bitmap = AvatarBitmapHelper.decodeBase64(user.getAvatarBase64());
                                if (bitmap != null) {
                                    binding.imgAvatar.setImageBitmap(bitmap);
                                }
                            HeaderClientHelper.cacheAvatar(
                                    PersonalInformationActivity.this,
                                    userId,
                                    user.getAvatarBase64());
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(PersonalInformationActivity.this,
                            "Error loading data", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
}