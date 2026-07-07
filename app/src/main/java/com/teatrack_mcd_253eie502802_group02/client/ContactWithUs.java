package com.teatrack_mcd_253eie502802_group02.client;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.ContactRequest;
import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ContactWithUs extends BaseActivity {

    private static final String DATABASE_URL = "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";

    private EditText edtFullName, edtEmail, edtPhone, edtContent;
    private TextView tvBranchSelection;
    private View btnSelectBranch;
    private RadioGroup radioTopic;
    private View btnSubmit;
    private DatabaseReference mDatabase;
    private final List<String> agencyNames = new ArrayList<>();
    private String selectedBranchName = "";
    private PopupWindow branchPopupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact_with_us);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        mDatabase = FirebaseDatabase.getInstance(DATABASE_URL).getReference();

        initViews();
        setupHeader();
        setupBranchPicker();
        loadBranchesFromFirebase();

        btnSubmit.setOnClickListener(v -> submitFeedback());
    }

    private void initViews() {
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtContent = findViewById(R.id.edtContent);
        tvBranchSelection = findViewById(R.id.tvBranchSelection);
        btnSelectBranch = findViewById(R.id.btnSelectBranch);
        radioTopic = findViewById(R.id.radioTopic);
        btnSubmit = findViewById(R.id.btnSubmit);

        setupTextWatcher(edtFullName);
        setupTextWatcher(edtEmail);
        setupTextWatcher(edtPhone);
        setupTextWatcher(edtContent);

        radioTopic.setOnCheckedChangeListener((group, checkedId) -> {
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof android.widget.RadioButton) {
                    android.widget.RadioButton rb = (android.widget.RadioButton) child;
                    if (rb.getId() == checkedId) {
                        rb.setTextColor(ContextCompat.getColor(this, R.color.brand_blue));
                        rb.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(this, R.font.inter_semibold));
                    } else {
                        rb.setTextColor(0xFF333333);
                        rb.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(this, R.font.inter));
                    }
                }
            }
        });
    }

    private void setupTextWatcher(EditText editText) {
        editText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editText.setActivated(s.length() > 0);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void setupHeader() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        View btnShare = findViewById(R.id.btnShare);
        if (btnShare != null) {
            btnShare.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.str_contact_title));
                startActivity(Intent.createChooser(intent, getString(R.string.blog_detail_share)));
            });
        }
    }

    private void setupBranchPicker() {
        if (btnSelectBranch != null) {
            btnSelectBranch.setOnClickListener(v -> showBranchPopup());
        }
    }

    private void loadBranchesFromFirebase() {
        mDatabase.child("agencies").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                agencyNames.clear();
                for (DataSnapshot agencySnapshot : snapshot.getChildren()) {
                    String name = agencySnapshot.child("name").getValue(String.class);
                    if (name != null && !name.trim().isEmpty()) {
                        agencyNames.add(name.trim());
                    }
                }
                if (branchPopupWindow != null && branchPopupWindow.isShowing()) {
                    View content = branchPopupWindow.getContentView();
                    if (content != null) {
                        populateBranchOptions(content.findViewById(R.id.layoutBranchOptions));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showBranchPopup() {
        if (btnSelectBranch == null) {
            return;
        }
        if (agencyNames.isEmpty()) {
            Toast.makeText(this, R.string.str_branch_prompt, Toast.LENGTH_SHORT).show();
            return;
        }
        if (branchPopupWindow != null && branchPopupWindow.isShowing()) {
            branchPopupWindow.dismiss();
            return;
        }

        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_contact_branch, null, false);
        branchPopupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        branchPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        branchPopupWindow.setOutsideTouchable(true);
        branchPopupWindow.setElevation(16f);

        populateBranchOptions(popupView.findViewById(R.id.layoutBranchOptions));

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int xOff = btnSelectBranch.getWidth() - popupView.getMeasuredWidth();
        branchPopupWindow.showAsDropDown(btnSelectBranch, Math.max(xOff, 0), 8);
    }

    private void populateBranchOptions(LinearLayout container) {
        if (container == null) {
            return;
        }
        container.removeAllViews();
        for (String name : agencyNames) {
            TextView option = createBranchOption(name);
            container.addView(option);
        }
    }

    private TextView createBranchOption(String name) {
        TextView option = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = containerTopMargin(name);
        option.setLayoutParams(params);
        option.setPadding(dp(8), dp(8), dp(32), dp(8));
        option.setText(name);
        option.setTextSize(12f);
        option.setTextColor(ContextCompat.getColor(this, R.color.black));
        applyPopupOptionState(option, name.equals(selectedBranchName));
        option.setOnClickListener(v -> {
            selectedBranchName = name;
            updateBranchSelectionUi();
            if (branchPopupWindow != null) {
                branchPopupWindow.dismiss();
            }
        });
        return option;
    }

    private int containerTopMargin(String name) {
        return agencyNames.indexOf(name) == 0 ? dp(6) : dp(1);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void applyPopupOptionState(TextView option, boolean selected) {
        option.setBackgroundResource(selected ? R.drawable.bg_filter_option_selected : android.R.color.transparent);
        option.setTextColor(ContextCompat.getColor(this, selected ? R.color.brand_blue : R.color.black));
        option.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
    }

    private void updateBranchSelectionUi() {
        if (tvBranchSelection == null) {
            return;
        }
        boolean hasSelection = selectedBranchName != null && !selectedBranchName.isEmpty();
        tvBranchSelection.setText(hasSelection ? selectedBranchName : "");
        tvBranchSelection.setTextColor(ContextCompat.getColor(
                this,
                hasSelection ? R.color.brand_blue : R.color.edittext_text_color
        ));
        if (btnSelectBranch != null) {
            btnSelectBranch.setActivated(hasSelection);
        }
    }

    private void submitFeedback() {
        if (!validateForm()) return;

        String fullname = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String branch = selectedBranchName.trim();
        String content = edtContent.getText().toString().trim();

        int selectedTopicId = radioTopic.getCheckedRadioButtonId();
        android.widget.RadioButton rbSelected = findViewById(selectedTopicId);
        String topic = rbSelected.getText().toString();

        String pushKey = mDatabase.child("contacts").push().getKey();
        if (pushKey == null) {
            Toast.makeText(this, "Lỗi: Không thể tạo ID", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String currentTime = sdf.format(new Date());

        ContactRequest request = new ContactRequest(
                pushKey,
                fullname,
                email,
                phone,
                branch,
                topic.toLowerCase(),
                content,
                currentTime,
                1,
                false,
                ""
        );

        mDatabase.child("contacts").child(pushKey).setValue(request)
                .addOnSuccessListener(aVoid -> showSuccessDialog())
                .addOnFailureListener(e -> Toast.makeText(ContactWithUs.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private boolean validateForm() {
        if (edtFullName.getText().toString().trim().isEmpty()) {
            edtFullName.setError(getString(R.string.err_required));
            return false;
        }
        String email = edtEmail.getText().toString().trim();
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError(getString(R.string.err_invalid_email));
            return false;
        }
        if (edtPhone.getText().toString().trim().isEmpty()) {
            edtPhone.setError(getString(R.string.err_required));
            return false;
        }
        if (selectedBranchName == null || selectedBranchName.trim().isEmpty()
                || !agencyNames.contains(selectedBranchName)) {
            Toast.makeText(this, R.string.str_branch_prompt, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (radioTopic.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, R.string.str_feedback_topic, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (edtContent.getText().toString().trim().isEmpty()) {
            edtContent.setError(getString(R.string.err_required));
            return false;
        }
        return true;
    }

    private void showSuccessDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_contact_succes);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.findViewById(R.id.btnOk).setOnClickListener(v -> {
            dialog.dismiss();
            clearForm();
        });
        dialog.show();
    }

    private void clearForm() {
        edtFullName.setText("");
        edtEmail.setText("");
        edtPhone.setText("");
        edtContent.setText("");
        radioTopic.clearCheck();
        selectedBranchName = "";
        updateBranchSelectionUi();

        edtFullName.setActivated(false);
        edtEmail.setActivated(false);
        edtPhone.setActivated(false);
        edtContent.setActivated(false);
    }
}
