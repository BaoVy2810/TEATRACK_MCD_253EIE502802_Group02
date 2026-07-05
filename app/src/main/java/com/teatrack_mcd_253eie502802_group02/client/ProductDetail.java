package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.text.Html;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import androidx.core.widget.NestedScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
import com.teatrack_mcd_253eie502802_group02.data.FirebaseReviewRepository;
import com.teatrack_mcd_253eie502802_group02.data.ReviewCatalogSync;
import com.teatrack_mcd_253eie502802_group02.model.CartItem;
import com.teatrack_mcd_253eie502802_group02.model.Product;
import com.teatrack_mcd_253eie502802_group02.model.ProductReview;
import com.teatrack_mcd_253eie502802_group02.shared.ui.CartBadgeHelper;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;
import com.teatrack_mcd_253eie502802_group02.util.CartActions;
import com.teatrack_mcd_253eie502802_group02.util.DateTimeHelper;
import com.teatrack_mcd_253eie502802_group02.util.ProductImageHelper;
import com.teatrack_mcd_253eie502802_group02.util.ReviewStatsHelper;
import com.teatrack_mcd_253eie502802_group02.util.ToppingPriceHelper;
import com.teatrack_mcd_253eie502802_group02.util.UserProfileHelper;
import com.teatrack_mcd_253eie502802_group02.util.UserRoleHelper;
import com.teatrack_mcd_253eie502802_group02.util.VipPriceUiHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;

public class ProductDetail extends BaseActivity {

    private static final int MAX_VISIBLE_REVIEWS = 3;
    private static final long REVIEW_SUCCESS_VISIBLE_MS = 5000L;

