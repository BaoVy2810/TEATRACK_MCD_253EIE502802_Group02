package com.teatrack_mcd_253eie502802_group02;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.teatrack_mcd_253eie502802_group02.data.FirebaseProductRepository;

public class TeaTrackApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable disk persistence (must be called before any Firebase usage).
        FirebaseDatabase.getInstance(FirebaseProductRepository.DB_URL)
                .setPersistenceEnabled(true);

        // keepSynced so the products node stays fresh in the local disk cache.
        new FirebaseProductRepository().prefetch();
    }
}
