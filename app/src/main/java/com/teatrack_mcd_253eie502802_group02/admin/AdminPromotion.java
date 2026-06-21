package com.teatrack_mcd_253eie502802_group02.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.adapter.AdminPromotionAdapter;
import com.teatrack_mcd_253eie502802_group02.model.Promotion;

import java.util.ArrayList;
import java.util.List;

public class AdminPromotion extends AppCompatActivity {

    private RecyclerView rvPromotionList;
    private AdminPromotionAdapter adapter;
    private List<Promotion> promotionList;
    private List<Promotion> filteredList;
    private DatabaseReference databaseReference;
    private EditText etSearch;
    private TextView tvEmptyState;
    private Button btnAddPromotion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_promotion);

        initViews();
        setupFirebase();
        setupSearch();

        btnAddPromotion.setOnClickListener(v -> showPromotionDialog(null));
    }

    private void initViews() {
        rvPromotionList = findViewById(R.id.rvPromotionList);
        etSearch = findViewById(R.id.etSearchPromotion);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnAddPromotion = findViewById(R.id.btnAddPromotion);

        promotionList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new AdminPromotionAdapter(filteredList, new AdminPromotionAdapter.OnPromotionActionListener() {
            @Override
            public void onEdit(Promotion promotion) {
                showPromotionDialog(promotion);
            }

            @Override
            public void onDelete(Promotion promotion) {
                confirmDelete(promotion);
            }
        });

        rvPromotionList.setLayoutManager(new LinearLayoutManager(this));
        rvPromotionList.setAdapter(adapter);
    }

    private void setupFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference("vouchers");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                promotionList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Promotion promotion = data.getValue(Promotion.class);
                    if (promotion != null) {
                        promotionList.add(promotion);
                    }
                }
                filter(etSearch.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminPromotion.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
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
        filteredList.clear();
        if (text.isEmpty()) {
            filteredList.addAll(promotionList);
        } else {
            String query = text.toLowerCase().trim();
            for (Promotion p : promotionList) {
                if (p.getCode().toLowerCase().contains(query) || p.getDescription().toLowerCase().contains(query)) {
                    filteredList.add(p);
                }
            }
        }
        adapter.notifyDataSetChanged();
        tvEmptyState.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showPromotionDialog(Promotion promotion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_promotion_admin, null);
        builder.setView(view);

        EditText etCode = view.findViewById(R.id.etPromotionCode);
        EditText etDesc = view.findViewById(R.id.etPromotionDesc);
        Spinner spnType = view.findViewById(R.id.spnPromotionType);
        EditText etValue = view.findViewById(R.id.etPromotionValue);
        EditText etMinSubtotal = view.findViewById(R.id.etMinSubtotal);
        Button btnSave = view.findViewById(R.id.btnSavePromotion);
        Button btnCancel = view.findViewById(R.id.btnCancelPromotion);
        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);

        // Setup Spinner
        String[] types = {getString(R.string.str_type_amount), getString(R.string.str_type_percent)};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnType.setAdapter(spinnerAdapter);

        AlertDialog dialog = builder.create();

        if (promotion != null) {
            tvTitle.setText(R.string.str_edit_promotion);
            etCode.setText(promotion.getCode());
            etDesc.setText(promotion.getDescription());
            spnType.setSelection("percent".equals(promotion.getType()) ? 1 : 0);
            etValue.setText(String.valueOf(promotion.getValue()));
            etMinSubtotal.setText(String.valueOf(promotion.getMinSubtotal()));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String code = etCode.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String valueStr = etValue.getText().toString().trim();
            String minStr = etMinSubtotal.getText().toString().trim();
            String type = spnType.getSelectedItemPosition() == 1 ? "percent" : "amount";

            if (code.isEmpty()) {
                etCode.setError(getString(R.string.str_error_code_empty));
                return;
            }

            // Check if code exists (excluding current promotion if editing)
            for (Promotion p : promotionList) {
                if (p.getCode().equalsIgnoreCase(code)) {
                    if (promotion == null || !promotion.getId().equals(p.getId())) {
                        etCode.setError(getString(R.string.str_error_code_exists));
                        return;
                    }
                }
            }

            if (desc.isEmpty() || valueStr.isEmpty()) {
                Toast.makeText(this, R.string.str_fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            double value;
            try {
                value = Double.parseDouble(valueStr);
            } catch (NumberFormatException e) {
                etValue.setError(getString(R.string.str_error_value_invalid));
                return;
            }

            if (value <= 0) {
                etValue.setError(getString(R.string.str_error_value_invalid));
                return;
            }

            if ("percent".equals(type) && value > 100) {
                etValue.setError(getString(R.string.str_error_percent_invalid));
                return;
            }

            double min = 0;
            if (!minStr.isEmpty()) {
                try {
                    min = Double.parseDouble(minStr);
                    if (min < 0) {
                        etMinSubtotal.setError(getString(R.string.str_error_min_subtotal_negative));
                        return;
                    }
                } catch (NumberFormatException e) {
                    etMinSubtotal.setError(getString(R.string.str_error_min_subtotal_negative));
                    return;
                }
            }

            String id = (promotion == null) ? databaseReference.push().getKey() : promotion.getId();
            Promotion newPromotion = new Promotion(id, code, desc, min, type, value);

            if (id != null) {
                databaseReference.child(id).setValue(newPromotion)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, promotion == null ? R.string.str_add_promotion_success : R.string.str_update_promotion_success, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, getString(R.string.str_error, e.getMessage()), Toast.LENGTH_SHORT).show());
            }
        });

        dialog.show();
    }

    private void confirmDelete(Promotion promotion) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.str_delete_promotion)
                .setMessage(R.string.str_delete_promotion_confirm)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    databaseReference.child(promotion.getId()).removeValue()
                            .addOnSuccessListener(aVoid -> Toast.makeText(AdminPromotion.this, R.string.str_delete_promotion_success, Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }
}
