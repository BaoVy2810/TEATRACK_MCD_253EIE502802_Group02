package com.teatrack_mcd_253eie502802_group02.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
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
import com.teatrack_mcd_253eie502802_group02.client.EditingPerInfoActivity;
import com.teatrack_mcd_253eie502802_group02.data.FirebaseProductRepository;
import com.teatrack_mcd_253eie502802_group02.databinding.ActivityPersonalInformationBinding;
import com.teatrack_mcd_253eie502802_group02.model.User;
import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;
import com.teatrack_mcd_253eie502802_group02.shared.ui.HeaderClientHelper;
import com.teatrack_mcd_253eie502802_group02.util.AdminSessionHelper;
import com.teatrack_mcd_253eie502802_group02.util.UserProfileHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AdminProfile extends BaseActivity {

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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.btnProfileBack.setOnClickListener(v -> finish());

        binding.btnEditInfo.setOnClickListener(v ->
                startActivityForResult(new Intent(this, EditingPerInfoActivity.class), EDIT_INFO_REQUEST));

        binding.btnEditAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        resolveAdminAndLoad();
    }

    private void resolveAdminAndLoad() {
        AdminSessionHelper.resolveAdminUserId(this, new AdminSessionHelper.ResolveCallback() {
            @Override
            public void onResolved(@NonNull String resolvedUserId) {
                userId = resolvedUserId;
                loadUserInfo();
            }

            @Override
            public void onFailed() {
                Toast.makeText(
                        AdminProfile.this,
                        getString(R.string.error_system_general, "Admin account not found"),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        resolveAdminAndLoad();
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
            compressAndSaveAvatar(data.getData());
        }
    }

    private void compressAndSaveAvatar(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, 200, 200, true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            String base64Image = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

            binding.imgAvatar.setImageBitmap(resized);
            HeaderClientHelper.cacheAvatar(this, userId, base64Image);
            saveBase64ToDatabase(base64Image);
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.msg_avatar_process_error), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveBase64ToDatabase(String base64Image) {
        if (userId == null) {
            Toast.makeText(this, getString(R.string.msg_user_id_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase.getInstance(FirebaseProductRepository.DB_URL)
                .getReference("Users")
                .child(userId)
                .child("avatarBase64")
                .setValue(base64Image)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, getString(R.string.msg_avatar_update_success), Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, getString(R.string.msg_avatar_save_failed, e.getMessage()), Toast.LENGTH_SHORT).show()
                );
    }

    private void loadUserInfo() {
        if (userId == null) {
            return;
        }

        userRef = FirebaseDatabase.getInstance(FirebaseProductRepository.DB_URL)
                .getReference("Users")
                .child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    return;
                }
                User user = snapshot.getValue(User.class);
                if (user == null) {
                    return;
                }

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
                    byte[] decodedBytes = Base64.decode(user.getAvatarBase64(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    binding.imgAvatar.setImageBitmap(bitmap);
                    HeaderClientHelper.cacheAvatar(AdminProfile.this, userId, user.getAvatarBase64());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminProfile.this, getString(R.string.error_loading_data), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
