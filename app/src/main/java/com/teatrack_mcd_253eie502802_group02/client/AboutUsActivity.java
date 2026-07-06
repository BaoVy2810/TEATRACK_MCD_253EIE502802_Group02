package com.teatrack_mcd_253eie502802_group02.client;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;
import com.teatrack_mcd_253eie502802_group02.shared.ui.StrokeTextView;

import java.util.ArrayList;
import java.util.List;

public class AboutUsActivity extends BaseActivity implements TextureView.SurfaceTextureListener {

    private static final long GREET_MARQUEE_MS = 5_500L;

    private TextureView textureVideo;
    private MediaPlayer mediaPlayer;
    private boolean videoPrepared = false;
    private NestedScrollView scrollView;

    private View sectionMission;
    private LinearLayout missionOverlay;
    private boolean missionWasVisible = false;
    private Animator missionAnimator;

    private View sectionVision;
    private LinearLayout visionOverlay;
    private boolean visionWasVisible = false;
    private Animator visionAnimator;

    private ImageView mosaicImg1;
    private ImageView mosaicImg2;
    private ImageView mosaicImg3;
    private ImageView mosaicImg4;
    private ImageView mosaicImg5;
    private boolean mosaicAnimStarted = false;

    private StrokeTextView tvGreetGhostTop;
    private StrokeTextView tvGreetGhostBot;
    private boolean greetAnimStarted = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<Animator> runningAnimators = new ArrayList<>();
    private ViewTreeObserver.OnScrollChangedListener scrollChangedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about_us);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        bindViews();
        setupTopBar();
        setupVideo();
        setupScrollListener();

        resetMissionState();
        resetVisionState();
    }

    private void bindViews() {
        scrollView = findViewById(R.id.nestedScrollView);
        textureVideo = findViewById(R.id.videoView);
        missionOverlay = findViewById(R.id.missionOverlay);
        sectionMission = findViewById(R.id.sectionMission);
        visionOverlay = findViewById(R.id.visionOverlay);
        sectionVision = findViewById(R.id.sectionVision);
        mosaicImg1 = findViewById(R.id.mosaicImg1);
        mosaicImg2 = findViewById(R.id.mosaicImg2);
        mosaicImg3 = findViewById(R.id.mosaicImg3);
        mosaicImg4 = findViewById(R.id.mosaicImg4);
        mosaicImg5 = findViewById(R.id.mosaicImg5);
        tvGreetGhostTop = findViewById(R.id.tvGreetGhostTop);
        tvGreetGhostBot = findViewById(R.id.tvGreetGhostBot);
    }

    private void setupTopBar() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Intent intent = new Intent(this, Homepage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        View btnShare = findViewById(R.id.btnShare);
        if (btnShare != null) {
            btnShare.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.about_page_title));
                startActivity(Intent.createChooser(intent, getString(R.string.blog_detail_share)));
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && videoPrepared && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void setupVideo() {
        if (textureVideo == null) {
            return;
        }
        textureVideo.setSurfaceTextureListener(this);
        textureVideo.setOpaque(true);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        surface.setDefaultBufferSize(width, height);
        startVideoPlayback(new Surface(surface), width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (mediaPlayer != null && videoPrepared) {
            applyVideoCenterCrop(width, height, mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        releaseMediaPlayer();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // no-op
    }

    private void startVideoPlayback(Surface surface, int viewWidth, int viewHeight) {
        releaseMediaPlayer();
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(
                    this,
                    Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video)
            );
            mediaPlayer.setSurface(surface);
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(0f, 0f);
            mediaPlayer.setOnPreparedListener(mp -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                videoPrepared = true;
                textureVideo.post(() ->
                        applyVideoCenterCrop(
                                textureVideo.getWidth(),
                                textureVideo.getHeight(),
                                mp.getVideoWidth(),
                                mp.getVideoHeight()
                        )
                );
                mp.start();
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                videoPrepared = false;
                return true;
            });
            mediaPlayer.setOnVideoSizeChangedListener((mp, width, height) ->
                    textureVideo.post(() ->
                            applyVideoCenterCrop(textureVideo.getWidth(), textureVideo.getHeight(), width, height)
                    )
            );
            mediaPlayer.prepareAsync();
        } catch (Exception ignored) {
            videoPrepared = false;
            releaseMediaPlayer();
        }
    }

    private void applyVideoCenterCrop(int viewWidth, int viewHeight, int videoWidth, int videoHeight) {
        if (textureVideo == null || viewWidth <= 0 || viewHeight <= 0 || videoWidth <= 0 || videoHeight <= 0) {
            return;
        }

        float viewAspect = (float) viewWidth / viewHeight;
        float videoAspect = (float) videoWidth / videoHeight;

        // TextureView transform uses normalized texture coordinates [0, 1].
        Matrix matrix = new Matrix();
        if (videoAspect > viewAspect) {
            float heightScale = videoAspect / viewAspect;
            matrix.setScale(1f, heightScale, 0.5f, 0.5f);
        } else {
            float widthScale = viewAspect / videoAspect;
            matrix.setScale(widthScale, 1f, 0.5f, 0.5f);
        }
        textureVideo.setTransform(matrix);
    }

    private void releaseMediaPlayer() {
        videoPrepared = false;
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.setOnPreparedListener(null);
        mediaPlayer.setOnErrorListener(null);
        mediaPlayer.setOnVideoSizeChangedListener(null);
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        } catch (Exception ignored) {
            // Player may not be started yet.
        }
        mediaPlayer.release();
        mediaPlayer = null;
    }

    private void setupScrollListener() {
        if (scrollView == null) {
            return;
        }
        scrollChangedListener = () -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            updateMissionScrollEffect();
            updateVisionScrollEffect();
            checkMosaicVisible();
            checkGreetVisible();
        };
        scrollView.getViewTreeObserver().addOnScrollChangedListener(scrollChangedListener);

        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        resetMissionState();
                        resetVisionState();
                        updateMissionScrollEffect();
                        updateVisionScrollEffect();
                        checkMosaicVisible();
                        checkGreetVisible();
                    }
                }
        );
    }

    private boolean isViewVisible(View view) {
        if (view == null) {
            return false;
        }
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        int viewTop = loc[1];
        int viewBot = loc[1] + view.getHeight();
        int screenH = getResources().getDisplayMetrics().heightPixels;
        int threshold = (int) (view.getHeight() * 0.25f);
        return viewBot - threshold > 0 && viewTop + threshold < screenH;
    }

    private void updateMissionScrollEffect() {
        if (missionOverlay == null || sectionMission == null) {
            return;
        }
        boolean visible = isViewVisible(sectionMission);
        if (visible == missionWasVisible) {
            return;
        }
        missionWasVisible = visible;
        if (visible) {
            playMissionAnimation();
        } else {
            resetMissionState();
        }
    }

    private float missionHiddenTranslationX() {
        float width = missionOverlay.getWidth();
        if (width <= 0f) {
            width = sectionMission.getWidth() * 0.78f;
        }
        return -width;
    }

    private void resetMissionState() {
        cancelMissionAnimator();
        if (missionOverlay == null) {
            return;
        }
        missionOverlay.setTranslationX(missionHiddenTranslationX());
        missionOverlay.setAlpha(1f);
    }

    private void playMissionAnimation() {
        if (missionOverlay == null) {
            return;
        }
        cancelMissionAnimator();
        float startX = missionHiddenTranslationX();
        missionOverlay.setTranslationX(startX);
        missionOverlay.setAlpha(1f);

        missionAnimator = ObjectAnimator.ofFloat(missionOverlay, View.TRANSLATION_X, startX, 0f);
        missionAnimator.setDuration(800);
        missionAnimator.setInterpolator(new DecelerateInterpolator(2f));
        trackAnimator(missionAnimator);
        missionAnimator.start();
    }

    private void cancelMissionAnimator() {
        if (missionAnimator != null) {
            missionAnimator.cancel();
            missionAnimator = null;
        }
    }

    private void updateVisionScrollEffect() {
        if (visionOverlay == null || sectionVision == null) {
            return;
        }
        boolean visible = isViewVisible(sectionVision);
        if (visible == visionWasVisible) {
            return;
        }
        visionWasVisible = visible;
        if (visible) {
            playVisionAnimation();
        } else {
            resetVisionState();
        }
    }

    private void resetVisionState() {
        cancelVisionAnimator();
        if (visionOverlay == null) {
            return;
        }
        visionOverlay.setScaleX(0.3f);
        visionOverlay.setScaleY(0.3f);
        visionOverlay.setAlpha(0.35f);
    }

    private void playVisionAnimation() {
        if (visionOverlay == null) {
            return;
        }
        cancelVisionAnimator();
        visionOverlay.setAlpha(1f);
        visionOverlay.post(() -> {
            visionOverlay.setPivotX(visionOverlay.getWidth() / 2f);
            visionOverlay.setPivotY(visionOverlay.getHeight() / 2f);

            ObjectAnimator scaleX = ObjectAnimator.ofFloat(visionOverlay, View.SCALE_X, 0.3f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(visionOverlay, View.SCALE_Y, 0.3f, 1f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(visionOverlay, View.ALPHA, 0.35f, 1f);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(scaleX, scaleY, alpha);
            set.setDuration(700);
            set.setInterpolator(new OvershootInterpolator(1.05f));
            visionAnimator = set;
            trackAnimator(set);
            set.start();
        });
    }

    private void cancelVisionAnimator() {
        if (visionAnimator != null) {
            visionAnimator.cancel();
            visionAnimator = null;
        }
    }

    private void checkMosaicVisible() {
        if (mosaicAnimStarted) {
            return;
        }
        FrameLayout sectionMosaic = findViewById(R.id.sectionMosaic);
        if (!isViewVisible(sectionMosaic)) {
            return;
        }
        mosaicAnimStarted = true;
        startMosaicAnimations();
    }

    private void startMosaicAnimations() {
        startMosaicPan(mosaicImg1, 10_000L, 0L);
        startMosaicPan(mosaicImg2, 4_800L, 150L);
        startMosaicPan(mosaicImg3, 6_200L, 350L);
        startMosaicPan(mosaicImg4, 4_800L, 150L);
        startMosaicPan(mosaicImg5, 5_000L, 500L);
    }

    private void startMosaicPan(ImageView image, long durationMs, long delayMs) {
        if (image == null) {
            return;
        }

        image.setScaleType(ImageView.ScaleType.MATRIX);

        image.post(() -> {
            Drawable drawable = image.getDrawable();
            float clipW = image.getWidth();
            float clipH = image.getHeight();
            if (drawable == null || clipW <= 0f || clipH <= 0f) {
                return;
            }

            int intrinsicW = drawable.getIntrinsicWidth();
            int intrinsicH = drawable.getIntrinsicHeight();
            if (intrinsicW <= 0 || intrinsicH <= 0) {
                return;
            }

            final float visualScale = 1.08f;
            final float scale = Math.max(clipW / intrinsicW, clipH / intrinsicH) * visualScale;
            final float scaledW = intrinsicW * scale;
            final float scaledH = intrinsicH * scale;
            final float dx = (clipW - scaledW) / 2f;
            final float dyCenter = (clipH - scaledH) / 2f;
            final float maxPan = Math.max(0f, (scaledH - clipH) / 2f);
            final float panRange = maxPan > 0f ? maxPan * 0.92f : clipH * 0.06f;
            final float fromDy = dyCenter - panRange;
            final float toDy = dyCenter + panRange;

            applyMosaicMatrix(image, scale, dx, fromDy);

            ValueAnimator pan = ValueAnimator.ofFloat(fromDy, toDy);
            pan.setDuration(durationMs);
            pan.setStartDelay(delayMs);
            pan.setInterpolator(new AccelerateDecelerateInterpolator());
            pan.setRepeatCount(ValueAnimator.INFINITE);
            pan.setRepeatMode(ValueAnimator.REVERSE);
            pan.addUpdateListener(animation -> {
                float dy = (float) animation.getAnimatedValue();
                applyMosaicMatrix(image, scale, dx, dy);
            });
            trackAnimator(pan);
            pan.start();
        });
    }

    private void applyMosaicMatrix(ImageView image, float scale, float dx, float dy) {
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        matrix.postTranslate(dx, dy);
        image.setImageMatrix(matrix);
    }

    private void checkGreetVisible() {
        if (greetAnimStarted || tvGreetGhostTop == null || tvGreetGhostBot == null) {
            return;
        }
        FrameLayout sectionGreet = findViewById(R.id.sectionGreet);
        if (!isViewVisible(sectionGreet)) {
            return;
        }
        greetAnimStarted = true;

        sectionGreet.post(() -> {
            float sectionH = sectionGreet.getHeight();
            float sectionW = sectionGreet.getWidth();
            tvGreetGhostTop.setTranslationY(-sectionH * 0.35f);
            tvGreetGhostBot.setTranslationY(sectionH * 0.35f);

            float textW = tvGreetGhostTop.getWidth();
            if (textW <= 0f) {
                textW = tvGreetGhostTop.getPaint().measureText(
                        tvGreetGhostTop.getText().toString()
                );
            }

            float overflow = Math.max(0f, textW - sectionW);
            float panRange = Math.min(sectionW * 0.16f, overflow * 0.28f);
            if (panRange < sectionW * 0.06f) {
                panRange = sectionW * 0.08f;
            }

            float topFrom = -panRange;
            float topTo = panRange;
            float botFrom = panRange;
            float botTo = -panRange;

            tvGreetGhostTop.setTranslationX(topFrom);
            tvGreetGhostBot.setTranslationX(botFrom);

            ObjectAnimator animTop = ObjectAnimator.ofFloat(
                    tvGreetGhostTop, View.TRANSLATION_X, topFrom, topTo
            );
            animTop.setDuration(GREET_MARQUEE_MS);
            animTop.setInterpolator(new LinearInterpolator());
            animTop.setRepeatCount(ValueAnimator.INFINITE);
            animTop.setRepeatMode(ValueAnimator.REVERSE);
            trackAnimator(animTop);
            animTop.start();

            ObjectAnimator animBot = ObjectAnimator.ofFloat(
                    tvGreetGhostBot, View.TRANSLATION_X, botFrom, botTo
            );
            animBot.setDuration(GREET_MARQUEE_MS);
            animBot.setInterpolator(new LinearInterpolator());
            animBot.setRepeatCount(ValueAnimator.INFINITE);
            animBot.setRepeatMode(ValueAnimator.REVERSE);
            trackAnimator(animBot);
            animBot.start();
        });
    }

    private void trackAnimator(Animator animator) {
        runningAnimators.add(animator);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                runningAnimators.remove(animation);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                runningAnimators.remove(animation);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        if (scrollView != null && scrollChangedListener != null) {
            scrollView.getViewTreeObserver().removeOnScrollChangedListener(scrollChangedListener);
            scrollChangedListener = null;
        }
        handler.removeCallbacksAndMessages(null);
        cancelMissionAnimator();
        cancelVisionAnimator();
        for (Animator animator : new ArrayList<>(runningAnimators)) {
            animator.cancel();
        }
        runningAnimators.clear();
        if (textureVideo != null) {
            textureVideo.setSurfaceTextureListener(null);
        }
        releaseMediaPlayer();
        super.onDestroy();
    }
}
