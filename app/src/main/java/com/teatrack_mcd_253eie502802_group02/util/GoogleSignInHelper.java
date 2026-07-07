package com.teatrack_mcd_253eie502802_group02.util;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.teatrack_mcd_253eie502802_group02.R;

public final class GoogleSignInHelper {

    private GoogleSignInHelper() {
    }

    public static GoogleSignInClient getClient(Context context) {
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        return GoogleSignIn.getClient(context.getApplicationContext(), options);
    }

    public static void signOutGoogle(Context context) {
        if (context == null) {
            return;
        }
        getClient(context).signOut();
    }
}
