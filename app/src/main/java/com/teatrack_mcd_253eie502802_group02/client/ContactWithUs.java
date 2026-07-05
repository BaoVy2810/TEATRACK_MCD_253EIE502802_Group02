package com.teatrack_mcd_253eie502802_group02.client;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioGroup;
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
import com.teatrack_mcd_253eie502802_group02.model.ContactRequest;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ContactWithUs extends AppCompatActivity {

    private static final String DATABASE_URL = "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";
    private EditText edtFullName, edtEmail, edtPhone, edtContent;
    private AutoCompleteTextView spinnerBranch;
    private RadioGroup radioTopic;
    private View btnSubmit;
    private DatabaseReference mDatabase;
    private List<String> branchNames = new ArrayList<>();
    private ArrayAdapter<String> branchAdapter;

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

        // Khởi tạo Firebase với URL Singapore
        mDatabase = FirebaseDatabase.getInstance(DATABASE_URL).getReference();

        initViews();
        setupHeader();
        setupNavBar();
        setupBranchSpinner();
        loadBranchesFromFirebase();

        btnSubmit.setOnClickListener(v -> submitFeedback());
    }

    private void initViews() {
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtContent = findViewById(R.id.edtContent);
        spinnerBranch = findViewById(R.id.spinnerBranch);
        radioTopic = findViewById(R.id.radioTopic);
        btnSubmit = findViewById(R.id.btnSubmit);

        // Setup TextWatchers to maintain blue color when text is present
        setupTextWatcher(edtFullName);
        setupTextWatcher(edtEmail);
        setupTextWatcher(edtPhone);
        setupTextWatcher(edtContent);
        setupTextWatcher(spinnerBranch);

        radioTopic.setOnCheckedChangeListener((group, checkedId) -> {
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof android.widget.RadioButton) {
                    android.widget.RadioButton rb = (android.widget.RadioButton) child;
                    if (rb.getId() == checkedId) {
                        rb.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.brand_blue));
                        // Sử dụng font SemiBold (thay cho Bold) để không bị lỗi font mặc định
                        rb.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(this, R.font.inter_semibold));
                    } else {
                        rb.setTextColor(0xFF333333);
                        // Quay lại font Regular
                        rb.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(this, R.font.inter));
                    }
                }
            }
        });
        spinnerBranch.setOnItemClickListener((parent, view, position, id) -> {
            spinnerBranch.setActivated(true);
            spinnerBranch.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.brand_blue));
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

    private void setupNavBar() {
        // Nav bar removed from layout
    }

    private void setupBranchSpinner() {
        branchAdapter = new ArrayAdapter<>(
                this,
                R.layout.item_spinner_branch,
                branchNames
        );
        spinnerBranch.setAdapter(branchAdapter);
    }

    private void loadBranchesFromFirebase() {
        mDatabase.child("agencies").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String prompt = getString(R.string.str_branch_prompt);
                branchNames.clear();
                branchNames.add(prompt);

                for (DataSnapshot agencySnapshot : snapshot.getChildren()) {
                    String name = agencySnapshot.child("name").getValue(String.class);
                    if (name != null) branchNames.add(name);
                }
                branchAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void submitFeedback() {
        if (!validateForm()) return;

        // Thu thập dữ liệu
        String fullname = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String branch = spinnerBranch.getText().toString();
        String content = edtContent.getText().toString().trim();

        int selectedTopicId = radioTopic.getCheckedRadioButtonId();
        android.widget.RadioButton rbSelected = findViewById(selectedTopicId);
        String topic = rbSelected.getText().toString();

        // Tạo key tự sinh bằng push()
        String pushKey = mDatabase.child("contacts").push().getKey();
        if (pushKey == null) {
            Toast.makeText(this, "Lỗi: Không thể tạo ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Định dạng thời gian: 16/03/2026 10:23
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String currentTime = sdf.format(new Date());

        // Tạo đối tượng yêu cầu với pushKey
        ContactRequest request = new ContactRequest(
                pushKey,
                fullname,
                email,
                phone,
                branch,
                topic.toLowerCase(), // Lưu topic viết thường
                content,
                currentTime,
                1, // status: 1 (Chờ xử lý)
                false, // read: false
                "" // note: rỗng
        );

        // Lưu vào Firebase
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
        if (spinnerBranch.getText().toString().isEmpty() || 
            spinnerBranch.getText().toString().equals(getString(R.string.str_branch_prompt))) {
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
        spinnerBranch.setText(""); // Xóa text của dropdown
        
        // Reset activated states
        edtFullName.setActivated(false);
        edtEmail.setActivated(false);
        edtPhone.setActivated(false);
        edtContent.setActivated(false);
        spinnerBranch.setActivated(false);
    }
}
