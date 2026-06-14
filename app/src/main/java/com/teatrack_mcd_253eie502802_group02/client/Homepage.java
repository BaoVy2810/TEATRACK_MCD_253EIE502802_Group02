package com.teatrack_mcd_253eie502802_group02.client;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.adapter.BannerAdapter;
import com.teatrack_mcd_253eie502802_group02.adapter.ProductCardAdapter;
import com.teatrack_mcd_253eie502802_group02.adapter.PromotionAdapter;
import com.teatrack_mcd_253eie502802_group02.adapter.NewsCardAdapter;
import com.teatrack_mcd_253eie502802_group02.model.NewsItem;
import com.teatrack_mcd_253eie502802_group02.model.Product;
import com.teatrack_mcd_253eie502802_group02.model.Promotion;

import java.util.ArrayList;
import java.util.List;

public class Homepage extends AppCompatActivity {

    private ViewPager2 viewPagerBanners;
    private BannerAdapter bannerAdapter;
    private View[] dots;

    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;
    private static final long BANNER_INTERVAL_MS = 5000L;

    private RecyclerView rvFeaturedProducts;
    private RecyclerView rvPromotions;
    private RecyclerView rvNews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        setupBanners();
        setupFeaturedProducts();
        setupPromotions();
        setupNews();
    }
    private void setupBanners() {
        viewPagerBanners = findViewById(R.id.viewPagerBanners);

        dots = new View[]{
                findViewById(R.id.dot1),
                findViewById(R.id.dot2),
                findViewById(R.id.dot3)
        };

        List<Integer> bannerImages = new ArrayList<>();
        bannerImages.add(R.mipmap.banner1);
        bannerImages.add(R.mipmap.banner2);
        bannerImages.add(R.mipmap.banner3);

        bannerAdapter = new BannerAdapter(bannerImages);
        viewPagerBanners.setAdapter(bannerAdapter);

        updateDots(0);

        viewPagerBanners.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateDots(position);
            }
        });

        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                int itemCount = bannerAdapter.getItemCount();
                if (itemCount == 0) return;
                int next = (viewPagerBanners.getCurrentItem() + 1) % itemCount;
                viewPagerBanners.setCurrentItem(next, true);
                bannerHandler.postDelayed(this, BANNER_INTERVAL_MS);
            }
        };
        bannerHandler.postDelayed(bannerRunnable, BANNER_INTERVAL_MS);
    }

    private void updateDots(int position) {
        int activeColor = ContextCompat.getColor(this, R.color.brand_blue);
        int inactiveColor = android.graphics.Color.parseColor("#D9D9D9");

        for (int i = 0; i < dots.length; i++) {
            int color = (i == position) ? activeColor : inactiveColor;
            dots[i].getBackground().mutate().setTint(color);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        bannerHandler.removeCallbacks(bannerRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bannerHandler.postDelayed(bannerRunnable, BANNER_INTERVAL_MS);
    }

    private void setupFeaturedProducts() {
        rvFeaturedProducts = findViewById(R.id.layoutFeaturedProducts);
        rvFeaturedProducts.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<Product> products = new ArrayList<>();
        products.add(new Product("Trà xanh bí đao", R.mipmap.traxanhbidao, 4.9f, "1k", "16.000", "19.000", "16.000", "19.000"));
        products.add(new Product("Trà xanh sữa", R.mipmap.traxanhsua, 4.9f, "1k", "21.000", "25.000", "21.000", "25.000"));
        products.add(new Product("Hồng trà đài loan", R.mipmap.hongtradailoan, 4.9f, "1k", "13.000", "16.000", "13.000", "16.000"));
        products.add(new Product("Trà bí đao", R.mipmap.trabidaongogia, 4.9f, "1k", "16.000", "19.000", "16.000", "19.000"));
        products.add(new Product("Trà ô long đào", R.mipmap.traolongdao, 4.9f, "1k", "18.000", "22.000", "18.000", "22.000"));
        products.add(new Product("Trà sữa trân châu", R.mipmap.trasuatranchauduongden, 4.9f, "1k", "20.000", "24.000", "20.000", "24.000"));

        rvFeaturedProducts.setAdapter(new ProductCardAdapter(products));
    }

    private void setupPromotions() {
        rvPromotions = findViewById(R.id.layoutPromotions);
        rvPromotions.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<Promotion> promotions = new ArrayList<>();
        promotions.add(new Promotion(R.mipmap.blog_5));
        promotions.add(new Promotion(R.mipmap.mauhonglangman));
        promotions.add(new Promotion(R.mipmap.blog_14));

        rvPromotions.setAdapter(new PromotionAdapter(promotions));
    }

    private void setupNews() {
        rvNews = findViewById(R.id.layoutNews);
        rvNews.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<NewsItem> newsItems = new ArrayList<>();
        newsItems.add(new NewsItem("NHÂM NHI TRÀ NGON\nNHẬN NGAY KHÓA XINH", "01 - 02.05.2026", R.mipmap.blog_6));
        newsItems.add(new NewsItem("CHÚC MỪNG NGÀY\nPHỤ NỮ VIỆT NAM 20.10.2025", "19 - 20.10.2025", R.mipmap.blog_7));
        newsItems.add(new NewsItem("TRÀ MÁT - GẤU XỊN", "15 - 16.01.2026", R.mipmap.blog_8));
        newsItems.add(new NewsItem("BLOG 4 TITLE", "DATE RANGE 4", R.mipmap.blog_9));
        newsItems.add(new NewsItem("BLOG 5 TITLE", "DATE RANGE 5", R.mipmap.blog_10));
        newsItems.add(new NewsItem("BLOG 6 TITLE", "DATE RANGE 6", R.mipmap.blog_11));

        rvNews.setAdapter(new NewsCardAdapter(newsItems));
    }
}
