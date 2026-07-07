package com.teatrack_mcd_253eie502802_group02.util;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;

import androidx.annotation.NonNull;

public final class ChatMessageFormatter {

    private ChatMessageFormatter() {
    }

    @NonNull
    public static CharSequence formatBotMessage(@NonNull String raw) {
        if (TextUtils.isEmpty(raw)) {
            return "";
        }

        String normalized = raw.replace("\r\n", "\n").replace("\\n", "\n").trim();
        normalized = forceLineBreaks(normalized);
        normalized = normalizeBullets(normalized);
        return buildSpannableWithBold(normalized);
    }

    @NonNull
    private static String forceLineBreaks(@NonNull String text) {
        String result = text;
        result = result.replaceAll("(?<!\\n)\\s*•\\s*", "\n• ");
        result = result.replaceAll("(?<!\\n)\\s+(\\d+\\.\\s)", "\n$1");
        result = result.replaceAll("\\n{3,}", "\n\n");
        return result.trim();
    }

    @NonNull
    private static String normalizeBullets(@NonNull String text) {
        String[] lines = text.split("\n", -1);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                if (i < lines.length - 1) {
                    builder.append('\n');
                }
                continue;
            }
            if (line.startsWith("* ")) {
                line = "• " + line.substring(2).trim();
            } else if (line.startsWith("- ")) {
                line = "• " + line.substring(2).trim();
            } else if (line.startsWith("•")) {
                line = line.startsWith("• ") ? line : "• " + line.substring(1).trim();
            }
            builder.append(line);
            if (i < lines.length - 1) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }

    @NonNull
    private static CharSequence buildSpannableWithBold(@NonNull String text) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        int index = 0;
        while (index < text.length()) {
            int open = text.indexOf("**", index);
            if (open < 0) {
                builder.append(text.substring(index));
                break;
            }
            builder.append(text.substring(index, open));
            int close = text.indexOf("**", open + 2);
            if (close < 0) {
                builder.append(text.substring(open));
                break;
            }
            int boldStart = builder.length();
            builder.append(text.substring(open + 2, close));
            builder.setSpan(
                    new StyleSpan(Typeface.BOLD),
                    boldStart,
                    builder.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            index = close + 2;
        }
        return builder;
    }
}
