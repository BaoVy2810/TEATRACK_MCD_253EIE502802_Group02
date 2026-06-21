package com.teatrack_mcd_253eie502802_group02.shared.ui;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.client.UserProfile;

public final class ProfileBackHelper {

    private ProfileBackHelper() {
    }

    public static void setupBackToProfile(Activity activity) {
        View backButton = activity.findViewById(R.id.btnProfileBack);
        if (backButton == null) {
            return;
        }
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(activity, UserProfile.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);
            activity.finish();
        });
    }
}
