package com.teatrack_mcd_253eie502802_group02;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class TeaTrackApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Kích hoạt tính năng lưu trữ dữ liệu ngoại tuyến của Firebase
        // Dữ liệu sẽ được lưu lại trên máy, giúp ứng dụng load ngay lập tức từ bộ nhớ đệm
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        
        // Bạn cũng có thể "keep synced" một node cụ thể để nó luôn được cập nhật ngầm
        FirebaseDatabase.getInstance().getReference("products").keepSynced(true);
    }
}
