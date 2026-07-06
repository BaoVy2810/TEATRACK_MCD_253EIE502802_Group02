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
        Runnable collapseExpanded = null;
        if (expandHandle != null) {
            collapseExpanded = setupAdminExpandBehavior(context, expandHandle, selectedId);
        }

        for (int itemId : itemIds) {
            View item = activity.findViewById(itemId);
            if (item == null) {
                continue;
            }
            updateItemState(context, item, itemId == selectedId);

            View.OnClickListener itemListener = listener;
            if (collapseExpanded != null && isPrimaryNavItem(itemId)) {
                Runnable collapse = collapseExpanded;
                itemListener = v -> {
                    collapse.run();
                    listener.onClick(v);
                };
            }
            item.setOnClickListener(itemListener);
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

    private static Runnable setupAdminExpandBehavior(Context context, View handle, int selectedId) {
        Activity activity = (Activity) context;
        View secondaryRow = activity.findViewById(R.id.nav_row_secondary);
        View dragPill = activity.findViewById(R.id.nav_drag_pill);
        if (secondaryRow == null) {
            return () -> {};
        }

        final boolean[] expanded = {isSecondaryNavItem(selectedId)};
        if (expanded[0]) {
            setExpandProgress(secondaryRow, dragPill, 1f);
        } else {
            setExpandProgress(secondaryRow, dragPill, 0f);
        }

        Runnable collapseIfExpanded = () -> {
            if (expanded[0]) {
                expanded[0] = false;
                animateAdminNavExpand(secondaryRow, dragPill, false, handle);
            }
        };

        Runnable toggleExpanded = () -> {
            expanded[0] = !expanded[0];
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
                        animateAdminNavExpand(secondaryRow, dragPill, shouldExpand, handle);
                        return true;
                    default:
                        return false;
                }
            }
        });

        return collapseIfExpanded;
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

    private static boolean isPrimaryNavItem(int itemId) {
        return itemId == R.id.nav_dashboard
                || itemId == R.id.nav_products
                || itemId == R.id.nav_orders
                || itemId == R.id.nav_account;
    }

    private static boolean isSecondaryNavItem(int itemId) {
        return itemId == R.id.nav_forum
                || itemId == R.id.nav_branch
                || itemId == R.id.nav_feedbacks
                || itemId == R.id.nav_promotion;
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
