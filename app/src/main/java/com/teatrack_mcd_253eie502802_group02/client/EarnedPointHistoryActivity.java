package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
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
import com.teatrack_mcd_253eie502802_group02.adapter.PointHistoryAdapter;
import com.teatrack_mcd_253eie502802_group02.model.PointTransaction;
import com.teatrack_mcd_253eie502802_group02.shared.ui.ProfileBackHelper;
import com.teatrack_mcd_253eie502802_group02.util.UserProfileHelper;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class EarnedPointHistoryActivity extends AppCompatActivity {

    private static final String DB_URL = "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";

    private RecyclerView rvPointsHistory;
    private View layoutEmptyPointHistory;
    private PointHistoryAdapter adapter;
    private List<PointTransaction> transactionList = new ArrayList<>();
    private TextView tvTotalPoints;
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
        loadUserId();
        setupRecyclerView();
        fetchPointsHistory();

        ProfileBackHelper.setupBackToProfile(this);
        findViewById(R.id.btnProfileBack).setOnClickListener(v -> finish());
    }

    private void initViews() {
        rvPointsHistory = findViewById(R.id.rvPointsHistory);
        layoutEmptyPointHistory = findViewById(R.id.layoutEmptyPointHistory);
        tvTotalPoints = findViewById(R.id.tvTotalPoints);

        TextView tvEmptyHistorySubtitle = findViewById(R.id.tvEmptyHistorySubtitle);
        if (tvEmptyHistorySubtitle != null) {
            tvEmptyHistorySubtitle.setText(
                    HtmlCompat.fromHtml(getString(R.string.history_empty_subtitle), HtmlCompat.FROM_HTML_MODE_LEGACY));
        }

        MaterialButton btnHistoryOrderNow = findViewById(R.id.btnHistoryOrderNow);
        if (btnHistoryOrderNow != null) {
            btnHistoryOrderNow.setOnClickListener(v -> openMenu());
        }
    }

    private void openMenu() {
        Intent intent = new Intent(this, Menu.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void loadUserId() {
        SharedPreferences prefs = getSharedPreferences(UserProfileHelper.PREF_NAME, MODE_PRIVATE);
        userId = prefs.getString(UserProfileHelper.KEY_USER_ID, "");
    }

    private void setupRecyclerView() {
        adapter = new PointHistoryAdapter(transactionList);
        rvPointsHistory.setLayoutManager(new LinearLayoutManager(this));
        rvPointsHistory.setAdapter(adapter);
    }

    private void updateEmptyState(boolean isEmpty) {
        if (layoutEmptyPointHistory != null) {
            layoutEmptyPointHistory.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        if (rvPointsHistory != null) {
            rvPointsHistory.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private void fetchPointsHistory() {
        if (userId == null || userId.isEmpty()) {
            updateEmptyState(true);
            return;
        }

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

                updateEmptyState(transactionList.isEmpty());
                Collections.reverse(transactionList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateEmptyState(true);
            }
        });

        DatabaseReference pointsRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference("Users").child(userId).child("points");
        pointsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long points = snapshot.getValue(Long.class);
                long value = points != null ? points : 0L;
                tvTotalPoints.setText(NumberFormat.getNumberInstance(Locale.getDefault()).format(value));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
