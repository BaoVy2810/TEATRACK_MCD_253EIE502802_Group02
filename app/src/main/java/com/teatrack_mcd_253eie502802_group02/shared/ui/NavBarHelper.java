package com.teatrack_mcd_253eie502802_group02.shared.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.teatrack_mcd_253eie502802_group02.R;

public final class NavBarHelper {

    private static final int EXPAND_DRAG_THRESHOLD_PX = 56;
    private static final float EXPAND_SNAP_RATIO = 0.38f;
    private static boolean adminNavExpanded = false;

    private NavBarHelper() {
    }

    public static void navigateWithoutTransition(Activity activity, Class<?> destination) {
        Intent intent = new Intent(activity, destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    public static void setupNavBar(Context context, int[] itemIds, int selectedId, View.OnClickListener listener) {
        Activity activity = (Activity) context;

        View expandHandle = activity.findViewById(R.id.nav_expand_handle);
        if (expandHandle != null) {
            setupAdminExpandBehavior(context, expandHandle);
        }

        for (int itemId : itemIds) {
            View item = activity.findViewById(itemId);
            if (item == null) {
                continue;
            }
            updateItemState(context, item, itemId == selectedId);
            item.setOnClickListener(listener);
        }
    }

    public static void updateItemState(Context context, View item, boolean selected) {
        item.setSelected(selected);
        int color = ContextCompat.getColor(context, selected ? R.color.white : R.color.text_secondary);

        item.setBackground(ResourcesCompat.getDrawable(
                context.getResources(), R.drawable.nav_item_background, context.getTheme()));

        if (item instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) item;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof ImageView) {
                    ((ImageView) child).setColorFilter(color);
                } else if (child instanceof TextView) {
                    ((TextView) child).setTextColor(color);
                    ((TextView) child).setTypeface(null, selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
                }
            }
        }
    }

    private static void setupAdminExpandBehavior(Context context, View handle) {
        Activity activity = (Activity) context;
        View secondaryRow = activity.findViewById(R.id.nav_row_secondary);
        View dragPill = activity.findViewById(R.id.nav_drag_pill);
        if (secondaryRow == null) {
            return;
        }

        final boolean[] expanded = {adminNavExpanded};
        setExpandProgress(secondaryRow, dragPill, expanded[0] ? 1f : 0f);

        Runnable toggleExpanded = () -> {
            expanded[0] = !expanded[0];
            adminNavExpanded = expanded[0];
            animateAdminNavExpand(secondaryRow, dragPill, expanded[0], handle);
        };

        handle.setOnClickListener(v -> toggleExpanded.run());

        handle.setOnTouchListener(new View.OnTouchListener() {
            private float startY;
            private float startProgress;
            private boolean dragged;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = event.getRawY();
                        startProgress = expanded[0] ? 1f : 0f;
                        dragged = false;
                        if (dragPill != null) {
                            dragPill.animate().scaleX(1.12f).scaleY(1.12f).setDuration(120L).start();
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float deltaY = startY - event.getRawY();
                        if (Math.abs(deltaY) > 8f) {
                            dragged = true;
                        }
                        float progress = startProgress + (deltaY / EXPAND_DRAG_THRESHOLD_PX);
                        progress = Math.max(0f, Math.min(1f, progress));
                        setExpandProgress(secondaryRow, dragPill, progress);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (dragPill != null) {
                            dragPill.animate().scaleX(1f).scaleY(1f).setDuration(160L).start();
                        }
                        if (!dragged) {
                            v.performClick();
                            return true;
                        }
                        float releaseDelta = startY - event.getRawY();
                        float releaseProgress = startProgress + (releaseDelta / EXPAND_DRAG_THRESHOLD_PX);
                        boolean shouldExpand = releaseProgress >= EXPAND_SNAP_RATIO;
                        expanded[0] = shouldExpand;
                        adminNavExpanded = shouldExpand;
                        animateAdminNavExpand(secondaryRow, dragPill, shouldExpand, handle);
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private static void setExpandProgress(View secondaryRow, View dragPill, float progress) {
        if (progress <= 0.01f) {
            secondaryRow.setVisibility(View.GONE);
            secondaryRow.setAlpha(0f);
            secondaryRow.setTranslationY(16f);
        } else {
            secondaryRow.setVisibility(View.VISIBLE);
            secondaryRow.setAlpha(progress);
            secondaryRow.setTranslationY(16f * (1f - progress));
        }

        if (dragPill != null) {
            dragPill.setAlpha(0.55f + (0.45f * (1f - progress)));
            dragPill.setTranslationY(-3f * progress);
            float scale = 1f + (0.08f * progress);
            dragPill.setScaleX(scale);
            dragPill.setScaleY(scale);
        }
    }

    private static void animateAdminNavExpand(
            View secondaryRow,
            View dragPill,
            boolean expand,
            View handle
    ) {
        float target = expand ? 1f : 0f;
        secondaryRow.animate().cancel();
        if (dragPill != null) {
            dragPill.animate().cancel();
        }

        if (expand) {
            secondaryRow.setVisibility(View.VISIBLE);
        }

        secondaryRow.animate()
                .alpha(target)
                .translationY(16f * (1f - target))
                .setDuration(expand ? 260L : 200L)
                .setInterpolator(new FastOutSlowInInterpolator())
                .withEndAction(() -> {
                    if (!expand) {
                        secondaryRow.setVisibility(View.GONE);
                        secondaryRow.setTranslationY(0f);
                    }
                })
                .start();

        if (dragPill != null) {
            float pillAlpha = 0.55f + (0.45f * (1f - target));
            dragPill.animate()
                    .alpha(pillAlpha)
                    .translationY(-3f * target)
                    .scaleX(1f + (0.08f * target))
                    .scaleY(1f + (0.08f * target))
                    .setDuration(expand ? 260L : 200L)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        handle.performHapticFeedback(
                expand ? HapticFeedbackConstants.CONTEXT_CLICK : HapticFeedbackConstants.CLOCK_TICK
        );
    }
}
