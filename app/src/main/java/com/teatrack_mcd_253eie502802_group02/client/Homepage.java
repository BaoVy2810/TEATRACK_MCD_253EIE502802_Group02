package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Homepage extends AppCompatActivity {

    private static final int[] NAV_ITEM_IDS = {
            R.id.nav_home,
            R.id.nav_menu,
            R.id.nav_orders,
            R.id.nav_promotion,
            R.id.nav_profile
    };

    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homepage);
        inflater = LayoutInflater.from(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupHeader();
        setupNavBar();
        bindHomepage();
    }

    private void setupHeader() {
        findViewById(R.id.btn_cart).setOnClickListener(v -> startActivity(new Intent(this, Cart.class)));
        findViewById(R.id.btn_profile).setOnClickListener(v -> startActivity(new Intent(this, UserProfile.class)));
    }

    private void setupNavBar() {
        NavBarHelper.setupNavBar(this, NAV_ITEM_IDS, R.id.nav_home, this::onNavItemClicked);
    }

    private void onNavItemClicked(View view) {
        int id = view.getId();
        Class<?> destination = null;

        if (id == R.id.nav_home) {
            destination = Homepage.class;
        } else if (id == R.id.nav_menu) {
            destination = Menu.class;
        } else if (id == R.id.nav_orders) {
            destination = OrderHistory.class;
        } else if (id == R.id.nav_promotion) {
            destination = BlogGeneral.class;
        } else if (id == R.id.nav_profile) {
            destination = UserProfile.class;
        }

        if (destination != null && destination != getClass()) {
            startActivity(new Intent(this, destination));
        }
    }

    private void bindHomepage() {
        bindBanners();
        bindCategories();
        bindProducts();
        bindPromotions();
        bindNews();
    }

    private void bindBanners() {
        LinearLayout bannerLayout = findViewById(R.id.layoutBanners);
        bannerLayout.removeAllViews();
        List<Integer> banners = Arrays.asList(
                R.mipmap.banner1,
                R.mipmap.banner_monmoi,
                R.mipmap.banner2
        );

        for (int bannerRes : banners) {
            View banner = inflater.inflate(R.layout.item_banner, bannerLayout, false);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(360), dp(142));
            params.setMargins(dp(2), 0, dp(10), 0);
            banner.setLayoutParams(params);

            ImageView image = banner.findViewById(R.id.imgBanner);
            image.setImageResource(bannerRes);
            image.setContentDescription(getString(R.string.home_exciting_promotions));
            bannerLayout.addView(banner);
        }

        LinearLayout dots = findViewById(R.id.layoutDots);
        for (int i = 0; i < banners.size(); i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(7), dp(7));
            params.setMargins(dp(5), dp(6), dp(5), 0);
            dot.setLayoutParams(params);
            dot.setBackground(circleDrawable(i == 0 ? R.color.brand_blue : R.color.white));
            dots.addView(dot);
        }
    }

    private void bindCategories() {
        LinearLayout categoryLayout = findViewById(R.id.layoutBeverageCategories);
        categoryLayout.removeAllViews();
        List<CategoryItem> categories = Arrays.asList(
                new CategoryItem("Pure Tea", R.mipmap.drink_1),
                new CategoryItem("Tea Latte", R.mipmap.tra_latte),
                new CategoryItem("Milk Tea", R.mipmap.tra_sua),
                new CategoryItem("New Arrivals", R.mipmap.thuc_uong_moi),
                new CategoryItem("Best Sellers", R.mipmap.hong_tra_trung_khung_long),
                new CategoryItem("Fruit Tea", R.mipmap.tra_yakult)
        );

        for (CategoryItem item : categories) {
            View category = inflater.inflate(R.layout.item_beverage_category, categoryLayout, false);
            ImageView image = category.findViewById(R.id.imgCategory);
            TextView label = category.findViewById(R.id.tvCategoryName);

            image.setImageResource(item.imageRes);
            image.setContentDescription(item.label);
            label.setText(item.label);
            categoryLayout.addView(category);
        }
    }

    private void bindProducts() {
        LinearLayout productLayout = findViewById(R.id.layoutFeaturedProducts);
        productLayout.removeAllViews();
        List<ProductItem> products = Arrays.asList(
                new ProductItem("NG03", "Tra xanh bi dao", R.mipmap.traxanhbidao, 19000, 22000, 16000, 19000, 4.9),
                new ProductItem("NG12", "Tra xanh sua", R.mipmap.traxanhsua, 24000, 27000, 21000, 24000, 4.9),
                new ProductItem("NG07", "Hong tra dai loan", R.mipmap.hongtradailoan, 16000, 19000, 13000, 16000, 4.9),
                new ProductItem("NG31", "Tra bi dao chanh", R.mipmap.trabidaochanh, 24000, 27000, 21000, 24000, 4.9),
                new ProductItem("NG33", "Hong tra trung khung long", R.mipmap.hongtratrungkhunglong, 26000, 29000, 23000, 26000, 4.8)
        );

        for (ProductItem item : products) {
            productLayout.addView(createProductCard(item));
        }
    }

    private void bindPromotions() {
        LinearLayout promoLayout = findViewById(R.id.layoutPromotions);
        promoLayout.removeAllViews();
        List<MediaItem> promos = Arrays.asList(
                new MediaItem("NGOGIAFS", "Giam 10.000d cho don tu 60.000d", R.mipmap.banner2),
                new MediaItem("NGOVIP15", "Giam 15% toi da 40.000d", R.mipmap.blog_5),
                new MediaItem("NGOGIA20", "Giam 20% cho don hang bat ki", R.mipmap.blog_9)
        );

        for (MediaItem item : promos) {
            promoLayout.addView(createMediaCard(item, false));
        }
    }

    private void bindNews() {
        LinearLayout newsLayout = findViewById(R.id.layoutNews);
        newsLayout.removeAllViews();
        List<MediaItem> news = Arrays.asList(
                new MediaItem("01 - 02.05.2026", "NHAM NHI TRA NGON NHAN NGAY KHOA XINH", R.mipmap.blog_6),
                new MediaItem("19 - 20.10.2025", "CHUC MUNG NGAY PHU NU VIET NAM 20.10.2025", R.mipmap.blog_5),
                new MediaItem("15 - 16.01.2026", "TRA MAT - GAU XINH - VUI HET MINH", R.mipmap.blog_12)
        );

        for (MediaItem item : news) {
            newsLayout.addView(createMediaCard(item, true));
        }
    }

    private View createProductCard(ProductItem item) {
        View card = inflater.inflate(R.layout.item_product_card, findViewById(R.id.layoutFeaturedProducts), false);

        ImageView image = card.findViewById(R.id.imgProduct);
        TextView name = card.findViewById(R.id.tvProductName);
        TextView rating = card.findViewById(R.id.tvRating);
        TextView reviews = card.findViewById(R.id.tvReviews);
        TextView sizeM = card.findViewById(R.id.tvSizeM);
        TextView priceM = card.findViewById(R.id.tvPriceM);
        TextView sizeL = card.findViewById(R.id.tvSizeL);
        TextView priceL = card.findViewById(R.id.tvPriceL);
        TextView vipPriceM = card.findViewById(R.id.tvVipPriceM);
        TextView vipPriceL = card.findViewById(R.id.tvVipPriceL);

        image.setImageResource(item.imageRes);
        image.setContentDescription(item.name);
        name.setText(item.name);
        rating.setText(String.valueOf(item.rating));
        reviews.setText("  1k  " + getString(R.string.home_rating_suffix));
        sizeM.setText(getString(R.string.home_price_medium));
        priceM.setText(formatPrice(item.priceM));
        sizeL.setText(getString(R.string.home_price_large));
        priceL.setText(formatPrice(item.priceL));
        vipPriceM.setText(formatPrice(item.vipPriceM));
        vipPriceL.setText(formatPrice(item.vipPriceL));

        return card;
    }

    private View createMediaCard(MediaItem item, boolean showText) {
        int layoutRes = showText ? R.layout.item_news_card : R.layout.item_promotion_banner;
        View card = inflater.inflate(layoutRes, showText ? findViewById(R.id.layoutNews) : findViewById(R.id.layoutPromotions), false);

        if (showText) {
            ImageView image = card.findViewById(R.id.imgNews);
            TextView title = card.findViewById(R.id.tvNewsTitle);
            TextView date = card.findViewById(R.id.tvNewsDate);

            image.setImageResource(item.imageRes);
            image.setContentDescription(item.title);
            title.setText(item.title);
            date.setText(item.subtitle);
        } else {
            ImageView image = card.findViewById(R.id.imgPromotion);
            image.setImageResource(item.imageRes);
            image.setContentDescription(item.title);
        }

        return card;
    }

    private GradientDrawable circleDrawable(int colorRes) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(ContextCompat.getColor(this, colorRes));
        return drawable;
    }

    private String formatPrice(int price) {
        return currencyFormat.format(price);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static final class CategoryItem {
        final String label;
        final int imageRes;

        CategoryItem(String label, int imageRes) {
            this.label = label;
            this.imageRes = imageRes;
        }
    }

    private static final class ProductItem {
        final String id;
        final String name;
        final int imageRes;
        final int priceM;
        final int priceL;
        final int vipPriceM;
        final int vipPriceL;
        final double rating;

        ProductItem(String id, String name, int imageRes, int priceM, int priceL, int vipPriceM, int vipPriceL, double rating) {
            this.id = id;
            this.name = name;
            this.imageRes = imageRes;
            this.priceM = priceM;
            this.priceL = priceL;
            this.vipPriceM = vipPriceM;
            this.vipPriceL = vipPriceL;
            this.rating = rating;
        }
    }

    private static final class MediaItem {
        final String subtitle;
        final String title;
        final int imageRes;

        MediaItem(String subtitle, String title, int imageRes) {
            this.subtitle = subtitle;
            this.title = title;
            this.imageRes = imageRes;
        }
    }
}
