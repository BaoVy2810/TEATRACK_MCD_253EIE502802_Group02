package com.teatrack_mcd_253eie502802_group02.shared.ui;

import android.view.View;
import android.view.ViewGroup;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.teatrack_mcd_253eie502802_group02.R;

public final class AdminInsetsHelper {

    private AdminInsetsHelper() {
    }

    public static void apply(ComponentActivity activity) {
        EdgeToEdge.enable(activity);

        View root = activity.findViewById(R.id.main);
        if (root == null) {
            View content = activity.findViewById(android.R.id.content);
            if (content instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) content;
                if (group.getChildCount() > 0) {
                    root = group.getChildAt(0);
                }
            }
        }
        if (root == null) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(root);
    }
}
