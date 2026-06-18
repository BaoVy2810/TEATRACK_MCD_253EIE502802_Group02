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
import android.text.Html;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.teatrack_mcd_253eie502802_group02.MainActivity;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.adapter.ProductCardAdapter;
import com.teatrack_mcd_253eie502802_group02.data.FirebaseProductRepository;
import com.teatrack_mcd_253eie502802_group02.model.Product;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;
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
    private TextView tabDescription;
    private TextView tabReview;
    private TextView tabCommitment;
    private View tabIndicator;
    private View tabIndicatorContainer;
    private LinearLayout layoutReview;
    private LinearLayout layoutDescription;
    private LinearLayout layoutCommitment;
    private LinearLayout layoutReviewForm;
    private TextView tvTabPlaceholder;
    private TextView tvInfoProductName;
    private TextView tvInfoWeight;
    private TextView tvInfoStorage;
    private TextView tvInfoOrigin;
    private TextView tvInfoTopping;
    private TextView tvCommitment1;
    private TextView tvCommitment2;
    private TextView tvCommitment3;
    private TextView tvCommitment4;
    private TextView tvReviewCount;
    private ImageView imgStar1, imgStar2, imgStar3, imgStar4, imgStar5;
    private int selectedRating = 0;
    private MaterialButton btnWriteReview;
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
        tabDescription = findViewById(R.id.tabDescription);
        tabReview = findViewById(R.id.tabReview);
        tabCommitment = findViewById(R.id.tabCommitment);
        tabIndicator = findViewById(R.id.tabIndicator);
        tabIndicatorContainer = findViewById(R.id.tabIndicatorContainer);
        layoutReview = findViewById(R.id.layoutReview);
        layoutDescription = findViewById(R.id.layoutDescription);
        layoutCommitment = findViewById(R.id.layoutCommitment);
        layoutReviewForm = findViewById(R.id.layoutReviewForm);
        tvTabPlaceholder = findViewById(R.id.tvTabPlaceholder);
        tvInfoProductName = findViewById(R.id.tvInfoProductName);
        tvInfoWeight = findViewById(R.id.tvInfoWeight);
        tvInfoStorage = findViewById(R.id.tvInfoStorage);
        tvInfoOrigin = findViewById(R.id.tvInfoOrigin);
        tvInfoTopping = findViewById(R.id.tvInfoTopping);
        tvCommitment1 = findViewById(R.id.tvCommitment1);
        tvCommitment2 = findViewById(R.id.tvCommitment2);
        tvCommitment3 = findViewById(R.id.tvCommitment3);
        tvCommitment4 = findViewById(R.id.tvCommitment4);
        tvReviewCount = findViewById(R.id.tvReviewCount);
        imgStar1 = findViewById(R.id.imgStar1);
        imgStar2 = findViewById(R.id.imgStar2);
        imgStar3 = findViewById(R.id.imgStar3);
        imgStar4 = findViewById(R.id.imgStar4);
        imgStar5 = findViewById(R.id.imgStar5);
        btnWriteReview = findViewById(R.id.btnWriteReview);
        View logo = findViewById(R.id.img_logo);
        if (logo != null) {
            logo.setOnClickListener(v -> openMainTab(MainActivity.TAB_HOME, null));
        }

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

        Product dummy = new Product();
        dummy.setImageRes(imageRes);
        setupThumbs(dummy);

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

        setupBottomNavigation();
        setupTabs();
        setupOptionSelections();
        setupToppingRows();
        setupDescriptionInfo(name);
        setupCommitmentContent();
        setupReviewSection();
        applyCustomizationState(false);
    }

    private void setupDescriptionInfo(String productName) {
        String resolvedName = productName != null ? productName : getString(R.string.product_detail_title);
        if (tvInfoProductName != null) {
            tvInfoProductName.setText(formatInfoRow(R.string.product_detail_info_name, resolvedName));
        }
        if (tvInfoWeight != null) {
            tvInfoWeight.setText(formatInfoRow(
                    R.string.product_detail_info_weight,
                    getString(R.string.product_detail_info_weight_value)));
        }
        if (tvInfoStorage != null) {
            tvInfoStorage.setText(formatInfoRow(
                    R.string.product_detail_info_storage,
                    getString(R.string.product_detail_info_storage_value)));
        }
        if (tvInfoOrigin != null) {
            tvInfoOrigin.setText(formatInfoRow(
                    R.string.product_detail_info_origin,
                    getString(R.string.product_detail_info_origin_value)));
        }
        if (tvInfoTopping != null) {
            tvInfoTopping.setText(formatInfoRow(
                    R.string.product_detail_info_topping,
                    getString(R.string.product_detail_info_topping_value)));
        }
    }

    private CharSequence formatInfoRow(int labelRes, String value) {
        String label = getString(labelRes);
        if (!label.endsWith(":")) {
            label = label + ":";
        }
        return Html.fromHtml("<b>" + label + "</b> " + value, Html.FROM_HTML_MODE_LEGACY);
    }

    private void setupCommitmentContent() {
        setHtmlText(tvCommitment1, R.string.product_detail_commitment_1);
        setHtmlText(tvCommitment2, R.string.product_detail_commitment_2);
        setHtmlText(tvCommitment3, R.string.product_detail_commitment_3);
        setHtmlText(tvCommitment4, R.string.product_detail_commitment_4);
    }

    private void setHtmlText(TextView textView, int stringRes) {
        if (textView == null) {
            return;
        }
        textView.setText(Html.fromHtml(getString(stringRes), Html.FROM_HTML_MODE_LEGACY));
    }

    private void setupReviewSection() {
        if (tvReviewCount != null) {
            tvReviewCount.setText(getString(R.string.product_detail_review_count_format, "0"));
        }

        View.OnClickListener starClickListener = v -> {
            int rating = 1;
            int id = v.getId();
            if (id == R.id.imgStar1) rating = 1;
            else if (id == R.id.imgStar2) rating = 2;
            else if (id == R.id.imgStar3) rating = 3;
            else if (id == R.id.imgStar4) rating = 4;
            else if (id == R.id.imgStar5) rating = 5;
            updateStarRating(rating);
        };

        if (imgStar1 != null) imgStar1.setOnClickListener(starClickListener);
        if (imgStar2 != null) imgStar2.setOnClickListener(starClickListener);
        if (imgStar3 != null) imgStar3.setOnClickListener(starClickListener);
        if (imgStar4 != null) imgStar4.setOnClickListener(starClickListener);
        if (imgStar5 != null) imgStar5.setOnClickListener(starClickListener);

        updateStarRating(0);

        if (btnWriteReview != null) {
            btnWriteReview.setOnClickListener(v -> {
                if (layoutReviewForm != null) {
                    if (layoutReviewForm.getVisibility() == View.VISIBLE) {
                        layoutReviewForm.setVisibility(View.GONE);
                    } else {
                        layoutReviewForm.setVisibility(View.VISIBLE);
                        updateStarRating(0);
                        EditText reviewTitle = findViewById(R.id.edtReviewTitle);
                        if (reviewTitle != null) {
                            reviewTitle.requestFocus();
                        }
                    }
                }
            });
        }
    }

    private void updateStarRating(int rating) {
        selectedRating = rating;
        ImageView[] stars = {imgStar1, imgStar2, imgStar3, imgStar4, imgStar5};
        int activeColor = ContextCompat.getColor(this, R.color.star_rating_active);
        int inactiveColor = ContextCompat.getColor(this, R.color.outline_variant);

        for (int i = 0; i < stars.length; i++) {
            if (stars[i] != null) {
                int color = i < rating ? activeColor : inactiveColor;
                ImageViewCompat.setImageTintList(stars[i], ColorStateList.valueOf(color));
            }
        }
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
            fetchRecommendedProducts(product.getCategory());
        }
        tvPriceM.setText(safePrice(product.getPriceM()));
        tvPriceL.setText(safePrice(product.getPriceL()));
        tvVipPriceM.setText(safePrice(product.getVipPriceM()));
        tvVipPriceL.setText(safePrice(product.getVipPriceL()));
        bindCustomizationPrice(String.valueOf(product.getPriceM()), String.valueOf(product.getPriceL()));
        if (tvRatingValue != null) {
            tvRatingValue.setText(String.format(java.util.Locale.getDefault(), "%.1f", product.getRating()));
        }
        tvRatingSummary.setText(getString(
                R.string.product_detail_views_format,
                product.getReviewCount()));
        ProductImageHelper.load(imgDetail, product);
        setupDescriptionInfo(product.getName());
        setupThumbs(product);
    }

    private void setupThumbs(Product product) {
        ShapeableImageView[] thumbs = {
                findViewById(R.id.thumb1),
                findViewById(R.id.thumb2),
                findViewById(R.id.thumb3),
                findViewById(R.id.thumb4)
        };

        List<String> sources = collectImageSources(product);
        boolean useImageRes = sources.isEmpty() && product.getImageRes(this) != 0;
        int visibleCount = useImageRes ? 1 : sources.size();
        int fallbackRes = product.getImageRes(this) != 0 ? product.getImageRes(this) : R.mipmap.traolongmochuong;

        for (int i = 0; i < thumbs.length; i++) {
            ShapeableImageView thumb = thumbs[i];
            if (thumb == null) {
                continue;
            }
            if (i < visibleCount) {
                thumb.setVisibility(View.VISIBLE);
                final int selectedIndex = i;
                if (useImageRes) {
                    thumb.setImageResource(fallbackRes);
                    thumb.setOnClickListener(v -> {
                        imgDetail.setImageResource(fallbackRes);
                        updateThumbSelection(thumbs, selectedIndex);
                    });
                } else {
                    String source = sources.get(i);
                    ProductImageHelper.loadFromSource(thumb, source, fallbackRes);
                    thumb.setOnClickListener(v -> {
                        ProductImageHelper.loadFromSource(imgDetail, source, fallbackRes);
                        updateThumbSelection(thumbs, selectedIndex);
                    });
                }
            } else {
                thumb.setVisibility(View.GONE);
                thumb.setOnClickListener(null);
            }
        }

        if (visibleCount > 0) {
            if (useImageRes) {
                imgDetail.setImageResource(fallbackRes);
            } else {
                ProductImageHelper.loadFromSource(imgDetail, sources.get(0), fallbackRes);
            }
            updateThumbSelection(thumbs, 0);
        }
    }

    private List<String> collectImageSources(Product product) {
        List<String> sources = new ArrayList<>();
        if (product.getImages() != null) {
            for (String image : product.getImages()) {
                if (image != null && !image.trim().isEmpty()) {
                    sources.add(image.trim());
                }
            }
        }
        if (sources.isEmpty() && product.getImage() != null && !product.getImage().trim().isEmpty()) {
            sources.add(product.getImage().trim());
        }
        return sources;
    }

    private void updateThumbSelection(ShapeableImageView[] thumbs, int selectedIndex) {
        int activeStroke = ContextCompat.getColor(this, R.color.brand_blue);
        int inactiveStroke = ContextCompat.getColor(this, R.color.outline_variant);
        for (int i = 0; i < thumbs.length; i++) {
            ShapeableImageView thumb = thumbs[i];
            if (thumb == null || thumb.getVisibility() != View.VISIBLE) {
                continue;
            }
            boolean isSelected = i == selectedIndex;
            thumb.setStrokeColor(ColorStateList.valueOf(isSelected ? activeStroke : inactiveStroke));
            thumb.setStrokeWidth(isSelected ? dp(2) : dp(1));
        }
    }

    private String safePrice(Object price) {
        String p = String.valueOf(price);
        if (p == null || p.isEmpty() || p.equals("null")) {
            return "0đ";
        }
        return p.endsWith("đ") ? p : p + "đ";
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
        setupLevelGroup(new MaterialButton[]{btnSweetnessNone, btnSweetnessLess, btnSweetnessNormal, btnSweetnessHigh}, 2);
        setupLevelGroup(new MaterialButton[]{btnIceLess, btnIceNormal, btnIceHigh}, 1);
    }

    private void setupSizeGroup(MaterialButton[] buttons, int defaultSelected) {
        for (int i = 0; i < buttons.length; i++) {
            MaterialButton button = buttons[i];
            if (button == null) continue;
            boolean isSelected = (i == defaultSelected);
            button.setSelected(isSelected);
            button.setTypeface(null, isSelected ? Typeface.BOLD : Typeface.NORMAL);
            
            button.setOnClickListener(v -> {
                for (MaterialButton b : buttons) {
                    if (b != null) {
                        b.setSelected(false);
                        b.setTypeface(null, Typeface.NORMAL);
                    }
                }
                v.setSelected(true);
                ((MaterialButton)v).setTypeface(null, Typeface.BOLD);
            });
        }
    }

    private void setupLevelGroup(MaterialButton[] buttons, int defaultSelected) {
        for (int i = 0; i < buttons.length; i++) {
            MaterialButton button = buttons[i];
            if (button == null) continue;
            boolean isSelected = (i == defaultSelected);
            button.setSelected(isSelected);
            button.setTypeface(null, isSelected ? Typeface.BOLD : Typeface.NORMAL);

            button.setOnClickListener(v -> {
                for (MaterialButton b : buttons) {
                    if (b != null) {
                        b.setSelected(false);
                        b.setTypeface(null, Typeface.NORMAL);
                    }
                }
                v.setSelected(true);
                ((MaterialButton)v).setTypeface(null, Typeface.BOLD);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure topping rows are always present after config changes/returning to screen.
        setupToppingRows();
    }

    private void fetchRecommendedProducts(String category) {
        repository.getProductsByCategory(category, new FirebaseProductRepository.ProductsCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                if (isFinishing()) return;
                List<Product> limited = new ArrayList<>();
                String currentName = tvDetailName.getText().toString();
                for (Product p : products) {
                    if (!p.getName().equalsIgnoreCase(currentName)) {
                        limited.add(p);
                    }
                    if (limited.size() >= 6) break;
                }
                RecyclerView rvRecommended = findViewById(R.id.rvRecommended);
                if (rvRecommended != null) {
                    ProductCardAdapter adapter = new ProductCardAdapter(limited, R.layout.item_product_card, ProductDetail.this::openRelatedDetail);
                    rvRecommended.setAdapter(adapter);
                }
            }

            @Override
            public void onError(String message) {
                RecyclerView rvRecommended = findViewById(R.id.rvRecommended);
                if (rvRecommended != null) {
                    rvRecommended.setAdapter(new ProductCardAdapter(getRecommendedProducts(), R.layout.item_product_card, ProductDetail.this::openRelatedDetail));
                }
            }
        });
    }

    private List<Product> getRecommendedProducts() {
        List<Product> products = new ArrayList<>();
        products.add(new Product("0", "P001", "Trà Xanh Sữa", "Trà Sữa", 24000, 28000, 21000, 25000, "", "", "traxanhsua.png", true, false));
        products.add(new Product("0", "P002", "Đường Đen", "Trà Sữa", 29000, 31000, 26000, 28000, "", "", "trasuatranchauduongden.png", true, false));
        products.add(new Product("0", "P003", "Trà Ô Long", "Trà Ô Long", 19000, 22000, 16000, 19000, "", "", "traolongmochuong.png", true, false));
        return products;
    }

    private void openRelatedDetail(Product product) {
        Intent intent = new Intent(this, ProductDetail.class);
        intent.putExtra("name", product.getName());
        intent.putExtra("category", product.getCategory());
        intent.putExtra("priceM", String.valueOf(product.getPriceM()));
        intent.putExtra("priceL", String.valueOf(product.getPriceL()));
        intent.putExtra("vipM", String.valueOf(product.getVipPriceM()));
        intent.putExtra("vipL", String.valueOf(product.getVipPriceL()));
        intent.putExtra("imageRes", product.getImageRes(this));
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

        int[] navIds = {
                R.id.nav_home,
                R.id.nav_menu,
                R.id.nav_orders,
                R.id.nav_promotion,
                R.id.nav_profile
        };
        NavBarHelper.setupNavBar(this, navIds, R.id.nav_menu, v -> {
            int id = v.getId();
            if (id == R.id.nav_home) {
                openMainTab(MainActivity.TAB_HOME, null);
            } else if (id == R.id.nav_menu) {
                openMainTab(MainActivity.TAB_MENU, null);
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, OrderHistory.class));
            } else if (id == R.id.nav_promotion) {
                startActivity(new Intent(this, BlogGeneral.class));
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, UserProfile.class));
            }
        });

        if (navMenu != null) {
            NavBarHelper.updateItemState(this, navMenu, true);
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

    private void setupTabs() {
        if (tabDescription == null || tabReview == null || tabCommitment == null) {
            return;
        }
        tabDescription.setOnClickListener(v -> selectTab(tabDescription));
        tabReview.setOnClickListener(v -> selectTab(tabReview));
        tabCommitment.setOnClickListener(v -> selectTab(tabCommitment));
        tabDescription.post(() -> selectTab(tabDescription));
    }

    private void selectTab(TextView selectedTab) {
        int activeColor = ContextCompat.getColor(this, R.color.brand_blue);
        int inactiveColor = ContextCompat.getColor(this, R.color.nav_inactive);

        tabDescription.setTextColor(selectedTab == tabDescription ? activeColor : inactiveColor);
        tabReview.setTextColor(selectedTab == tabReview ? activeColor : inactiveColor);
        tabCommitment.setTextColor(selectedTab == tabCommitment ? activeColor : inactiveColor);

        if (layoutDescription != null) {
            layoutDescription.setVisibility(selectedTab == tabDescription ? View.VISIBLE : View.GONE);
        }
        if (layoutReview != null) {
            layoutReview.setVisibility(selectedTab == tabReview ? View.VISIBLE : View.GONE);
        }
        if (layoutCommitment != null) {
            layoutCommitment.setVisibility(selectedTab == tabCommitment ? View.VISIBLE : View.GONE);
        }
        if (tvTabPlaceholder != null) {
            tvTabPlaceholder.setVisibility(View.GONE);
        }
        if (layoutReviewForm != null) {
            layoutReviewForm.setVisibility(View.GONE);
        }
        alignIndicatorToTab(selectedTab);
    }

    private void alignIndicatorToTab(TextView target) {
        if (tabIndicator == null || tabIndicatorContainer == null || target == null) {
            return;
        }
        int[] targetPos = new int[2];
        int[] containerPos = new int[2];
        target.getLocationOnScreen(targetPos);
        tabIndicatorContainer.getLocationOnScreen(containerPos);

        float x = targetPos[0] - containerPos[0];
        ViewGroup.LayoutParams layoutParams = tabIndicator.getLayoutParams();
        layoutParams.width = target.getWidth();
        tabIndicator.setLayoutParams(layoutParams);
        tabIndicator.animate().x(x).setDuration(180).start();
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
            tvNoteChevron.setRotation(isCustomizationExpanded ? 180f : 0f);
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

    private void addToppingRow(String name, String price, boolean unused) {
        View row = getLayoutInflater().inflate(R.layout.item_topping, toppingRowsContainer, false);
        TextView tvName = row.findViewById(R.id.tvToppingName);
        TextView tvPrice = row.findViewById(R.id.tvToppingPrice);
        TextView tvQty = row.findViewById(R.id.tvQuantity);
        ImageButton btnMinus = row.findViewById(R.id.btnMinus);
        ImageButton btnPlus = row.findViewById(R.id.btnPlus);

        tvName.setText(name);
        tvPrice.setText("+ " + price);
        toppingQuantities.put(name, 0);

        btnMinus.setOnClickListener(v -> {
            int current = toppingQuantities.get(name);
            if (current > 0) {
                current--;
                toppingQuantities.put(name, current);
                tvQty.setText(String.valueOf(current));
                tvQty.setTextColor(ContextCompat.getColor(this, current > 0 ? R.color.on_surface : R.color.nav_inactive));
                btnMinus.setColorFilter(ContextCompat.getColor(this, current > 0 ? R.color.on_surface : R.color.nav_inactive));
            }
        });

        btnPlus.setOnClickListener(v -> {
            int current = toppingQuantities.get(name);
            current++;
            toppingQuantities.put(name, current);
            tvQty.setText(String.valueOf(current));
            tvQty.setTextColor(ContextCompat.getColor(this, R.color.on_surface));
            btnMinus.setColorFilter(ContextCompat.getColor(this, R.color.on_surface));
        });

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
        super.onBackPressed();
        backToMenu();
    }
}