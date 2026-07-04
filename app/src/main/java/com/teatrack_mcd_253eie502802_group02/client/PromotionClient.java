package com.teatrack_mcd_253eie502802_group02.client;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.adapter.PromotionClientAdapter;
import com.teatrack_mcd_253eie502802_group02.model.Promotion;
import com.teatrack_mcd_253eie502802_group02.shared.QRCodeGenerator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PromotionClient extends AppCompatActivity {

    private static final String DB_URL = "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";
    private static final int POINTS_TO_REDEEM = 8;

    private TextView tvUserName, tvCurrentPoints, tvUserIdDisplay;
    private ImageView ivCodeDisplay;
    private TabLayout tabLayoutCode;
    private GridLayout gridPointIcons;
    private MaterialButton btnRedeem;
    private RecyclerView rvPromotions;

    private String userId;
    private long currentPoints = 0;
    private PromotionClientAdapter promotionAdapter;
    private List<Promotion> promotionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_promotion_client);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        initViews();
        setupNavigation();
        loadUserData();
        setupTabs();
        setupRecyclerView();
        loadFirebaseData();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupNavigation() {
        int[] navIds = {
                R.id.nav_home,
                R.id.nav_menu,
                R.id.nav_orders,
                R.id.nav_promotion,
                R.id.nav_profile
        };
        com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper.setupNavBar(this, navIds, R.id.nav_promotion, v -> {
            int id = v.getId();
            if (id == R.id.nav_promotion) return;

            android.content.Intent intent = null;
            if (id == R.id.nav_home) intent = new android.content.Intent(this, Homepage.class);
            else if (id == R.id.nav_menu) intent = new android.content.Intent(this, Menu.class);
            else if (id == R.id.nav_orders) intent = new android.content.Intent(this, OrderHistory.class);
            else if (id == R.id.nav_profile) intent = new android.content.Intent(this, UserProfile.class);

            if (intent != null) {
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvCurrentPoints = findViewById(R.id.tvCurrentPoints);
        tvUserIdDisplay = findViewById(R.id.tvUserIdDisplay);
        ivCodeDisplay = findViewById(R.id.ivCodeDisplay);
        tabLayoutCode = findViewById(R.id.tabLayoutCode);
        gridPointIcons = findViewById(R.id.gridPointIcons);
        btnRedeem = findViewById(R.id.btnRedeem);
        rvPromotions = findViewById(R.id.rvPromotions);

        btnRedeem.setOnClickListener(v -> handleRedeem());
        findViewById(R.id.btnPointsHistory).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, EarnedPointHistoryActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.btnMyRewards).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, MyRewardsActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        userId = prefs.getString("userId", "");
        String userName = prefs.getString("username", getString(R.string.loyalty_user_name_default));

        tvUserName.setText(userName);
        tvUserIdDisplay.setText(getString(R.string.loyalty_uid_format, userId));

        generateCode(0); // Default QR
    }

    private void setupTabs() {
        tabLayoutCode.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                generateCode(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void generateCode(int position) {
        if (userId == null || userId.isEmpty()) return;

        Bitmap bitmap;
        if (position == 0) {
            bitmap = QRCodeGenerator.generateQRCode(userId, 512);
        } else {
            bitmap = QRCodeGenerator.generateBarcode(userId, 800, 300);
        }

        if (bitmap != null) {
            ivCodeDisplay.setImageBitmap(bitmap);
        }
    }

    private void setupRecyclerView() {
        promotionAdapter = new PromotionClientAdapter(promotionList);
        rvPromotions.setLayoutManager(new LinearLayoutManager(this));
        rvPromotions.setAdapter(promotionAdapter);
    }

    private void loadFirebaseData() {
        if (userId == null || userId.isEmpty()) return;

        // Load Points
        DatabaseReference pointsRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference("Users").child(userId).child("points");
        pointsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long points = snapshot.getValue(Long.class);
                currentPoints = (points != null) ? points : 0;
                updatePointsUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Load Promotions
        DatabaseReference vouchersRef = FirebaseDatabase.getInstance(DB_URL).getReference("vouchers");
        vouchersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                promotionList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Promotion p = data.getValue(Promotion.class);
                    if (p != null) promotionList.add(p);
                }
                promotionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updatePointsUI() {
        tvCurrentPoints.setText(getString(R.string.loyalty_points_format, (int) currentPoints));
        
        // Render 8 point icons
        gridPointIcons.removeAllViews();
        for (int i = 0; i < POINTS_TO_REDEEM; i++) {
            ImageView icon = new ImageView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = dpToPx(40);
            params.height = dpToPx(40);
            params.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
            icon.setLayoutParams(params);

            if (i < currentPoints) {
                icon.setImageResource(R.drawable.points); // Earned
            } else {
                icon.setImageResource(R.drawable.ic_star); // Empty/Star placeholder
                icon.setAlpha(0.3f);
            }
            gridPointIcons.addView(icon);
        }

        btnRedeem.setEnabled(currentPoints >= POINTS_TO_REDEEM);
        if (currentPoints >= POINTS_TO_REDEEM) {
            btnRedeem.setText(R.string.loyalty_btn_redeem_now);
        } else {
            btnRedeem.setText(getString(R.string.loyalty_points_needed_format, (int) (POINTS_TO_REDEEM - currentPoints)));
        }
    }

    private void handleRedeem() {
        if (currentPoints < POINTS_TO_REDEEM) return;

        DatabaseReference userPointsRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference("Users").child(userId).child("points");

        userPointsRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Long points = currentData.getValue(Long.class);
                if (points == null || points < POINTS_TO_REDEEM) {
                    return Transaction.abort();
                }
                currentData.setValue(points - POINTS_TO_REDEEM);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                if (committed) {
                    logRedemption();
                    grantReward();
                    Toast.makeText(PromotionClient.this, R.string.loyalty_redeem_success, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(PromotionClient.this, R.string.loyalty_redeem_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void grantReward() {
        // Create a special promotion object for the free drink
        DatabaseReference userVouchersRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference("Users").child(userId).child("vouchers").push();

        Map<String, Object> reward = new HashMap<>();
        reward.put("id", userVouchersRef.getKey());
        reward.put("title", getString(R.string.loyalty_reward_title));
        reward.put("description", getString(R.string.loyalty_reward_description));
        reward.put("discount", "100%");
        reward.put("date", getString(R.string.loyalty_reward_unlimited));
        reward.put("image", "");
        reward.put("isUsed", false);
        reward.put("type", "personal");
        
        userVouchersRef.setValue(reward);
    }

    private void logRedemption() {
        DatabaseReference historyRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference("PointsHistory").child(userId).push();
        
        Map<String, Object> historyEntry = new HashMap<>();
        historyEntry.put("pointsChange", -POINTS_TO_REDEEM);
        historyEntry.put("createdAt", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        historyEntry.put("type", "redeem");
        historyEntry.put("description", getString(R.string.loyalty_redeem_history_desc));
        
        historyRef.setValue(historyEntry);

        // Also add to My Rewards / Vouchers node for the user if needed
        // For now, just logging the point change is enough as per requirements
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
