package com.teatrack_mcd_253eie502802_group02.shared.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.teatrack_mcd_253eie502802_group02.R;

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
        int color = ContextCompat.getColor(context, selected ? R.color.white : R.color.nav_inactive);

        // Set background: blue rounded when selected, transparent when not
        if (selected) {
            item.setBackground(ResourcesCompat.getDrawable(
                    context.getResources(), R.drawable.nav_item_background, context.getTheme()));
        } else {
            item.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        }

        if (item instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) item;
            if (group.getChildCount() >= 2) {
                View iconView = group.getChildAt(0);
                View labelView = group.getChildAt(1);

                if (iconView instanceof ImageView) {
                    ((ImageView) iconView).setColorFilter(color);
                }
                if (labelView instanceof TextView) {
                    ((TextView) labelView).setTextColor(color);
                }
            }
        }
    }
}