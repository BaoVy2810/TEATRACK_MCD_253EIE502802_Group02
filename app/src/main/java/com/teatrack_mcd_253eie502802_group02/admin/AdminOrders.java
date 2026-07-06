package com.teatrack_mcd_253eie502802_group02.admin;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.FirebaseOrder;
import com.teatrack_mcd_253eie502802_group02.shared.ui.HeaderMenuHelper;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminOrders extends AppCompatActivity {

    private static final String DB_URL =
            "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";

    private RecyclerView    rvOrders;
    private LinearLayout    layoutEmptyOrders;
    private EditText        etSearchOrders;
    private TextView        tabAll, tabPending, tabProcessing, tabShipping, tabCompleted, tabCancelled;

    private AdminOrderAdapter       adapter;
    private final List<FirebaseOrder> allOrders = new ArrayList<>();
    private String currentFilter = "all";

    private DatabaseReference ordersRef;
    private ValueEventListener ordersListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);
        com.teatrack_mcd_253eie502802_group02.shared.ui.AdminInsetsHelper.apply(this);

        bindViews();
        setupRecyclerView();
        setupFilterTabs();
        setupSearch();
        setupBottomNavigation();
        HeaderMenuHelper.setupProfileMenu(this);
        loadOrders();
    }

    private void bindViews() {
        rvOrders          = findViewById(R.id.rvOrders);
        layoutEmptyOrders = findViewById(R.id.layoutEmptyOrders);
        etSearchOrders    = findViewById(R.id.etSearchOrders);
        tabAll            = findViewById(R.id.tabAll);
        tabPending        = findViewById(R.id.tabPending);
        tabProcessing     = findViewById(R.id.tabProcessing);
        tabShipping       = findViewById(R.id.tabShipping);
        tabCompleted      = findViewById(R.id.tabCompleted);
        tabCancelled      = findViewById(R.id.tabCancelled);
    }

    private void setupRecyclerView() {
        adapter = new AdminOrderAdapter(this, new ArrayList<>(), this::onOrderAction);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);
    }

    // ── Filter tabs ───────────────────────────────────────────────────────────

    private void setupFilterTabs() {
        View.OnClickListener tabClick = v -> {
            int id = v.getId();
            if      (id == R.id.tabAll)        currentFilter = "all";
            else if (id == R.id.tabPending)    currentFilter = "pending";
            else if (id == R.id.tabProcessing) currentFilter = "processing";
            else if (id == R.id.tabShipping)   currentFilter = "shipping";
            else if (id == R.id.tabCompleted)  currentFilter = "completed";
            else if (id == R.id.tabCancelled)  currentFilter = "cancelled";
            updateTabSelection();
            applyFilter();
        };

        tabAll.setOnClickListener(tabClick);
        tabPending.setOnClickListener(tabClick);
        tabProcessing.setOnClickListener(tabClick);
        tabShipping.setOnClickListener(tabClick);
        tabCompleted.setOnClickListener(tabClick);
        tabCancelled.setOnClickListener(tabClick);

        updateTabSelection();
    }

    private void updateTabSelection() {
        TextView[] tabs    = {tabAll, tabPending, tabProcessing, tabShipping, tabCompleted, tabCancelled};
        String[]   filters = {"all", "pending", "processing", "shipping", "completed", "cancelled"};
        int[]      icons   = {
                0,                          // All — no icon
                R.drawable.pending_tab,
                R.drawable.processing_tab,
                R.drawable.shipping_tab,
                R.drawable.finished_tab,
                R.drawable.cancel
        };

        int colorActive   = getColor(R.color.white);
        int colorInactive = getColor(R.color.text_secondary);

        for (int i = 0; i < tabs.length; i++) {
            boolean active = filters[i].equals(currentFilter);
            tabs[i].setBackgroundResource(active
                    ? R.drawable.bg_admin_order_tab_active
                    : R.drawable.bg_admin_order_tab_inactive);
            int textColor = active ? colorActive : colorInactive;
            tabs[i].setTextColor(textColor);
            tabs[i].setTypeface(null, active
                    ? android.graphics.Typeface.BOLD
                    : android.graphics.Typeface.NORMAL);

            // Tint icon to match text color (skip if no icon)
            if (icons[i] != 0) {
                Drawable icon = AppCompatResources.getDrawable(this, icons[i]);
                if (icon != null) {
                    icon = DrawableCompat.wrap(icon.mutate());
                    DrawableCompat.setTint(icon, textColor);
                    int size = Math.round(14 * getResources().getDisplayMetrics().density);
                    icon.setBounds(0, 0, size, size);
                    tabs[i].setCompoundDrawables(icon, null, null, null);
                }
            } else {
                tabs[i].setCompoundDrawables(null, null, null, null);
            }
        }
    }

    private void updateTabCounts() {
        int cntAll = allOrders.size();
        int cntPending = 0, cntProcessing = 0, cntShipping = 0, cntCompleted = 0, cntCancelled = 0;
        for (FirebaseOrder o : allOrders) {
            String s = o.getStatus() != null ? o.getStatus() : "";
            switch (s) {
                case "pending":    cntPending++;    break;
                case "processing":
                case "ready":      cntProcessing++; break;
                case "shipping":   cntShipping++;   break;
                case "completed":  cntCompleted++;  break;
                case "cancelled":  cntCancelled++;  break;
            }
        }
        tabAll.setText("All (" + cntAll + ")");
        tabPending.setText("Pending (" + cntPending + ")");
        tabProcessing.setText("Processing (" + cntProcessing + ")");
        tabShipping.setText("Shipping (" + cntShipping + ")");
        tabCompleted.setText("Completed (" + cntCompleted + ")");
        tabCancelled.setText("Cancelled (" + cntCancelled + ")");
    }

    // ── Search ────────────────────────────────────────────────────────────────

    private void setupSearch() {
        etSearchOrders.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { applyFilter(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void applyFilter() {
        String query = etSearchOrders.getText().toString().trim().toLowerCase();
        List<FirebaseOrder> filtered = new ArrayList<>();

        for (FirebaseOrder order : allOrders) {
            String status = order.getStatus() != null ? order.getStatus() : "";
            boolean matchesStatus = currentFilter.equals("all")
                    || status.equalsIgnoreCase(currentFilter)
                    || (currentFilter.equals("processing") && status.equalsIgnoreCase("ready"));

            String orderId = order.getOrderId() != null ? order.getOrderId().toLowerCase() : "";
            String name    = order.getCustomerName() != null ? order.getCustomerName().toLowerCase() : "";
            String phone   = order.getCustomerPhone() != null ? order.getCustomerPhone() : "";
            boolean matchesQuery = query.isEmpty()
                    || orderId.contains(query)
                    || name.contains(query)
                    || phone.contains(query);

            if (matchesStatus && matchesQuery) filtered.add(order);
        }

        adapter.updateData(filtered);
        layoutEmptyOrders.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        rvOrders.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    // ── Load from Firebase ────────────────────────────────────────────────────

    private void loadOrders() {
        ordersRef = FirebaseDatabase.getInstance(DB_URL).getReference("orders");
        ordersListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                allOrders.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    FirebaseOrder order = child.getValue(FirebaseOrder.class);
                    if (order != null) {
                        order.setId(child.getKey());
                        if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
                            order.setOrderId(child.getKey());
                        }
                        allOrders.add(order);
                    }
                }
                // Sort newest first by createdAt
                Collections.sort(allOrders, (a, b) -> {
                    String ca = a.getCreatedAt() != null ? a.getCreatedAt() : "";
                    String cb = b.getCreatedAt() != null ? b.getCreatedAt() : "";
                    return cb.compareTo(ca);
                });
                updateTabCounts();
                applyFilter();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AdminOrders.this,
                        getString(R.string.str_load_orders_error), Toast.LENGTH_SHORT).show();
            }
        };
        ordersRef.addValueEventListener(ordersListener);
    }

    // ── Action handler ────────────────────────────────────────────────────────

    private void onOrderAction(FirebaseOrder order, String action) {
        switch (action) {
            case AdminOrderAdapter.ACTION_CONFIRM:
                confirmOrder(order);
                break;
            case AdminOrderAdapter.ACTION_VIEW_DETAIL:
            case AdminOrderAdapter.ACTION_UPDATE_STATUS:
                openOrderDetail(order);
                break;
            case AdminOrderAdapter.ACTION_CANCEL:
                showCancelDialog(order);
                break;
        }
    }

    private void confirmOrder(FirebaseOrder order) {
        String key = getOrderKey(order);
        if (key == null) return;
        FirebaseDatabase.getInstance(DB_URL)
                .getReference("orders")
                .child(key)
                .child("status")
                .setValue("processing")
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Order confirmed", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showCancelDialog(FirebaseOrder order) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_cancel_order, null);
        dialog.setContentView(dialogView);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvTitle   = dialogView.findViewById(R.id.tvDeleteTitle);
        TextView tvMessage = dialogView.findViewById(R.id.tvDeleteMessage);
        TextView tvOrderId = dialogView.findViewById(R.id.tvCancelOrderId);
        com.google.android.material.button.MaterialButton btnConfirm =
                dialogView.findViewById(R.id.btnConfirmDelete);

        tvTitle.setText("Cancel Order");
        String orderId = order.getOrderId() != null ? order.getOrderId() : "";
        tvMessage.setText("Are you sure you want to cancel order");
        if (tvOrderId != null) {
            tvOrderId.setText("#" + orderId + "?");
        }
        btnConfirm.setText("Cancel");
        btnConfirm.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(getColor(R.color.danger)));

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            String key = getOrderKey(order);
            if (key == null) { dialog.dismiss(); return; }
            FirebaseDatabase.getInstance(DB_URL)
                    .getReference("orders")
                    .child(key)
                    .child("status")
                    .setValue("cancelled")
                    .addOnSuccessListener(unused -> {
                        dialog.dismiss();
                        Toast.makeText(this, "Order cancelled", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        dialog.dismiss();
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.76),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void openOrderDetail(FirebaseOrder order) {
        Intent intent = new Intent(this, AdminOrderDetailActivity.class);
        intent.putExtra("order", order);
        startActivity(intent);
    }

    private String getOrderKey(FirebaseOrder order) {
        return com.teatrack_mcd_253eie502802_group02.util.FirebaseOrderHelper.resolveFirebaseKey(order);
    }

    // ── Bottom navigation ─────────────────────────────────────────────────────

    private void setupBottomNavigation() {
        int[] navItemIds = {
                R.id.nav_dashboard,
                R.id.nav_products,
                R.id.nav_orders,
                R.id.nav_account,
                R.id.nav_forum,
                R.id.nav_branch,
                R.id.nav_feedbacks,
                R.id.nav_promotion
        };

        NavBarHelper.setupNavBar(this, navItemIds, R.id.nav_orders, v -> {
            int id = v.getId();
            if (id == R.id.nav_orders) return;

            Class<?> destination = null;
            if      (id == R.id.nav_dashboard)  destination = AdminDashboard.class;
            else if (id == R.id.nav_products)   destination = AdminProduct.class;
            else if (id == R.id.nav_account)    destination = AdminAccount.class;
            else if (id == R.id.nav_forum)      destination = AdminBlog.class;
            else if (id == R.id.nav_branch)     destination = AdminAgency.class;
            else if (id == R.id.nav_feedbacks)  destination = AdminComplaints.class;
            else if (id == R.id.nav_promotion)  destination = AdminPromotion.class;

            if (destination != null) {
                NavBarHelper.navigateWithoutTransition(this, destination);
            }
        });
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ordersRef != null && ordersListener != null) {
            ordersRef.removeEventListener(ordersListener);
        }
    }
}
