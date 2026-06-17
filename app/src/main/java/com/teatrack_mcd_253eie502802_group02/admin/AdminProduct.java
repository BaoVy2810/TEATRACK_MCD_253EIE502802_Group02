package com.teatrack_mcd_253eie502802_group02.admin;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.adapter.ProductAdapter;
import com.teatrack_mcd_253eie502802_group02.model.Product;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminProduct extends AppCompatActivity {

    private ProductAdapter productAdapter;
    private List<Product> productList;
    private List<Product> filteredList;
    private DatabaseReference productsRef;
    private EditText etSearch;
    private MaterialButton btnCategorySelect;
    private List<String> categoryList = new ArrayList<>();
    private String selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product);

        selectedCategory = getString(R.string.filter_all);
        initViews();
        setupFirebase();
        setupSearch();
        setupBottomNavigation();
    }

    private void initViews() {
        RecyclerView rvProducts = findViewById(R.id.rvProducts);
        etSearch = findViewById(R.id.etSearch);
        btnCategorySelect = findViewById(R.id.btnCategorySelect);
        
        if (btnCategorySelect != null) {
            btnCategorySelect.setOnClickListener(this::showCategoryDialog);
        }

        productList = new ArrayList<>();
        filteredList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, filteredList);
        
        productAdapter.setOnEditClickListener(product -> 
            new EditProductDialog(this, product, categoryList).show()
        );

        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(productAdapter);

        View fabAddProduct = findViewById(R.id.fabAddProduct);
        if (fabAddProduct != null) {
            fabAddProduct.setOnClickListener(v -> showAddProductDialog());
        }

        findViewById(R.id.btnExportExcel).setOnClickListener(v ->
            Toast.makeText(this, R.string.msg_excel_init, Toast.LENGTH_SHORT).show()
        );

        View btnProfile = findViewById(R.id.btn_profile);
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, AdminProfile.class))
            );
        }
    }

    private void setupFirebase() {
        productsRef = FirebaseDatabase.getInstance().getReference(getString(R.string.firebase_collection_products));
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                Set<String> categories = new HashSet<>();
                categories.add(getString(R.string.filter_all));

                for (DataSnapshot data : snapshot.getChildren()) {
                    Product product = data.getValue(Product.class);
                    if (product != null) {
                        product.setId(data.getKey());
                        productList.add(product);
                        if (product.getCategory() != null) {
                            categories.add(product.getCategory());
                        }
                    }
                }

                List<String> sortedCategories = new ArrayList<>(categories);
                Collections.sort(sortedCategories);

                String allLabel = getString(R.string.filter_all);
                if (sortedCategories.contains(allLabel)) {
                    sortedCategories.remove(allLabel);
                    sortedCategories.add(0, allLabel);
                }

                categoryList = sortedCategories;
                updateCategoryButtonText();
                performFilter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminProduct.this, getString(R.string.error_firebase, error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCategoryButtonText() {
        if (btnCategorySelect != null) {
            btnCategorySelect.setText(String.format("%s ▾", selectedCategory));
        }
    }

    private void showCategoryDialog(View anchor) {
        if (categoryList == null || categoryList.isEmpty()) return;

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_category_selector);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.6);
            params.gravity = Gravity.TOP | Gravity.START;
            
            int[] location = new int[2];
            anchor.getLocationOnScreen(location);
            params.x = location[0];
            params.y = location[1] + anchor.getHeight();
            
            dialog.getWindow().setAttributes(params);
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        TextView tvTitle = dialog.findViewById(R.id.tvCategoryDialogTitle);
        if (tvTitle != null) tvTitle.setText(selectedCategory);

        RecyclerView rvCategories = dialog.findViewById(R.id.rvCategoryList);
        if (rvCategories != null) {
            rvCategories.setLayoutManager(new LinearLayoutManager(this));
            rvCategories.setAdapter(new CategoryDialogAdapter(categoryList, category -> {
                selectedCategory = category;
                updateCategoryButtonText();
                performFilter();
                dialog.dismiss();
            }));
        }

        dialog.show();
    }

    private void showAddProductDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_product);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
            dialog.getWindow().setAttributes(params);
        }

        EditText etProductId = dialog.findViewById(R.id.etProductId);
        EditText etProductName = dialog.findViewById(R.id.etProductName);
        Spinner spinnerCategory = dialog.findViewById(R.id.spinnerCategory);
        CheckBox cbVisible = dialog.findViewById(R.id.cbVisible);
        CheckBox cbSpecial = dialog.findViewById(R.id.cbSpecial);
        EditText etPriceM = dialog.findViewById(R.id.etPriceM);
        EditText etPriceL = dialog.findViewById(R.id.etPriceL);
        EditText etVipPriceM = dialog.findViewById(R.id.etVipPriceM);
        EditText etVipPriceL = dialog.findViewById(R.id.etVipPriceL);
        EditText etProductInfo = dialog.findViewById(R.id.etProductInfo);
        EditText etProductDesc = dialog.findViewById(R.id.etProductDesc);
        
        List<String> spinnerCategories = new ArrayList<>(categoryList);
        spinnerCategories.remove(getString(R.string.filter_all));
        
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerCategories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        dialog.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            String id = etProductId.getText().toString().trim();
            String name = etProductName.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "";
            
            if (id.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please fill in ID and Name", Toast.LENGTH_SHORT).show();
                return;
            }

            int pM = getIntFromEt(etPriceM);
            int pL = getIntFromEt(etPriceL);
            int vM = getIntFromEt(etVipPriceM);
            int vL = getIntFromEt(etVipPriceL);

            Product newProduct = new Product(
                id, id, name, category, pM, pL, vM, vL, 
                etProductInfo.getText().toString().trim(), 
                etProductDesc.getText().toString().trim(), 
                "logo_ngo_gia.png", cbVisible.isChecked(), cbSpecial.isChecked()
            );

            productsRef.child(id).setValue(newProduct)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }

    private int getIntFromEt(EditText et) {
        String s = et.getText().toString().trim();
        return s.isEmpty() ? 0 : Integer.parseInt(s);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { performFilter(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void performFilter() {
        String query = etSearch.getText().toString().toLowerCase().trim();
        filteredList.clear();

        for (Product product : productList) {
            boolean matchesSearch = product.getName().toLowerCase().contains(query);
            boolean matchesCategory = selectedCategory.equals(getString(R.string.filter_all)) ||
                                     (product.getCategory() != null && product.getCategory().equals(selectedCategory));

            if (matchesSearch && matchesCategory) {
                filteredList.add(product);
            }
        }
        productAdapter.updateList(filteredList);
    }

    private void setupBottomNavigation() {
        int[] navItemIds = {
                R.id.nav_dashboard, R.id.nav_products, R.id.nav_orders,
                R.id.nav_account, R.id.nav_forum, R.id.nav_branch,
                R.id.nav_feedbacks, R.id.nav_promotion
        };

        NavBarHelper.setupNavBar(this, navItemIds, R.id.nav_products, v -> {
            int id = v.getId();
            if (id == R.id.nav_products) return;

            Class<?> destination = null;
            if (id == R.id.nav_dashboard) destination = AdminDashboard.class;
            else if (id == R.id.nav_account) destination = AdminAccount.class;

            if (destination != null) {
                startActivity(new Intent(this, destination));
                finish();
            } else {
                Toast.makeText(this, R.string.msg_under_development, Toast.LENGTH_SHORT).show();
            }
        });
    }

    interface OnCategorySelectedListener {
        void onCategorySelected(String category);
    }

    static class CategoryDialogAdapter extends RecyclerView.Adapter<CategoryDialogAdapter.ViewHolder> {
        private final List<String> categories;
        private final OnCategorySelectedListener listener;

        CategoryDialogAdapter(List<String> categories, OnCategorySelectedListener listener) {
            this.categories = categories;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_dialog, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String category = categories.get(position);
            holder.tvName.setText(category);
            holder.divider.setVisibility(position == categories.size() - 1 ? View.GONE : View.VISIBLE);
            holder.itemView.setOnClickListener(v -> listener.onCategorySelected(category));
        }

        @Override
        public int getItemCount() {
            return categories.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            View divider;
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvCategoryItemName);
                divider = itemView.findViewById(R.id.divider);
            }
        }
    }
}
