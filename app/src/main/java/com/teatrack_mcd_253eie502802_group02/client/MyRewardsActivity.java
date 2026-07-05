package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.adapter.PromotionClientAdapter;
import com.teatrack_mcd_253eie502802_group02.model.Promotion;
import com.teatrack_mcd_253eie502802_group02.util.UserProfileHelper;

import java.util.ArrayList;
import java.util.List;

public class MyRewardsActivity extends AppCompatActivity {

    private static final String DB_URL = "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";

    private RecyclerView rvMyRewards;
    private View layoutEmptyRewards;
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

        findViewById(R.id.btnProfileBack).setOnClickListener(v -> finish());

        initViews();
        loadUserData();
        setupRecyclerView();
        fetchMyRewards();
    }

    private void initViews() {
        rvMyRewards = findViewById(R.id.rvMyRewards);
        layoutEmptyRewards = findViewById(R.id.layoutEmptyRewards);
        rvMyRewards.setVisibility(View.GONE);

        MaterialButton btnOrderNow = findViewById(R.id.btnOrderNow);
        btnOrderNow.setOnClickListener(v -> openMenu());
    }

    private void openMenu() {
        Intent intent = new Intent(this, Menu.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void loadUserData() {
        userId = UserProfileHelper.getUserId(this);
    }

    private void setupRecyclerView() {
        adapter = new PromotionClientAdapter(rewardList);
        rvMyRewards.setLayoutManager(new LinearLayoutManager(this));
        rvMyRewards.setAdapter(adapter);
    }

    private void updateEmptyState(boolean isEmpty) {
        if (layoutEmptyRewards != null) {
            layoutEmptyRewards.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        if (rvMyRewards != null) {
            rvMyRewards.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private void fetchMyRewards() {
        if (userId == null || userId.isEmpty()) {
            updateEmptyState(true);
            return;
        }

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
                updateEmptyState(rewardList.isEmpty());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateEmptyState(true);
            }
        });
    }
}
