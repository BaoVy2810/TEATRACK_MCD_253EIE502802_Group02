package com.teatrack_mcd_253eie502802_group02.shared.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.teatrack_mcd_253eie502802_group02.R;


public final class NavBarHelper {

    private NavBarHelper() {
    }

    public static void setupNavBar(Context context, int[] itemIds, int selectedId, View.OnClickListener listener) {
        for (int itemId : itemIds) {
            View item = ((android.app.Activity) context).findViewById(itemId);
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

        if (selected) {
            item.setBackground(ResourcesCompat.getDrawable(
                    context.getResources(), R.drawable.nav_item_background, context.getTheme()));
        } else {
            item.setBackgroundResource(android.R.color.transparent);
        }

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
}