    private int quantity = 1;
    private ImageView imgDetail;
    private TextView tvDetailName;
    private TextView tvTopTitle;
    private TextView tvCategory;
    private TextView tvRatingValue;
    private TextView tvSizeBadgeM;
    private TextView tvSizeBadgeL;
    private TextView tvPriceM;
    private TextView tvPriceL;
    private TextView tvVipPriceM;
    private TextView tvVipPriceL;
    private TextView tvVipBadgeM;
    private TextView tvVipBadgeL;
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
    private TextView tvReviewAverage;
    private TextView tvReviewSuccessMessage;
    private ImageView imgStar1, imgStar2, imgStar3, imgStar4, imgStar5;
    private ImageView imgSummaryStar1, imgSummaryStar2, imgSummaryStar3, imgSummaryStar4, imgSummaryStar5;
    private ProgressBar bar1, bar2, bar3, bar4, bar5;
    private LinearLayout layoutReviewSuccess;
    private LinearLayout layoutReviewEmpty;
    private View cardReviewList;
    private LinearLayout layoutReviewList;
    private NestedScrollView scrollReviewList;
    private final Handler reviewUiHandler = new Handler(Looper.getMainLooper());
    private Runnable hideReviewSuccessRunnable;
    private int selectedRating = 0;
    private MaterialButton btnWriteReview;
    private MaterialButton btnSubmitReview;
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
    private boolean isCustomizationExpanded = false;
    private final Map<String, Integer> toppingQuantities = new LinkedHashMap<>();
    private final Map<String, Integer> toppingUnitPrices = new LinkedHashMap<>();
    private final FirebaseProductRepository repository = new FirebaseProductRepository();
    private final FirebaseReviewRepository reviewRepository = new FirebaseReviewRepository();
    private final ReviewCatalogSync reviewCatalogSync = new ReviewCatalogSync();
    private Product currentProduct;
    private List<ProductReview> productReviews = new ArrayList<>();

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

        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnShare = findViewById(R.id.btnShare);
        imgDetail = findViewById(R.id.imgDetail);
        tvDetailName = findViewById(R.id.tvDetailName);
        tvTopTitle = findViewById(R.id.tvTopTitle);
        tvCategory = findViewById(R.id.tvCategory);
        tvRatingValue = findViewById(R.id.tvRatingValue);
        tvSizeBadgeM = findViewById(R.id.tvSizeBadgeM);
        tvSizeBadgeL = findViewById(R.id.tvSizeBadgeL);
        tvPriceM = findViewById(R.id.tvPriceM);
        tvPriceL = findViewById(R.id.tvPriceL);
        tvVipPriceM = findViewById(R.id.tvVipPriceM);
        tvVipPriceL = findViewById(R.id.tvVipPriceL);
        tvVipBadgeM = findViewById(R.id.tvVipBadgeM);
        tvVipBadgeL = findViewById(R.id.tvVipBadgeL);
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
        tvReviewAverage = findViewById(R.id.tvReviewAverage);
        tvReviewSuccessMessage = findViewById(R.id.tvReviewSuccessMessage);
        layoutReviewSuccess = findViewById(R.id.layoutReviewSuccess);
        layoutReviewEmpty = findViewById(R.id.layoutReviewEmpty);
        cardReviewList = findViewById(R.id.cardReviewList);
        layoutReviewList = findViewById(R.id.layoutReviewList);
        scrollReviewList = findViewById(R.id.scrollReviewList);
        bar1 = findViewById(R.id.bar1);
        bar2 = findViewById(R.id.bar2);
        bar3 = findViewById(R.id.bar3);
        bar4 = findViewById(R.id.bar4);
        bar5 = findViewById(R.id.bar5);
        imgSummaryStar1 = findViewById(R.id.imgSummaryStar1);
        imgSummaryStar2 = findViewById(R.id.imgSummaryStar2);
        imgSummaryStar3 = findViewById(R.id.imgSummaryStar3);
        imgSummaryStar4 = findViewById(R.id.imgSummaryStar4);
        imgSummaryStar5 = findViewById(R.id.imgSummaryStar5);
        imgStar1 = findViewById(R.id.imgStar1);
        imgStar2 = findViewById(R.id.imgStar2);
        imgStar3 = findViewById(R.id.imgStar3);
        imgStar4 = findViewById(R.id.imgStar4);
        imgStar5 = findViewById(R.id.imgStar5);
        btnWriteReview = findViewById(R.id.btnWriteReview);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);
        View logo = findViewById(R.id.img_logo);
        if (logo != null) {
            logo.setOnClickListener(v -> startActivity(new Intent(this, Homepage.class)));
        }

        btnBack.setOnClickListener(v -> backToMenu());
        if (btnShare != null) {
            btnShare.setOnClickListener(v -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareText = tvDetailName != null && tvDetailName.getText() != null
                        ? tvDetailName.getText().toString()
                        : getString(R.string.product_detail_title);
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.product_detail_share)));
            });
        }
        breadcrumbHome.setOnClickListener(v -> startActivity(new Intent(this, Homepage.class)));
        breadcrumbMenu.setOnClickListener(v -> openMainTab(MainActivity.TAB_MENU, null));
        breadcrumbTeaLatte.setOnClickListener(v ->
                openMainTab(MainActivity.TAB_MENU, getString(R.string.firebase_category_tea_latte)));
        noteToggle.setOnClickListener(v -> toggleCustomization());

        String name = getIntent().getStringExtra("name");
        String priceM = getIntent().getStringExtra("priceM");
        String priceL = getIntent().getStringExtra("priceL");
        String vipM = getIntent().getStringExtra("vipM");
        String vipL = getIntent().getStringExtra("vipL");
        int imageRes = getIntent().getIntExtra("imageRes", 0);
        if (imageRes == 0) {
            imageRes = R.mipmap.logo_ngo_gia;
        }
        float rating = getIntent().getFloatExtra("rating", 4.9f);
        String reviewCount = getIntent().getStringExtra("reviewCount");
        String category = getIntent().getStringExtra("category");

        bindFromIntent(name, category, priceM, priceL, vipM, vipL, imageRes, rating, reviewCount);
        CartBadgeHelper.setup(this);
        if (currentProduct != null) {
            setupThumbs(currentProduct);
        }
        String productId = getIntent().getStringExtra("productId");
        if (productId != null && !productId.isEmpty()) {
            fetchFromFirebaseById(productId);
        } else if (name != null) {
            fetchFromFirebase(name);
        }

        btnQtyMinus.setOnClickListener(v -> {
            quantity = Math.max(1, quantity - 1);
            tvQuantity.setText(String.valueOf(quantity));
        });
        btnQtyPlus.setOnClickListener(v -> {
            quantity += 1;
            tvQuantity.setText(String.valueOf(quantity));
        });

        btnAddToCart.setOnClickListener(v -> CartActions.addItem(this, buildCartItem()));
        btnBuyNow.setOnClickListener(v -> {
            CartActions.addItem(this, buildCartItem());
            startActivity(new Intent(this, Cart.class));
        });

        if (rvRecommended != null) {
            rvRecommended.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvRecommended.setAdapter(new ProductCardAdapter(
                    new ArrayList<>(),
                    R.layout.item_product_card,
                    ProductDetail.this::openRelatedDetail,
                    product -> CartActions.addDefaultProduct(ProductDetail.this, product)
            ));
        }

        setupBottomNavigation();
        setupTabs();
        setupOptionSelections();
        setupToppingRows();
        setupDescriptionInfo(name);
        setupCommitmentContent();
        setupReviewSection();
        applyCustomizationState(false);

        if (tabDescription != null) {
            tabDescription.post(() -> selectTab(tabDescription, false));
        }
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
        if (tvReviewAverage != null) {
            tvReviewAverage.setText("0.0");
        }
        updateSummaryStars(0f);
        updateProgressBars(new int[]{0, 0, 0, 0, 0}, 0);

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

        if (btnSubmitReview != null) {
            btnSubmitReview.setOnClickListener(v -> submitProductReview());
        }

        loadProductReviews();
    }

    private void loadProductReviews() {
        String productId = resolveProductId();
        if (productId == null || productId.isEmpty()) {
            return;
        }

        reviewRepository.getReviewsForProduct(productId, new FirebaseReviewRepository.ReviewsCallback() {
            @Override
            public void onSuccess(List<ProductReview> reviews) {
                if (isFinishing()) {
                    return;
                }
                productReviews = reviews != null ? reviews : new ArrayList<>();
                updateReviewStats(productReviews);
                renderReviewList(productReviews);
            }

            @Override
            public void onError(String message) {
                // Keep default empty state.
            }
        });
    }

    private String resolveProductId() {
        if (currentProduct != null) {
            String id = currentProduct.getId();
            if (id != null && !id.isEmpty()) {
                return id;
            }
        }
        String fromIntent = getIntent().getStringExtra("productId");
        if (fromIntent != null && !fromIntent.isEmpty()) {
            return fromIntent;
        }
        return null;
    }

    private void submitProductReview() {
        if (selectedRating < 1) {
            Toast.makeText(this, R.string.product_detail_review_rating_required, Toast.LENGTH_SHORT).show();
            return;
        }

        String productId = resolveProductId();
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, R.string.product_detail_review_submit_error, Toast.LENGTH_SHORT).show();
            return;
        }

        EditText edtTitle = findViewById(R.id.edtReviewTitle);
        EditText edtComment = findViewById(R.id.edtReviewComment);
        String title = edtTitle != null && edtTitle.getText() != null
                ? edtTitle.getText().toString().trim() : "";
        String comment = edtComment != null && edtComment.getText() != null
                ? edtComment.getText().toString().trim() : "";

        String userName = getReviewerName();
        String userId = UserProfileHelper.getUserId(this);

        ProductReview review = new ProductReview();
        review.setProductId(productId);
        review.setUserId(userId);
        review.setUserName(userName);
        review.setRating((float) selectedRating);
        review.setTitle(title);
        review.setComment(comment);
        review.setCreatedAt(DateTimeHelper.isoNow());

        btnSubmitReview.setEnabled(false);
        reviewRepository.submitReview(review, new FirebaseReviewRepository.SubmitCallback() {
            @Override
            public void onSuccess(ProductReview savedReview) {
                if (isFinishing()) {
                    return;
                }
                btnSubmitReview.setEnabled(true);
                productReviews.add(0, savedReview);
                updateReviewStats(productReviews);
                showReviewSubmitted(savedReview);
                if (layoutReviewForm != null) {
                    layoutReviewForm.setVisibility(View.GONE);
                }
                if (edtTitle != null) {
                    edtTitle.setText("");
                }
                if (edtComment != null) {
                    edtComment.setText("");
                }
                updateStarRating(0);
            }

            @Override
            public void onError(String message) {
                if (isFinishing()) {
                    return;
                }
                btnSubmitReview.setEnabled(true);
                Toast.makeText(ProductDetail.this, R.string.product_detail_review_submit_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showReviewSubmitted(ProductReview review) {
        if (layoutReviewSuccess != null) {
            layoutReviewSuccess.setVisibility(View.VISIBLE);
        }
        if (layoutReviewEmpty != null) {
            layoutReviewEmpty.setVisibility(View.GONE);
        }

        String productName = currentProduct != null && currentProduct.getName() != null
                ? currentProduct.getName()
                : (tvDetailName != null && tvDetailName.getText() != null
                ? tvDetailName.getText().toString() : getString(R.string.product_detail_title));
        if (tvReviewSuccessMessage != null) {
            String escaped = Html.escapeHtml(productName);
            String html = getString(R.string.product_detail_review_success_message, "<b>" + escaped + "</b>");
            tvReviewSuccessMessage.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));
        }

        renderReviewList(productReviews);
        scheduleHideReviewSuccess();
    }

    private void scheduleHideReviewSuccess() {
        if (hideReviewSuccessRunnable != null) {
            reviewUiHandler.removeCallbacks(hideReviewSuccessRunnable);
        }
        hideReviewSuccessRunnable = () -> {
            if (isFinishing()) {
                return;
            }
            if (layoutReviewSuccess != null) {
                layoutReviewSuccess.setVisibility(View.GONE);
            }
        };
        reviewUiHandler.postDelayed(hideReviewSuccessRunnable, REVIEW_SUCCESS_VISIBLE_MS);
    }

    private void renderReviewList(List<ProductReview> reviews) {
        if (layoutReviewList == null) {
            return;
        }

        layoutReviewList.removeAllViews();
        int limit = Math.min(MAX_VISIBLE_REVIEWS, reviews.size());
        if (limit == 0) {
            if (cardReviewList != null) {
                cardReviewList.setVisibility(View.GONE);
            }
            if (layoutReviewEmpty != null) {
                layoutReviewEmpty.setVisibility(View.VISIBLE);
            }
            return;
        }

        if (layoutReviewEmpty != null) {
            layoutReviewEmpty.setVisibility(View.GONE);
        }
        if (cardReviewList != null) {
            cardReviewList.setVisibility(View.VISIBLE);
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < limit; i++) {
            View card = inflater.inflate(R.layout.item_review_card, layoutReviewList, false);
            bindReviewCardView(card, reviews.get(i));
            layoutReviewList.addView(card);
        }

        if (scrollReviewList != null) {
            scrollReviewList.scrollTo(0, 0);
        }
    }

    @Override
    protected void onDestroy() {
        if (hideReviewSuccessRunnable != null) {
            reviewUiHandler.removeCallbacks(hideReviewSuccessRunnable);
        }
        super.onDestroy();
    }

    private void applyVipPricePresentation() {
        VipPriceUiHelper.applyDetailPrices(this,
                tvSizeBadgeM, tvPriceM, tvVipBadgeM, tvVipPriceM,
                tvSizeBadgeL, tvPriceL, tvVipBadgeL, tvVipPriceL);
    }

    private void bindReviewCardView(View card, ProductReview review) {
        if (card == null || review == null) {
            return;
        }

        TextView tvInitial = card.findViewById(R.id.tvReviewInitial);
        TextView tvUserName = card.findViewById(R.id.tvReviewUserName);
        TextView tvDate = card.findViewById(R.id.tvReviewDate);
        TextView tvTitle = card.findViewById(R.id.tvReviewTitle);
        TextView tvComment = card.findViewById(R.id.tvReviewComment);
        LinearLayout starRow = card.findViewById(R.id.layoutReviewCardStars);

        String userName = review.getUserName() != null && !review.getUserName().isEmpty()
                ? review.getUserName()
                : getString(R.string.product_detail_review_guest_name);
        if (tvInitial != null) {
            tvInitial.setText(getInitial(userName));
        }
        if (tvUserName != null) {
            tvUserName.setText(userName);
        }
        if (tvDate != null) {
            tvDate.setText(formatReviewDate(review.getCreatedAt()));
        }

        if (starRow != null) {
            starRow.removeAllViews();
            int activeColor = ContextCompat.getColor(this, R.color.star_rating_active);
            int inactiveColor = ContextCompat.getColor(this, R.color.outline_variant);
            for (int i = 1; i <= 5; i++) {
                ImageView star = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        dpToPx(14), dpToPx(14));
                if (i > 1) {
                    params.setMarginStart(dpToPx(2));
                }
                star.setLayoutParams(params);
                star.setImageResource(R.drawable.ic_star_rating);
                int color = i <= Math.round(review.getRating()) ? activeColor : inactiveColor;
                ImageViewCompat.setImageTintList(star, ColorStateList.valueOf(color));
                starRow.addView(star);
            }
        }

        String title = review.getTitle() != null ? review.getTitle().trim() : "";
        String comment = review.getComment() != null ? review.getComment().trim() : "";
        if (tvTitle != null) {
            if (!title.isEmpty()) {
                tvTitle.setText(title);
                tvTitle.setVisibility(View.VISIBLE);
            } else {
                tvTitle.setVisibility(View.GONE);
            }
        }
        if (tvComment != null) {
            String display = !comment.isEmpty() ? comment : title;
            if (!display.isEmpty()) {
                tvComment.setText("\"" + display + "\"");
                tvComment.setVisibility(View.VISIBLE);
            } else {
                tvComment.setVisibility(View.GONE);
            }
        }
    }

    private void updateReviewStats(List<ProductReview> reviews) {
        int total = reviews != null ? reviews.size() : 0;
        if (total == 0) {
            if (tvReviewAverage != null) {
                tvReviewAverage.setText("0.0");
            }
            updateSummaryStars(0f);
            if (tvReviewCount != null) {
                tvReviewCount.setText(getString(R.string.product_detail_review_count_format, "0"));
            }
            updateProgressBars(new int[]{0, 0, 0, 0, 0}, 0);
            return;
        }

        int[] counts = new int[5];
        double sum = 0;
        for (ProductReview review : reviews) {
            int rating = Math.max(1, Math.min(5, Math.round(review.getRating())));
            counts[rating - 1]++;
            sum += rating;
        }

        float average = (float) (sum / total);
        if (tvReviewAverage != null) {
            tvReviewAverage.setText(String.format(Locale.getDefault(), "%.1f", average));
        }
        updateSummaryStars(average);
        if (tvReviewCount != null) {
            tvReviewCount.setText(getString(R.string.product_detail_review_count_format, formatReviewCount(total)));
        }
        updateHeaderRating(average, total);
        updateProgressBars(counts, total);
    }

    private void updateHeaderRating(float average, int total) {
        if (tvRatingValue != null && total > 0) {
            tvRatingValue.setText(String.format(Locale.getDefault(), "%.1f", average));
        }
        if (tvRatingSummary != null && total > 0) {
            String countText = getString(R.string.product_detail_review_count_format, formatReviewCount(total));
            tvRatingSummary.setText("(" + countText + ")");
        }
    }

    private void updateProgressBars(int[] counts, int total) {
        ProgressBar[] bars = {bar5, bar4, bar3, bar2, bar1};
        for (int i = 0; i < bars.length; i++) {
            if (bars[i] == null) {
                continue;
            }
            int starLevel = 5 - i;
            int count = counts[starLevel - 1];
            int progress = total > 0 ? Math.round(count * 100f / total) : 0;
            bars[i].setProgress(progress);
        }
    }

    private void updateSummaryStars(float average) {
        ImageView[] stars = {
                imgSummaryStar1, imgSummaryStar2, imgSummaryStar3, imgSummaryStar4, imgSummaryStar5
        };
        int activeColor = ContextCompat.getColor(this, R.color.star_rating_active);
        int inactiveColor = ContextCompat.getColor(this, R.color.outline_variant);
        int filledCount = Math.round(average);

        for (int i = 0; i < stars.length; i++) {
            if (stars[i] == null) {
                continue;
            }
            int color = (i + 1) <= filledCount ? activeColor : inactiveColor;
            ImageViewCompat.setImageTintList(stars[i], ColorStateList.valueOf(color));
        }
    }

    private String getReviewerName() {
        String displayName = UserProfileHelper.getDisplayFullName(this);
        if (!displayName.isEmpty()) {
            return displayName;
        }
        return getString(R.string.product_detail_review_guest_name);
    }

    private String getInitial(String name) {
        if (name == null || name.isEmpty()) {
            return "?";
        }
        return name.substring(0, 1).toUpperCase(Locale.getDefault());
    }

    private String formatReviewCount(int count) {
        if (count >= 1000) {
            if (count % 1000 == 0) {
                return (count / 1000) + "k";
            }
            return String.format(Locale.getDefault(), "%.1fk", count / 1000f);
        }
        return String.valueOf(count);
    }

    private String formatReviewDate(String createdAt) {
        String formatted = DateTimeHelper.formatDisplayDate(createdAt, "dd/MM/yyyy");
        if (!formatted.isEmpty()) {
            return formatted;
        }
        return DateTimeHelper.formatDisplayDate(DateTimeHelper.isoNow(), "dd/MM/yyyy");
    }

    private int dpToPx(int dp) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics()));
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
        applyVipPricePresentation();
        bindCustomizationPrice(priceM, priceL);
        tvRatingSummary.setText(getString(
                R.string.product_detail_views_format,
                reviewCount == null ? "1k" : reviewCount));
        tvDescription.setText(getString(R.string.product_detail_description_default));
        imgDetail.setImageResource(imageRes);
        currentProduct = buildProductFromIntent(name, category, priceM, priceL, vipM, vipL, imageRes, rating, reviewCount);
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

    private void fetchFromFirebaseById(String productId) {
        repository.getProductById(productId, new FirebaseProductRepository.ProductCallback() {
            @Override
            public void onSuccess(Product product) {
                if (isFinishing()) {
                    return;
                }
                bindFromProduct(product);
            }

            @Override
            public void onError(String message) {
                String name = getIntent().getStringExtra("name");
                if (name != null && !name.isEmpty()) {
                    fetchFromFirebase(name);
                }
            }
        });
    }

    private void bindFromProduct(Product product) {
        applyResolvedImageRes(product);
        currentProduct = product;
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
        applyVipPricePresentation();
        if (tvRatingValue != null) {
            tvRatingValue.setText(String.format(java.util.Locale.getDefault(), "%.1f", product.getRating()));
        }
        tvRatingSummary.setText(getString(
                R.string.product_detail_views_format,
                product.getReviewCount()));
        ProductImageHelper.load(imgDetail, product);
        setupDescriptionInfo(product.getName());
        setupThumbs(product);
        loadProductReviews();
        if (tabDescription != null) {
            tabDescription.post(() -> selectTab(tabDescription, false));
        }
    }

    private void applyResolvedImageRes(Product product) {
        ProductImageHelper.applyTrustedImageRes(this, product);
    }

    private void setupThumbs(Product product) {
        ProductImageHelper.applyTrustedImageRes(this, product);

        ShapeableImageView[] thumbs = {
                findViewById(R.id.thumb1),
                findViewById(R.id.thumb2),
                findViewById(R.id.thumb3),
                findViewById(R.id.thumb4)
        };

        int resolvedRes = product.getImageRes() != 0
                ? product.getImageRes()
                : ProductImageHelper.resolveLocalRes(this, product);

        List<String> sources = resolvedRes != 0
                ? new ArrayList<>()
                : collectImageSources(product);
        boolean useImageRes = resolvedRes != 0;
        int visibleCount = useImageRes ? 1 : sources.size();
        int fallbackRes = resolvedRes != 0 ? resolvedRes : R.mipmap.logo_ngo_gia;

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
        if (price == null) return "0đ";
        String p = String.valueOf(price);
        if (p.isEmpty() || p.equalsIgnoreCase("null")) {
            return "0đ";
        }

        try {
            String clean = p.replaceAll("[^0-9]", "");
            if (clean.isEmpty()) return p.endsWith("đ") ? p : p + "đ";
            long val = Long.parseLong(clean);
            return String.format(java.util.Locale.US, "%,d", val).replace(',', '.') + "đ";
        } catch (Exception e) {
            return p.endsWith("đ") ? p : p + "đ";
        }
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
        CartBadgeHelper.updateBadge(this);
        UserRoleHelper.refreshRoleFromFirebase(this, this::applyVipPricePresentation);
    }

    private void fetchRecommendedProducts(String category) {
        repository.getProductsByCategory(category, new FirebaseProductRepository.ProductsCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                if (isFinishing()) return;
                List<Product> limited = new ArrayList<>();
                String currentName = tvDetailName != null ? tvDetailName.getText().toString() : "";
                for (Product p : products) {
                    if (p == null || p.getName() == null) {
                        continue;
                    }
                    if (!p.getName().equalsIgnoreCase(currentName)) {
                        limited.add(p);
                    }
                    if (limited.size() >= 6) break;
                }
                if (limited.isEmpty()) {
                    bindRecommendedAdapter(limited);
                    return;
                }
                reviewCatalogSync.syncProducts(limited, new ReviewCatalogSync.Callback() {
                    @Override
                    public void onReady(Map<String, ReviewStatsHelper.Stats> statsByProductId) {
                        if (isFinishing()) {
                            return;
                        }
                        ReviewStatsHelper.applyStatsToProducts(limited, statsByProductId);
                        bindRecommendedAdapter(limited);
                    }

                    @Override
                    public void onError(String message) {
                        if (!isFinishing()) {
                            bindRecommendedAdapter(limited);
                        }
                    }
                });
            }

            @Override
            public void onError(String message) {
                bindRecommendedAdapter(getRecommendedProducts());
            }
        });
    }

    private void bindRecommendedAdapter(List<Product> products) {
        RecyclerView rvRecommended = findViewById(R.id.rvRecommended);
        if (rvRecommended != null) {
            rvRecommended.setAdapter(new ProductCardAdapter(
                    products,
                    R.layout.item_product_card,
                    ProductDetail.this::openRelatedDetail,
                    product -> CartActions.addDefaultProduct(ProductDetail.this, product)
            ));
        }
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
        if (product.getId() != null && !product.getId().isEmpty()) {
            intent.putExtra("productId", product.getId());
        }
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
                Intent intent = new Intent(this, Homepage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            } else if (id == R.id.nav_menu) {
                openMainTab(MainActivity.TAB_MENU, null);
                overridePendingTransition(0, 0);
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

        if (navMenu != null) {
            NavBarHelper.updateItemState(this, navMenu, true);
        }
    }

    private void setupTabs() {
        if (tabDescription == null || tabReview == null || tabCommitment == null) {
            return;
        }
        tabDescription.setOnClickListener(v -> selectTab(tabDescription));
        tabReview.setOnClickListener(v -> selectTab(tabReview));
        tabCommitment.setOnClickListener(v -> selectTab(tabCommitment));
    }

    private void selectTab(TextView selectedTab) {
        selectTab(selectedTab, true);
    }

    private void selectTab(TextView selectedTab, boolean animateIndicator) {
        if (tabDescription == null || tabReview == null || tabCommitment == null) {
            return;
        }
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
        alignIndicatorToTab(selectedTab, animateIndicator);
    }

    private void alignIndicatorToTab(TextView target, boolean animate) {
        if (tabIndicator == null || tabIndicatorContainer == null || target == null) {
            return;
        }

        Runnable positionIndicator = () -> {
            if (target.getWidth() <= 0) {
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
            if (animate) {
                tabIndicator.animate().x(x).setDuration(180).start();
            } else {
                tabIndicator.animate().cancel();
                tabIndicator.setX(x);
            }
        };

        if (target.getWidth() > 0) {
            target.post(positionIndicator);
            return;
        }

        target.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (target.getWidth() <= 0) {
                    return;
                }
                target.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                positionIndicator.run();
            }
        });
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
        toppingUnitPrices.clear();
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
        int unitPrice = ToppingPriceHelper.getPrice(name);
        if (unitPrice <= 0) {
            unitPrice = parseToppingPriceLabel(price);
        }
        toppingUnitPrices.put(name, unitPrice);
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

    private CartItem buildCartItem() {
        CartItem item = currentProduct != null
                ? CartItem.fromProduct(this, currentProduct)
                : new CartItem();

        if ((item.getProductName() == null || item.getProductName().isEmpty()) && tvDetailName != null) {
            item.setProductName(tvDetailName.getText().toString());
        }

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
            if (entry.getValue() != null && entry.getValue() > 0) {
                int toppingPrice = toppingUnitPrices.containsKey(entry.getKey())
                        ? toppingUnitPrices.get(entry.getKey())
                        : ToppingPriceHelper.getPrice(entry.getKey());
                item.addTopping(entry.getKey(), toppingPrice, entry.getValue());
            }
        }
        return item;
    }

    private int parseToppingPriceLabel(String label) {
        if (label == null) {
            return 0;
        }
        try {
            String digits = label.replaceAll("[^0-9]", "");
            return digits.isEmpty() ? 0 : Integer.parseInt(digits);
        } catch (Exception e) {
            return 0;
        }
    }

    private Product buildProductFromIntent(String name, String category, String priceM, String priceL,
                                           String vipM, String vipL, int imageRes, float rating, String reviewCount) {
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
        String productId = getIntent().getStringExtra("productId");
        if (productId != null && !productId.isEmpty()) {
            product.setId(productId);
        }
        return product;
    }

    private int parsePriceInt(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }
        try {
            String clean = value.replaceAll("[^0-9]", "");
            return clean.isEmpty() ? 0 : Integer.parseInt(clean);
        } catch (Exception e) {
            return 0;
        }
    }

    private String getSelectedSize() {
        if (btnSizeOptionL != null && btnSizeOptionL.isSelected()) {
            return "L";
        }
        return "M";
    }

    private String getSelectedSugar() {
        if (btnSweetnessLess != null && btnSweetnessLess.isSelected()) {
            return btnSweetnessLess.getText().toString();
        }
        if (btnSweetnessHigh != null && btnSweetnessHigh.isSelected()) {
            return btnSweetnessHigh.getText().toString();
        }
        if (btnSweetnessNormal != null) {
            return btnSweetnessNormal.getText().toString();
        }
        return getString(R.string.product_detail_level_medium);
    }

    private String getSelectedIce() {
        if (btnIceLess != null && btnIceLess.isSelected()) {
            return btnIceLess.getText().toString();
        }
        if (btnIceHigh != null && btnIceHigh.isSelected()) {
            return btnIceHigh.getText().toString();
        }
        if (btnIceNormal != null) {
            return btnIceNormal.getText().toString();
        }
        return getString(R.string.product_detail_level_medium);
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