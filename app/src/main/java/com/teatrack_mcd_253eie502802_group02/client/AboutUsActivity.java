package com.teatrack_mcd_253eie502802_group02.client;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.VideoView;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.shared.ui.CartBadgeHelper;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;

import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;

public class AboutUsActivity extends BaseActivity {

    private VideoView videoView;
    private ScrollView scrollView;

    private LinearLayout missionOverlay;
    private boolean missionAnimDone = false;

    private LinearLayout visionOverlay;
    private boolean visionAnimDone = false;

    private LinearLayout mosaicCol1, mosaicCol2, mosaicCol3, mosaicCol4, mosaicCol5;
    private boolean mosaicAnimStarted = false;

    private TextView tvGreetGhostTop, tvGreetGhostBot;
    private boolean greetAnimStarted = false;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        bindViews();
        setupHeader();
        setupNavBar();
        setupVideo();
        setupScrollListener();

        missionOverlay.setTranslationX(-3000f);
        missionOverlay.setAlpha(0f);

        visionOverlay.setScaleX(0.3f);
        visionOverlay.setScaleY(0.3f);
        visionOverlay.setAlpha(0f);
    }

    private void bindViews() {
        scrollView      = findViewById(R.id.scrollView);
        videoView       = findViewById(R.id.videoView);
        missionOverlay  = findViewById(R.id.missionOverlay);
        visionOverlay   = findViewById(R.id.visionOverlay);
        mosaicCol1      = findViewById(R.id.mosaicCol1);
        mosaicCol2      = findViewById(R.id.mosaicCol2);
        mosaicCol3      = findViewById(R.id.mosaicCol3);
        mosaicCol4      = findViewById(R.id.mosaicCol4);
        mosaicCol5      = findViewById(R.id.mosaicCol5);
        tvGreetGhostTop = findViewById(R.id.tvGreetGhostTop);
        tvGreetGhostBot = findViewById(R.id.tvGreetGhostBot);
    }

    private void setupHeader() {
        CartBadgeHelper.setup(this);

        findViewById(R.id.btn_profile).setOnClickListener(v ->
                startActivity(new Intent(this, UserProfile.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        CartBadgeHelper.updateBadge(this);
        if (videoView != null && !videoView.isPlaying()) videoView.start();
    }

    private void setupNavBar() {
        int[] navItemIds = {
                R.id.nav_home,
                R.id.nav_menu,
                R.id.nav_orders,
                R.id.nav_promotion,
                R.id.nav_profile
        };

        NavBarHelper.setupNavBar(this, navItemIds, -1, v -> {
            int id = v.getId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, Homepage.class));
            } else if (id == R.id.nav_menu) {
                startActivity(new Intent(this, Menu.class));
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, OrderHistory.class));
            } else if (id == R.id.nav_promotion) {
                Toast.makeText(this, R.string.str_coming_soon, Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, UserProfile.class));
            }
        });
    }

    private void setupVideo() {
        Uri videoUri = Uri.parse(
                "android.resource://" + getPackageName() + "/" + R.raw.video
        );
        videoView.setVideoURI(videoUri);
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            mp.setVolume(0f, 0f);
            mp.start();
        });
        videoView.setOnErrorListener((mp, what, extra) -> {
            videoView.setVisibility(View.GONE);
            return true;
        });
        videoView.start();
    }


    private void setupScrollListener() {
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            checkMissionVisible();
            checkVisionVisible();
            checkMosaicVisible();
            checkGreetVisible();
        });

        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        checkMissionVisible();
                        checkVisionVisible();
                        checkMosaicVisible();
                        checkGreetVisible();
                    }
                }
        );
    }

    private boolean isViewVisible(View view) {
        if (view == null) return false;
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        int viewTop    = loc[1];
        int viewBot    = loc[1] + view.getHeight();
        int screenH    = getResources().getDisplayMetrics().heightPixels;
        int threshold  = (int)(view.getHeight() * 0.30f);
        return viewBot - threshold > 0 && viewTop + threshold < screenH;
    }

    private void checkMissionVisible() {
        if (missionAnimDone) return;
        if (!isViewVisible(missionOverlay)) return;
        missionAnimDone = true;

        float startX = -getResources().getDisplayMetrics().widthPixels - 100f;
        missionOverlay.setTranslationX(startX);
        missionOverlay.setAlpha(1f);

        ObjectAnimator slide = ObjectAnimator.ofFloat(missionOverlay, "translationX", startX, 0f);
        slide.setDuration(700);
        slide.setInterpolator(new android.view.animation.DecelerateInterpolator(2f));
        slide.start();
    }


    private void checkVisionVisible() {
        if (visionAnimDone) return;

        if (isViewVisible(visionOverlay)) {
            visionAnimDone = true;

            visionOverlay.setAlpha(1f);

            ObjectAnimator scaleX =
                    ObjectAnimator.ofFloat(visionOverlay, "scaleX", 0.3f, 1f);
            ObjectAnimator scaleY =
                    ObjectAnimator.ofFloat(visionOverlay, "scaleY", 0.3f, 1f);
            ObjectAnimator alpha =
                    ObjectAnimator.ofFloat(visionOverlay, "alpha", 0.3f, 1f);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(scaleX, scaleY, alpha);
            set.setDuration(650);
            set.setInterpolator(
                    new OvershootInterpolator(1.1f)
            );
            set.start();
        }
    }


    private void checkMosaicVisible() {
        if (mosaicAnimStarted) return;
        FrameLayout sectionMosaic = findViewById(R.id.sectionMosaic);
        if (!isViewVisible(sectionMosaic)) return;
        mosaicAnimStarted = true;

        // Cột 1, 3, 5: Trượt từ dưới lên
        startMosaicColumn(mosaicCol1, true,  0);
        startMosaicColumn(mosaicCol3, true,  400);
        if (mosaicCol5 != null) startMosaicColumn(mosaicCol5, true, 800);

        // Cột 2, 4: Trượt từ trên xuống
        startMosaicColumn(mosaicCol2, false, 200);
        if (mosaicCol4 != null) startMosaicColumn(mosaicCol4, false, 600);
    }

    private void startMosaicColumn(LinearLayout col, boolean scrollUp, long delayMs) {
        if (col == null) return;
        float dpShift = 170f * getResources().getDisplayMetrics().density;

        float from = 0f;
        float to   = scrollUp ? -dpShift : dpShift;

        ObjectAnimator anim = ObjectAnimator.ofFloat(col, "translationY", from, to);
        anim.setDuration(3500);
        anim.setStartDelay(delayMs);
        anim.setInterpolator(new android.view.animation.LinearInterpolator());
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setRepeatMode(ValueAnimator.REVERSE);
        anim.start();
    }

    private void checkGreetVisible() {
        if (greetAnimStarted) return;
        FrameLayout sectionGreet = findViewById(R.id.sectionGreet);
        if (!isViewVisible(sectionGreet)) return;
        greetAnimStarted = true;

        tvGreetGhostTop.post(() -> {
            float screenWidth = getResources().getDisplayMetrics().widthPixels;
            float textW = tvGreetGhostTop.getWidth();
            if (textW == 0) textW = screenWidth;

            ObjectAnimator animTop = ObjectAnimator.ofFloat(
                    tvGreetGhostTop, "translationX", -textW, screenWidth
            );
            animTop.setDuration(10000);
            animTop.setInterpolator(new android.view.animation.LinearInterpolator());
            animTop.setRepeatCount(ValueAnimator.INFINITE);
            animTop.setRepeatMode(ValueAnimator.RESTART);
            animTop.start();

            ObjectAnimator animBot = ObjectAnimator.ofFloat(
                    tvGreetGhostBot, "translationX", screenWidth, -textW
            );
            animBot.setDuration(10000);
            animBot.setInterpolator(new android.view.animation.LinearInterpolator());
            animBot.setRepeatCount(ValueAnimator.INFINITE);
            animBot.setRepeatMode(ValueAnimator.RESTART);
            animBot.start();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) videoView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (videoView != null) videoView.stopPlayback();
    }
}