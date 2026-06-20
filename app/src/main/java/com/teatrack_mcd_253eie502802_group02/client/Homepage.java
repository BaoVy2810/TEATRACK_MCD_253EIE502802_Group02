package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.teatrack_mcd_253eie502802_group02.MainActivity;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.adapter.BannerAdapter;
import com.teatrack_mcd_253eie502802_group02.adapter.NewsCardAdapter;
import com.teatrack_mcd_253eie502802_group02.adapter.ProductCardAdapter;
import com.teatrack_mcd_253eie502802_group02.adapter.PromotionAdapter;
import com.teatrack_mcd_253eie502802_group02.data.FirebaseProductRepository;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.model.NewsItem;
import com.teatrack_mcd_253eie502802_group02.model.Product;
import com.teatrack_mcd_253eie502802_group02.model.Promotion;
import com.teatrack_mcd_253eie502802_group02.shared.ui.CartBadgeHelper;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;
import com.teatrack_mcd_253eie502802_group02.util.CartActions;

import com.teatrack_mcd_253eie502802_group02.R;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;

public class Homepage extends BaseActivity {
    private static final int[] NAV_IDS = {
            R.id.nav_home,
            R.id.nav_menu,
            R.id.nav_orders,
            R.id.nav_promotion,
            R.id.nav_profile
    };
    private final FirebaseProductRepository productRepository = new FirebaseProductRepository();

