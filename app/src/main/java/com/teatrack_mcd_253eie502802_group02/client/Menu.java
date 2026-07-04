package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
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
import com.teatrack_mcd_253eie502802_group02.adapter.MenuProductAdapter;
import com.teatrack_mcd_253eie502802_group02.data.FirebaseProductRepository;
import com.teatrack_mcd_253eie502802_group02.model.Product;
import com.teatrack_mcd_253eie502802_group02.shared.ui.CartBadgeHelper;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;
import com.teatrack_mcd_253eie502802_group02.util.CartActions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;

public class Menu extends BaseActivity {
    private static final String CANONICAL_NEW_ARRIVALS = "New Arrivals";
    private static final String CANONICAL_BEST_SELLERS = "Best Sellers";

    private static final int[] NAV_IDS = {
            R.id.nav_home,
            R.id.nav_menu,
            R.id.nav_orders,
            R.id.nav_promotion,
            R.id.nav_profile
    };

    private final FirebaseProductRepository repository = new FirebaseProductRepository();
    private final List<Product> allProducts = new ArrayList<>();
    private final List<Product> filteredProducts = new ArrayList<>();
    private MenuProductAdapter adapter;

    private TextView tvMenuSectionTitle;
    private TextView tvMenuSectionSubtitle;
    private TextView tvMenuResultCount;
    private EditText edtMenuSearch;
    private ImageButton btnMenuFilter;

    private String selectedCategory;
    private String searchQuery = "";
    private String selectedSort = "featured";
    private PopupWindow filterPopupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        selectedCategory = getIntent() != null
                ? getIntent().getStringExtra(MainActivity.EXTRA_MENU_CATEGORY)
                : null;

        bindViews();
        setupProducts();
        setupSearch();
        setupFilterPopupTrigger();
        loadInitialProducts();
        loadProductsFromFirebase();
        setupBottomNav();
        CartBadgeHelper.setup(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        selectedCategory = intent != null
                ? intent.getStringExtra(MainActivity.EXTRA_MENU_CATEGORY)
                : null;
        loadInitialProducts();
        loadProductsFromFirebase();
    }

    private void loadInitialProducts() {
        allProducts.clear();
        allProducts.addAll(getInitialMenuProducts());
        applyFilter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CartBadgeHelper.updateBadge(this);
    }

    private void bindViews() {
        tvMenuSectionTitle = findViewById(R.id.tvMenuSectionTitle);
        tvMenuSectionSubtitle = findViewById(R.id.tvMenuSectionSubtitle);
        tvMenuResultCount = findViewById(R.id.tvMenuResultCount);
        edtMenuSearch = findViewById(R.id.edtMenuSearch);
        btnMenuFilter = findViewById(R.id.btnMenuFilter);
        View logo = findViewById(R.id.img_logo);
        if (logo != null) {
            logo.setOnClickListener(v -> startActivity(new Intent(this, Homepage.class)));
        }
        if (edtMenuSearch != null) {
            int brandBlue = ContextCompat.getColor(this, R.color.brand_blue);
            edtMenuSearch.setTextColor(brandBlue);
            edtMenuSearch.setHintTextColor(brandBlue);
        }
    }

    private void setupProducts() {
        RecyclerView rvMenuProducts = findViewById(R.id.rvMenuProducts);
        rvMenuProducts.setLayoutManager(new GridLayoutManager(this, 1));
        rvMenuProducts.setHasFixedSize(true);
        adapter = new MenuProductAdapter(this::openProductDetail, this::onAddToCart);
        rvMenuProducts.setAdapter(adapter);
    }

    private void setupSearch() {
        if (edtMenuSearch == null) {
            return;
        }
        edtMenuSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s == null ? "" : s.toString().trim();
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupFilterPopupTrigger() {
        if (btnMenuFilter != null) {
            btnMenuFilter.setOnClickListener(v -> showFilterPopup());
        }
    }

    private void showFilterPopup() {
        if (btnMenuFilter == null) {
            return;
        }
        if (filterPopupWindow != null && filterPopupWindow.isShowing()) {
            filterPopupWindow.dismiss();
            return;
        }

        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_menu_filter, null, false);
        filterPopupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        filterPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        filterPopupWindow.setOutsideTouchable(true);
        filterPopupWindow.setElevation(16f);

        bindPopupOptions(popupView);
        updatePopupSelectionState(popupView);

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int xOff = btnMenuFilter.getWidth() - popupView.getMeasuredWidth();
        filterPopupWindow.showAsDropDown(btnMenuFilter, xOff, 10);
    }

