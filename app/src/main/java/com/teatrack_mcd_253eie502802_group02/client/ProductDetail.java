package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.teatrack_mcd_253eie502802_group02.MainActivity;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.adapter.ProductCardAdapter;
import com.teatrack_mcd_253eie502802_group02.data.FirebaseProductRepository;
import com.teatrack_mcd_253eie502802_group02.model.Product;
import com.teatrack_mcd_253eie502802_group02.util.ProductImageHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProductDetail extends AppCompatActivity {

    private int quantity = 1;
    private ImageView imgDetail;
    private TextView tvDetailName;
    private TextView tvTopTitle;
    private TextView tvCategory;
    private TextView tvRatingValue;
    private TextView tvPriceM;
    private TextView tvPriceL;
    private TextView tvVipPriceM;
    private TextView tvVipPriceL;
    private TextView tvRatingSummary;
    private TextView tvDescription;
    private TextView breadcrumbTeaLatte;
    private MaterialButton btnSizeOptionM;
    private MaterialButton btnSizeOptionL;
    private MaterialButton btnSweetnessNone;
    private MaterialButton btnSweetnessLess;
    private MaterialButton btnSweetnessNormal;
    private MaterialButton btnSweetnessHigh;
    private MaterialButton btnIceLess;
    private MaterialButton btnIceNormal;
    private MaterialButton btnIceHigh;
    private LinearLayout customizationContent;
    private TextView tvNoteChevron;
    private LinearLayout toppingRowsContainer;
    private boolean isCustomizationExpanded = true;
    private final Map<String, Integer> toppingQuantities = new LinkedHashMap<>();
    private final FirebaseProductRepository repository = new FirebaseProductRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        ImageView btnBack = findViewById(R.id.btnBack);
        imgDetail = findViewById(R.id.imgDetail);
        tvDetailName = findViewById(R.id.tvDetailName);
        tvTopTitle = findViewById(R.id.tvTopTitle);
        tvCategory = findViewById(R.id.tvCategory);
        tvRatingValue = findViewById(R.id.tvRatingValue);
        tvPriceM = findViewById(R.id.tvPriceM);
        tvPriceL = findViewById(R.id.tvPriceL);
        tvVipPriceM = findViewById(R.id.tvVipPriceM);
        tvVipPriceL = findViewById(R.id.tvVipPriceL);
        tvRatingSummary = findViewById(R.id.tvRatingSummary);
        tvDescription = findViewById(R.id.tvDescription);
        btnSizeOptionM = findViewById(R.id.btnSizeOptionM);
        btnSizeOptionL = findViewById(R.id.btnSizeOptionL);
        btnSweetnessNone = findViewById(R.id.btnSweetnessNone);
        btnSweetnessLess = findViewById(R.id.btnSweetnessLess);
        btnSweetnessNormal = findViewById(R.id.btnSweetnessNormal);
        btnSweetnessHigh = findViewById(R.id.btnSweetnessHigh);
        btnIceLess = findViewById(R.id.btnIceLess);
        btnIceNormal = findViewById(R.id.btnIceNormal);
        btnIceHigh = findViewById(R.id.btnIceHigh);
        TextView tvQuantity = findViewById(R.id.tvQuantity);
        ImageButton btnQtyMinus = findViewById(R.id.btnQtyMinus);
        ImageButton btnQtyPlus = findViewById(R.id.btnQtyPlus);
        RecyclerView rvRecommended = findViewById(R.id.rvRecommended);
        TextView btnAddToCart = findViewById(R.id.btnAddToCart);
        TextView btnBuyNow = findViewById(R.id.btnBuyNow);
        TextView breadcrumbHome = findViewById(R.id.breadcrumbHome);
        TextView breadcrumbMenu = findViewById(R.id.breadcrumbMenu);
        breadcrumbTeaLatte = findViewById(R.id.breadcrumbTeaLatte);
        LinearLayout noteToggle = findViewById(R.id.noteToggle);
        customizationContent = findViewById(R.id.customizationContent);
        tvNoteChevron = findViewById(R.id.tvNoteChevron);
        toppingRowsContainer = findViewById(R.id.toppingRowsContainer);

        btnBack.setOnClickListener(v -> backToMenu());
        breadcrumbHome.setOnClickListener(v -> openMainTab(MainActivity.TAB_HOME, null));
        breadcrumbMenu.setOnClickListener(v -> openMainTab(MainActivity.TAB_MENU, null));
        breadcrumbTeaLatte.setOnClickListener(v ->
                openMainTab(MainActivity.TAB_MENU, getString(R.string.firebase_category_tea_latte)));
        noteToggle.setOnClickListener(v -> toggleCustomization());

        String name = getIntent().getStringExtra("name");
        String priceM = getIntent().getStringExtra("priceM");
        String priceL = getIntent().getStringExtra("priceL");
        String vipM = getIntent().getStringExtra("vipM");
        String vipL = getIntent().getStringExtra("vipL");
        int imageRes = getIntent().getIntExtra("imageRes", R.mipmap.traolongmochuong);
        float rating = getIntent().getFloatExtra("rating", 4.9f);
        String reviewCount = getIntent().getStringExtra("reviewCount");
        String category = getIntent().getStringExtra("category");

        bindFromIntent(name, category, priceM, priceL, vipM, vipL, imageRes, rating, reviewCount);
        fetchFromFirebase(name);

        setupThumbs(imageRes);

        btnQtyMinus.setOnClickListener(v -> {
            quantity = Math.max(1, quantity - 1);
            tvQuantity.setText(String.valueOf(quantity));
        });
        btnQtyPlus.setOnClickListener(v -> {
            quantity += 1;
            tvQuantity.setText(String.valueOf(quantity));
        });

        btnAddToCart.setOnClickListener(v ->
                Toast.makeText(this, R.string.product_detail_added_to_cart, Toast.LENGTH_SHORT).show());
        btnBuyNow.setOnClickListener(v ->
                Toast.makeText(this, R.string.product_detail_buy_now_message, Toast.LENGTH_SHORT).show());

        rvRecommended.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvRecommended.setAdapter(new ProductCardAdapter(getRecommendedProducts(), this::openRelatedDetail));

        setupBottomNavigation();
        setupOptionSelections();
        setupToppingRows();
        applyCustomizationState(false);
    }

    private void bindFromIntent(String name, String category, String priceM, String priceL, String vipM, String vipL,
                                int imageRes, float rating, String reviewCount) {
        String resolvedName = name != null ? name : getString(R.string.product_detail_title);
        tvDetailName.setText(resolvedName);
        if (tvTopTitle != null) {
            tvTopTitle.setText(resolvedName);
        }
        tvCategory.setText(category != null ? category : getString(R.string.product_detail_category_default));
        if (breadcrumbTeaLatte != null && category != null && !category.isEmpty()) {
            breadcrumbTeaLatte.setText(category);
        }
        if (tvRatingValue != null) {
            tvRatingValue.setText(String.format(java.util.Locale.getDefault(), "%.1f", rating));
        }
        tvPriceM.setText(safePrice(priceM));
        tvPriceL.setText(safePrice(priceL));
        tvVipPriceM.setText(safePrice(vipM));
        tvVipPriceL.setText(safePrice(vipL));
        bindCustomizationPrice(priceM, priceL);
        tvRatingSummary.setText(getString(
                R.string.product_detail_views_format,
                reviewCount == null ? "1k" : reviewCount));
        tvDescription.setText(getString(R.string.product_detail_description_default));
        imgDetail.setImageResource(imageRes);
    }

    private void fetchFromFirebase(String name) {
        if (name == null || name.isEmpty()) {
            return;
        }
        repository.getProductByName(name, new FirebaseProductRepository.ProductCallback() {
            @Override
            public void onSuccess(Product product) {
                if (isFinishing()) {
                    return;
                }
                bindFromProduct(product);
            }

            @Override
            public void onError(String message) {
                // fallback to intent data
            }
        });
    }

    private void bindFromProduct(Product product) {
        tvDetailName.setText(product.getName());
        if (tvTopTitle != null) {
            tvTopTitle.setText(product.getName());
        }
        if (product.getCategory() != null && !product.getCategory().isEmpty()) {
            tvCategory.setText(product.getCategory());
            if (breadcrumbTeaLatte != null) {
                breadcrumbTeaLatte.setText(product.getCategory());
            }
        }
        tvPriceM.setText(safePrice(product.getPriceM()));
        tvPriceL.setText(safePrice(product.getPriceL()));
        tvVipPriceM.setText(safePrice(product.getVipPriceM()));
        tvVipPriceL.setText(safePrice(product.getVipPriceL()));
        bindCustomizationPrice(product.getPriceM(), product.getPriceL());
        if (tvRatingValue != null) {
            tvRatingValue.setText(String.format(java.util.Locale.getDefault(), "%.1f", product.getRating()));
        }
        tvRatingSummary.setText(getString(
                R.string.product_detail_views_format,
                product.getReviewCount()));
        ProductImageHelper.load(imgDetail, product);
    }

    private void setupThumbs(int primaryImage) {
        ImageView thumb1 = findViewById(R.id.thumb1);
        ImageView thumb2 = findViewById(R.id.thumb2);
        ImageView thumb3 = findViewById(R.id.thumb3);
        ImageView thumb4 = findViewById(R.id.thumb4);

        thumb1.setImageResource(primaryImage);
        thumb4.setImageResource(primaryImage);
        thumb1.setOnClickListener(v -> imgDetail.setImageDrawable(thumb1.getDrawable()));
        thumb2.setOnClickListener(v -> imgDetail.setImageDrawable(thumb2.getDrawable()));
        thumb3.setOnClickListener(v -> imgDetail.setImageDrawable(thumb3.getDrawable()));
        thumb4.setOnClickListener(v -> imgDetail.setImageDrawable(thumb4.getDrawable()));
    }

    private String safePrice(String price) {
        if (price == null || price.isEmpty()) {
            return "0đ";
        }
        return price.endsWith("đ") ? price : price + "đ";
    }

    private void bindCustomizationPrice(String priceM, String priceL) {
        if (btnSizeOptionM != null) {
            btnSizeOptionM.setText(getString(R.string.product_detail_size_m_price_format, safePrice(priceM)));
        }
        if (btnSizeOptionL != null) {
            btnSizeOptionL.setText(getString(R.string.product_detail_size_l_price_format, safePrice(priceL)));
        }
    }

    private void setupOptionSelections() {
        setupSizeGroup(new MaterialButton[]{btnSizeOptionM, btnSizeOptionL}, 0);
        setupLevelGroup(new MaterialButton[]{btnSweetnessNone, btnSweetnessLess, btnSweetnessNormal, btnSweetnessHigh}, 1);
        setupLevelGroup(new MaterialButton[]{btnIceLess, btnIceNormal, btnIceHigh}, 1);
    }

    private void setupSizeGroup(MaterialButton[] buttons, int defaultSelected) {
        for (int i = 0; i < buttons.length; i++) {
            MaterialButton button = buttons[i];
            if (button == null) {
                continue;
            }
            final int selectedIndex = i;
            applySizeButtonState(button, i == defaultSelected);
            button.setOnClickListener(v -> {
                for (int j = 0; j < buttons.length; j++) {
                    MaterialButton item = buttons[j];
                    if (item != null) {
                        applySizeButtonState(item, j == selectedIndex);
                    }
                }
            });
        }
    }

    private void setupLevelGroup(MaterialButton[] buttons, int defaultSelected) {
        for (int i = 0; i < buttons.length; i++) {
            MaterialButton button = buttons[i];
            if (button == null) {
                continue;
            }
            final int selectedIndex = i;
            applyLevelButtonState(button, i == defaultSelected);
            button.setOnClickListener(v -> {
                for (int j = 0; j < buttons.length; j++) {
                    MaterialButton item = buttons[j];
                    if (item != null) {
                        applyLevelButtonState(item, j == selectedIndex);
                    }
                }
            });
        }
    }

    private void applySizeButtonState(MaterialButton button, boolean isSelected) {
        int bgColor = Color.parseColor(isSelected ? "#C7D8F4" : "#FFFFFF");
        int textColor = ContextCompat.getColor(this, isSelected ? R.color.brand_blue : R.color.nav_inactive);
        int strokeColor = ContextCompat.getColor(this, isSelected ? R.color.brand_blue : R.color.outline_variant);
        button.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        button.setTextColor(textColor);
        button.setStrokeColor(ColorStateList.valueOf(strokeColor));
        button.setStrokeWidth(isSelected ? dp(2) : dp(1));
        button.setCornerRadius(dp(20));
    }

    private void applyLevelButtonState(MaterialButton button, boolean isSelected) {
        int bgColor = Color.parseColor(isSelected ? "#C7D8F4" : "#D8E0F5");
        int textColor = ContextCompat.getColor(this, isSelected ? R.color.brand_blue : R.color.nav_inactive);
        int strokeColor = Color.parseColor(isSelected ? "#0088FF" : "#D8E0F5");
        button.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        button.setTextColor(textColor);
        button.setStrokeColor(ColorStateList.valueOf(strokeColor));
        button.setStrokeWidth(isSelected ? dp(2) : dp(1));
        button.setCornerRadius(dp(20));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure topping rows are always present after config changes/returning to screen.
        setupToppingRows();
    }

    private List<Product> getRecommendedProducts() {
        List<Product> products = new ArrayList<>();
        products.add(new Product("Trà Xanh Sữa", R.mipmap.traxanhsua, 4.8f, "500", "24.000", "28.000", "21.000", "25.000"));
        products.add(new Product("Đường Đen", R.mipmap.trasuatranchauduongden, 5.0f, "500", "29.000", "31.000", "26.000", "28.000"));
        products.add(new Product("Trà Ô Long", R.mipmap.traolongmochuong, 4.8f, "500", "19.000", "22.000", "16.000", "19.000"));
        return products;
    }

    private void openRelatedDetail(Product product) {
        Intent intent = new Intent(this, ProductDetail.class);
        intent.putExtra("name", product.getName());
        intent.putExtra("category", product.getCategory());
        intent.putExtra("priceM", product.getPriceM());
        intent.putExtra("priceL", product.getPriceL());
        intent.putExtra("vipM", product.getVipPriceM());
        intent.putExtra("vipL", product.getVipPriceL());
        intent.putExtra("imageRes", product.getImageRes());
        intent.putExtra("rating", product.getRating());
        intent.putExtra("reviewCount", product.getReviewCount());
        startActivity(intent);
    }

    private void backToMenu() {
        openMainTab(MainActivity.TAB_MENU, null);
        finish();
    }

    private void openMainTab(String tab, String category) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_SELECTED_TAB, tab);
        if (category != null) {
            intent.putExtra(MainActivity.EXTRA_MENU_CATEGORY, category);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void setupBottomNavigation() {
        View navHome = findViewById(R.id.nav_home);
        View navMenu = findViewById(R.id.nav_menu);
        View navOrders = findViewById(R.id.nav_orders);
        View navPromotion = findViewById(R.id.nav_promotion);
        View navProfile = findViewById(R.id.nav_profile);

        if (navMenu != null) {
            navMenu.setSelected(true);
        }
        if (navHome != null) {
            navHome.setOnClickListener(v -> openMainTab(MainActivity.TAB_HOME, null));
        }
        if (navMenu != null) {
            navMenu.setOnClickListener(v -> openMainTab(MainActivity.TAB_MENU, null));
        }
        if (navOrders != null) {
            navOrders.setOnClickListener(v -> startActivity(new Intent(this, OrderHistory.class)));
        }
        if (navPromotion != null) {
            navPromotion.setOnClickListener(v -> startActivity(new Intent(this, BlogGeneral.class)));
        }
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> startActivity(new Intent(this, UserProfile.class)));
        }
    }

    private void toggleCustomization() {
        isCustomizationExpanded = !isCustomizationExpanded;
        applyCustomizationState(true);
    }

    private void applyCustomizationState(boolean animateChevron) {
        if (customizationContent != null) {
            customizationContent.setVisibility(isCustomizationExpanded ? View.VISIBLE : View.GONE);
        }
        if (tvNoteChevron != null) {
            tvNoteChevron.setText(isCustomizationExpanded ? "⌄" : "⌃");
        }
    }

    private void setupToppingRows() {
        if (toppingRowsContainer == null) {
            return;
        }
        toppingRowsContainer.removeAllViews();
        String[][] toppings = {
                {"Sương sáo", "3.000đ"},
                {"Thạch dừa nguyên vị", "3.000đ"},
                {"Hạt é", "3.000đ"},
                {"Thạch dứa hương đào", "5.000đ"},
                {"Thạch aiyu", "5.000đ"},
                {"Thạch sợi lá dứa", "5.000đ"},
                {"Thạch sương sáo viên (8)", "5.000đ"},
                {"Trân châu hoàng kim", "5.000đ"},
                {"Trân châu đường đen", "5.000đ"},
                {"Trân châu 3Q trắng/đen", "5.000đ"},
                {"Trân châu khoai môn", "5.000đ"},
                {"Hạt thủy tinh củ năng", "5.000đ"},
                {"Hạt thủy tinh lúa mạch", "5.000đ"},
                {"Đào miếng", "5.000đ"},
                {"Khoai môn nghiền", "5.000đ"},
                {"Hạt sen", "7.000đ"},
                {"Kem tươi vani", "7.000đ"},
                {"Kem cheese", "7.000đ"},
                {"Pudding trứng", "7.000đ"},
                {"Thạch sữa viên (8)", "7.000đ"}
        };
        for (int i = 0; i < toppings.length; i++) {
            addToppingRow(toppings[i][0], toppings[i][1], i > 0);
        }
    }

    private void addToppingRow(String name, String price, boolean addTopMargin) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.TOP);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (addTopMargin) {
            rowParams.topMargin = dp(12);
        }
        row.setLayoutParams(rowParams);

        LinearLayout infoColumn = new LinearLayout(this);
        infoColumn.setOrientation(LinearLayout.VERTICAL);
        infoColumn.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView nameView = new TextView(this);
        nameView.setText(name);
        nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        nameView.setTypeface(Typeface.DEFAULT_BOLD);
        nameView.setTextColor(ContextCompat.getColor(this, R.color.on_surface));

        TextView priceView = new TextView(this);
        priceView.setText("+ " + price);
        priceView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        priceView.setTypeface(Typeface.DEFAULT_BOLD);
        priceView.setTextColor(ContextCompat.getColor(this, R.color.brand_blue));

        infoColumn.addView(nameView);
        infoColumn.addView(priceView);

        com.google.android.material.card.MaterialCardView stepperCard =
                new com.google.android.material.card.MaterialCardView(this);
        LinearLayout.LayoutParams stepperParams = new LinearLayout.LayoutParams(dp(150), dp(48));
        stepperCard.setLayoutParams(stepperParams);
        stepperCard.setCardElevation(0f);
        stepperCard.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
        stepperCard.setStrokeColor(ContextCompat.getColor(this, R.color.outline_variant));
        stepperCard.setStrokeWidth(dp(1));

        LinearLayout stepperContent = new LinearLayout(this);
        stepperContent.setOrientation(LinearLayout.HORIZONTAL);
        stepperContent.setGravity(Gravity.CENTER_VERTICAL);
        stepperContent.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        stepperContent.setPadding(dp(8), 0, dp(8), 0);

        ImageButton minusButton = new ImageButton(this);
        minusButton.setLayoutParams(new LinearLayout.LayoutParams(dp(32), dp(32)));
        minusButton.setBackgroundResource(R.drawable.bg_arrow_circle);
        minusButton.setImageResource(R.drawable.ic_minus_line);
        minusButton.setColorFilter(ContextCompat.getColor(this, R.color.nav_inactive));

        TextView quantityView = new TextView(this);
        LinearLayout.LayoutParams qtyParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        quantityView.setLayoutParams(qtyParams);
        quantityView.setGravity(Gravity.CENTER);
        quantityView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        quantityView.setTypeface(Typeface.DEFAULT_BOLD);
        quantityView.setTextColor(ContextCompat.getColor(this, R.color.nav_inactive));
        quantityView.setText("0");

        ImageButton plusButton = new ImageButton(this);
        plusButton.setLayoutParams(new LinearLayout.LayoutParams(dp(32), dp(32)));
        plusButton.setBackgroundResource(R.drawable.bg_circle_brand_blue);
        plusButton.setImageResource(R.drawable.ic_plus_line);
        plusButton.setColorFilter(ContextCompat.getColor(this, R.color.white));

        toppingQuantities.put(name, 0);
        minusButton.setOnClickListener(v -> {
            int current = toppingQuantities.get(name);
            int next = Math.max(0, current - 1);
            toppingQuantities.put(name, next);
            quantityView.setText(String.valueOf(next));
        });
        plusButton.setOnClickListener(v -> {
            int current = toppingQuantities.get(name);
            int next = current + 1;
            toppingQuantities.put(name, next);
            quantityView.setText(String.valueOf(next));
        });

        stepperContent.addView(minusButton);
        stepperContent.addView(quantityView);
        stepperContent.addView(plusButton);
        stepperCard.addView(stepperContent);

        row.addView(infoColumn);
        row.addView(stepperCard);
        toppingRowsContainer.addView(row);
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()));
    }

    @Override
    public void onBackPressed() {
        backToMenu();
    }
}