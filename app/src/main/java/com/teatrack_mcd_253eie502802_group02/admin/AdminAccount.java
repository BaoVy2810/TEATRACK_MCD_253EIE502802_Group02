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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

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
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminAccount extends AppCompatActivity implements AccountAdapter.AccountActionListener {

    private String[] ROLE_FILTERS;
    private String[] ROLES;
    private String[] STATUSES;

    private ActivityAdminAccountBinding binding;
    private AccountAdapter adapter;
    private DatabaseReference usersRef;
    private ValueEventListener usersListener;
    private final List<User> allUsers = new ArrayList<>();
    private String currentQuery = "";
    private String currentRoleFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        com.teatrack_mcd_253eie502802_group02.shared.ui.AdminInsetsHelper.apply(this);

        ROLE_FILTERS = new String[]{
                getString(R.string.filter_account_all),
                getString(R.string.role_admin),
                getString(R.string.role_customer),
                getString(R.string.role_customer_vip)
        };
        ROLES = new String[]{
                getString(R.string.role_admin),
                getString(R.string.role_customer),
                getString(R.string.role_customer_vip)
        };
        STATUSES = new String[]{
                getString(R.string.status_active),
                getString(R.string.status_inactive),
                getString(R.string.status_locked)
        };
        currentRoleFilter = ROLE_FILTERS[0];

        String firebaseUrl = "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";
        usersRef = FirebaseDatabase.getInstance(firebaseUrl).getReference("Users");
        setupRecyclerView();
        setupFilters();
        setupActions();
        setupBottomNav();
        listenUsers();
        com.teatrack_mcd_253eie502802_group02.shared.ui.HeaderMenuHelper.setupProfileMenu(this);
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
        EditText etSearch = binding.getRoot().findViewById(R.id.etSearch);
        etSearch.setHint(R.string.str_search_hint_account);
        etSearch.addTextChangedListener(new TextWatcher() {
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

        updateRoleFilterButtonText();
        binding.btnRoleFilter.setOnClickListener(this::showRoleFilterPopup);
    }

    private void updateRoleFilterButtonText() {
        binding.btnRoleFilter.setText(String.format("%s ▾", currentRoleFilter));
    }

    private void showRoleFilterPopup(View anchor) {
        showOptionPopup(anchor, Arrays.asList(ROLE_FILTERS), currentRoleFilter, selected -> {
            currentRoleFilter = selected;
            updateRoleFilterButtonText();
            applyFilters();
        });
    }

    private void showOptionPopup(View anchor, List<String> options, String selected,
                                 OptionSelectedListener listener) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.dialog_category_selector, null);
        popupView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        int minWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.45);
        int anchorWidth = anchor.getWidth() > 0 ? anchor.getWidth() : 0;
        int popupWidth = Math.max(Math.max(anchorWidth, popupView.getMeasuredWidth()), minWidth);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                popupWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(20);

        RecyclerView rvCategories = popupView.findViewById(R.id.rvCategoryList);
        if (rvCategories != null) {
            rvCategories.setLayoutManager(new LinearLayoutManager(this));
            rvCategories.setAdapter(new CategoryDialogAdapter(options, selected, choice -> {
                listener.onSelected(choice);
                popupWindow.dismiss();
            }));
        }

        popupWindow.showAsDropDown(anchor, 0, 10);
    }

    private interface OptionSelectedListener {
        void onSelected(String value);
    }

    private void setupActions() {
        View fabAddAccount = binding.fabAddAccount;
        if (fabAddAccount != null) {
            fabAddAccount.setOnTouchListener(new View.OnTouchListener() {
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
                                showAccountDialog(null);
                            }
                            return true;
                    }
                    return false;
                }
            });
        }

        binding.btnExportExcel.setOnClickListener(v ->
                Toast.makeText(this, getString(R.string.msg_export_excel), Toast.LENGTH_SHORT).show());
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
                NavBarHelper.navigateWithoutTransition(this, destination);
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
                Toast.makeText(AdminAccount.this, getString(R.string.error_load_users, error.getMessage()), Toast.LENGTH_SHORT).show();
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
            boolean matchesRole = getString(R.string.filter_account_all).equals(currentRoleFilter) || currentRoleFilter.equalsIgnoreCase(user.getRole());

            if (matchesSearch && matchesRole) {
                filteredUsers.add(user);
            }
        }

        filteredUsers.sort((a, b) -> {
            String idA = safe(a.getId()).toUpperCase(Locale.getDefault());
            String idB = safe(b.getId()).toUpperCase(Locale.getDefault());
            // Extract numeric part (e.g. "CS01" → 1) for natural sort
            try {
                int numA = Integer.parseInt(idA.replaceAll("[^0-9]", ""));
                int numB = Integer.parseInt(idB.replaceAll("[^0-9]", ""));
                return Integer.compare(numA, numB);
            } catch (NumberFormatException e) {
                return idA.compareTo(idB);
            }
        });
        adapter.submitList(filteredUsers);
        binding.tvEmptyState.setVisibility(filteredUsers.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    @Override
    public void onView(User user) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View detailView = LayoutInflater.from(this).inflate(R.layout.dialog_account_detail, null);
        dialog.setContentView(detailView);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92);
            dialog.getWindow().setAttributes(params);
        }

        String initial = getInitial(user.getFullName(), user.getUsername());
        ((TextView) detailView.findViewById(R.id.tvDetailInitial)).setText(initial);
        ((TextView) detailView.findViewById(R.id.tvDetailFullName)).setText(safe(user.getFullName()));
        ((TextView) detailView.findViewById(R.id.tvDetailUsername)).setText("@" + safe(user.getUsername()));
        ((TextView) detailView.findViewById(R.id.tvDetailId)).setText(safe(user.getId()));
        ((TextView) detailView.findViewById(R.id.tvDetailRole)).setText(safe(user.getRole()));
        ((TextView) detailView.findViewById(R.id.tvDetailEmail)).setText(safe(user.getEmail()));
        ((TextView) detailView.findViewById(R.id.tvDetailPhone)).setText(
                safe(user.getPhoneNumber()).isEmpty() ? getString(R.string.text_na) : user.getPhoneNumber());
        ((TextView) detailView.findViewById(R.id.tvDetailAddress)).setText(
                safe(user.getAddress()).isEmpty() ? getString(R.string.text_na) : user.getAddress());
        ((TextView) detailView.findViewById(R.id.tvDetailCreatedAt)).setText(safe(user.getCreatedAt()));

        // Status badge with colour
        TextView tvStatus = detailView.findViewById(R.id.tvDetailStatus);
        tvStatus.setText(safe(user.getStatus()));
        bindDetailStatus(tvStatus, user.getStatus());

        detailView.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        detailView.findViewById(R.id.btnCloseDetail).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void bindDetailStatus(TextView tvStatus, String status) {
        int textColor;
        int bgColor;
        if (getString(R.string.status_locked).equalsIgnoreCase(status)) {
            textColor = ContextCompat.getColor(this, R.color.danger);
            bgColor = ContextCompat.getColor(this, R.color.danger_bg);
        } else if (getString(R.string.status_inactive).equalsIgnoreCase(status)) {
            textColor = ContextCompat.getColor(this, R.color.text_secondary);
            bgColor = ContextCompat.getColor(this, R.color.divider);
        } else {
            textColor = ContextCompat.getColor(this, R.color.success);
            bgColor = ContextCompat.getColor(this, R.color.success_bg);
        }
        tvStatus.setTextColor(textColor);
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(bgColor);
        bg.setCornerRadius(999 * getResources().getDisplayMetrics().density);
        bg.setStroke(1, adjustAlpha(textColor, 0.35f));
        tvStatus.setBackground(bg);
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(android.graphics.Color.alpha(color) * factor);
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private String getInitial(String fullName, String username) {
        String source = !safe(fullName).isEmpty() ? fullName : username;
        source = safe(source).trim();
        return source.isEmpty() ? "?" : source.substring(0, 1).toUpperCase();
    }

    @Override
    public void onEdit(User user) {
        showAccountDialog(user);
    }

    @Override
    public void onDelete(User user) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_delete_confirm);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            dialog.getWindow().setAttributes(params);
        }

        TextView tvMessage = dialog.findViewById(R.id.tvDeleteMessage);
        String name = safe(user.getFullName()).isEmpty() ? safe(user.getUsername()) : user.getFullName();
        String fullMessage = "The account <font color='#0088ff'><b>" + android.text.TextUtils.htmlEncode(name) + "</b></font> will be permanently deleted.";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tvMessage.setText(android.text.Html.fromHtml(fullMessage, android.text.Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvMessage.setText(android.text.Html.fromHtml(fullMessage));
        }

        dialog.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnConfirmDelete).setOnClickListener(v -> {
            usersRef.child(user.getId()).removeValue()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, getString(R.string.msg_delete_success), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, getString(R.string.error_delete_failed, e.getMessage()), Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }

    @Override
    public void onUpgradeVip(User user) {
        boolean isVip = getString(R.string.role_customer_vip).equalsIgnoreCase(user.getRole());
        String newRole = isVip ? getString(R.string.role_customer) : getString(R.string.role_customer_vip);
        String title = isVip ? getString(R.string.dialog_downgrade_vip_title) : getString(R.string.dialog_upgrade_vip_title);
        String name = safe(user.getFullName()).isEmpty() ? safe(user.getUsername()) : user.getFullName();
        String message = isVip
                ? "Are you sure you want to downgrade <b>" + name + "</b> to Customer?"
                : "Are you sure you want to upgrade <b>" + name + "</b> to Customer VIP?";

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_upgrade_vip);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            dialog.getWindow().setAttributes(params);
        }

        ((TextView) dialog.findViewById(R.id.tvVipTitle)).setText(title);
        TextView tvMsg = dialog.findViewById(R.id.tvVipMessage);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tvMsg.setText(android.text.Html.fromHtml(message, android.text.Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvMsg.setText(android.text.Html.fromHtml(message));
        }

        dialog.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnConfirmVip).setOnClickListener(v ->
                usersRef.child(user.getId()).child("role").setValue(newRole)
                        .addOnSuccessListener(unused -> {
                            // Update local object immediately — don't wait for Firebase listener
                            user.setRole(newRole);
                            applyFilters();
                            Toast.makeText(this,
                                    isVip ? getString(R.string.msg_downgrade_vip_success) : getString(R.string.msg_upgrade_vip_success),
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this,
                                getString(R.string.error_save_failed, e.getMessage()), Toast.LENGTH_SHORT).show()));
        dialog.show();
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
            dialog.getWindow().setWindowAnimations(android.R.style.Animation_Dialog);
        }

        setupDialogDropdowns(dialogBinding);
        bindDialogData(dialogBinding, editingUser);

        dialogBinding.btnClose.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.btnSave.setOnClickListener(v -> saveAccount(dialog, dialogBinding, editingUser));

        dialog.show();
    }

    private void setupDialogDropdowns(DialogAddEditAccountBinding dialogBinding) {
        dialogBinding.roleSelectRow.setOnClickListener(v ->
                showOptionPopup(v, Arrays.asList(ROLES), textOf(dialogBinding.tvRoleSelect),
                        selected -> dialogBinding.tvRoleSelect.setText(selected)));
        dialogBinding.statusSelectRow.setOnClickListener(v ->
                showOptionPopup(v, Arrays.asList(STATUSES), textOf(dialogBinding.tvStatusSelect),
                        selected -> dialogBinding.tvStatusSelect.setText(selected)));
    }

    private void bindDialogData(DialogAddEditAccountBinding dialogBinding, User user) {
        boolean isEdit = user != null;
        dialogBinding.tvDialogTitle.setText(isEdit ? getString(R.string.dialog_edit_account_title) : getString(R.string.dialog_add_account_title));
        if (isEdit) {
            dialogBinding.btnSave.setText(getString(R.string.btn_edit));
            dialogBinding.btnSave.setIcon(ContextCompat.getDrawable(this, R.drawable.edit2));
        } else {
            dialogBinding.btnSave.setText(getString(R.string.btn_add));
            dialogBinding.btnSave.setIcon(ContextCompat.getDrawable(this, R.drawable.plus));
        }

        if (isEdit) {
            dialogBinding.etFullName.setText(user.getFullName());
            dialogBinding.etUsername.setText(user.getUsername());
            dialogBinding.etEmail.setText(user.getEmail());
            dialogBinding.etPhoneNumber.setText(user.getPhoneNumber());
            dialogBinding.etAddress.setText(user.getAddress());
            dialogBinding.tvRoleSelect.setText(user.getRole());
            dialogBinding.tvStatusSelect.setText(user.getStatus());
        } else {
            dialogBinding.tvRoleSelect.setText(ROLES[1]);
            dialogBinding.tvStatusSelect.setText(STATUSES[0]);
        }

        TextWatcher clearErrorsWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                dialogBinding.etFullName.setError(null);
                dialogBinding.etUsername.setError(null);
                dialogBinding.etEmail.setError(null);
                dialogBinding.etPhoneNumber.setError(null);
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
        String role = textOf(dialogBinding.tvRoleSelect);
        String status = textOf(dialogBinding.tvStatusSelect);

        if (!validate(dialogBinding, editingUser, fullName, username, email, phoneNumber)) {
            return;
        }

        String id = editingUser == null ? generateNextAccountId() : editingUser.getId();
        if (id == null) {
            Toast.makeText(this, getString(R.string.error_cannot_create_account_id), Toast.LENGTH_SHORT).show();
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
                    // Update local list immediately — don't wait for Firebase listener
                    if (editingUser != null) {
                        // Edit: find and update existing entry in-place
                        for (int i = 0; i < allUsers.size(); i++) {
                            if (id.equals(safe(allUsers.get(i).getId()))) {
                                allUsers.set(i, user);
                                break;
                            }
                        }
                    } else {
                        // Add: append new user
                        allUsers.add(user);
                    }
                    applyFilters();
                    Toast.makeText(this, getString(editingUser == null ? R.string.msg_save_success : R.string.msg_update_success), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(this, getString(R.string.error_save_failed, e.getMessage()), Toast.LENGTH_SHORT).show());
    }

    private boolean validate(DialogAddEditAccountBinding dialogBinding, User editingUser,
                             String fullName, String username, String email, String phoneNumber) {
        boolean valid = true;

        if (fullName.isEmpty()) {
            dialogBinding.etFullName.setError(getString(R.string.error_fullname_required));
            valid = false;
        }

        if (username.isEmpty()) {
            dialogBinding.etUsername.setError(getString(R.string.error_username_required));
            valid = false;
        } else if (isUsernameExists(username, editingUser == null ? null : editingUser.getId())) {
            dialogBinding.etUsername.setError(getString(R.string.error_username_exists));
            valid = false;
        }

        if (email.isEmpty()) {
            dialogBinding.etEmail.setError(getString(R.string.error_email_required));
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            dialogBinding.etEmail.setError(getString(R.string.error_email_invalid));
            valid = false;
        }

        if (phoneNumber.isEmpty()) {
            dialogBinding.etPhoneNumber.setError(getString(R.string.error_phone_required));
            valid = false;
        } else if (!phoneNumber.matches("^[0-9]{10}$")) {
            dialogBinding.etPhoneNumber.setError(getString(R.string.error_phone_format));
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
            candidate = String.format(Locale.getDefault(), "CS%03d", nextNumber++);
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
        Toast.makeText(this, getString(R.string.msg_migration_start), Toast.LENGTH_SHORT).show();
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
                        user.setRole(getString(R.string.role_customer));
                        isModified = true;
                    }
                    if (TextUtils.isEmpty(user.getStatus())) {
                        user.setStatus(getString(R.string.status_active));
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
                        .setTitle(getString(R.string.dialog_migration_complete_title))
                        .setMessage(getString(R.string.msg_migration_success_format, updatedCount))
                        .setPositiveButton(getString(R.string.btn_ok), null)
                        .show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminAccount.this, getString(R.string.error_migration_failed, error.getMessage()), Toast.LENGTH_SHORT).show();
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
