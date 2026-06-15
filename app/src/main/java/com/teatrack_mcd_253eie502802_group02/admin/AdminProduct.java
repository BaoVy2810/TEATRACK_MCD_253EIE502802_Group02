package com.teatrack_mcd_253eie502802_group02.admin;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    private RecyclerView rvProducts;
    private ProductAdapter adapter;
    private List<Product> productList;
    private List<Product> filteredList;
    private DatabaseReference productsRef;
    private EditText etSearch;
    private com.google.android.material.button.MaterialButton btnCategorySelect;
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
        rvProducts = findViewById(R.id.rvProducts);
        etSearch = findViewById(R.id.etSearch);
        btnCategorySelect = findViewById(R.id.btnCategorySelect);
        if (btnCategorySelect != null) {
            btnCategorySelect.setOnClickListener(this::showCategoryDialog);
        }

        productList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new ProductAdapter(this, filteredList);

        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(adapter);

        // Setup Add Product floating action button
        View fabAddProduct = findViewById(R.id.fabAddProduct);
        if (fabAddProduct != null) {
            fabAddProduct.setOnClickListener(v ->
                Toast.makeText(this, R.string.msg_under_development, Toast.LENGTH_SHORT).show()
            );
        }

        findViewById(R.id.btnExportExcel).setOnClickListener(v ->
            Toast.makeText(this, R.string.msg_excel_init, Toast.LENGTH_SHORT).show()
        );

        // Setup Header actions
        View btnProfile = findViewById(R.id.btn_profile);
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, com.teatrack_mcd_253eie502802_group02.admin.AdminProfile.class))
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

                // Ensure "All categories" is always first
                String allLabel = getString(R.string.filter_all);
                if (sortedCategories.contains(allLabel)) {
                    sortedCategories.remove(allLabel);
                    sortedCategories.add(0, allLabel);
                }

                updateCategoryList(sortedCategories);
                performFilter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminProduct.this, getString(R.string.error_firebase, error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCategoryList(List<String> categories) {
        this.categoryList = categories;
        if (!categoryList.contains(selectedCategory)) {
            selectedCategory = getString(R.string.filter_all);
        }
        if (btnCategorySelect != null) {
            btnCategorySelect.setText(selectedCategory + " ▾");
        }
    }

    /**
     * Shows a custom category selector dialog matching the mockup design:
     * - Blue gradient header with title and checkmark icon
     * - White body with category items in blue text
     * - Subtle blue dividers between items
     */
    private void showCategoryDialog(View anchor) {
        if (categoryList == null || categoryList.isEmpty()) return;

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_category_selector);

        // Make dialog background transparent so our rounded corners show
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            dialog.getWindow().setAttributes(params);
        }

        // Set header title to current selection
        TextView tvTitle = dialog.findViewById(R.id.tvCategoryDialogTitle);
        if (tvTitle != null) {
            tvTitle.setText(selectedCategory);
        }

        // Setup RecyclerView with category items
        RecyclerView rvCategories = dialog.findViewById(R.id.rvCategoryList);
        if (rvCategories != null) {
            rvCategories.setLayoutManager(new LinearLayoutManager(this));
            CategoryDialogAdapter categoryAdapter = new CategoryDialogAdapter(categoryList, category -> {
                selectedCategory = category;
                if (btnCategorySelect != null) {
                    btnCategorySelect.setText(selectedCategory + " ▾");
                }
                performFilter();
                dialog.dismiss();
            });
            rvCategories.setAdapter(categoryAdapter);
        }

        dialog.show();
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {}
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
        adapter.updateList(filteredList);
    }

    private void setupBottomNavigation() {
        int[] navItemIds = {
                R.id.nav_dashboard,
                R.id.nav_products,
                R.id.nav_orders,
                R.id.nav_account,
                R.id.nav_forum,
                R.id.nav_branch,
                R.id.nav_feedbacks,
                R.id.nav_promotion
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

    // ─── Inner adapter for the category dialog RecyclerView ───────────────────

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
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_category_dialog, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String category = categories.get(position);
            holder.tvName.setText(category);

            // Hide divider for last item
            if (position == categories.size() - 1) {
                holder.divider.setVisibility(View.GONE);
            } else {
                holder.divider.setVisibility(View.VISIBLE);
            }

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategorySelected(category);
                }
            });
        }

        @Override
        public int getItemCount() {
            return categories != null ? categories.size() : 0;
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
