package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.google.firebase.database.DatabaseException;
import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.adapter.OrderHistoryAdapter;
import com.teatrack_mcd_253eie502802_group02.shared.ui.CartBadgeHelper;
import com.teatrack_mcd_253eie502802_group02.model.FirebaseOrder;
import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderHistory extends BaseActivity {

    private static final String DATABASE_URL =
            "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";
    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_USER_ID = "userId";

    // Views
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvOrders;
    private LinearLayout layoutSkeleton;
    private LinearLayout layoutEmpty;
    private EditText etSearch;

    // Tabs
    private View tabAll, tabPending, tabProcessing, tabReady, tabShipping, tabDelivered, tabCancelled;
    private View activeTab;

    // Data
    private final List<FirebaseOrder> allOrders = new ArrayList<>();
    private OrderHistoryAdapter adapter;
    private String currentStatus = null; // null = all
    private String currentSearch = "";
    private ValueEventListener ordersListener;
    private DatabaseReference ordersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();
        setupAdapter();
        setupTabs();
        setupSearch();
        setupSwipeRefresh();
        setupBackButton();
        setupNavBar();
        CartBadgeHelper.setup(this);
        loadOrders();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CartBadgeHelper.updateBadge(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ordersRef != null && ordersListener != null) {
            ordersRef.removeEventListener(ordersListener);
        }
    }

    // ── Setup ────────────────────────────────────────────────────────────────

    private void bindViews() {
        swipeRefresh   = findViewById(R.id.swipeRefresh);
        rvOrders       = findViewById(R.id.rvOrders);
        layoutSkeleton = findViewById(R.id.layoutSkeleton);
        layoutEmpty    = findViewById(R.id.layoutEmpty);
        etSearch       = findViewById(R.id.etSearchOrder);

        tabAll        = findViewById(R.id.tabAll);
        tabPending    = findViewById(R.id.tabPending);
        tabProcessing = findViewById(R.id.tabProcessing);
        tabReady      = findViewById(R.id.tabReady);
        tabShipping   = findViewById(R.id.tabShipping);
        tabDelivered  = findViewById(R.id.tabDelivered);
        tabCancelled  = findViewById(R.id.tabCancelled);

        activeTab = tabAll;
        initTabLabels();
        setTabActive(tabAll);
    }

    private void initTabLabels() {
        setTabLabel(tabAll, R.string.str_tab_all);
        setTabLabel(tabPending, R.string.str_tab_pending);
        setTabLabel(tabProcessing, R.string.str_tab_processing);
        setTabLabel(tabReady, R.string.str_tab_ready);
        setTabLabel(tabShipping, R.string.str_tab_shipping);
        setTabLabel(tabDelivered, R.string.str_tab_delivered);
        setTabLabel(tabCancelled, R.string.str_tab_cancelled);
    }

    private void setTabLabel(View tab, int labelRes) {
        TextView label = tab.findViewById(R.id.tvTabLabel);
        if (label != null) {
            label.setText(labelRes);
        }
    }

    private void setupAdapter() {
        adapter = new OrderHistoryAdapter(this);
        adapter.setListener(order ->
                startActivity(OrderDetails.newIntent(this, order))
        );
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);
    }

    private void setupBackButton() {
        View btn = findViewById(R.id.btnBack);
        if (btn != null) btn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupNavBar() {
        int[] navIds = {
                R.id.nav_home, R.id.nav_menu, R.id.nav_orders, R.id.nav_promotion, R.id.nav_profile
        };
        NavBarHelper.setupNavBar(this, navIds, R.id.nav_orders, v -> {
            int id = v.getId();
            if (id == R.id.nav_orders) return; // already here
            Intent intent = null;
            if (id == R.id.nav_home)
                intent = new Intent(this, Homepage.class);
            else if (id == R.id.nav_menu)
                intent = new Intent(this, Menu.class);
            else if (id == R.id.nav_profile)
                intent = new Intent(this, UserProfile.class);
            else if (id == R.id.nav_promotion)
                Toast.makeText(this, R.string.str_coming_soon, Toast.LENGTH_SHORT).show();
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }

    private void setupTabs() {
        View.OnClickListener tabClick = v -> {
            if (v.getId() == R.id.tabAll)             selectTab(tabAll,        null);
            else if (v.getId() == R.id.tabPending)    selectTab(tabPending,    "pending");
            else if (v.getId() == R.id.tabProcessing) selectTab(tabProcessing, "processing");
            else if (v.getId() == R.id.tabReady)      selectTab(tabReady,      "ready");
            else if (v.getId() == R.id.tabShipping)   selectTab(tabShipping,   "shipping");
            else if (v.getId() == R.id.tabDelivered)  selectTab(tabDelivered,  "completed");
            else if (v.getId() == R.id.tabCancelled)  selectTab(tabCancelled,  "cancelled");
        };

        tabAll.setOnClickListener(tabClick);
        tabPending.setOnClickListener(tabClick);
        tabProcessing.setOnClickListener(tabClick);
        tabReady.setOnClickListener(tabClick);
        tabShipping.setOnClickListener(tabClick);
        tabDelivered.setOnClickListener(tabClick);
        tabCancelled.setOnClickListener(tabClick);
    }

    private void selectTab(View tab, String status) {
        setTabInactive(activeTab);
        setTabActive(tab);
        activeTab = tab;
        currentStatus = status;
        applyFilter();
    }

    private void setTabActive(View tab) {
        if (tab == null) {
            return;
        }
        tab.setBackgroundResource(R.drawable.bg_order_tab_active);
        TextView label = tab.findViewById(R.id.tvTabLabel);
        TextView count = tab.findViewById(R.id.tvTabCount);
        if (label != null) {
            label.setTextColor(ContextCompat.getColor(this, R.color.white));
            label.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        if (count != null) {
            count.setBackgroundResource(R.drawable.bg_order_tab_count_active);
            count.setTextColor(ContextCompat.getColor(this, R.color.white));
            count.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }

    private void setTabInactive(View tab) {
        if (tab == null) {
            return;
        }
        tab.setBackgroundResource(R.drawable.bg_order_tab_inactive);
        TextView label = tab.findViewById(R.id.tvTabLabel);
        TextView count = tab.findViewById(R.id.tvTabCount);
        if (label != null) {
            label.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            label.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        if (count != null) {
            count.setBackgroundResource(R.drawable.bg_order_tab_count_inactive);
            count.setTextColor(ContextCompat.getColor(this, R.color.on_surface));
            count.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }

    private void updateTabCounts() {
        int cntAll = allOrders.size();
        int cntPending = 0;
        int cntProcessing = 0;
        int cntReady = 0;
        int cntShipping = 0;
        int cntDelivered = 0;
        int cntCancelled = 0;

        for (FirebaseOrder order : allOrders) {
            String status = order.getStatus() != null ? order.getStatus() : "";
            switch (status) {
                case "pending":
                    cntPending++;
                    break;
                case "processing":
                    cntProcessing++;
                    break;
                case "ready":
                    cntReady++;
                    break;
                case "shipping":
                    cntShipping++;
                    break;
                case "completed":
                case "delivered":
                    cntDelivered++;
                    break;
                case "cancelled":
                    cntCancelled++;
                    break;
                default:
                    break;
            }
        }

        setTabCount(tabAll, cntAll);
        setTabCount(tabPending, cntPending);
        setTabCount(tabProcessing, cntProcessing);
        setTabCount(tabReady, cntReady);
        setTabCount(tabShipping, cntShipping);
        setTabCount(tabDelivered, cntDelivered);
        setTabCount(tabCancelled, cntCancelled);
    }

    private void setTabCount(View tab, int count) {
        if (tab == null) {
            return;
        }
        TextView countView = tab.findViewById(R.id.tvTabCount);
        if (countView != null) {
            countView.setText(String.valueOf(count));
        }
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int cnt, int af) {}
            @Override public void onTextChanged(CharSequence s, int st, int bf, int cnt) {
                currentSearch = s.toString().trim().toLowerCase();
                applyFilter();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.brand_blue);
        swipeRefresh.setOnRefreshListener(() -> {
            allOrders.clear();
            loadOrders();
        });
    }

    // ── Firebase ─────────────────────────────────────────────────────────────

    private void loadOrders() {
        showSkeleton(true);

        String userId = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USER_ID, "");

        if (userId.isEmpty()) {
            showSkeleton(false);
            swipeRefresh.setRefreshing(false);
            showEmpty(true);
            return;
        }

        ordersRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("orders");

        if (ordersListener != null) {
            ordersRef.removeEventListener(ordersListener);
        }

        ordersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allOrders.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    FirebaseOrder order;
                    try {
                        order = child.getValue(FirebaseOrder.class);
                    } catch (DatabaseException e) {
                        // Skip orders with incompatible data format (e.g. items stored as array)
                        continue;
                    }
                    if (order == null) continue;
                    order.setId(child.getKey());
                    if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
                        order.setOrderId(child.getKey());
                    }

                    if (userId.equals(order.getUserId())) {
                        allOrders.add(order);
                    }
                }

                // Sort newest first
                Collections.sort(allOrders, (a, b) -> {
                    String ca = a.getCreatedAt() != null ? a.getCreatedAt() : "";
                    String cb = b.getCreatedAt() != null ? b.getCreatedAt() : "";
                    return cb.compareTo(ca);
                });

                showSkeleton(false);
                swipeRefresh.setRefreshing(false);
                updateTabCounts();
                applyFilter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showSkeleton(false);
                swipeRefresh.setRefreshing(false);
                Toast.makeText(OrderHistory.this,
                        getString(R.string.str_load_orders_error), Toast.LENGTH_SHORT).show();
            }
        };

        ordersRef.addValueEventListener(ordersListener);
    }

    // ── Filtering ─────────────────────────────────────────────────────────────

    private void applyFilter() {
        List<FirebaseOrder> filtered = new ArrayList<>();
        for (FirebaseOrder order : allOrders) {
            if (currentStatus != null) {
                String s = order.getStatus();
                boolean match = currentStatus.equals(s);
                // "completed" tab also matches legacy "delivered" status
                if (!match && "completed".equals(currentStatus) && "delivered".equals(s)) match = true;
                if (!match) continue;
            }
            if (!currentSearch.isEmpty()) {
                String id = order.getOrderId() != null ? order.getOrderId().toLowerCase() : "";
                if (!id.contains(currentSearch)) continue;
            }
            filtered.add(order);
        }

        adapter.setOrders(filtered);
        showEmpty(filtered.isEmpty() && layoutSkeleton.getVisibility() != View.VISIBLE);
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private void showSkeleton(boolean show) {
        layoutSkeleton.setVisibility(show ? View.VISIBLE : View.GONE);
        rvOrders.setVisibility(show ? View.GONE : View.VISIBLE);
        if (show) layoutEmpty.setVisibility(View.GONE);
    }

    private void showEmpty(boolean show) {
        layoutEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            rvOrders.setVisibility(View.GONE);
        } else if (layoutSkeleton.getVisibility() != View.VISIBLE) {
            rvOrders.setVisibility(View.VISIBLE);
        }
    }
}