    private void bindPopupOptions(View popupView) {
        Map<Integer, String> categoryOptions = new LinkedHashMap<>();
        categoryOptions.put(R.id.optCategoryAll, "");
        categoryOptions.put(R.id.optCategoryPureTea, getString(R.string.firebase_category_pure_tea));
        categoryOptions.put(R.id.optCategoryMilkTea, getString(R.string.firebase_category_milk_tea));
        categoryOptions.put(R.id.optCategoryTeaLatte, getString(R.string.firebase_category_tea_latte));
        categoryOptions.put(R.id.optCategoryFruitTea, getString(R.string.firebase_category_fruit_tea));
        categoryOptions.put(R.id.optCategoryNewArrivals, getString(R.string.firebase_category_new_arrivals));
        categoryOptions.put(R.id.optCategoryBestSellers, getString(R.string.firebase_category_best_sellers));
        for (Map.Entry<Integer, String> entry : categoryOptions.entrySet()) {
            TextView option = popupView.findViewById(entry.getKey());
            if (option != null) {
                option.setOnClickListener(v -> {
                    String nextCategory = entry.getValue().isEmpty() ? null : entry.getValue();
                    if ((selectedCategory == null && nextCategory != null)
                            || (selectedCategory != null && !selectedCategory.equalsIgnoreCase(nextCategory == null ? "" : nextCategory))) {
                        selectedCategory = nextCategory;
                        applyFilter();
                    }
                    if (filterPopupWindow != null) {
                        filterPopupWindow.dismiss();
                    }
                });
            }
        }

        Map<Integer, String> sortOptions = new LinkedHashMap<>();
        sortOptions.put(R.id.optSortFeatured, "featured");
        sortOptions.put(R.id.optSortNameAsc, "name_asc");
        sortOptions.put(R.id.optSortNameDesc, "name_desc");
        sortOptions.put(R.id.optSortPriceAsc, "price_asc");
        sortOptions.put(R.id.optSortPriceDesc, "price_desc");
        for (Map.Entry<Integer, String> entry : sortOptions.entrySet()) {
            TextView option = popupView.findViewById(entry.getKey());
            if (option != null) {
                option.setOnClickListener(v -> {
                    if (!selectedSort.equals(entry.getValue())) {
                        selectedSort = entry.getValue();
                        applyFilter();
                    }
                    if (filterPopupWindow != null) {
                        filterPopupWindow.dismiss();
                    }
                });
            }
        }
    }

    private void updatePopupSelectionState(View popupView) {
        applyPopupOptionState(popupView.findViewById(R.id.optCategoryAll), selectedCategory == null || selectedCategory.isEmpty());
        applyPopupOptionState(popupView.findViewById(R.id.optCategoryPureTea),
                equalsCategory(selectedCategory, R.string.firebase_category_pure_tea));
        applyPopupOptionState(popupView.findViewById(R.id.optCategoryMilkTea),
                equalsCategory(selectedCategory, R.string.firebase_category_milk_tea));
        applyPopupOptionState(popupView.findViewById(R.id.optCategoryTeaLatte),
                equalsCategory(selectedCategory, R.string.firebase_category_tea_latte));
        applyPopupOptionState(popupView.findViewById(R.id.optCategoryFruitTea),
                equalsCategory(selectedCategory, R.string.firebase_category_fruit_tea));
        applyPopupOptionState(popupView.findViewById(R.id.optCategoryNewArrivals),
                equalsCategory(selectedCategory, R.string.firebase_category_new_arrivals));
        applyPopupOptionState(popupView.findViewById(R.id.optCategoryBestSellers),
                equalsCategory(selectedCategory, R.string.firebase_category_best_sellers));

        applyPopupOptionState(popupView.findViewById(R.id.optSortFeatured), "featured".equals(selectedSort));
        applyPopupOptionState(popupView.findViewById(R.id.optSortNameAsc), "name_asc".equals(selectedSort));
        applyPopupOptionState(popupView.findViewById(R.id.optSortNameDesc), "name_desc".equals(selectedSort));
        applyPopupOptionState(popupView.findViewById(R.id.optSortPriceAsc), "price_asc".equals(selectedSort));
        applyPopupOptionState(popupView.findViewById(R.id.optSortPriceDesc), "price_desc".equals(selectedSort));
    }

