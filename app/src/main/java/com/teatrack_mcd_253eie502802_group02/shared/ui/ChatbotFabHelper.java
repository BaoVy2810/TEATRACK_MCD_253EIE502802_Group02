package com.teatrack_mcd_253eie502802_group02.shared.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.client.ChatbotBubble;

public final class ChatbotFabHelper {

    private static final String PREF_NAME = "chatbot_fab_prefs";
    private static final String KEY_X_RATIO = "fab_x_ratio";
    private static final String KEY_Y_RATIO = "fab_y_ratio";
    private static final int CLICK_THRESHOLD_PX = 10;

    private ChatbotFabHelper() {
    }

    public static void attachIfNeeded(Activity activity) {
        if (!shouldShow(activity) || activity.findViewById(R.id.fabChatbot) != null) {
            return;
        }

        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null) {
            return;
        }

        View overlay = LayoutInflater.from(activity).inflate(R.layout.layout_chatbot_fab, content, false);
        View fab = overlay.findViewById(R.id.fabChatbot);
        if (fab == null) {
            return;
        }

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) fab.getLayoutParams();
        params.gravity = android.view.Gravity.NO_GRAVITY;
        params.setMarginStart(0);
        params.setMarginEnd(0);
        params.topMargin = 0;
        params.bottomMargin = 0;
        fab.setLayoutParams(params);

        bind(fab, overlay, activity);
        content.addView(overlay);
        overlay.bringToFront();

        fab.post(() -> restoreOrPlaceDefault(activity, overlay, fab));
    }

    public static void bind(View fabRoot, View overlay, Activity activity) {
        if (fabRoot == null || overlay == null || activity == null) {
            return;
        }

        View pulseRing1 = fabRoot.findViewById(R.id.pulseRing1);
        View pulseRing2 = fabRoot.findViewById(R.id.pulseRing2);
        startPulseAnimation(activity, pulseRing1, R.anim.chatbot_fab_pulse);
        startPulseAnimation(activity, pulseRing2, R.anim.chatbot_fab_pulse_delayed);
        setupDrag(fabRoot, overlay, activity);
    }

    private static void setupDrag(View fab, View overlay, Activity activity) {
        fab.setOnTouchListener(new View.OnTouchListener() {
            private float initialX;
            private float initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = v.getX();
                        initialY = v.getY();
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        v.setX(initialX + (event.getRawX() - initialTouchX));
                        v.setY(initialY + (event.getRawY() - initialTouchY));
                        clampPosition(activity, overlay, v);
                        return true;

                    case MotionEvent.ACTION_UP:
                        clampPosition(activity, overlay, v);
                        savePosition(activity, overlay, v);
                        float diffX = Math.abs(event.getRawX() - initialTouchX);
                        float diffY = Math.abs(event.getRawY() - initialTouchY);
                        if (diffX < CLICK_THRESHOLD_PX && diffY < CLICK_THRESHOLD_PX) {
                            activity.startActivity(new Intent(activity, ChatbotBubble.class));
                        }
                        return true;

                    default:
                        return false;
                }
            }
        });
    }

    private static void restoreOrPlaceDefault(Activity activity, View overlay, View fab) {
        if (overlay.getWidth() == 0 || overlay.getHeight() == 0) {
            return;
        }

        SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (prefs.contains(KEY_X_RATIO) && prefs.contains(KEY_Y_RATIO)) {
            float x = prefs.getFloat(KEY_X_RATIO, 0f) * overlay.getWidth();
            float y = prefs.getFloat(KEY_Y_RATIO, 0f) * overlay.getHeight();
            fab.setX(x);
            fab.setY(y);
            clampPosition(activity, overlay, fab);
            return;
        }

        placeDefault(activity, overlay, fab);
    }

    private static void placeDefault(Activity activity, View overlay, View fab) {
        int marginEnd = activity.getResources().getDimensionPixelSize(R.dimen.fab_margin_end);
        int marginBottom = activity.getResources().getDimensionPixelSize(
                hasClientNavBar(activity) ? R.dimen.fab_margin_bottom_above_nav : R.dimen.fab_margin_bottom);

        float x = overlay.getWidth() - fab.getWidth() - marginEnd;
        float y = overlay.getHeight() - fab.getHeight() - marginBottom;
        fab.setX(x);
        fab.setY(y);
        clampPosition(activity, overlay, fab);
        savePosition(activity, overlay, fab);
    }

    private static void clampPosition(Activity activity, View overlay, View fab) {
        int margin = activity.getResources().getDimensionPixelSize(R.dimen.fab_margin_end);
        float minX = margin;
        float maxX = Math.max(minX, overlay.getWidth() - fab.getWidth() - margin);
        float minY = getTopBound(activity, overlay) + margin;
        float maxY = Math.max(minY, getBottomBound(activity, overlay, fab) - margin);

        float clampedX = Math.max(minX, Math.min(fab.getX(), maxX));
        float clampedY = Math.max(minY, Math.min(fab.getY(), maxY));
        fab.setX(clampedX);
        fab.setY(clampedY);
    }

    private static float getTopBound(Activity activity, View overlay) {
        View header = activity.findViewById(R.id.header_client);
        if (header == null) {
            header = activity.findViewById(R.id.headerClient);
        }
        if (header != null && header.getHeight() > 0) {
            int[] headerLoc = new int[2];
            int[] overlayLoc = new int[2];
            header.getLocationOnScreen(headerLoc);
            overlay.getLocationOnScreen(overlayLoc);
            return headerLoc[1] + header.getHeight() - overlayLoc[1];
        }
        return activity.getResources().getDimensionPixelSize(R.dimen.header_height);
    }

    private static float getBottomBound(Activity activity, View overlay, View fab) {
        View navBar = activity.findViewById(R.id.navBarClient);
        if (navBar == null) {
            navBar = activity.findViewById(R.id.layout_nav_bar);
        }
        if (navBar != null && navBar.getHeight() > 0) {
            int[] navLoc = new int[2];
            int[] overlayLoc = new int[2];
            navBar.getLocationOnScreen(navLoc);
            overlay.getLocationOnScreen(overlayLoc);
            return navLoc[1] - overlayLoc[1] - fab.getHeight();
        }

        int marginBottom = activity.getResources().getDimensionPixelSize(R.dimen.fab_margin_bottom);
        return overlay.getHeight() - fab.getHeight() - marginBottom;
    }

    private static void savePosition(Activity activity, View overlay, View fab) {
        if (overlay.getWidth() == 0 || overlay.getHeight() == 0) {
            return;
        }
        activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putFloat(KEY_X_RATIO, fab.getX() / overlay.getWidth())
                .putFloat(KEY_Y_RATIO, fab.getY() / overlay.getHeight())
                .apply();
    }

    private static void startPulseAnimation(Activity activity, View target, int animRes) {
        if (target == null) {
            return;
        }
        Animation animation = AnimationUtils.loadAnimation(activity, animRes);
        target.startAnimation(animation);
    }

    private static boolean shouldShow(Activity activity) {
        if (activity instanceof ChatbotBubble) {
            return false;
        }
        return hasClientHeader(activity) && hasClientNavBar(activity);
    }

    private static boolean hasClientHeader(Activity activity) {
        return activity.findViewById(R.id.header_client) != null;
    }

    private static boolean hasClientNavBar(Activity activity) {
        return activity.findViewById(R.id.navBarClient) != null
                || activity.findViewById(R.id.layout_nav_bar) != null;
    }
}
