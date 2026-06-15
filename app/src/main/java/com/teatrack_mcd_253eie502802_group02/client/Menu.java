package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.teatrack_mcd_253eie502802_group02.MainActivity;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.adapter.ProductCardAdapter;
import com.teatrack_mcd_253eie502802_group02.data.FirebaseProductRepository;
import com.teatrack_mcd_253eie502802_group02.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Menu extends AppCompatActivity {
    private final FirebaseProductRepository repository = new FirebaseProductRepository();
    private final List<Product> allProducts = new ArrayList<>();
    private final List<Product> filteredProducts = new ArrayList<>();
    private ProductCardAdapter adapter;

    private TextView chipAllCategories;
    private TextView chipPureTea;
    private TextView chipMilkTea;
    private TextView chipTeaLatte;
    private TextView chipFruitTea;
    private TextView tvMenuSectionTitle;
    private String selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        selectedCategory = getIntent() != null
                ? getIntent().getStringExtra(MainActivity.EXTRA_MENU_CATEGORY)
                : null;

        bindViews();
        setupProducts();
        setupFilterChips();
        loadProductsFromFirebase();
        setupBottomNav();
    }

    private void bindViews() {
        chipAllCategories = findViewById(R.id.chipAllCategories);
        chipPureTea = findViewById(R.id.chipPureTea);
        chipMilkTea = findViewById(R.id.chipMilkTea);
        chipTeaLatte = findViewById(R.id.chipTeaLatte);
        chipFruitTea = findViewById(R.id.chipFruitTea);
        tvMenuSectionTitle = findViewById(R.id.tvMenuSectionTitle);
    }

    private void setupProducts() {
        RecyclerView rvMenuProducts = findViewById(R.id.rvMenuProducts);
        rvMenuProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rvMenuProducts.setNestedScrollingEnabled(false);
        adapter = new ProductCardAdapter(filteredProducts);
        rvMenuProducts.setAdapter(adapter);
    }

    private void setupFilterChips() {
        if (chipAllCategories != null) {
            chipAllCategories.setOnClickListener(v -> {
                selectedCategory = null;
                applyFilter();
            });
        }
        if (chipPureTea != null) {
            chipPureTea.setOnClickListener(v -> {
                selectedCategory = getString(R.string.firebase_category_pure_tea);
                applyFilter();
            });
        }
        if (chipMilkTea != null) {
            chipMilkTea.setOnClickListener(v -> {
                selectedCategory = getString(R.string.firebase_category_milk_tea);
                applyFilter();
            });
        }
        if (chipTeaLatte != null) {
            chipTeaLatte.setOnClickListener(v -> {
                selectedCategory = getString(R.string.firebase_category_tea_latte);
                applyFilter();
            });
        }
        if (chipFruitTea != null) {
            chipFruitTea.setOnClickListener(v -> {
                selectedCategory = getString(R.string.firebase_category_fruit_tea);
                applyFilter();
            });
        }
        applyFilterChipStyles();
    }

    private void loadProductsFromFirebase() {
        repository.getAllProducts(new FirebaseProductRepository.ProductsCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                allProducts.clear();
                if (products != null) {
                    allProducts.addAll(products);
                }
                if (allProducts.isEmpty()) {
                    allProducts.addAll(getFallbackMenuProducts());
                }
                applyFilter();
            }

            @Override
            public void onError(String message) {
                allProducts.clear();
                allProducts.addAll(getFallbackMenuProducts());
                applyFilter();
                Toast.makeText(Menu.this, R.string.category_products_load_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilter() {
        filteredProducts.clear();
        if (selectedCategory == null || selectedCategory.isEmpty()) {
            filteredProducts.addAll(allProducts);
        } else {
            for (Product product : allProducts) {
                String category = product.getCategory();
                if (category != null && category.equalsIgnoreCase(selectedCategory)) {
                    filteredProducts.add(product);
                }
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        if (tvMenuSectionTitle != null) {
            tvMenuSectionTitle.setText(
                    selectedCategory == null || selectedCategory.isEmpty()
                            ? getString(R.string.menu_all_products_title)
                            : selectedCategory.toUpperCase(Locale.getDefault()));
        }
        applyFilterChipStyles();
    }

    private void applyFilterChipStyles() {
        applyChipState(chipAllCategories, selectedCategory == null || selectedCategory.isEmpty());
        applyChipState(chipPureTea, equalsCategory(selectedCategory, R.string.firebase_category_pure_tea));
        applyChipState(chipMilkTea, equalsCategory(selectedCategory, R.string.firebase_category_milk_tea));
        applyChipState(chipTeaLatte, equalsCategory(selectedCategory, R.string.firebase_category_tea_latte));
        applyChipState(chipFruitTea, equalsCategory(selectedCategory, R.string.firebase_category_fruit_tea));
    }

    private boolean equalsCategory(String value, int stringRes) {
        return value != null && value.equalsIgnoreCase(getString(stringRes));
    }

    private void applyChipState(TextView chip, boolean selected) {
        if (chip == null) {
            return;
        }
        chip.setBackgroundResource(selected ? R.drawable.nav_item_background : R.drawable.bg_size_tag);
        chip.setTextColor(ContextCompat.getColor(this, selected ? R.color.white : R.color.brand_blue));
    }

    private List<Product> getFallbackMenuProducts() {
        List<Product> products = new ArrayList<>();
        products.add(new Product("Trà Ô Long Mộc Hương", R.mipmap.traolongmochuong, 4.8f, "500", "19.000", "22.000", "16.000", "19.000"));
        products.add(new Product("Hồng Trà Bí Đao", R.mipmap.hongtrabidao, 4.8f, "500", "19.000", "22.000", "16.000", "19.000"));
        products.add(new Product("Trà Xanh Bí Đao", R.mipmap.traxanhbidao, 4.8f, "500", "19.000", "22.000", "16.000", "19.000"));
        products.add(new Product("Trà Xanh Hoa Nhài", R.mipmap.traxanhhoanhai, 4.8f, "500", "19.000", "22.000", "16.000", "19.000"));
        products.add(new Product("Sữa Tươi Khoai Môn", R.mipmap.suatuoikhoaimonnghien, 4.8f, "500", "25.000", "29.000", "22.000", "26.000"));
        products.add(new Product("Ô Long Latte", R.mipmap.olonglatte, 4.8f, "500", "24.000", "28.000", "21.000", "25.000"));
        return products;
    }

    private void setupBottomNav() {
        View navHome = findViewById(R.id.nav_home);
        View navMenu = findViewById(R.id.nav_menu);
        View navOrders = findViewById(R.id.nav_orders);
        View navPromotion = findViewById(R.id.nav_promotion);
        View navProfile = findViewById(R.id.nav_profile);

        if (navMenu != null) navMenu.setSelected(true);
        if (navHome != null) navHome.setOnClickListener(v -> startActivity(new Intent(this, Homepage.class)));
        if (navMenu != null) navMenu.setOnClickListener(v -> {
            selectedCategory = null;
            applyFilter();
        });
        if (navOrders != null) navOrders.setOnClickListener(v -> startActivity(new Intent(this, OrderHistory.class)));
        if (navPromotion != null) navPromotion.setOnClickListener(v -> startActivity(new Intent(this, BlogGeneral.class)));
        if (navProfile != null) navProfile.setOnClickListener(v -> startActivity(new Intent(this, UserProfile.class)));
    }
}