    private void applyPopupOptionState(TextView option, boolean selected) {
        if (option == null) {
            return;
        }
        option.setBackgroundResource(selected ? R.drawable.bg_filter_option_selected : android.R.color.transparent);
        option.setTextColor(ContextCompat.getColor(this, selected ? R.color.brand_blue : R.color.black));
        option.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
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
        for (Product product : allProducts) {
            boolean categoryMatch = selectedCategory == null
                    || selectedCategory.isEmpty()
                    || matchesCategory(product, selectedCategory);
            boolean searchMatch = searchQuery.isEmpty() || matchesSearch(product, searchQuery);
            if (categoryMatch && searchMatch) {
                filteredProducts.add(product);
            }
        }

        sortProducts(filteredProducts);
        if (adapter != null) {
            adapter.submitList(new ArrayList<>(filteredProducts));
        }
        if (tvMenuSectionTitle != null) {
            String title = (selectedCategory == null || selectedCategory.isEmpty())
                    ? getString(R.string.menu_all_products_title)
                    : selectedCategory;
            tvMenuSectionTitle.setText(title.toUpperCase(Locale.getDefault()));
        }
        if (tvMenuSectionSubtitle != null) {
            if (!searchQuery.isEmpty()) {
                tvMenuSectionSubtitle.setText(getString(R.string.menu_section_subtitle_search, searchQuery));
            } else {
                tvMenuSectionSubtitle.setText(resolveCategorySubtitle());
            }
        }
        if (tvMenuResultCount != null) {
            tvMenuResultCount.setText(getString(R.string.menu_result_count_format, filteredProducts.size()));
        }
    }

    private boolean matchesSearch(Product product, String query) {
        String normalizedQuery = safeString(query).toLowerCase(Locale.getDefault());
        String name = safeString(product.getName()).toLowerCase(Locale.getDefault());
        String category = safeString(product.getCategory()).toLowerCase(Locale.getDefault());
        return name.contains(normalizedQuery) || category.contains(normalizedQuery);
    }

    private void sortProducts(List<Product> list) {
        switch (selectedSort) {
            case "name_asc":
                list.sort((a, b) -> safeString(a.getName()).compareToIgnoreCase(safeString(b.getName())));
                break;
            case "name_desc":
                list.sort((a, b) -> safeString(b.getName()).compareToIgnoreCase(safeString(a.getName())));
                break;
            case "price_asc":
                list.sort((a, b) -> Integer.compare(a.getPriceM(), b.getPriceM()));
                break;
            case "price_desc":
                list.sort((a, b) -> Integer.compare(b.getPriceM(), a.getPriceM()));
                break;
            default:
                break;
        }
    }

    private boolean equalsCategory(String value, int stringRes) {
        return value != null && value.equalsIgnoreCase(getString(stringRes));
    }

