package com.teatrack_mcd_253eie502802_group02.admin;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.adapter.AccountAdapter;
import com.teatrack_mcd_253eie502802_group02.databinding.ActivityAdminAccountBinding;
import com.teatrack_mcd_253eie502802_group02.databinding.DialogAddEditAccountBinding;
import com.teatrack_mcd_253eie502802_group02.model.User;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminAccount extends AppCompatActivity implements AccountAdapter.AccountActionListener {

    private static final String[] ROLE_FILTERS = {"All", "Admin", "Customer", "Customer Vip"};
    private static final String[] ROLES = {"Admin", "Customer", "Customer Vip"};
    private static final String[] STATUSES = {"Active", "Inactive", "Locked"};

    private ActivityAdminAccountBinding binding;
    private AccountAdapter adapter;
    private DatabaseReference usersRef;
    private ValueEventListener usersListener;
    private final List<User> allUsers = new ArrayList<>();
    private String currentQuery = "";
    private String currentRoleFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAdminAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String firebaseUrl = "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";
        usersRef = FirebaseDatabase.getInstance(firebaseUrl).getReference("Users");
        setupRecyclerView();
        setupFilters();
        setupActions();
        setupBottomNav();
        listenUsers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usersListener != null) {
            usersRef.removeEventListener(usersListener);
        }
    }

    private void setupRecyclerView() {
        adapter = new AccountAdapter(this, this);
        binding.rvAccounts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAccounts.setAdapter(adapter);
        binding.rvAccounts.setNestedScrollingEnabled(false);
    }

    private void setupFilters() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, ROLE_FILTERS);
        binding.spinnerRoleFilter.setAdapter(roleAdapter);
        binding.spinnerRoleFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                currentRoleFilter = ROLE_FILTERS[position];
                applyFilters();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void setupActions() {
        binding.btnAddAccount.setOnClickListener(v -> showAccountDialog(null));
        binding.btnRefresh.setOnClickListener(v -> applyFilters());
        
        // Nhấn giữ nút Refresh để chạy Migration dữ liệu cũ
        binding.btnRefresh.setOnLongClickListener(v -> {
            migrateUserData();
            return true;
        });

        binding.btnExportExcel.setOnClickListener(v ->
                Toast.makeText(this, "Xuất Excel", Toast.LENGTH_SHORT).show());
    }

    private void setupBottomNav() {
        int[] navItemIds = {
                R.id.nav_dashboard, R.id.nav_products, R.id.nav_orders, R.id.nav_account,
                R.id.nav_forum, R.id.nav_branch, R.id.nav_feedbacks, R.id.nav_promotion
        };
        NavBarHelper.setupNavBar(this, navItemIds, R.id.nav_account, view -> {
            int id = view.getId();
            if (id == R.id.nav_account) return;

            Class<?> destination = null;
            if (id == R.id.nav_dashboard) {
                destination = AdminDashboard.class;
            } else if (id == R.id.nav_products) {
                destination = AdminProduct.class;
            } else if (id == R.id.nav_orders) {
                destination = AdminOrders.class;
            } else if (id == R.id.nav_forum) {
                destination = AdminBlog.class;
            } else if (id == R.id.nav_branch) {
                destination = AdminAgency.class;
            } else if (id == R.id.nav_feedbacks) {
                destination = AdminComplaints.class;
            } else if (id == R.id.nav_promotion) {
                destination = AdminPromotion.class;
            }

            if (destination != null) {
                startActivity(new Intent(this, destination));
                finish();
            }
        });
    }

    private void listenUsers() {
        usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allUsers.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    User user = child.getValue(User.class);
                    if (user != null) {
                        if (TextUtils.isEmpty(user.getId())) {
                            user.setId(child.getKey());
                        }
                        allUsers.add(user);
                    }
                }
                applyFilters();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminAccount.this, "Load users failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        usersRef.addValueEventListener(usersListener);
    }

    private void applyFilters() {
        String query = currentQuery.toLowerCase(Locale.getDefault());
        List<User> filteredUsers = new ArrayList<>();

        for (User user : allUsers) {
            String fullName = safe(user.getFullName()).toLowerCase(Locale.getDefault());
            String username = safe(user.getUsername()).toLowerCase(Locale.getDefault());
            boolean matchesSearch = query.isEmpty() || fullName.contains(query) || username.contains(query);
            boolean matchesRole = "All".equals(currentRoleFilter) || currentRoleFilter.equalsIgnoreCase(user.getRole());

            if (matchesSearch && matchesRole) {
                filteredUsers.add(user);
            }
        }

        adapter.submitList(filteredUsers);
        binding.tvEmptyState.setVisibility(filteredUsers.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    @Override
    public void onView(User user) {
        String message = "ID: " + safe(user.getId())
                + "\nFull name: " + safe(user.getFullName())
                + "\nUsername: " + safe(user.getUsername())
                + "\nEmail: " + safe(user.getEmail())
                + "\nRole: " + safe(user.getRole())
                + "\nStatus: " + safe(user.getStatus())
                + "\nPhone: " + safe(user.getPhoneNumber())
                + "\nAddress: " + safe(user.getAddress())
                + "\nCreated: " + safe(user.getCreatedAt());

        new MaterialAlertDialogBuilder(this)
                .setTitle("Account Detail")
                .setMessage(message)
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    public void onEdit(User user) {
        showAccountDialog(user);
    }

    @Override
    public void onDelete(User user) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete account")
                .setMessage("Are you sure you want to delete this account?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> usersRef.child(user.getId()).removeValue()
                        .addOnSuccessListener(unused -> Toast.makeText(this, "Delete successfully", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()))
                .show();
    }

    private void showAccountDialog(User editingUser) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        DialogAddEditAccountBinding dialogBinding = DialogAddEditAccountBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92);
            dialog.getWindow().setAttributes(params);
        }

        setupDialogDropdowns(dialogBinding);
        bindDialogData(dialogBinding, editingUser);

        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.btnSave.setOnClickListener(v -> saveAccount(dialog, dialogBinding, editingUser));

        dialog.show();
    }

    private void setupDialogDropdowns(DialogAddEditAccountBinding dialogBinding) {
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ROLES);
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, STATUSES);
        dialogBinding.actRole.setAdapter(roleAdapter);
        dialogBinding.actStatus.setAdapter(statusAdapter);
    }

    private void bindDialogData(DialogAddEditAccountBinding dialogBinding, User user) {
        boolean isEdit = user != null;
        dialogBinding.tvDialogTitle.setText(isEdit ? "Edit Account" : "Add Account");
        dialogBinding.btnSave.setText(isEdit ? "Update" : "Save");

        if (isEdit) {
            dialogBinding.etFullName.setText(user.getFullName());
            dialogBinding.etUsername.setText(user.getUsername());
            dialogBinding.etEmail.setText(user.getEmail());
            dialogBinding.etPhoneNumber.setText(user.getPhoneNumber());
            dialogBinding.etAddress.setText(user.getAddress());
            dialogBinding.actRole.setText(user.getRole(), false);
            dialogBinding.actStatus.setText(user.getStatus(), false);
        } else {
            dialogBinding.actRole.setText(ROLES[1], false);
            dialogBinding.actStatus.setText(STATUSES[0], false);
        }

        TextWatcher clearErrorsWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                dialogBinding.tilFullName.setError(null);
                dialogBinding.tilUsername.setError(null);
                dialogBinding.tilEmail.setError(null);
                dialogBinding.tilPhoneNumber.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        dialogBinding.etFullName.addTextChangedListener(clearErrorsWatcher);
        dialogBinding.etUsername.addTextChangedListener(clearErrorsWatcher);
        dialogBinding.etEmail.addTextChangedListener(clearErrorsWatcher);
        dialogBinding.etPhoneNumber.addTextChangedListener(clearErrorsWatcher);
    }

    private void saveAccount(Dialog dialog, DialogAddEditAccountBinding dialogBinding, User editingUser) {
        String fullName = textOf(dialogBinding.etFullName);
        String username = textOf(dialogBinding.etUsername);
        String email = textOf(dialogBinding.etEmail);
        String phoneNumber = textOf(dialogBinding.etPhoneNumber);
        String address = textOf(dialogBinding.etAddress);
        String role = textOf(dialogBinding.actRole);
        String status = textOf(dialogBinding.actStatus);

        if (!validate(dialogBinding, editingUser, fullName, username, email, phoneNumber)) {
            return;
        }

        String id = editingUser == null ? generateNextAccountId() : editingUser.getId();
        if (id == null) {
            Toast.makeText(this, "Cannot create account id", Toast.LENGTH_SHORT).show();
            return;
        }

        String createdAt = editingUser == null || TextUtils.isEmpty(editingUser.getCreatedAt())
                ? new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(new Date())
                : editingUser.getCreatedAt();

        User user = new User(id, fullName, username, email, role, status, phoneNumber, address, createdAt);
        if (editingUser != null) {
            user.setPassword(editingUser.getPassword());
        }

        usersRef.child(id).setValue(user)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, editingUser == null ? "Save successfully" : "Update successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private boolean validate(DialogAddEditAccountBinding dialogBinding, User editingUser,
                             String fullName, String username, String email, String phoneNumber) {
        boolean valid = true;

        if (fullName.isEmpty()) {
            dialogBinding.tilFullName.setError("Full Name is required");
            valid = false;
        }

        if (username.isEmpty()) {
            dialogBinding.tilUsername.setError("Username is required");
            valid = false;
        } else if (isUsernameExists(username, editingUser == null ? null : editingUser.getId())) {
            dialogBinding.tilUsername.setError("Username already exists");
            valid = false;
        }

        if (email.isEmpty()) {
            dialogBinding.tilEmail.setError("Email is required");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            dialogBinding.tilEmail.setError("Invalid email format");
            valid = false;
        }

        if (phoneNumber.isEmpty()) {
            dialogBinding.tilPhoneNumber.setError("Phone Number is required");
            valid = false;
        } else if (!phoneNumber.matches("^[0-9]{10}$")) {
            dialogBinding.tilPhoneNumber.setError("Phone Number must be exactly 10 digits");
            valid = false;
        }

        return valid;
    }

    private boolean isUsernameExists(String username, String editingId) {
        for (User user : allUsers) {
            boolean sameUser = editingId != null && editingId.equals(user.getId());
            if (!sameUser && username.equalsIgnoreCase(safe(user.getUsername()))) {
                return true;
            }
        }
        return false;
    }

    private String generateNextAccountId() {
        Pattern pattern = Pattern.compile("^CS(\\d+)$", Pattern.CASE_INSENSITIVE);
        int maxNumber = 0;

        // Scan current Firebase data and continue from the largest CS number.
        for (User user : allUsers) {
            Matcher matcher = pattern.matcher(safe(user.getId()));
            if (matcher.matches()) {
                try {
                    maxNumber = Math.max(maxNumber, Integer.parseInt(matcher.group(1)));
                } catch (NumberFormatException ignored) {
                    // Ignore malformed IDs and keep checking the rest.
                }
            }
        }

        int nextNumber = maxNumber + 1;
        String candidate;
        do {
            candidate = String.format(Locale.getDefault(), "CS%02d", nextNumber++);
        } while (isAccountIdExists(candidate));

        return candidate;
    }

    private boolean isAccountIdExists(String id) {
        for (User user : allUsers) {
            if (id.equalsIgnoreCase(safe(user.getId()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Migration Script: Chuẩn hóa dữ liệu người dùng cũ sang định dạng mới.
     * Hàm này sẽ quét toàn bộ node "Users" và bổ sung các trường thiếu.
     */
    private void migrateUserData() {
        Toast.makeText(this, "Đang bắt đầu chuẩn hóa dữ liệu...", Toast.LENGTH_SHORT).show();
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int updatedCount = 0;
                String now = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(new Date());

                for (DataSnapshot child : snapshot.getChildren()) {
                    User user = child.getValue(User.class);
                    if (user == null) continue;

                    boolean isModified = false;

                    // 1. Đồng bộ ID nếu chưa có
                    if (TextUtils.isEmpty(user.getId())) {
                        user.setId(child.getKey());
                        isModified = true;
                    }

                    // 2. Chuyển name (cũ) -> fullName và username (mới)
                    if (TextUtils.isEmpty(user.getFullName()) && !TextUtils.isEmpty(user.getName())) {
                        user.setFullName(user.getName());
                        isModified = true;
                    }
                    if (TextUtils.isEmpty(user.getUsername()) && !TextUtils.isEmpty(user.getName())) {
                        user.setUsername(user.getName());
                        isModified = true;
                    }

                    // 3. Chuyển phone (cũ) -> phoneNumber (mới)
                    if (TextUtils.isEmpty(user.getPhoneNumber()) && !TextUtils.isEmpty(user.getPhone())) {
                        user.setPhoneNumber(user.getPhone());
                        isModified = true;
                    }

                    // 4. Đảm bảo Address không bị null
                    if (user.getAddress() == null) {
                        user.setAddress("");
                        isModified = true;
                    }

                    // 5. Khởi tạo Role/Status mặc định nếu trống
                    if (TextUtils.isEmpty(user.getRole())) {
                        user.setRole("Customer");
                        isModified = true;
                    }
                    if (TextUtils.isEmpty(user.getStatus())) {
                        user.setStatus("Active");
                        isModified = true;
                    }

                    // 6. Gán ngày tạo nếu chưa có
                    if (TextUtils.isEmpty(user.getCreatedAt())) {
                        user.setCreatedAt(now);
                        isModified = true;
                    }

                    if (isModified) {
                        usersRef.child(child.getKey()).setValue(user);
                        updatedCount++;
                    }
                }
                new MaterialAlertDialogBuilder(AdminAccount.this)
                        .setTitle("Migration Hoàn Tất")
                        .setMessage("Đã chuẩn hóa thành công " + updatedCount + " tài khoản cũ.")
                        .setPositiveButton("OK", null)
                        .show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminAccount.this, "Lỗi migration: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String textOf(android.widget.TextView textView) {
        return textView.getText() == null ? "" : textView.getText().toString().trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
