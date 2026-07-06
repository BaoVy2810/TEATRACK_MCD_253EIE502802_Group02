package com.teatrack_mcd_253eie502802_group02.client;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.teatrack_mcd_253eie502802_group02.adapter.PointHistoryAdapter;
import com.teatrack_mcd_253eie502802_group02.model.PointTransaction;
import com.teatrack_mcd_253eie502802_group02.shared.ui.ProfileBackHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;

public class EarnedPointHistoryActivity extends BaseActivity {

    private static final String DB_URL = "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";
    
    private RecyclerView rvPointsHistory;
    private PointHistoryAdapter adapter;
    private List<PointTransaction> transactionList = new ArrayList<>();
    private TextView tvProfileName, tvTotalPoints, tvEmptyHistory;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_earned_point_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        initViews();
        loadUserData();
        setupRecyclerView();
        fetchPointsHistory();
        
        ProfileBackHelper.setupBackToProfile(this);
        findViewById(R.id.btnProfileBack).setOnClickListener(v -> finish());
    }

    private void initViews() {
        rvPointsHistory = findViewById(R.id.rvPointsHistory);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvTotalPoints = findViewById(R.id.tvTotalPoints);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        userId = prefs.getString("userId", "");
        String userName = prefs.getString("username", getString(R.string.loyalty_user_name_default));
        tvProfileName.setText(userName);
    }

    private void setupRecyclerView() {
        adapter = new PointHistoryAdapter(transactionList);
        rvPointsHistory.setLayoutManager(new LinearLayoutManager(this));
        rvPointsHistory.setAdapter(adapter);
    }

    private void fetchPointsHistory() {
        if (userId == null || userId.isEmpty()) return;

        DatabaseReference historyRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference("PointsHistory").child(userId);
        
        historyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactionList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    PointTransaction tx = data.getValue(PointTransaction.class);
                    if (tx != null) {
                        transactionList.add(tx);
                    }
                }
                
                if (transactionList.isEmpty()) {
                    tvEmptyHistory.setVisibility(android.view.View.VISIBLE);
                } else {
                    tvEmptyHistory.setVisibility(android.view.View.GONE);
                }

                Collections.reverse(transactionList); // Newest first
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        DatabaseReference pointsRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference("Users").child(userId).child("points");
        pointsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long points = snapshot.getValue(Long.class);
                tvTotalPoints.setText(String.valueOf(points != null ? points : 0));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
