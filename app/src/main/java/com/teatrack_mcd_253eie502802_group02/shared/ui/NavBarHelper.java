package com.teatrack_mcd_253eie502802_group02.shared.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.teatrack_mcd_253eie502802_group02.R;


public final class NavBarHelper {

    private static final String ADMIN_NAV_PREFS = "admin_nav_prefs";
    private static final String KEY_PROMOTED_TAB_ID = "promoted_tab_id";

    private static final int PRIMARY_SWAP_INDEX = 3;
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
        applyPersistedAdminLayout(activity);

        if (isSecondaryNavItem(selectedId)) {
            promoteSecondaryTab(activity, selectedId);
        }

        View.OnClickListener wrappedListener = v -> {
            int id = v.getId();
            if (isSecondaryNavItem(id)) {
                promoteSecondaryTab(activity, id);
            } else if (id == R.id.nav_account) {
                restoreAccountToPrimary(activity);
            }
            listener.onClick(v);
        };

        for (int itemId : itemIds) {
            View item = activity.findViewById(itemId);
            if (item == null) {
                continue;
            }
            updateItemState(context, item, itemId == selectedId);
            item.setOnClickListener(wrappedListener);
        }

        View expandHandle = activity.findViewById(R.id.nav_expand_handle);
        if (expandHandle != null) {
            setupAdminExpandBehavior(context, expandHandle, selectedId);
        }
    }

    private static void applyPersistedAdminLayout(Activity activity) {
        int promotedId = getPromotedTabId(activity);
        if (promotedId != View.NO_ID && isSecondaryNavItem(promotedId)) {
            promoteSecondaryTab(activity, promotedId);
        }
    }

    private static void promoteSecondaryTab(Activity activity, int secondaryItemId) {
        if (!isSecondaryNavItem(secondaryItemId)) {
            return;
        }

        LinearLayout primaryRow = activity.findViewById(R.id.nav_row_primary);
        LinearLayout secondaryRow = activity.findViewById(R.id.nav_row_secondary);
        if (primaryRow == null || secondaryRow == null) {
            return;
        }

        View primarySlot = primaryRow.getChildAt(PRIMARY_SWAP_INDEX);
        if (primarySlot != null && primarySlot.getId() == secondaryItemId) {
            savePromotedTabId(activity, secondaryItemId);
            return;
        }

        int secondaryIndex = findChildIndexById(secondaryRow, secondaryItemId);
        if (secondaryIndex < 0) {
            return;
        }

        swapNavChildren(primaryRow, secondaryRow, PRIMARY_SWAP_INDEX, secondaryIndex);
        savePromotedTabId(activity, secondaryItemId);
    }

    private static void restoreAccountToPrimary(Activity activity) {
        LinearLayout primaryRow = activity.findViewById(R.id.nav_row_primary);
        LinearLayout secondaryRow = activity.findViewById(R.id.nav_row_secondary);
        if (primaryRow == null || secondaryRow == null) {
            return;
        }

        View primarySlot = primaryRow.getChildAt(PRIMARY_SWAP_INDEX);
        if (primarySlot != null && primarySlot.getId() == R.id.nav_account) {
            clearPromotedTabId(activity);
            return;
        }

        int accountSecondaryIndex = findChildIndexById(secondaryRow, R.id.nav_account);
        if (accountSecondaryIndex < 0) {
            return;
        }

        swapNavChildren(primaryRow, secondaryRow, PRIMARY_SWAP_INDEX, accountSecondaryIndex);
        clearPromotedTabId(activity);
    }

    private static void swapNavChildren(
            LinearLayout primaryRow,
            LinearLayout secondaryRow,
            int primaryIndex,
            int secondaryIndex
    ) {
        View primaryChild = primaryRow.getChildAt(primaryIndex);
        View secondaryChild = secondaryRow.getChildAt(secondaryIndex);
        if (primaryChild == null || secondaryChild == null) {
            return;
        }

        LinearLayout.LayoutParams primaryLp = copyNavItemLayoutParams(
                (LinearLayout.LayoutParams) primaryChild.getLayoutParams()
        );
        LinearLayout.LayoutParams secondaryLp = copyNavItemLayoutParams(
                (LinearLayout.LayoutParams) secondaryChild.getLayoutParams()
        );

        primaryRow.removeViewAt(primaryIndex);
        secondaryRow.removeViewAt(secondaryIndex);

        primaryChild.setLayoutParams(primaryLp);
        secondaryChild.setLayoutParams(secondaryLp);

        primaryRow.addView(secondaryChild, primaryIndex);
        secondaryRow.addView(primaryChild, secondaryIndex);
    }

    private static LinearLayout.LayoutParams copyNavItemLayoutParams(LinearLayout.LayoutParams source) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
        );
        if (source != null) {
            params.gravity = source.gravity;
            params.leftMargin = source.leftMargin;
            params.topMargin = source.topMargin;
            params.rightMargin = source.rightMargin;
            params.bottomMargin = source.bottomMargin;
        }
        return params;
    }

    private static int findChildIndexById(ViewGroup parent, int viewId) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (parent.getChildAt(i).getId() == viewId) {
                return i;
            }
        }
        return -1;
    }

    private static SharedPreferences adminNavPrefs(Context context) {
        return context.getSharedPreferences(ADMIN_NAV_PREFS, Context.MODE_PRIVATE);
    }

    private static void savePromotedTabId(Context context, int tabId) {
        adminNavPrefs(context).edit().putInt(KEY_PROMOTED_TAB_ID, tabId).apply();
    }

    private static void clearPromotedTabId(Context context) {
        adminNavPrefs(context).edit().remove(KEY_PROMOTED_TAB_ID).apply();
    }

    private static int getPromotedTabId(Context context) {
        return adminNavPrefs(context).getInt(KEY_PROMOTED_TAB_ID, View.NO_ID);
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

    private static void setupAdminExpandBehavior(Context context, View handle, int selectedId) {
        Activity activity = (Activity) context;
        View secondaryRow = activity.findViewById(R.id.nav_row_secondary);
        View dragPill = activity.findViewById(R.id.nav_drag_pill);
        if (secondaryRow == null) {
            return;
        }

        final boolean[] expanded = {isSecondaryNavItem(selectedId)};
        if (expanded[0]) {
            setExpandProgress(secondaryRow, dragPill, 1f);
        } else {
            setExpandProgress(secondaryRow, dragPill, 0f);
        }

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