    private String resolveCategorySubtitle() {
        if (selectedCategory == null || selectedCategory.isEmpty()) {
            return getString(R.string.menu_section_subtitle_default);
        }
        if (equalsCategory(selectedCategory, R.string.firebase_category_pure_tea)) {
            return getString(R.string.menu_section_subtitle_pure_tea);
        }
        if (equalsCategory(selectedCategory, R.string.firebase_category_milk_tea)) {
            return getString(R.string.menu_section_subtitle_milk_tea);
        }
        if (equalsCategory(selectedCategory, R.string.firebase_category_tea_latte)) {
            return getString(R.string.menu_section_subtitle_tea_latte);
        }
        if (equalsCategory(selectedCategory, R.string.firebase_category_fruit_tea)) {
            return getString(R.string.menu_section_subtitle_fruit_tea);
        }
        if (equalsCategory(selectedCategory, R.string.firebase_category_new_arrivals)) {
            return getString(R.string.menu_section_subtitle_new_arrivals);
        }
        if (equalsCategory(selectedCategory, R.string.firebase_category_best_sellers)) {
            return getString(R.string.menu_section_subtitle_best_sellers);
        }
        return getString(R.string.menu_section_subtitle_default);
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private long parsePrice(String value) {
        if (value == null) {
            return 0L;
        }
        String cleaned = value.replace("đ", "").replace(".", "").replace(",", "").trim();
        if (cleaned.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(cleaned);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private boolean matchesCategory(Product product, String filterCategory) {
        String productCategory = safeString(product.getCategory());
        if (productCategory.isEmpty()) {
            return false;
        }
        String canonicalFilter = normalizeCategoryKey(filterCategory);
        String canonicalProduct = normalizeCategoryKey(productCategory);
        return !canonicalFilter.isEmpty()
                && canonicalFilter.equalsIgnoreCase(canonicalProduct);
    }

    private String normalizeCategoryKey(String category) {
        if (category == null) {
            return "";
        }
        String trimmed = category.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        if (equalsCategory(trimmed, R.string.firebase_category_new_arrivals)
                || trimmed.equalsIgnoreCase(CANONICAL_NEW_ARRIVALS)
                || trimmed.equalsIgnoreCase("New Drinks")) {
            return CANONICAL_NEW_ARRIVALS;
        }
        if (equalsCategory(trimmed, R.string.firebase_category_best_sellers)
                || trimmed.equalsIgnoreCase(CANONICAL_BEST_SELLERS)
                || trimmed.equalsIgnoreCase("Hot Drinks")) {
            return CANONICAL_BEST_SELLERS;
        }
        return trimmed;
    }

    private List<Product> getInitialMenuProducts() {
        if (selectedCategory != null && !selectedCategory.isEmpty()) {
            List<Product> categoryProducts = resolveCategoryFallback(selectedCategory);
            if (!categoryProducts.isEmpty()) {
                for (Product product : categoryProducts) {
                    product.setCategory(selectedCategory);
                }
                return categoryProducts;
            }
        }
        return getFallbackMenuProducts();
    }

    private List<Product> resolveCategoryFallback(String category) {
        Map<String, List<Product>> map = CategoryProductData.getCategoryProductsMap();
        List<Product> products = map.get(category);
        if (products != null && !products.isEmpty()) {
            return new ArrayList<>(products);
        }
        String normalized = normalizeCategoryKey(category);
        products = map.get(normalized);
        return products != null ? new ArrayList<>(products) : new ArrayList<>();
    }

    private List<Product> getFallbackMenuProducts() {
        List<Product> products = new ArrayList<>();
        products.add(new Product("Trà Ô Long Mộc Hương", R.mipmap.traolongmochuong, 4.8f, "500", 19000, 22000, 16000, 19000));
        products.add(new Product("Hồng Trà Bí Đao", R.mipmap.hongtrabidao, 4.8f, "500", 19000, 22000, 16000, 19000));
        products.add(new Product("Trà Xanh Bí Đao", R.mipmap.traxanhbidao, 4.8f, "500", 19000, 22000, 16000, 19000));
        products.add(new Product("Trà Xanh Hoa Nhài", R.mipmap.traxanhhoanhai, 4.8f, "500", 19000, 22000, 16000, 19000));
        products.add(new Product("Sữa Tươi Khoai Môn", R.mipmap.suatuoikhoaimonnghien, 4.8f, "500", 25000, 29000, 22000, 26000));
        products.add(new Product("Ô Long Latte", R.mipmap.olonglatte, 4.8f, "500", 24000, 28000, 21000, 25000));
        return products;
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(this, ProductDetail.class);
        intent.putExtra("name", product.getName());
        intent.putExtra("category", product.getCategory());
        intent.putExtra("priceM", String.valueOf(product.getPriceM()));
        intent.putExtra("priceL", String.valueOf(product.getPriceL()));
        intent.putExtra("vipM", String.valueOf(product.getVipPriceM()));
        intent.putExtra("vipL", String.valueOf(product.getVipPriceL()));
        intent.putExtra("imageRes", product.getImageRes());
        intent.putExtra("rating", product.getRating());
        intent.putExtra("reviewCount", product.getReviewCount());
        startActivity(intent);
    }

    private void onAddToCart(Product product) {
        CartActions.addDefaultProduct(this, product);
    }

    private void setupBottomNav() {
        NavBarHelper.setupNavBar(this, NAV_IDS, R.id.nav_menu, v -> {
            int id = v.getId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(this, Homepage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            } else if (id == R.id.nav_menu) {
                selectedCategory = null;
                applyFilter();
            } else if (id == R.id.nav_orders) {
                Intent intent = new Intent(this, OrderHistory.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            } else if (id == R.id.nav_promotion) {
                Intent intent = new Intent(this, PromotionClient.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, UserProfile.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Homepage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        if (filterPopupWindow != null && filterPopupWindow.isShowing()) {
            filterPopupWindow.dismiss();
        }
        super.onDestroy();
    }
}