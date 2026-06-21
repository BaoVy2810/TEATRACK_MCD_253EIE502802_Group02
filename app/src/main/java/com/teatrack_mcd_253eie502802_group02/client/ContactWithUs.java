package com.teatrack_mcd_253eie502802_group02.client;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.ContactRequest;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;

import java.util.ArrayList;
import java.util.List;

public class ContactWithUs extends AppCompatActivity {

    private EditText edtFullName, edtEmail, edtPhone, edtContent;
    private Spinner spinnerBranch;
    private RadioGroup radioTopic;
    private MaterialButton btnSubmit;
    private DatabaseReference mDatabase;
    private List<String> branchNames = new ArrayList<>();
    private ArrayAdapter<String> branchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_with_us);

        mDatabase = FirebaseDatabase.getInstance().getReference();

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
    }

    private void setupHeader() {
        findViewById(R.id.btn_cart).setOnClickListener(v ->
                startActivity(new Intent(this, Cart.class)));

        findViewById(R.id.btn_profile).setOnClickListener(v ->
                startActivity(new Intent(this, UserProfile.class)));
    }

    private void setupNavBar() {
        int[] navItemIds = {
                R.id.nav_home,
                R.id.nav_menu,
                R.id.nav_orders,
                R.id.nav_promotion,
                R.id.nav_profile
        };

        // We are on Contact, but maybe we mark another as active or none
        NavBarHelper.setupNavBar(this, navItemIds, -1, v -> {
            int id = v.getId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, Homepage.class));
            } else if (id == R.id.nav_menu) {
                startActivity(new Intent(this, Menu.class));
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, OrderHistory.class));
            } else if (id == R.id.nav_promotion) {
                // startActivity(new Intent(this, Promotion.class));
                Toast.makeText(this, R.string.str_coming_soon, Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, UserProfile.class));
            }
        });
    }

    private void setupBranchSpinner() {
        branchNames.add(getString(R.string.str_branch_prompt));

        branchAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                branchNames
        );
        branchAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerBranch.setAdapter(branchAdapter);
    }

    private void loadBranchesFromFirebase() {
        mDatabase.child("agencies").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Keep the prompt at index 0
                String prompt = getString(R.string.str_branch_prompt);
                branchNames.clear();
                branchNames.add(prompt);

                for (DataSnapshot agencySnapshot : snapshot.getChildren()) {
                    // Assuming each agency has a "name" field
                    String name = agencySnapshot.child("name").getValue(String.class);
                    if (name != null) {
                        branchNames.add(name);
                    }
                }
                branchAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ContactWithUs.this, "Lỗi tải chi nhánh", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitFeedback() {
        if (!validateForm()) return;

        // Lấy dữ liệu từ giao diện
        String fullname = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String branch = spinnerBranch.getSelectedItem().toString();
        String content = edtContent.getText().toString().trim();

        // Lấy Topic từ RadioButton đã chọn
        int selectedTopicId = radioTopic.getCheckedRadioButtonId();
        android.widget.RadioButton rbSelected = findViewById(selectedTopicId);
        String topic = rbSelected.getText().toString();

        // Tạo đối tượng ContactRequest
        ContactRequest request = new ContactRequest(
                fullname, email, phone, branch, topic, content, System.currentTimeMillis()
        );

        // Lưu dữ liệu vào node "contacts" trên Firebase
        mDatabase.child("contacts").push().setValue(request)
                .addOnSuccessListener(aVoid -> {
                    showSuccessDialog();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi gửi phản hồi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateForm() {
        String fullname = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String content = edtContent.getText().toString().trim();

        if (fullname.isEmpty()) {
            edtFullName.setError(getString(R.string.err_required));
            return false;
        }
        if (email.isEmpty()) {
            edtEmail.setError(getString(R.string.err_required));
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError(getString(R.string.err_invalid_email));
            return false;
        }
        if (phone.isEmpty()) {
            edtPhone.setError(getString(R.string.err_required));
            return false;
        }
        if (content.isEmpty()) {
            edtContent.setError(getString(R.string.err_required));
            return false;
        }
        if (spinnerBranch.getSelectedItemPosition() == 0) {
            Toast.makeText(this, R.string.str_branch_prompt, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (radioTopic.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, R.string.str_feedback_topic, Toast.LENGTH_SHORT).show();
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

        MaterialButton btnOk = dialog.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(v -> {
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
        spinnerBranch.setSelection(0);
    }
}