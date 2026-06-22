package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.text.Html;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.teatrack_mcd_253eie502802_group02.MainActivity;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.adapter.ProductCardAdapter;
import com.teatrack_mcd_253eie502802_group02.data.FirebaseProductRepository;
import com.teatrack_mcd_253eie502802_group02.model.CartItem;
import com.teatrack_mcd_253eie502802_group02.model.Product;
import com.teatrack_mcd_253eie502802_group02.shared.ui.CartBadgeHelper;
import com.teatrack_mcd_253eie502802_group02.util.CartActions;
import com.teatrack_mcd_253eie502802_group02.util.ProductImageHelper;
import com.teatrack_mcd_253eie502802_group02.util.ToppingPriceHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;

public class ProductDetail extends BaseActivity {

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
    private MaterialButton btnWriteReview;
    private MaterialButton btnSizeOptionM;
    private MaterialButton btnSizeOptionL;
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
    private Product currentProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_detail);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        ImageView btnBack    = findViewById(R.id.btnBack);
        imgDetail            = findViewById(R.id.imgDetail);
        tvDetailName         = findViewById(R.id.tvDetailName);
        tvTopTitle           = findViewById(R.id.tvTopTitle);
        tvCategory           = findViewById(R.id.tvCategory);
        tvRatingValue        = findViewById(R.id.tvRatingValue);
        tvPriceM             = findViewById(R.id.tvPriceM);
        tvPriceL             = findViewById(R.id.tvPriceL);
        tvVipPriceM          = findViewById(R.id.tvVipPriceM);
        tvVipPriceL          = findViewById(R.id.tvVipPriceL);
        tvRatingSummary      = findViewById(R.id.tvRatingSummary);
        tvDescription        = findViewById(R.id.tvDescription);
        btnSizeOptionM       = findViewById(R.id.btnSizeOptionM);
        btnSizeOptionL       = findViewById(R.id.btnSizeOptionL);
        btnSweetnessLess     = findViewById(R.id.btnSweetnessLess);
        btnSweetnessNormal   = findViewById(R.id.btnSweetnessNormal);
        btnSweetnessHigh     = findViewById(R.id.btnSweetnessHigh);
        btnIceLess           = findViewById(R.id.btnIceLess);
        btnIceNormal         = findViewById(R.id.btnIceNormal);
        btnIceHigh           = findViewById(R.id.btnIceHigh);
        TextView tvQuantity  = findViewById(R.id.tvQuantity);
        ImageButton btnQtyMinus = findViewById(R.id.btnQtyMinus);
        ImageButton btnQtyPlus  = findViewById(R.id.btnQtyPlus);
        RecyclerView rvRecommended = findViewById(R.id.rvRecommended);
        TextView btnAddToCart = findViewById(R.id.btnAddToCart);
        TextView btnBuyNow    = findViewById(R.id.btnBuyNow);
        TextView breadcrumbHome = findViewById(R.id.breadcrumbHome);
        TextView breadcrumbMenu = findViewById(R.id.breadcrumbMenu);
        breadcrumbTeaLatte      = findViewById(R.id.breadcrumbTeaLatte);
        LinearLayout noteToggle  = findViewById(R.id.noteToggle);
        customizationContent     = findViewById(R.id.customizationContent);
        tvNoteChevron            = findViewById(R.id.tvNoteChevron);
        toppingRowsContainer     = findViewById(R.id.toppingRowsContainer);
        tabDescription           = findViewById(R.id.tabDescription);
        tabReview                = findViewById(R.id.tabReview);
        tabCommitment            = findViewById(R.id.tabCommitment);
        tabIndicator             = findViewById(R.id.tabIndicator);
        tabIndicatorContainer    = findViewById(R.id.tabIndicatorContainer);
        layoutReview             = findViewById(R.id.layoutReview);
        layoutDescription        = findViewById(R.id.layoutDescription);
        layoutCommitment         = findViewById(R.id.layoutCommitment);
        layoutReviewForm         = findViewById(R.id.layoutReviewForm);
        tvTabPlaceholder         = findViewById(R.id.tvTabPlaceholder);
        tvInfoProductName        = findViewById(R.id.tvInfoProductName);
        tvInfoWeight             = findViewById(R.id.tvInfoWeight);
        tvInfoStorage            = findViewById(R.id.tvInfoStorage);
        tvInfoOrigin             = findViewById(R.id.tvInfoOrigin);
        tvInfoTopping            = findViewById(R.id.tvInfoTopping);
        tvCommitment1            = findViewById(R.id.tvCommitment1);
        tvCommitment2            = findViewById(R.id.tvCommitment2);
        tvCommitment3            = findViewById(R.id.tvCommitment3);
        tvCommitment4            = findViewById(R.id.tvCommitment4);
        tvReviewCount            = findViewById(R.id.tvReviewCount);
        imgStar1                 = findViewById(R.id.imgStar1);
        imgStar2                 = findViewById(R.id.imgStar2);
        imgStar3                 = findViewById(R.id.imgStar3);
        imgStar4                 = findViewById(R.id.imgStar4);
        imgStar5                 = findViewById(R.id.imgStar5);
        btnWriteReview           = findViewById(R.id.btnWriteReview);

        // ── fix: img_logo không có trong layout này ──────────────────────────
        // Đã xóa findViewById(R.id.img_logo) vì layout activity_product_detail
        // không khai báo view đó.

        if (btnBack != null) btnBack.setOnClickListener(v -> backToMenu());

        if (breadcrumbHome != null) {
            breadcrumbHome.setOnClickListener(v -> startActivity(new Intent(this, Homepage.class)));
        }
        if (breadcrumbMenu != null) {
            breadcrumbMenu.setOnClickListener(v -> openMainTab(null));
        }
        if (breadcrumbTeaLatte != null) {
            breadcrumbTeaLatte.setOnClickListener(v ->
                    openMainTab(getString(R.string.firebase_category_tea_latte)));
        }
        if (noteToggle != null) noteToggle.setOnClickListener(v -> toggleCustomization());

        String name        = getIntent().getStringExtra("name");
        String priceM      = getIntent().getStringExtra("priceM");
        String priceL      = getIntent().getStringExtra("priceL");
        String vipM        = getIntent().getStringExtra("vipM");
        String vipL        = getIntent().getStringExtra("vipL");
        int    imageRes    = getIntent().getIntExtra("imageRes", R.mipmap.traolongmochuong);
        float  rating      = getIntent().getFloatExtra("rating", 4.9f);
        String reviewCount = getIntent().getStringExtra("reviewCount");
        String category    = getIntent().getStringExtra("category");

        bindFromIntent(name, category, priceM, priceL, vipM, vipL, imageRes, rating, reviewCount);
        CartBadgeHelper.setup(this);

        if (name != null) {
            fetchFromFirebase(name);
        } else {
            selectTab(tabDescription);
        }

        Product dummy = new Product();
        dummy.setImageRes(imageRes);
        setupThumbs(dummy);

        if (btnQtyMinus != null) {
            btnQtyMinus.setOnClickListener(v -> {
                quantity = Math.max(1, quantity - 1);
                if (tvQuantity != null) tvQuantity.setText(String.valueOf(quantity));
            });
        }
        if (btnQtyPlus != null) {
            btnQtyPlus.setOnClickListener(v -> {
                quantity += 1;
                if (tvQuantity != null) tvQuantity.setText(String.valueOf(quantity));
            });
        }

        if (btnAddToCart != null) btnAddToCart.setOnClickListener(v -> CartActions.addItem(this, buildCartItem()));
        if (btnBuyNow != null) {
            btnBuyNow.setOnClickListener(v -> {
                CartActions.addItemSilent(this, buildCartItem());
                startActivity(new Intent(this, Cart.class));
                finish();
            });
        }

        if (rvRecommended != null) {
            rvRecommended.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvRecommended.setAdapter(new ProductCardAdapter(
                    new ArrayList<>(),
                    R.layout.item_product_card,
                    ProductDetail.this::openRelatedDetail,
                    product -> CartActions.addDefaultProduct(ProductDetail.this, product)
            ));
        }

        setupTabs();
        setupOptionSelections();
        setupToppingRows();
        setupDescriptionInfo(name);
        setupCommitmentContent();
        setupReviewSection();
        applyCustomizationState();
    }

    // ── Description info ──────────────────────────────────────────────────────

    private void setupDescriptionInfo(String productName) {
        String resolvedName = productName != null ? productName : getString(R.string.product_detail_title);
        if (tvInfoProductName != null)
            tvInfoProductName.setText(formatInfoRow(R.string.product_detail_info_name, resolvedName));
        if (tvInfoWeight != null)
            tvInfoWeight.setText(formatInfoRow(R.string.product_detail_info_weight, getString(R.string.product_detail_info_weight_value)));
        if (tvInfoStorage != null)
            tvInfoStorage.setText(formatInfoRow(R.string.product_detail_info_storage, getString(R.string.product_detail_info_storage_value)));
        if (tvInfoOrigin != null)
            tvInfoOrigin.setText(formatInfoRow(R.string.product_detail_info_origin, getString(R.string.product_detail_info_origin_value)));
        if (tvInfoTopping != null)
            tvInfoTopping.setText(formatInfoRow(R.string.product_detail_info_topping, getString(R.string.product_detail_info_topping_value)));
    }

    private CharSequence formatInfoRow(int labelRes, String value) {
        String label = getString(labelRes);
        if (!label.endsWith(":")) label = label + ":";
        return Html.fromHtml("<b>" + label + "</b> " + value, Html.FROM_HTML_MODE_LEGACY);
    }

    // ── Commitment ───────────────────────────────────────────────────────────

    private void setupCommitmentContent() {
        setHtmlText(tvCommitment1, R.string.product_detail_commitment_1);
        setHtmlText(tvCommitment2, R.string.product_detail_commitment_2);
        setHtmlText(tvCommitment3, R.string.product_detail_commitment_3);
        setHtmlText(tvCommitment4, R.string.product_detail_commitment_4);
    }

    private void setHtmlText(TextView textView, int stringRes) {
        if (textView == null) return;
        textView.setText(Html.fromHtml(getString(stringRes), Html.FROM_HTML_MODE_LEGACY));
    }

    // ── Reviews ───────────────────────────────────────────────────────────────

    private void setupReviewSection() {
        if (tvReviewCount != null)
            tvReviewCount.setText(getString(R.string.product_detail_review_count_format, "0"));

        View.OnClickListener starClickListener = v -> {
            int starRating = 1;
            int id = v.getId();
            if      (id == R.id.imgStar1) starRating = 1;
            else if (id == R.id.imgStar2) starRating = 2;
            else if (id == R.id.imgStar3) starRating = 3;
            else if (id == R.id.imgStar4) starRating = 4;
            else if (id == R.id.imgStar5) starRating = 5;
            updateStarRating(starRating);
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
                    boolean showing = layoutReviewForm.getVisibility() == View.VISIBLE;
                    layoutReviewForm.setVisibility(showing ? View.GONE : View.VISIBLE);
                    if (!showing) {
                        updateStarRating(0);
                        EditText reviewTitle = findViewById(R.id.edtReviewTitle);
                        if (reviewTitle != null) reviewTitle.requestFocus();
                    }
                }
            });
        }
    }

    private void updateStarRating(int rating) {
        ImageView[] stars = {imgStar1, imgStar2, imgStar3, imgStar4, imgStar5};
        int activeColor   = ContextCompat.getColor(this, R.color.star_rating_active);
        int inactiveColor = ContextCompat.getColor(this, R.color.outline_variant);
        for (int i = 0; i < stars.length; i++) {
            if (stars[i] != null) {
                ImageViewCompat.setImageTintList(stars[i],
                        ColorStateList.valueOf(i < rating ? activeColor : inactiveColor));
            }
        }
    }

    // ── Bind helpers ─────────────────────────────────────────────────────────

    private void bindFromIntent(String name, String category,
                                String priceM, String priceL,
                                String vipM,   String vipL,
                                int imageRes, float rating, String reviewCount) {
        String resolvedName = name != null ? name : getString(R.string.product_detail_title);
        tvDetailName.setText(resolvedName);
        if (tvTopTitle != null) tvTopTitle.setText(resolvedName);

        tvCategory.setText(category != null ? category : getString(R.string.product_detail_category_default));
        if (breadcrumbTeaLatte != null && category != null && !category.isEmpty())
            breadcrumbTeaLatte.setText(category);

        if (tvRatingValue != null)
            tvRatingValue.setText(String.format(java.util.Locale.getDefault(), "%.1f", rating));

        tvPriceM.setText(safePrice(priceM));
        tvPriceL.setText(safePrice(priceL));
        tvVipPriceM.setText(safePrice(vipM));
        tvVipPriceL.setText(safePrice(vipL));
        bindCustomizationPrice(priceM, priceL);

        tvRatingSummary.setText(getString(R.string.product_detail_views_format,
                reviewCount == null ? "1k" : reviewCount));
        tvDescription.setText(getString(R.string.product_detail_description_default));
        imgDetail.setImageResource(imageRes);

        currentProduct = buildProductFromIntent(name, category, priceM, priceL, vipM, vipL, imageRes, rating, reviewCount);
    }

    private void fetchFromFirebase(String name) {
        if (name == null || name.isEmpty()) return;
        repository.getProductByName(name, new FirebaseProductRepository.ProductCallback() {
            @Override public void onSuccess(Product product) {
                if (!isFinishing()) bindFromProduct(product);
            }
            @Override public void onError(String message) { /* fallback to intent data */ }
        });
    }

    private void bindFromProduct(Product product) {
        currentProduct = product;
        tvDetailName.setText(product.getName());
        if (tvTopTitle != null) tvTopTitle.setText(product.getName());

        if (product.getCategory() != null && !product.getCategory().isEmpty()) {
            tvCategory.setText(product.getCategory());
            if (breadcrumbTeaLatte != null) breadcrumbTeaLatte.setText(product.getCategory());
            fetchRecommendedProducts(product.getCategory());
        }

        tvPriceM.setText(safePrice(product.getPriceM()));
        tvPriceL.setText(safePrice(product.getPriceL()));
        tvVipPriceM.setText(safePrice(product.getVipPriceM()));
        tvVipPriceL.setText(safePrice(product.getVipPriceL()));
        bindCustomizationPrice(String.valueOf(product.getPriceM()), String.valueOf(product.getPriceL()));

        if (tvRatingValue != null)
            tvRatingValue.setText(String.format(java.util.Locale.getDefault(), "%.1f", product.getRating()));

        tvRatingSummary.setText(getString(R.string.product_detail_views_format, product.getReviewCount()));
        ProductImageHelper.load(imgDetail, product);
        setupDescriptionInfo(product.getName());
        setupThumbs(product);
        selectTab(tabDescription);
    }

    // ── Thumbnails ────────────────────────────────────────────────────────────

    private void setupThumbs(Product product) {
        ShapeableImageView[] thumbs = {
                findViewById(R.id.thumb1),
                findViewById(R.id.thumb2),
                findViewById(R.id.thumb3),
                findViewById(R.id.thumb4)
        };

        List<String> sources  = collectImageSources(product);
        boolean useImageRes   = sources.isEmpty() && product.getImageRes(this) != 0;
        int visibleCount      = useImageRes ? 1 : sources.size();
        int fallbackRes       = product.getImageRes(this) != 0 ? product.getImageRes(this) : R.mipmap.traolongmochuong;

        for (int i = 0; i < thumbs.length; i++) {
            ShapeableImageView thumb = thumbs[i];
            if (thumb == null) continue;

            if (i < visibleCount) {
                thumb.setVisibility(View.VISIBLE);
                final int idx = i;
                if (useImageRes) {
                    thumb.setImageResource(fallbackRes);
                    thumb.setOnClickListener(v -> {
                        imgDetail.setImageResource(fallbackRes);
                        updateThumbSelection(thumbs, idx);
                    });
                } else {
                    String source = sources.get(i);
                    ProductImageHelper.loadFromSource(thumb, source, fallbackRes);
                    thumb.setOnClickListener(v -> {
                        ProductImageHelper.loadFromSource(imgDetail, source, fallbackRes);
                        updateThumbSelection(thumbs, idx);
                    });
                }
            } else {
                thumb.setVisibility(View.GONE);
                thumb.setOnClickListener(null);
            }
        }

        if (visibleCount > 0) {
            if (useImageRes) imgDetail.setImageResource(fallbackRes);
            else ProductImageHelper.loadFromSource(imgDetail, sources.get(0), fallbackRes);
            updateThumbSelection(thumbs, 0);
        }
    }

    private List<String> collectImageSources(Product product) {
        List<String> sources = new ArrayList<>();
        if (product.getImages() != null) {
            for (String image : product.getImages()) {
                if (image != null && !image.trim().isEmpty()) sources.add(image.trim());
            }
        }
        if (sources.isEmpty() && product.getImage() != null && !product.getImage().trim().isEmpty())
            sources.add(product.getImage().trim());
        return sources;
    }

    private void updateThumbSelection(ShapeableImageView[] thumbs, int selectedIndex) {
        int activeStroke   = ContextCompat.getColor(this, R.color.brand_blue);
        int inactiveStroke = ContextCompat.getColor(this, R.color.outline_variant);
        for (int i = 0; i < thumbs.length; i++) {
            ShapeableImageView thumb = thumbs[i];
            if (thumb == null || thumb.getVisibility() != View.VISIBLE) continue;
            boolean sel = i == selectedIndex;
            thumb.setStrokeColor(ColorStateList.valueOf(sel ? activeStroke : inactiveStroke));
            thumb.setStrokeWidth(sel ? dp(2) : dp(1));
        }
    }

    // ── Price helpers ─────────────────────────────────────────────────────────

    private String safePrice(Object price) {
        if (price == null) return "0\u0111";
        String p = String.valueOf(price);
        if (p.isEmpty() || p.equalsIgnoreCase("null")) return "0\u0111";
        try {
            String clean = p.replaceAll("[^0-9]", "");
            if (clean.isEmpty()) return p.endsWith("\u0111") ? p : p + "\u0111";
            long val = Long.parseLong(clean);
            return String.format(java.util.Locale.US, "%,d", val).replace(',', '.') + "\u0111";
        } catch (Exception e) {
            return p.endsWith("\u0111") ? p : p + "\u0111";
        }
    }

    private void bindCustomizationPrice(String priceM, String priceL) {
        if (btnSizeOptionM != null)
            btnSizeOptionM.setText(getString(R.string.product_detail_size_m_price_format, safePrice(priceM)));
        if (btnSizeOptionL != null)
            btnSizeOptionL.setText(getString(R.string.product_detail_size_l_price_format, safePrice(priceL)));
    }

    // ── Option selections ─────────────────────────────────────────────────────

    private void setupOptionSelections() {
        setupButtonGroup(new MaterialButton[]{btnSizeOptionM, btnSizeOptionL}, 0);
        setupButtonGroup(new MaterialButton[]{btnSweetnessLess, btnSweetnessNormal, btnSweetnessHigh}, 1);
        setupButtonGroup(new MaterialButton[]{btnIceLess, btnIceNormal, btnIceHigh}, 1);
    }

    private void setupButtonGroup(MaterialButton[] buttons, int defaultSelected) {
        for (int i = 0; i < buttons.length; i++) {
            MaterialButton button = buttons[i];
            if (button == null) continue;
            boolean sel = (i == defaultSelected);
            button.setSelected(sel);
            button.setTypeface(null, sel ? Typeface.BOLD : Typeface.NORMAL);
            button.setOnClickListener(v -> {
                for (MaterialButton b : buttons) {
                    if (b != null) { b.setSelected(false); b.setTypeface(null, Typeface.NORMAL); }
                }
                v.setSelected(true);
                ((MaterialButton) v).setTypeface(null, Typeface.BOLD);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CartBadgeHelper.updateBadge(this);
    }

    // ── Recommended products ──────────────────────────────────────────────────

    private void fetchRecommendedProducts(String category) {
        repository.getProductsByCategory(category, new FirebaseProductRepository.ProductsCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                if (isFinishing()) return;
                List<Product> limited = new ArrayList<>();
                String currentName = tvDetailName != null ? tvDetailName.getText().toString() : "";
                for (Product p : products) {
                    if (p == null || p.getName() == null) continue;
                    if (!p.getName().equalsIgnoreCase(currentName)) limited.add(p);
                    if (limited.size() >= 6) break;
                }
                RecyclerView rv = findViewById(R.id.rvRecommended);
                if (rv != null) rv.setAdapter(new ProductCardAdapter(
                        limited, R.layout.item_product_card,
                        ProductDetail.this::openRelatedDetail,
                        p -> CartActions.addDefaultProduct(ProductDetail.this, p)));
            }

            @Override
            public void onError(String message) {
                RecyclerView rv = findViewById(R.id.rvRecommended);
                if (rv != null) rv.setAdapter(new ProductCardAdapter(
                        getRecommendedProducts(), R.layout.item_product_card,
                        ProductDetail.this::openRelatedDetail,
                        p -> CartActions.addDefaultProduct(ProductDetail.this, p)));
            }
        });
    }

    private List<Product> getRecommendedProducts() {
        List<Product> products = new ArrayList<>();
        products.add(new Product("0", "P001", "Tra Xanh Sua",  "Tra Sua", 24000, 28000, 21000, 25000, "", "", "traxanhsua.png",              true, false));
        products.add(new Product("0", "P002", "Duong Den",     "Tra Sua", 29000, 31000, 26000, 28000, "", "", "trasuatranchauduongden.png",   true, false));
        products.add(new Product("0", "P003", "Tra O Long",    "Tra O Long", 19000, 22000, 16000, 19000, "", "", "traolongmochuong.png",       true, false));
        return products;
    }

    private void openRelatedDetail(Product product) {
        Intent intent = new Intent(this, ProductDetail.class);
        intent.putExtra("name",        product.getName());
        intent.putExtra("category",    product.getCategory());
        intent.putExtra("priceM",      String.valueOf(product.getPriceM()));
        intent.putExtra("priceL",      String.valueOf(product.getPriceL()));
        intent.putExtra("vipM",        String.valueOf(product.getVipPriceM()));
        intent.putExtra("vipL",        String.valueOf(product.getVipPriceL()));
        intent.putExtra("imageRes",    product.getImageRes(this));
        intent.putExtra("rating",      product.getRating());
        intent.putExtra("reviewCount", product.getReviewCount());
        startActivity(intent);
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private void backToMenu() {
        openMainTab(null);
        finish();
    }

    private void openMainTab(String category) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_SELECTED_TAB, MainActivity.TAB_MENU);
        if (category != null) {
            intent.putExtra(MainActivity.EXTRA_MENU_CATEGORY,
                    com.teatrack_mcd_253eie502802_group02.util.CategoryKeys.normalize(category));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    // ── Tabs ──────────────────────────────────────────────────────────────────

    private void setupTabs() {
        if (tabDescription == null || tabReview == null || tabCommitment == null) return;
        tabDescription.setOnClickListener(v -> selectTab(tabDescription));
        tabReview.setOnClickListener(v -> selectTab(tabReview));
        tabCommitment.setOnClickListener(v -> selectTab(tabCommitment));
    }

    private void selectTab(TextView selectedTab) {
        if (tabDescription == null || tabReview == null || tabCommitment == null) return;
        int activeColor   = ContextCompat.getColor(this, R.color.brand_blue);
        int inactiveColor = ContextCompat.getColor(this, R.color.nav_inactive);

        tabDescription.setTextColor(selectedTab == tabDescription ? activeColor : inactiveColor);
        tabReview.setTextColor     (selectedTab == tabReview      ? activeColor : inactiveColor);
        tabCommitment.setTextColor (selectedTab == tabCommitment  ? activeColor : inactiveColor);

        if (layoutDescription != null)
            layoutDescription.setVisibility(selectedTab == tabDescription ? View.VISIBLE : View.GONE);
        if (layoutReview != null)
            layoutReview.setVisibility(selectedTab == tabReview ? View.VISIBLE : View.GONE);
        if (layoutCommitment != null)
            layoutCommitment.setVisibility(selectedTab == tabCommitment ? View.VISIBLE : View.GONE);
        if (tvTabPlaceholder != null) tvTabPlaceholder.setVisibility(View.GONE);
        if (layoutReviewForm != null) layoutReviewForm.setVisibility(View.GONE);
        alignIndicatorToTab(selectedTab);
    }

    private void alignIndicatorToTab(TextView target) {
        if (tabIndicator == null || tabIndicatorContainer == null || target == null) return;
        int[] targetPos    = new int[2];
        int[] containerPos = new int[2];
        target.getLocationOnScreen(targetPos);
        tabIndicatorContainer.getLocationOnScreen(containerPos);
        float x = targetPos[0] - containerPos[0];
        ViewGroup.LayoutParams lp = tabIndicator.getLayoutParams();
        lp.width = target.getWidth();
        tabIndicator.setLayoutParams(lp);
        tabIndicator.animate().x(x).setDuration(180).start();
    }

    // ── Customization panel ───────────────────────────────────────────────────

    private void toggleCustomization() {
        isCustomizationExpanded = !isCustomizationExpanded;
        applyCustomizationState();
    }

    private void applyCustomizationState() {
        if (customizationContent != null)
            customizationContent.setVisibility(isCustomizationExpanded ? View.VISIBLE : View.GONE);
        if (tvNoteChevron != null)
            tvNoteChevron.setRotation(isCustomizationExpanded ? 180f : 0f);
    }

    // ── Toppings ──────────────────────────────────────────────────────────────

    private void setupToppingRows() {
        if (toppingRowsContainer == null) return;
        toppingRowsContainer.removeAllViews();
        String[][] toppings = {
                {"Suong sao",                  "3.000d"},
                {"Thach dua nguyen vi",        "3.000d"},
                {"Hat e",                      "3.000d"},
                {"Thach dua huong dao",        "5.000d"},
                {"Thach aiyu",                 "5.000d"},
                {"Thach soi la dua",           "5.000d"},
                {"Thach suong sao vien (8)",   "5.000d"},
                {"Tran chau hoang kim",        "5.000d"},
                {"Tran chau duong den",        "5.000d"},
                {"Tran chau 3Q trang/den",     "5.000d"},
                {"Tran chau khoai mon",        "5.000d"},
                {"Hat thuy tinh cu nang",      "5.000d"},
                {"Hat thuy tinh lua mach",     "5.000d"},
                {"Dao mieng",                  "5.000d"},
                {"Khoai mon nghien",           "5.000d"},
                {"Hat sen",                    "7.000d"},
                {"Kem tuoi vani",              "7.000d"},
                {"Kem cheese",                 "7.000d"},
                {"Pudding trung",              "7.000d"},
                {"Thach sua vien (8)",         "7.000d"}
        };
        for (String[] topping : toppings) {
            addToppingRow(topping[0], topping[1]);
        }
    }

    private void addToppingRow(String name, String price) {
        View row = getLayoutInflater().inflate(R.layout.item_topping, toppingRowsContainer, false);
        TextView    tvName  = row.findViewById(R.id.tvToppingName);
        TextView    tvPrice = row.findViewById(R.id.tvToppingPrice);
        TextView    tvQty   = row.findViewById(R.id.tvQuantity);
        ImageButton btnMinus = row.findViewById(R.id.btnMinus);
        ImageButton btnPlus  = row.findViewById(R.id.btnPlus);

        tvName.setText(name);
        tvPrice.setText(getString(R.string.topping_price_format, price));
        toppingQuantities.put(name, 0);

        btnMinus.setOnClickListener(v -> {
            Integer cur = toppingQuantities.get(name);
            int current = cur != null ? cur : 0;
            if (current > 0) {
                current--;
                toppingQuantities.put(name, current);
                tvQty.setText(String.valueOf(current));
                int color = ContextCompat.getColor(this, current > 0 ? R.color.on_surface : R.color.nav_inactive);
                tvQty.setTextColor(color);
                btnMinus.setColorFilter(color);
            }
        });

        btnPlus.setOnClickListener(v -> {
            Integer cur = toppingQuantities.get(name);
            int current = (cur != null ? cur : 0) + 1;
            toppingQuantities.put(name, current);
            tvQty.setText(String.valueOf(current));
            int color = ContextCompat.getColor(this, R.color.on_surface);
            tvQty.setTextColor(color);
            btnMinus.setColorFilter(color);
        });

        toppingRowsContainer.addView(row);
    }

    // ── Cart item builder ─────────────────────────────────────────────────────

    private CartItem buildCartItem() {
        CartItem item = currentProduct != null
                ? CartItem.fromProduct(this, currentProduct)
                : new CartItem();

        if ((item.getProductName() == null || item.getProductName().isEmpty()) && tvDetailName != null)
            item.setProductName(tvDetailName.getText().toString());

        String size = getSelectedSize();
        item.setSize(size);
        item.setSugar(getSelectedSugar());
        item.setIce(getSelectedIce());
        item.setQuantity(quantity);

        if (currentProduct != null) {
            if ("L".equals(size)) {
                item.setUnitPrice(currentProduct.getPriceL());
                item.setVipUnitPrice(currentProduct.getVipPriceL());
            } else {
                item.setUnitPrice(currentProduct.getPriceM());
                item.setVipUnitPrice(currentProduct.getVipPriceM());
            }
            item.setImageRes(currentProduct.getImageRes(this));
            item.setImage(currentProduct.getImage());
        }

        for (Map.Entry<String, Integer> entry : toppingQuantities.entrySet()) {
            Integer qty = entry.getValue();
            if (qty != null && qty > 0)
                item.addTopping(entry.getKey(), ToppingPriceHelper.getPrice(entry.getKey()), qty);
        }
        return item;
    }

    private Product buildProductFromIntent(String name, String category,
                                           String priceM, String priceL,
                                           String vipM,   String vipL,
                                           int imageRes, float rating, String reviewCount) {
        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setImageRes(imageRes);
        product.setRating(rating);
        product.setReviewCount(reviewCount);
        product.setPrice(parsePriceInt(priceM));
        product.setPriceL(parsePriceInt(priceL));
        product.setVipPriceM(parsePriceInt(vipM));
        product.setVipPriceL(parsePriceInt(vipL));
        return product;
    }

    private int parsePriceInt(String value) {
        if (value == null || value.isEmpty()) return 0;
        try {
            String clean = value.replaceAll("[^0-9]", "");
            return clean.isEmpty() ? 0 : Integer.parseInt(clean);
        } catch (Exception e) {
            return 0;
        }
    }

    private String getSelectedSize() {
        return (btnSizeOptionL != null && btnSizeOptionL.isSelected()) ? "L" : "M";
    }

    private String getSelectedSugar() {
        if (btnSweetnessLess  != null && btnSweetnessLess.isSelected())  return btnSweetnessLess.getText().toString();
        if (btnSweetnessHigh  != null && btnSweetnessHigh.isSelected())  return btnSweetnessHigh.getText().toString();
        if (btnSweetnessNormal != null) return btnSweetnessNormal.getText().toString();
        return getString(R.string.product_detail_level_medium);
    }

    private String getSelectedIce() {
        if (btnIceLess   != null && btnIceLess.isSelected())  return btnIceLess.getText().toString();
        if (btnIceHigh   != null && btnIceHigh.isSelected())  return btnIceHigh.getText().toString();
        if (btnIceNormal != null) return btnIceNormal.getText().toString();
        return getString(R.string.product_detail_level_medium);
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()));
    }

    @Override
    public void onBackPressed() {
        backToMenu();
    }
}