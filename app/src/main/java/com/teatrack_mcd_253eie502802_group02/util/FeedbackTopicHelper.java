package com.teatrack_mcd_253eie502802_group02.util;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.teatrack_mcd_253eie502802_group02.R;

import java.util.Locale;

public final class FeedbackTopicHelper {

    public enum TopicType {
        PRAISE(R.string.str_topic_badge_praise, R.color.topic_praise),
        SUGGEST(R.string.str_topic_badge_suggest, R.color.topic_suggest),
        COMPLAIN(R.string.str_topic_badge_complain, R.color.topic_complain),
        OTHER(R.string.str_topic_badge_other, R.color.topic_other);

        private final int labelRes;
        private final int colorRes;

        TopicType(@StringRes int labelRes, @ColorRes int colorRes) {
            this.labelRes = labelRes;
            this.colorRes = colorRes;
        }
    }

    private FeedbackTopicHelper() {
    }

    @NonNull
    public static TopicType resolve(String topic) {
        if (topic == null || topic.trim().isEmpty()) {
            return TopicType.OTHER;
        }

        String normalized = topic.toLowerCase(Locale.ROOT).trim();
        if (normalized.contains("praise") || normalized.contains("khen")) {
            return TopicType.PRAISE;
        }
        if (normalized.contains("suggest") || normalized.contains("đề xuất") || normalized.contains("de xuat")) {
            return TopicType.SUGGEST;
        }
        if (normalized.contains("complain")
                || normalized.contains("góp ý")
                || normalized.contains("gop y")
                || normalized.contains("than phi")
                || normalized.contains("khiếu")
                || normalized.contains("khieu")) {
            return TopicType.COMPLAIN;
        }
        return TopicType.OTHER;
    }

    @NonNull
    public static String getDisplayLabel(@NonNull Context context, String topic) {
        TopicType type = resolve(topic);
        return capitalizeFirst(context.getString(type.labelRes));
    }

    public static void applyTopicBadgeV2(@NonNull TextView badge, String topic) {
        TopicType type = resolve(topic);
        Context context = badge.getContext();
        badge.setText(capitalizeFirst(context.getString(type.labelRes)));
        badge.setTextColor(ContextCompat.getColor(context, R.color.white));
        badge.setTextSize(12);
        badge.setIncludeFontPadding(false);

        float density = context.getResources().getDisplayMetrics().density;
        int horizontal = Math.round(10 * density);
        int vertical = Math.round(4 * density);
        badge.setPadding(horizontal, vertical, horizontal, vertical);

        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(16 * density);
        background.setColor(ContextCompat.getColor(context, type.colorRes));
        badge.setBackground(background);
    }

    @NonNull
    private static String capitalizeFirst(@NonNull String value) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        if (trimmed.length() == 1) {
            return trimmed.toUpperCase(Locale.getDefault());
        }
        return trimmed.substring(0, 1).toUpperCase(Locale.getDefault()) + trimmed.substring(1);
    }
}
