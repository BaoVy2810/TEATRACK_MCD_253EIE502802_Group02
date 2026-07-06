package com.teatrack_mcd_253eie502802_group02.admin;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.adapter.AgencyAdapter;
import com.teatrack_mcd_253eie502802_group02.model.Agency;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminAgency extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView rvStores;
    private View fabAddAgency;
    private View layoutEmptyState;

    private static final int[] NAV_ITEM_IDS = {
            R.id.nav_dashboard,
            R.id.nav_products,
            R.id.nav_orders,
            R.id.nav_account,
            R.id.nav_forum,
            R.id.nav_branch,
            R.id.nav_feedbacks,
            R.id.nav_promotion
    };

    private AgencyAdapter agencyAdapter;
    private List<Agency> agencyList;
    private List<Agency> fullAgencyList;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private EditText currentImageEditText;
    private ShapeableImageView currentPreviewImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_agency);
        com.teatrack_mcd_253eie502802_group02.shared.ui.AdminInsetsHelper.apply(this);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            if (currentPreviewImage != null) {
                                currentPreviewImage.setImageURI(imageUri);
                                currentPreviewImage.setVisibility(View.VISIBLE);
                            }
                            uploadImageToFirebase(imageUri);
                        }
                    }
                }
        );

        initViews();
        setupRecyclerView();
        initFirebaseConnection();
        setupNavBar();
        setupHeader();

        if (fabAddAgency != null) {
            fabAddAgency.setOnTouchListener(new View.OnTouchListener() {
                private float initialX, initialY, initialTouchX, initialTouchY;
                private static final int CLICK_THRESHOLD = 10;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = v.getX();
                            initialY = v.getY();
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            v.setX(initialX + (event.getRawX() - initialTouchX));
                            v.setY(initialY + (event.getRawY() - initialTouchY));
                            return true;

                        case MotionEvent.ACTION_UP:
                            float diffX = Math.abs(event.getRawX() - initialTouchX);
                            float diffY = Math.abs(event.getRawY() - initialTouchY);
                            if (diffX < CLICK_THRESHOLD && diffY < CLICK_THRESHOLD) {
                                showAddAgencyDialog();
                            }
                            return true;
                    }
                    return false;
                }
            });
        }
        setupSearch();
    }

    private void setupHeader() {
        View btnProfile = findViewById(R.id.btn_profile);
        if (btnProfile != null) {
            com.teatrack_mcd_253eie502802_group02.shared.ui.HeaderMenuHelper.setupProfileMenu(this);
        }
    }

    private void setupNavBar() {
        NavBarHelper.setupNavBar(this, NAV_ITEM_IDS, R.id.nav_branch, v -> {
            int id = v.getId();
            Class<?> destination = null;
            if (id == R.id.nav_dashboard) destination = AdminDashboard.class;
            else if (id == R.id.nav_products) destination = AdminProduct.class;
            else if (id == R.id.nav_orders) destination = AdminOrders.class;
            else if (id == R.id.nav_account) destination = AdminAccount.class;
            else if (id == R.id.nav_forum) destination = AdminBlog.class;
            else if (id == R.id.nav_branch) return; // Đang ở đây rồi
            else if (id == R.id.nav_feedbacks) destination = AdminComplaints.class;
            else if (id == R.id.nav_promotion) destination = AdminPromotion.class;

            if (destination != null) {
                NavBarHelper.navigateWithoutTransition(this, destination);
            }
        });
    }

    private String generateNextAgencyId(List<Agency> currentList) {
        int maxNumber = 0;
        if (currentList != null && !currentList.isEmpty()) {
            for (Agency agency : currentList) {
                String id = agency.getId();
                if (id != null && id.startsWith("CN")) {
                    try {
                        int number = Integer.parseInt(id.substring(2).trim());
                        if (number > maxNumber) {
                            maxNumber = number;
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        int nextNumber = maxNumber + 1;
        return String.format("CN%02d", nextNumber);
    }

    /**
     * Dialog Thêm Mới Chi Nhánh
     */
    private void showAddAgencyDialog() {
        Dialog dialog = new Dialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_agency, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        ImageButton btnCloseDialog = view.findViewById(R.id.btnCloseDialog);
        EditText etId = view.findViewById(R.id.etId);
        EditText etName = view.findViewById(R.id.etName);
        EditText etAddress = view.findViewById(R.id.etAddress);
        EditText etPhone = view.findViewById(R.id.etPhone);
        EditText etImage = view.findViewById(R.id.etImage);
        EditText etMapEmbed = view.findViewById(R.id.etMapEmbed);
        com.google.android.material.button.MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        com.google.android.material.button.MaterialButton btnSubmit = view.findViewById(R.id.btnSubmit);
        com.google.android.material.button.MaterialButton btnChooseImage = view.findViewById(R.id.btnChooseImage);
        ShapeableImageView ivPreview = view.findViewById(R.id.ivPreview);

        tvDialogTitle.setText("ADD NEW AGENCY");
        if (btnSubmit != null) {
            btnSubmit.setText(R.string.btn_add);
            btnSubmit.setIcon(androidx.core.content.ContextCompat.getDrawable(this, R.drawable.plus));
        }

        String nextId = generateNextAgencyId(agencyList);
        if (etId != null) {
            etId.setText(nextId);
            etId.setEnabled(false);
        }

        if (btnCloseDialog != null) {
            btnCloseDialog.setOnClickListener(v -> dialog.dismiss());
        }
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }
        if (btnChooseImage != null) {
            btnChooseImage.setOnClickListener(v -> pickImage(etImage, ivPreview));
        }

        if (etImage != null && ivPreview != null) {
            etImage.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(android.text.Editable s) {
                    String input = s.toString().trim();
                    if (!input.isEmpty()) {
                        ivPreview.setVisibility(View.VISIBLE);
                        if (input.startsWith("http")) {
                            Glide.with(AdminAgency.this).load(input).into(ivPreview);
                        } else {
                            int resId = getResources().getIdentifier(input.replace(".png", "").replace(".jpg", ""), "mipmap", getPackageName());
                            if (resId == 0) {
                                resId = getResources().getIdentifier(input.replace(".png", "").replace(".jpg", ""), "drawable", getPackageName());
                            }
                            if (resId != 0) {
                                ivPreview.setImageResource(resId);
                            } else {
                                ivPreview.setImageResource(android.R.drawable.ic_menu_gallery);
                            }
                        }
                    } else {
                        ivPreview.setVisibility(View.GONE);
                    }
                }
            });
        }

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String id = etId != null ? etId.getText().toString().trim() : "";
                String name = etName != null ? etName.getText().toString().trim() : "";
                String address = etAddress != null ? etAddress.getText().toString().trim() : "";
                String phone = etPhone != null ? etPhone.getText().toString().trim() : "";
                String image = etImage != null ? etImage.getText().toString().trim() : "";
                String mapEmbed = etMapEmbed != null ? etMapEmbed.getText().toString().trim() : "";

                if (name.isEmpty() || address.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ Tên và Địa chỉ chi nhánh!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String currentTimestamp = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                        .format(new java.util.Date());

                Agency newAgency = new Agency(id, name, address, phone, image, currentTimestamp, mapEmbed, true);

                databaseReference.child(id).setValue(newAgency).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Thêm chi nhánh thành công", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        dialog.show();
    }

    /**
     * Dialog Cập Nhật / Sửa Chi Nhánh (Gọi từ Adapter)
     */
    public void showEditAgencyDialog(Agency agency) {
        Dialog dialog = new Dialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_agency, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        ImageButton btnCloseDialog = view.findViewById(R.id.btnCloseDialog);
        EditText etId = view.findViewById(R.id.etId);
        EditText etName = view.findViewById(R.id.etName);
        EditText etAddress = view.findViewById(R.id.etAddress);
        EditText etPhone = view.findViewById(R.id.etPhone);
        EditText etImage = view.findViewById(R.id.etImage);
        EditText etMapEmbed = view.findViewById(R.id.etMapEmbed);
        com.google.android.material.button.MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        com.google.android.material.button.MaterialButton btnSubmit = view.findViewById(R.id.btnSubmit);
        com.google.android.material.button.MaterialButton btnChooseImage = view.findViewById(R.id.btnChooseImage);
        ShapeableImageView ivPreview = view.findViewById(R.id.ivPreview);

        if (tvDialogTitle != null) {
            tvDialogTitle.setText("EDIT AGENCY");
        }
        if (btnSubmit != null) {
            btnSubmit.setText(R.string.btn_edit);
            btnSubmit.setIcon(androidx.core.content.ContextCompat.getDrawable(this, R.drawable.edit2));
        }

        if (etId != null) {
            etId.setText(agency.getId());
            etId.setEnabled(false);
        }
        if (etName != null) {
            etName.setText(agency.getName());
        }
        if (etAddress != null) {
            etAddress.setText(agency.getAddress());
        }
        if (etPhone != null) {
            etPhone.setText(agency.getPhone());
        }
        if (etImage != null) {
            etImage.setText(agency.getImage());
        }
        if (etMapEmbed != null) {
            etMapEmbed.setText(agency.getMapEmbed());
        }

        if (ivPreview != null && agency.getImage() != null && !agency.getImage().isEmpty()) {
            ivPreview.setVisibility(View.VISIBLE);
            if (agency.getImage().startsWith("http")) {
                Glide.with(this).load(agency.getImage()).into(ivPreview);
            } else {
                int resId = getResources().getIdentifier(agency.getImage().replace(".png", "").replace(".jpg", ""), "mipmap", getPackageName());
                if (resId == 0) {
                    resId = getResources().getIdentifier(agency.getImage().replace(".png", "").replace(".jpg", ""), "drawable", getPackageName());
                }
                if (resId != 0) ivPreview.setImageResource(resId);
            }
        }

        if (btnCloseDialog != null) {
            btnCloseDialog.setOnClickListener(v -> dialog.dismiss());
        }
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }
        if (btnChooseImage != null) {
            btnChooseImage.setOnClickListener(v -> pickImage(etImage, ivPreview));
        }

        if (etImage != null && ivPreview != null) {
            etImage.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(android.text.Editable s) {
                    String input = s.toString().trim();
                    if (!input.isEmpty()) {
                        ivPreview.setVisibility(View.VISIBLE);
                        if (input.startsWith("http")) {
                            Glide.with(AdminAgency.this).load(input).into(ivPreview);
                        } else {
                            int resId = getResources().getIdentifier(input.replace(".png", "").replace(".jpg", ""), "mipmap", getPackageName());
                            if (resId == 0) {
                                resId = getResources().getIdentifier(input.replace(".png", "").replace(".jpg", ""), "drawable", getPackageName());
                            }
                            if (resId != 0) {
                                ivPreview.setImageResource(resId);
                            } else {
                                ivPreview.setImageResource(android.R.drawable.ic_menu_gallery);
                            }
                        }
                    } else {
                        ivPreview.setVisibility(View.GONE);
                    }
                }
            });
        }

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String id = etId != null ? etId.getText().toString().trim() : "";
                String name = etName != null ? etName.getText().toString().trim() : "";
                String address = etAddress != null ? etAddress.getText().toString().trim() : "";
                String phone = etPhone != null ? etPhone.getText().toString().trim() : "";
                String image = etImage != null ? etImage.getText().toString().trim() : "";
                String mapEmbed = etMapEmbed != null ? etMapEmbed.getText().toString().trim() : "";

                if (name.isEmpty() || address.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ Tên và Địa chỉ chi nhánh!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Agency updatedAgency = new Agency(id, name, address, phone, image, agency.getCreatedAt(), mapEmbed, agency.isVisible());

                databaseReference.child(id).setValue(updatedAgency).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Cập nhật chi nhánh thành công", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        dialog.show();
    }

    /**
     * Hàm xóa chi nhánh (Gọi từ Adapter)
     */
    public void deleteAgency(Agency agency) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_delete_confirm);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            dialog.getWindow().setAttributes(params);
        }

        TextView tvTitle = dialog.findViewById(R.id.tvDeleteTitle);
        if (tvTitle != null) tvTitle.setText(R.string.modal_delete_title);

        TextView tvMessage = dialog.findViewById(R.id.tvDeleteMessage);
        String name = agency.getName();
        String fullMessage = "The agency <font color='#0088ff'><b>" + android.text.TextUtils.htmlEncode(name) + "</b></font> will be permanently deleted.";
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tvMessage.setText(Html.fromHtml(fullMessage, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvMessage.setText(Html.fromHtml(fullMessage));
        }

        dialog.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnConfirmDelete).setOnClickListener(v -> {
            databaseReference.child(agency.getId()).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AdminAgency.this, "Xóa thành công!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(AdminAgency.this, "Lỗi khi xóa", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void setupSearch() {
        if (etSearch == null) {
            return;
        }
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String text) {
        if (agencyAdapter == null || fullAgencyList == null) {
            return;
        }
        String query = text != null ? text.toLowerCase() : "";
        List<Agency> filteredList = new ArrayList<>();
        for (Agency item : fullAgencyList) {
            if (item == null) {
                continue;
            }
            if ((item.getName() != null && item.getName().toLowerCase().contains(query)) ||
                    (item.getAddress() != null && item.getAddress().toLowerCase().contains(query))) {
                filteredList.add(item);
            }
        }
        agencyAdapter.updateList(filteredList);
        updateEmptyState(filteredList.isEmpty());
    }

    private void updateEmptyState(boolean empty) {
        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        }
        if (rvStores != null) {
            rvStores.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
    }

    private void setupEmptyStateContent() {
        if (layoutEmptyState == null) return;
        ImageView icon = layoutEmptyState.findViewById(R.id.ivEmptyIcon);
        TextView title = layoutEmptyState.findViewById(R.id.tvEmptyTitle);
        TextView desc = layoutEmptyState.findViewById(R.id.tvEmptyDesc);
        icon.setImageResource(R.drawable.location);
        title.setText(R.string.empty_agencies_title);
        desc.setText(R.string.empty_agencies_desc);
    }

    public void pickImage(EditText targetEditText, ShapeableImageView previewImageView) {
        this.currentImageEditText = targetEditText;
        this.currentPreviewImage = previewImageView;
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadImageToFirebase(Uri imageUri) {
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference fileRef = storageReference.child(fileName);

        Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();

        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                if (currentImageEditText != null) {
                    currentImageEditText.setText(uri.toString());
                }
                Toast.makeText(AdminAgency.this, "Tải ảnh thành công", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(AdminAgency.this, "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        etSearch.setHint(R.string.str_agency_search);
        rvStores = findViewById(R.id.rvStores);
        fabAddAgency = findViewById(R.id.fabAddAgency);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        setupEmptyStateContent();
    }

    private void setupRecyclerView() {
        rvStores.setLayoutManager(new LinearLayoutManager(this));
        agencyList = new ArrayList<>();
        fullAgencyList = new ArrayList<>();
        // Truyền context `this` vào Adapter để Adapter có thể tương tác gọi Dialog/Xóa
        agencyAdapter = new AgencyAdapter(this, agencyList);
        rvStores.setAdapter(agencyAdapter);
    }

    private void initFirebaseConnection() {
        String firebaseUrl = "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";
        databaseReference = FirebaseDatabase.getInstance(firebaseUrl).getReference("agencies");
        storageReference = FirebaseStorage.getInstance().getReference("agency_images");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                agencyList.clear();
                fullAgencyList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    try {
                        Agency agency = dataSnapshot.getValue(Agency.class);
                        if (agency != null) {
                            agencyList.add(agency);
                            fullAgencyList.add(agency);
                        }
                    } catch (Exception ignored) {}
                }
                agencyAdapter.notifyDataSetChanged();
                String query = etSearch != null ? etSearch.getText().toString() : "";
                filter(query.isEmpty() ? "" : query);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminAgency.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}