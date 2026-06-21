package com.teatrack_mcd_253eie502802_group02.shared.ui;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.client.PolicyandTermActivity;

public final class PolicyBackHelper {

    private PolicyBackHelper() {
    }

    public static void setupBackToPolicyTerms(Activity activity) {
        View backButton = activity.findViewById(R.id.btnProfileBack);
        if (backButton == null) {
            return;
        }
        backButton.setOnClickListener(v -> navigateBackToPolicyTerms(activity));
    }

    public static void navigateBackToPolicyTerms(Activity activity) {
        Intent intent = new Intent(activity, PolicyandTermActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
        activity.finish();
    }
}
