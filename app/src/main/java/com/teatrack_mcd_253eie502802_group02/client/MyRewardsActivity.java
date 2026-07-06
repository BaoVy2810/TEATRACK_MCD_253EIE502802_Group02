package com.teatrack_mcd_253eie502802_group02.client;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.adapter.PromotionClientAdapter;
import com.teatrack_mcd_253eie502802_group02.model.Promotion;

import java.util.ArrayList;
import java.util.List;

import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;

public class MyRewardsActivity extends BaseActivity {

    private static final String DB_URL = "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";
    
    private RecyclerView rvMyRewards;
    private TextView tvEmptyRewards;
    private PromotionClientAdapter adapter;
    private List<Promotion> rewardList = new ArrayList<>();
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_rewards);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        initViews();
        loadUserData();
        setupRecyclerView();
        fetchMyRewards();
    }

    private void initViews() {
        rvMyRewards = findViewById(R.id.rvMyRewards);
        tvEmptyRewards = findViewById(R.id.tvEmptyRewards);
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        userId = prefs.getString("userId", "");
    }

    private void setupRecyclerView() {
        adapter = new PromotionClientAdapter(rewardList);
        rvMyRewards.setLayoutManager(new LinearLayoutManager(this));
        rvMyRewards.setAdapter(adapter);
    }

    private void fetchMyRewards() {
        if (userId == null || userId.isEmpty()) return;

        // In this system, redeemed rewards might be stored under Users/{uid}/vouchers
        // or we just show all active vouchers for now if the requirement is simple.
        // Based on PromotionClient.java, it loads from "vouchers" global node.
        // If we want "My Rewards" to be specific, we'd check a user-specific node.
        
        DatabaseReference userVouchersRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference("Users").child(userId).child("vouchers");
        
        userVouchersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                rewardList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Promotion p = data.getValue(Promotion.class);
                    Boolean isUsed = data.child("isUsed").getValue(Boolean.class);
                    if (p != null && (isUsed == null || !isUsed)) {
                        rewardList.add(p);
                    }
                }
                
                if (rewardList.isEmpty()) {
                    tvEmptyRewards.setVisibility(View.VISIBLE);
                } else {
                    tvEmptyRewards.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
