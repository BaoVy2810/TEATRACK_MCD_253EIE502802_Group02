package com.teatrack_mcd_253eie502802_group02.client;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioGroup;
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

    private static final String DATABASE_URL = "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";
    private EditText edtFullName, edtEmail, edtPhone, edtContent;
    private AutoCompleteTextView spinnerBranch;
    private RadioGroup radioTopic;
    private MaterialButton btnSubmit;
    private DatabaseReference mDatabase;
    private List<String> branchNames = new ArrayList<>();
    private ArrayAdapter<String> branchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_with_us);

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
        
        // Reset text
        edtFullName.setText("");
        edtEmail.setText("");
        edtPhone.setText("");
        edtContent.setText("");
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

        NavBarHelper.setupNavBar(this, navItemIds, -1, v -> {
            int id = v.getId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, Homepage.class));
            } else if (id == R.id.nav_menu) {
                startActivity(new Intent(this, Menu.class));
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, OrderHistory.class));
            } else if (id == R.id.nav_promotion) {
                startActivity(new Intent(this, BlogGeneral.class));
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, UserProfile.class));
            }
        });
    }

    private void setupBranchSpinner() {
        branchAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1, // Dùng layout mặc định của Android
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

        // Tạo đối tượng yêu cầu
        ContactRequest request = new ContactRequest(
                fullname, email, phone, branch, topic, content, System.currentTimeMillis()
        );

        // Lưu vào node 'contacts' trên Firebase
        mDatabase.child("contacts").push().setValue(request)
                .addOnSuccessListener(aVoid -> showSuccessDialog())
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
    }
}