    private ViewPager2 viewPagerBanners;
    private View dot1;
    private View dot2;
    private View dot3;
    private ImageView imgBannerPreview;
    private RecyclerView rvFeaturedProducts;
    private RecyclerView rvPromotions;
    private RecyclerView rvNews;
    private ProductCardAdapter featuredAdapter;
    private final List<Product> featuredProducts = new ArrayList<>();
    private final List<NewsItem> newsItems = new ArrayList<>();
    private NewsCardAdapter newsAdapter;
    private final android.os.Handler bannerHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable bannerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homepage);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        bindViews();
        setupBottomNav();
        setupBanners();
        setupFeaturedProducts();
        setupPromotions();
        setupNews();
        setupCategoryActions();
        setupStoryAction();
        CartBadgeHelper.setup(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoScroll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAutoScroll();
        CartBadgeHelper.updateBadge(this);
    }

    private void startAutoScroll() {
        if (bannerRunnable == null || viewPagerBanners == null) return;
        bannerHandler.removeCallbacks(bannerRunnable);
        bannerHandler.postDelayed(bannerRunnable, 5000);
    }

    private void stopAutoScroll() {
        if (bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }

    private void bindViews() {
        viewPagerBanners = findViewById(R.id.viewPagerBanners);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        imgBannerPreview = findViewById(R.id.imgBannerPreview);
        rvFeaturedProducts = findViewById(R.id.layoutFeaturedProducts);
        rvPromotions = findViewById(R.id.layoutPromotions);
        rvNews = findViewById(R.id.layoutNews);
        View logo = findViewById(R.id.img_logo);
        if (logo != null) {
            logo.setOnClickListener(v -> startActivity(new Intent(this, Homepage.class)));
        }
    }

    private void setupBottomNav() {
        NavBarHelper.setupNavBar(this, NAV_IDS, R.id.nav_home, this::onNavItemClicked);
    }

    private void onNavItemClicked(View view) {
        int id = view.getId();
        if (id == R.id.nav_home) {
            return;
        }
        if (id == R.id.nav_menu) {
            startActivity(new Intent(this, Menu.class));
        } else if (id == R.id.nav_orders) {
            startActivity(new Intent(this, OrderHistory.class));
        } else if (id == R.id.nav_promotion) {
            startActivity(new Intent(this, BlogGeneral.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, UserProfile.class));
        }
    }

    private void setupBanners() {
        if (viewPagerBanners == null || dot1 == null || dot2 == null || dot3 == null) {
            return;
        }
        List<Integer> banners = new ArrayList<>();
        banners.add(R.mipmap.banner1);
        if (resourceExists("banner2")) {
            banners.add(R.mipmap.banner2);
        }
        if (resourceExists("banner3")) {
            banners.add(R.mipmap.banner3);
        }
        if (banners.isEmpty()) {
            return;
        }

        BannerAdapter adapter = new BannerAdapter(banners);
        adapter.setOnItemClickListener(position -> {
            // Navigate to BlogDetail with dummy data
            Intent intent = new Intent(this, BlogDetail.class);
            intent.putExtra("blog_id", "banner_blog_" + position);
            startActivity(intent);
        });
        viewPagerBanners.setAdapter(adapter);

        if (imgBannerPreview != null) {
            imgBannerPreview.setVisibility(View.GONE);
        }

        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                int current = viewPagerBanners.getCurrentItem();
                int next = (current + 1) % banners.size();
                viewPagerBanners.setCurrentItem(next, true);
                bannerHandler.postDelayed(this, 5000);
            }
        };

        viewPagerBanners.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateBannerDots(position);
                // Reset timer when user swipes manually
                bannerHandler.removeCallbacks(bannerRunnable);
                bannerHandler.postDelayed(bannerRunnable, 5000);
            }
        });
        updateBannerDots(0);
        startAutoScroll();
    }

    private void updateBannerDots(int position) {
        int active = ContextCompat.getColor(this, R.color.brand_blue);
        int inactive = ContextCompat.getColor(this, R.color.outline_variant);
        dot1.setBackgroundTintList(android.content.res.ColorStateList.valueOf(position == 0 ? active : inactive));
        dot2.setBackgroundTintList(android.content.res.ColorStateList.valueOf(position == 1 ? active : inactive));
        dot3.setBackgroundTintList(android.content.res.ColorStateList.valueOf(position == 2 ? active : inactive));
    }

    private void setupFeaturedProducts() {
        if (rvFeaturedProducts == null) {
            return;
        }
        rvFeaturedProducts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        featuredAdapter = new ProductCardAdapter(
                featuredProducts,
                this::openProductDetail,
                this::onAddToCart
        );
        rvFeaturedProducts.setAdapter(featuredAdapter);
        loadFeaturedProducts();
    }

    private void loadFeaturedProducts() {
        String category = getString(R.string.firebase_category_tea_latte);
        productRepository.getProductsByCategory(category, new FirebaseProductRepository.ProductsCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                List<Product> source = products == null || products.isEmpty()
                        ? getFallbackCategoryProducts("Tea Latte")
                        : products;
                featuredProducts.clear();
                featuredProducts.addAll(source);
                featuredAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String message) {
                featuredProducts.clear();
                featuredProducts.addAll(getFallbackCategoryProducts("Tea Latte"));
                featuredAdapter.notifyDataSetChanged();
            }
        });
    }

    private void onAddToCart(Product product) {
        CartActions.addDefaultProduct(this, product);
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

    private void setupPromotions() {
        if (rvPromotions == null) {
            return;
        }
        rvPromotions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        List<Promotion> promotions = new ArrayList<>();
        promotions.add(new Promotion(R.mipmap.banner_monmoi));
        promotions.add(new Promotion(R.mipmap.banner_aboutus));
        promotions.add(new Promotion(R.mipmap.banner_aboutus2));
        rvPromotions.setAdapter(new PromotionAdapter(promotions));
    }

    private void setupNews() {
        if (rvNews == null) {
            return;
        }
        rvNews.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        newsAdapter = new NewsCardAdapter(newsItems);
        rvNews.setAdapter(newsAdapter);
        loadNews();
    }

    private void setupCategoryActions() {
        bindCategoryClick(R.id.categoryPureTea, getString(R.string.firebase_category_pure_tea));
        bindCategoryClick(R.id.categoryTeaLatte, getString(R.string.firebase_category_tea_latte));
        bindCategoryClick(R.id.categoryMilkTea, getString(R.string.firebase_category_milk_tea));
        bindCategoryClick(R.id.categoryNewArrivals, getString(R.string.firebase_category_new_arrivals));
        bindCategoryClick(R.id.categoryBestSellers, getString(R.string.firebase_category_best_sellers));
        bindCategoryClick(R.id.categoryFruitTea, getString(R.string.firebase_category_fruit_tea));
    }

    private void bindCategoryClick(int viewId, String firebaseCategory) {
        View categoryView = findViewById(viewId);
        if (categoryView == null) {
            return;
        }
        categoryView.setOnClickListener(v -> {
            Intent intent = new Intent(this, Menu.class);
            intent.putExtra(MainActivity.EXTRA_MENU_CATEGORY, firebaseCategory);
            startActivity(intent);
        });
    }

    private List<Product> getFallbackCategoryProducts(String fallbackCategory) {
        Map<String, List<Product>> map = CategoryProductData.getCategoryProductsMap();
        List<Product> products = map.get(fallbackCategory);
        return products != null ? products : new ArrayList<>();
    }

    private void setupStoryAction() {
        View btnStory = findViewById(R.id.btnNgoGiaStory);
        if (btnStory != null) {
            btnStory.setOnClickListener(v -> startActivity(new Intent(this, AboutUsActivity.class)));
        }
    }

    private void loadNews() {
        DatabaseReference blogsRef = FirebaseDatabase.getInstance()
                .getReference(getString(R.string.firebase_collection_blogs));
        blogsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<NewsItem> loaded = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String title = firstNonEmpty(
                            readField(child, "heading"),
                            readField(child, getString(R.string.firebase_field_title)));
                    String date = readField(child, getString(R.string.firebase_field_date));
                    String imageField = firstNonEmpty(
                            readField(child, "thumbnailImage"),
                            readField(child, getString(R.string.firebase_field_image)),
                            readFirstImage(child));
                    if (title == null || title.isEmpty()) {
                        continue;
                    }
                    int imageRes = resolveImageRes(imageField);
                    loaded.add(new NewsItem(
                            title,
                            (date == null || date.isEmpty()) ? "--" : date,
                            imageRes));
                    if (loaded.size() >= 10) {
                        break;
                    }
                }
                if (loaded.isEmpty()) {
                    loaded = getFallbackNews();
                }
                newsItems.clear();
                newsItems.addAll(loaded);
                if (newsAdapter != null) {
                    newsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                newsItems.clear();
                newsItems.addAll(getFallbackNews());
                if (newsAdapter != null) {
                    newsAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private List<NewsItem> getFallbackNews() {
        List<NewsItem> fallback = new ArrayList<>();
        fallback.add(new NewsItem("Vẹn tròn trung thu - Trọn vị Ngô Gia", "26-31.07.25", R.mipmap.blog_1_2));
        fallback.add(new NewsItem("Bí quyết chọn trà hợp gu mỗi ngày", "10-17.08.25", R.mipmap.blog_3_1));
        fallback.add(new NewsItem("Món mới mùa hè đã lên kệ", "01-08.09.25", R.mipmap.blog_3_2));
        return fallback;
    }

    private String readField(DataSnapshot snapshot, String key) {
        Object value = snapshot.child(key).getValue();
        return value == null ? null : String.valueOf(value);
    }

    private String readFirstImage(DataSnapshot snapshot) {
        DataSnapshot first = snapshot.child("images").child("0");
        Object value = first.getValue();
        return value == null ? null : String.valueOf(value);
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    private int resolveImageRes(String source) {
        if (source == null || source.isEmpty()) {
            return R.mipmap.blog_1_2;
        }
        String name = source;
        int slash = name.lastIndexOf('/');
        if (slash >= 0 && slash < name.length() - 1) {
            name = name.substring(slash + 1);
        }
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            name = name.substring(0, dot);
        }
        int mipmapRes = getResources().getIdentifier(name, "mipmap", getPackageName());
        if (mipmapRes != 0) {
            return mipmapRes;
        }
        int drawableRes = getResources().getIdentifier(name, "drawable", getPackageName());
        return drawableRes != 0 ? drawableRes : R.mipmap.blog_1_2;
    }

    private boolean resourceExists(String mipmapName) {
        return getResources().getIdentifier(mipmapName, "mipmap", getPackageName()) != 0;
    }

    private void safeInit(Runnable action) {
        try {
            action.run();
        } catch (Exception ignored) {
            // Keep home screen usable even if one section fails.
        }
    }
}
