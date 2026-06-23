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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
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
import com.teatrack_mcd_253eie502802_group02.util.CloudinaryHelper;

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
    private AddProductDialog currentAddDialog;
    private EditProductDialog currentEditDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product);

        CloudinaryHelper.init(this);
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

        productAdapter.setOnEditClickListener(product -> {
            currentEditDialog = new EditProductDialog(this, product, categoryList);
            currentEditDialog.show();
        });

        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(productAdapter);

        View fabAddProduct = findViewById(R.id.fabAddProduct);
        if (fabAddProduct != null) {
            fabAddProduct.setOnTouchListener(new View.OnTouchListener() {
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
                                // Nếu không di chuyển nhiều thì coi là hành động Click
                                currentAddDialog = new AddProductDialog(AdminProduct.this, categoryList);
                                currentAddDialog.show();
                            }
                            return true;
                    }
                    return false;
                }
            });
        }

        findViewById(R.id.btnExportExcel).setOnClickListener(v ->
                Toast.makeText(this, R.string.msg_excel_init, Toast.LENGTH_SHORT).show()
        );

        View btnProfile = findViewById(R.id.btn_profile);
        if (btnProfile != null) {
            com.teatrack_mcd_253eie502802_group02.shared.ui.HeaderMenuHelper.setupProfileMenu(this);
        }
    }

    private void setupFirebase() {
        // Sử dụng URL tường minh cho khu vực asia-southeast1
        String dbUrl = "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";
        productsRef = FirebaseDatabase.getInstance(dbUrl).getReference(getString(R.string.firebase_collection_products));

        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();

                if (!snapshot.exists()) {
                    android.util.Log.w("AdminProduct", "Không tìm thấy nút 'products' trên Firebase");
                    Toast.makeText(AdminProduct.this, "Chưa có dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
                }

                Set<String> categories = new HashSet<>();
                categories.add(getString(R.string.filter_all));

                for (DataSnapshot data : snapshot.getChildren()) {
                    try {
                        Product product = data.getValue(Product.class);
                        if (product != null) {
                            // Ưu tiên dùng Key của Firebase làm ID nếu ID trong Object bị trống
                            if (product.getId() == null || product.getId().isEmpty()) {
                                product.setId(data.getKey());
                            }
                            // Đồng bộ code và id
                            if (product.getCode() == null || product.getCode().isEmpty()) {
                                product.setCode(data.getKey());
                            }

                            productList.add(product);
                            if (product.getCategory() != null && !product.getCategory().isEmpty()) {
                                categories.add(product.getCategory());
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("AdminProduct", "Lỗi deserialization tại " + data.getKey() + ": " + e.getMessage());
                    }
                }

                android.util.Log.d("AdminProduct", "Đã tải " + productList.size() + " sản phẩm");

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
                android.util.Log.e("AdminProduct", "Lỗi Firebase: " + error.getMessage());
                Toast.makeText(AdminProduct.this, "Lỗi kết nối Firebase: " + error.getMessage(), Toast.LENGTH_LONG).show();
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

        View popupView = LayoutInflater.from(this).inflate(R.layout.dialog_category_selector, null);
        PopupWindow popupWindow = new PopupWindow(popupView,
                (int) (getResources().getDisplayMetrics().widthPixels * 0.45),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(20);

        TextView tvTitle = popupView.findViewById(R.id.tvCategoryDialogTitle);
        if (tvTitle != null) {
            tvTitle.setText(selectedCategory);
            tvTitle.setVisibility(View.GONE);
        }

        RecyclerView rvCategories = popupView.findViewById(R.id.rvCategoryList);
        if (rvCategories != null) {
            rvCategories.setLayoutManager(new LinearLayoutManager(this));
            rvCategories.setAdapter(new CategoryDialogAdapter(categoryList, selectedCategory, category -> {
                selectedCategory = category;
                updateCategoryButtonText();
                performFilter();
                popupWindow.dismiss();
            }));
        }

        popupWindow.showAsDropDown(anchor, 0, 10);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1001: // Thêm sản phẩm - Chọn từ Thư viện
                    if (data != null && data.getData() != null && currentAddDialog != null) {
                        currentAddDialog.handleImageResult(data.getData());
                    }
                    break;
                case 1002: // Thêm sản phẩm - Chụp từ Camera
                    if (currentAddDialog != null) {
                        currentAddDialog.handleCameraResult();
                    }
                    break;
                case 1003: // Chỉnh sửa sản phẩm - Chọn từ Thư viện
                    if (data != null && data.getData() != null && currentEditDialog != null) {
                        currentEditDialog.handleImageResult(data.getData());
                    }
                    break;
                case 1004: // Chỉnh sửa sản phẩm - Chụp từ Camera
                    if (currentEditDialog != null) {
                        currentEditDialog.handleCameraResult();
                    }
                    break;
            }
        }
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
            else if (id == R.id.nav_orders) destination = AdminOrders.class;
            else if (id == R.id.nav_account) destination = AdminAccount.class;
            else if (id == R.id.nav_forum) destination = AdminBlog.class;
            else if (id == R.id.nav_branch) destination = AdminAgency.class;
            else if (id == R.id.nav_feedbacks) destination = AdminComplaints.class;
            else if (id == R.id.nav_promotion) destination = AdminPromotion.class;

            if (destination != null) {
                startActivity(new Intent(this, destination));
                finish();
            }
        });
    }
}
