package com.teatrack_mcd_253eie502802_group02.admin;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;
import com.teatrack_mcd_253eie502802_group02.adapter.BranchAnalysisAdapter;
import com.teatrack_mcd_253eie502802_group02.adapter.RecentOrderAdapter;
import com.teatrack_mcd_253eie502802_group02.databinding.ActivityAdminDashboardBinding;
import com.teatrack_mcd_253eie502802_group02.model.Agency;
import com.teatrack_mcd_253eie502802_group02.model.Branch;
import com.teatrack_mcd_253eie502802_group02.model.FirebaseOrder;
import com.teatrack_mcd_253eie502802_group02.util.DateTimeHelper;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;

public class AdminDashboard extends BaseActivity {

    private static final String UNASSIGNED_BRANCH_ID = "__unassigned_branch__";

    private ActivityAdminDashboardBinding binding;
    private DatabaseReference mDatabase;
    private final List<FirebaseOrder> allOrders = new ArrayList<>();
    private final List<Agency> allBranches = new ArrayList<>();
    private final java.util.Map<String, String> productCategoryMap = new java.util.HashMap<>();

    private String activeLineRange = "Weekly";
    private String activeBarRange = "Last 7 Days";
    private long customStartMillis = 0L;
    private long customEndMillis = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String dbUrl = "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";
        mDatabase = FirebaseDatabase.getInstance(dbUrl).getReference();

        setupStatCards();
        setupLineChart();
        setupBarChart();
        setupDonutChart();
        setupRecentOrders();
        setupBottomNavigation();
        setupHeader();

        // Setup Time Range Toggle for Line Chart
        setupLineChartToggle();

        // Setup Time Range Picker for Bar Chart
        binding.btnBarChartRange.setOnClickListener(this::showTimeRangePicker);

        // Fetch real data from Firebase
        fetchDashboardData();
    }

    private void showTimeRangePicker(View anchor) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.layout_time_range_picker, null);
        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(20);

        String currentRange = binding.btnBarChartRange.getText().toString();

        setupPickerItem(popupView, R.id.btnToday, "Today", currentRange, popupWindow);
        setupPickerItem(popupView, R.id.btnLast7Days, "Last 7 Days", currentRange, popupWindow);
        setupPickerItem(popupView, R.id.btnLast30Days, "Last 30 Days", currentRange, popupWindow);
        setupPickerItem(popupView, R.id.btnLast90Days, "Last 90 Days", currentRange, popupWindow);
        setupPickerItem(popupView, R.id.btnYearToDate, "Year to Date", currentRange, popupWindow);

        popupView.findViewById(R.id.btnCustomRange).setOnClickListener(v -> {
            popupWindow.dismiss();
            showCustomDateRangePicker();
        });

        popupWindow.showAsDropDown(anchor, 0, 10);
    }

    private void showCustomDateRangePicker() {
        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Select Date Range")
                        .setTheme(R.style.CustomMaterialCalendar)
                        .build();

        dateRangePicker.show(getSupportFragmentManager(), "DATE_RANGE_PICKER");

        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            if (selection.first != null && selection.second != null) {
                customStartMillis = selection.first;
                customEndMillis = selection.second;
                activeBarRange = "Custom";

                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                String startDate = sdf.format(new Date(selection.first));
                String endDate = sdf.format(new Date(selection.second));

                String rangeLabel = startDate + " - " + endDate;
                binding.btnBarChartRange.setText(rangeLabel);

                // Cập nhật dữ liệu dựa trên khoảng ngày đã chọn
                filterAndDisplayData();
            }
        });
    }

    private void setupPickerItem(View root, int viewId, String rangeName, String currentRange, PopupWindow popup) {
        View itemView = root.findViewById(viewId);
        if (itemView == null) return;

        android.widget.TextView tv = null;
        android.widget.ImageView iv = null;

        if (viewId == R.id.btnToday) { tv = root.findViewById(R.id.tvToday); iv = root.findViewById(R.id.icToday); }
        else if (viewId == R.id.btnLast7Days) { tv = root.findViewById(R.id.tvLast7Days); iv = root.findViewById(R.id.icLast7Days); }
        else if (viewId == R.id.btnLast30Days) { tv = root.findViewById(R.id.tvLast30Days); iv = root.findViewById(R.id.icLast30Days); }
        else if (viewId == R.id.btnLast90Days) { tv = root.findViewById(R.id.tvLast90Days); iv = root.findViewById(R.id.icLast90Days); }
        else if (viewId == R.id.btnYearToDate) { tv = root.findViewById(R.id.tvYearToDate); iv = root.findViewById(R.id.icYearToDate); }

        String displayName = tv != null ? tv.getText().toString() : rangeName;
        boolean isActive = displayName.equalsIgnoreCase(currentRange);

        if (isActive) {
            itemView.setBackgroundResource(R.drawable.bg_time_range_active);
            if (tv != null) {
                tv.setTextColor(Color.WHITE);
                tv.setTypeface(null, android.graphics.Typeface.BOLD);
            }
            if (iv != null) iv.setVisibility(View.VISIBLE);
        } else {
            itemView.setBackgroundResource(android.R.color.transparent);
            if (tv != null) {
                tv.setTextColor(Color.parseColor("#007AFF"));
                tv.setTypeface(null, android.graphics.Typeface.NORMAL);
            }
            if (iv != null) iv.setVisibility(View.GONE);
        }

        itemView.setOnClickListener(v -> {
            binding.btnBarChartRange.setText(displayName);
            popup.dismiss();
            activeBarRange = rangeName;
            filterAndDisplayData();
        });
    }

    private void setupLineChartToggle() {
        binding.btnToggleMonthly.setOnClickListener(v -> updateToggleState(binding.btnToggleMonthly));
        binding.btnToggleWeekly.setOnClickListener(v -> updateToggleState(binding.btnToggleWeekly));
        binding.btnToggleToday.setOnClickListener(v -> updateToggleState(binding.btnToggleToday));
    }

    private void updateToggleState(android.widget.TextView activeBtn) {
        android.widget.TextView[] buttons = {binding.btnToggleMonthly, binding.btnToggleWeekly, binding.btnToggleToday};
        for (android.widget.TextView btn : buttons) {
            if (btn == activeBtn) {
                btn.setBackgroundResource(R.drawable.bg_toggle_item_active_white);
                btn.setTextColor(Color.parseColor("#007AFF"));
            } else {
                btn.setBackgroundResource(R.drawable.bg_toggle_item_inactive_transparent);
                btn.setTextColor(Color.WHITE);
            }
        }
        
        if (activeBtn == binding.btnToggleMonthly) {
            activeLineRange = "Monthly";
        } else if (activeBtn == binding.btnToggleWeekly) {
            activeLineRange = "Weekly";
        } else if (activeBtn == binding.btnToggleToday) {
            activeLineRange = "Today";
        }
        
        filterAndDisplayData();
    }

    private void setupHeader() {
        View header = findViewById(R.id.headerAdmin);
        if (header != null) {
            com.teatrack_mcd_253eie502802_group02.shared.ui.HeaderMenuHelper.setupProfileMenu(this);
            header.findViewById(R.id.btn_notification).setOnClickListener(v -> Toast.makeText(this, "Opening Notifications...", Toast.LENGTH_SHORT).show());
        }
    }

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

        NavBarHelper.setupNavBar(this, navItemIds, R.id.nav_dashboard, v -> {
            int id = v.getId();
            if (id == R.id.nav_dashboard) return;

            Class<?> destination = null;
            if (id == R.id.nav_products) destination = AdminProduct.class;
            else if (id == R.id.nav_orders) destination = AdminOrders.class;
            else if (id == R.id.nav_account) destination = AdminAccount.class;
            else if (id == R.id.nav_forum) destination = AdminBlog.class;
            else if (id == R.id.nav_branch) destination = AdminAgency.class;
            else if (id == R.id.nav_feedbacks) destination = AdminComplaints.class;
            else if (id == R.id.nav_promotion) destination = AdminPromotion.class;

            if (destination != null) {
                startActivity(new Intent(this, destination));
                finish();
            }
        });
    }

    private void fetchDashboardData() {
        android.util.Log.d("AdminDashboard", "Fetching data from Firebase...");

        mDatabase.child("agencies").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                android.util.Log.d("AdminDashboard", "Branches count: " + snapshot.getChildrenCount());
                allBranches.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Agency agency = data.getValue(Agency.class);
                    if (agency != null) {
                        if (agency.getId() == null || agency.getId().isEmpty()) {
                            agency.setId(data.getKey());
                        }
                        allBranches.add(agency);
                    }
                }
                binding.rvBranchAnalysis.setLayoutManager(new LinearLayoutManager(AdminDashboard.this));
                if (!allOrders.isEmpty()) {
                    filterAndDisplayData();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                android.util.Log.e("AdminDashboard", "Branches error: " + error.getMessage());
            }
        });

        mDatabase.child("products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                android.util.Log.d("AdminDashboard", "Products count: " + count);
                binding.cardProducts.tvStatValue.setText(String.valueOf(count));
                
                productCategoryMap.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    com.teatrack_mcd_253eie502802_group02.model.Product product = data.getValue(com.teatrack_mcd_253eie502802_group02.model.Product.class);
                    if (product != null && product.getId() != null) {
                        productCategoryMap.put(product.getId(), product.getCategory());
                    } else if (product != null && data.getKey() != null) {
                        productCategoryMap.put(data.getKey(), product.getCategory());
                    }
                }
                if (!allOrders.isEmpty()) {
                    filterAndDisplayData();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                android.util.Log.e("AdminDashboard", "Products error: " + error.getMessage());
            }
        });

        mDatabase.child("orders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                android.util.Log.d("AdminDashboard", "Orders count: " + snapshot.getChildrenCount());
                allOrders.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    FirebaseOrder order = data.getValue(FirebaseOrder.class);
                    if (order != null) allOrders.add(0, order);
                }
                filterAndDisplayData();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                android.util.Log.e("AdminDashboard", "Orders error: " + error.getMessage());
            }
        });
    }

    private long getStartOfDay(long millis) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
        cal.setTimeInMillis(millis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private boolean isCompletedOrder(FirebaseOrder order) {
        if (order == null || order.getStatus() == null) return false;
        return "completed".equalsIgnoreCase(order.getStatus())
                || "delivered".equalsIgnoreCase(order.getStatus());
    }

    private String resolveOrderAgencyId(FirebaseOrder order) {
        if (order == null) return "";

        String agencyId = order.getAgencyId();
        if (agencyId != null && !agencyId.isEmpty()) {
            for (Agency agency : allBranches) {
                if (agencyId.equals(agency.getId())) {
                    return agencyId;
                }
            }
        }

        String branchAddr = order.getBranchAddress();
        if (branchAddr == null || branchAddr.isEmpty()) {
            branchAddr = order.getCustomerAddress();
        }
        if (branchAddr == null || branchAddr.isEmpty()) return "";

        for (Agency agency : allBranches) {
            String address = agency.getAddress();
            String name = agency.getName();
            if ((address != null && branchAddr.equalsIgnoreCase(address))
                    || (name != null && branchAddr.equalsIgnoreCase(name))) {
                return agency.getId();
            }
        }

        return "";
    }

    /** Count revenue from the amount paid on the order. */
    private double getOrderRevenue(FirebaseOrder order) {
        double total = Math.max(0, order.getTotal());
        if (total > 0) return total;

        double subtotal = Math.max(0, order.getSubtotal());
        if (subtotal > 0 || order.getShipping() > 0 || order.getDiscount() > 0) {
            return Math.max(0, subtotal + order.getShipping() - order.getDiscount());
        }

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            double sum = 0;
            for (com.teatrack_mcd_253eie502802_group02.model.FirebaseOrderItem item : order.getItems()) {
                sum += item.getLineTotal();
            }
            return Math.max(0, sum + order.getShipping() - order.getDiscount());
        }

        double val = 0;
        if (val == 0 && order.getMeta() != null && order.getMeta().contains("•")) {
            try {
                String meta = order.getMeta();
                String priceStr = meta.substring(meta.lastIndexOf("•") + 1).trim()
                        .replace("đ", "").replace(".", "");
                val = Double.parseDouble(priceStr);
            } catch (Exception ignored) {}
        }
        return val;
    }

    private void filterAndDisplayData() {
        double currentRevenue = 0, prevRevenue = 0;
        int currentOrders = 0, prevOrders = 0;
        int currentSold = 0, prevSold = 0;
        List<FirebaseOrder> filteredOrders = new ArrayList<>();
        java.util.Map<String, Float> categorySales = new java.util.HashMap<>();
        
        // Data for Branch Analysis
        java.util.Map<String, Double> branchRevenueMap = new java.util.HashMap<>();
        java.util.Map<String, Integer> branchOrderCountMap = new java.util.HashMap<>();

        long now = System.currentTimeMillis();
        long todayStart = getStartOfDay(now);

        long currentStart = 0;
        long currentEnd = now;
        long prevStart = 0;
        long prevEnd = 0;

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
        cal.setTimeInMillis(now);

        if ("Today".equals(activeBarRange)) {
            currentStart = todayStart;
            currentEnd = now;
            prevStart = todayStart - 24 * 3600 * 1000L;
            prevEnd = todayStart;
        } else if ("Last 7 Days".equals(activeBarRange)) {
            currentStart = todayStart - 6 * 24 * 3600 * 1000L;
            currentEnd = now;
            prevStart = currentStart - 7 * 24 * 3600 * 1000L;
            prevEnd = currentStart;
        } else if ("Last 30 Days".equals(activeBarRange)) {
            currentStart = todayStart - 29 * 24 * 3600 * 1000L;
            currentEnd = now;
            prevStart = currentStart - 30 * 24 * 3600 * 1000L;
            prevEnd = currentStart;
        } else if ("Last 90 Days".equals(activeBarRange)) {
            currentStart = todayStart - 89 * 24 * 3600 * 1000L;
            currentEnd = now;
            prevStart = currentStart - 90 * 24 * 3600 * 1000L;
            prevEnd = currentStart;
        } else if ("Year to Date".equals(activeBarRange)) {
            cal.set(Calendar.MONTH, Calendar.JANUARY);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            currentStart = cal.getTimeInMillis();
            currentEnd = now;
            
            Calendar prevCal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
            prevCal.setTimeInMillis(now);
            prevCal.add(Calendar.YEAR, -1);
            prevEnd = prevCal.getTimeInMillis();
            
            prevCal.set(Calendar.MONTH, Calendar.JANUARY);
            prevCal.set(Calendar.DAY_OF_MONTH, 1);
            prevCal.set(Calendar.HOUR_OF_DAY, 0);
            prevCal.set(Calendar.MINUTE, 0);
            prevCal.set(Calendar.SECOND, 0);
            prevCal.set(Calendar.MILLISECOND, 0);
            prevStart = prevCal.getTimeInMillis();
        } else if ("Custom".equals(activeBarRange)) {
            currentStart = getStartOfDay(customStartMillis);
            Calendar endCal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
            endCal.setTimeInMillis(customEndMillis);
            endCal.set(Calendar.HOUR_OF_DAY, 23);
            endCal.set(Calendar.MINUTE, 59);
            endCal.set(Calendar.SECOND, 59);
            endCal.set(Calendar.MILLISECOND, 999);
            currentEnd = endCal.getTimeInMillis();
            
            long duration = currentEnd - currentStart;
            prevStart = currentStart - duration - 1000L;
            prevEnd = currentStart;
        }

        for (FirebaseOrder order : allOrders) {
            String createdAt = order.getCreatedAt();
            if (createdAt == null || createdAt.isEmpty()) {
                createdAt = order.getDate();
            }
            if (createdAt == null) continue;

            long orderTime = DateTimeHelper.toEpochMillis(createdAt);

            boolean isCompleted = isCompletedOrder(order);

            double val = getOrderRevenue(order);

            if (orderTime >= currentStart && orderTime <= currentEnd) {
                if (isCompleted) {
                    currentRevenue += val;
                    currentOrders++;
                }

                if (isCompleted) {
                    String agencyId = resolveOrderAgencyId(order);
                    if (agencyId == null || agencyId.isEmpty()) {
                        agencyId = UNASSIGNED_BRANCH_ID;
                    }
                    branchOrderCountMap.put(agencyId, branchOrderCountMap.getOrDefault(agencyId, 0) + 1);
                    branchRevenueMap.put(agencyId, branchRevenueMap.getOrDefault(agencyId, 0.0) + val);
                }
                
                if (order.getItems() != null) {
                    double orderItemsSubtotal = 0;
                    if (isCompleted) {
                        for (com.teatrack_mcd_253eie502802_group02.model.FirebaseOrderItem item : order.getItems()) {
                            orderItemsSubtotal += Math.max(0, item.getLineTotal());
                        }
                    }

                    for (com.teatrack_mcd_253eie502802_group02.model.FirebaseOrderItem item : order.getItems()) {
                        if (isCompleted) {
                            currentSold += item.getQuantity();
                        }
                        
                        if (isCompleted) {
                            String category = productCategoryMap.get(item.getProductId());
                            if (category != null) {
                                double itemRevenue = orderItemsSubtotal > 0
                                        ? val * Math.max(0, item.getLineTotal()) / orderItemsSubtotal
                                        : 0;
                                categorySales.put(category, categorySales.getOrDefault(category, 0f) + (float) itemRevenue);
                            }
                        }
                    }
                } else {
                    if (isCompleted) {
                        currentSold++;
                    }
                }
                if (isCompleted) {
                    filteredOrders.add(order);
                }
            } else if (orderTime >= prevStart && orderTime < prevEnd) {
                if (isCompleted) {
                    prevRevenue += val;
                    prevOrders++;
                }
                if (order.getItems() != null) {
                    for (com.teatrack_mcd_253eie502802_group02.model.FirebaseOrderItem item : order.getItems()) {
                        if (isCompleted) {
                            prevSold += item.getQuantity();
                        }
                    }
                } else {
                    if (isCompleted) {
                        prevSold++;
                    }
                }
            }
        }

        DecimalFormat df = new DecimalFormat("#,###");
        binding.cardRevenue.tvStatValue.setText(df.format(currentRevenue) + "đ");
        binding.cardOrders.tvStatValue.setText(String.valueOf(currentOrders));
        binding.cardSold.tvStatValue.setText(String.valueOf(currentSold));

        // Update Deltas and Labels
        int labelRes = R.string.stat_mom;
        if ("Today".equals(activeBarRange)) {
            labelRes = R.string.stat_dod;
        } else if ("Last 7 Days".equals(activeBarRange)) {
            labelRes = R.string.stat_wow;
        }
        
        updateStatComparison(binding.cardRevenue.getRoot(), currentRevenue, prevRevenue, labelRes);
        updateStatComparison(binding.cardOrders.getRoot(), currentOrders, prevOrders, labelRes);
        updateStatComparison(binding.cardSold.getRoot(), currentSold, prevSold, labelRes);

        if (!filteredOrders.isEmpty()) {
            List<FirebaseOrder> displayList = filteredOrders.subList(0, Math.min(filteredOrders.size(), 5));
            binding.rvRecentOrders.setAdapter(new RecentOrderAdapter(displayList));
        } else {
            binding.rvRecentOrders.setAdapter(new RecentOrderAdapter(new ArrayList<>()));
        }

        // Update Branch Analysis Table
        List<Branch> branchDisplayList = new ArrayList<>();
        java.util.Set<String> displayedAgencyIds = new java.util.HashSet<>();
        for (Agency agency : allBranches) {
            String id = agency.getId();
            int count = branchOrderCountMap.getOrDefault(id, 0);
            double rev = branchRevenueMap.getOrDefault(id, 0.0);
            branchDisplayList.add(new Branch(agency.getName(), count, df.format(rev) + "đ"));
        }
        for (Agency agency : allBranches) {
            displayedAgencyIds.add(agency.getId());
        }
        for (String id : branchRevenueMap.keySet()) {
            if (displayedAgencyIds.contains(id)) continue;
            int count = branchOrderCountMap.getOrDefault(id, 0);
            double rev = branchRevenueMap.getOrDefault(id, 0.0);
            String label = UNASSIGNED_BRANCH_ID.equals(id) ? "Unassigned Branch" : id;
            branchDisplayList.add(new Branch(label, count, df.format(rev) + "Ä‘"));
        }
        binding.rvBranchAnalysis.setAdapter(new BranchAnalysisAdapter(branchDisplayList));
        
        updateLineChart();
        updateBarChart();
        updateDonutChartWithData(categorySales, currentRevenue);
    }

    private void updateStatComparison(View cardView, double current, double prev, int labelRes) {
        android.widget.TextView tvDelta = cardView.findViewById(R.id.tvStatDelta);
        android.widget.TextView tvLabel = cardView.findViewById(R.id.tvStatLabel);
        android.widget.ImageView ivTrend = cardView.findViewById(R.id.ivTrendIcon);

        tvLabel.setText(getString(labelRes));

        if (prev == 0) {
            tvDelta.setText(current > 0 ? "+100%" : "0%");
        } else {
            double percent = ((current - prev) / prev) * 100;
            String sign = percent >= 0 ? "+" : "";
            tvDelta.setText(sign + new DecimalFormat("#.#").format(percent) + "%");

            if (ivTrend != null) {
                if (percent < 0) {
                    ivTrend.setRotation(180);
                    tvDelta.setTextColor(Color.parseColor("#FF3B30")); // Red for decrease
                } else {
                    ivTrend.setRotation(0);
                    tvDelta.setTextColor(Color.WHITE);
                }
            }
        }
    }

    private void updateLineChart() {
        long now = System.currentTimeMillis();
        long todayStart = getStartOfDay(now);
        
        int numPoints = 7;
        String[] labels = new String[7];
        float[] revenueData;
        
        if ("Today".equals(activeLineRange)) {
            numPoints = 24;
            labels = new String[24];
            for (int h = 0; h < 24; h++) {
                labels[h] = String.format(Locale.US, "%02d:00", h);
            }
            revenueData = new float[24];
            
            for (FirebaseOrder order : allOrders) {
                if (!isCompletedOrder(order)) continue;
                String createdAt = order.getCreatedAt() != null ? order.getCreatedAt() : order.getDate();
                if (createdAt == null) continue;
                long oTime = DateTimeHelper.toEpochMillis(createdAt);
                if (oTime >= todayStart && oTime <= now) {
                    Calendar oCal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
                    oCal.setTimeInMillis(oTime);
                    int hour = oCal.get(Calendar.HOUR_OF_DAY);
                    
                    double val = getOrderRevenue(order);
                    revenueData[hour] += (float) val;
                }
            }
        } else if ("Weekly".equals(activeLineRange)) {
            numPoints = 7;
            labels = new String[7];
            long[] dayStarts = new long[7];
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
            for (int i = 0; i < 7; i++) {
                long dStart = todayStart - (6 - i) * 24 * 3600 * 1000L;
                dayStarts[i] = dStart;
                labels[i] = dayFormat.format(new Date(dStart));
            }
            revenueData = new float[7];
            
            long startRange = todayStart - 6 * 24 * 3600 * 1000L;
            for (FirebaseOrder order : allOrders) {
                if (!isCompletedOrder(order)) continue;
                String createdAt = order.getCreatedAt() != null ? order.getCreatedAt() : order.getDate();
                if (createdAt == null) continue;
                long oTime = DateTimeHelper.toEpochMillis(createdAt);
                if (oTime >= startRange && oTime <= now) {
                    long oDayStart = getStartOfDay(oTime);
                    for (int i = 0; i < 7; i++) {
                        if (dayStarts[i] == oDayStart) {
                            double val = getOrderRevenue(order);
                            revenueData[i] += (float) val;
                            break;
                        }
                    }
                }
            }
        } else { // "Monthly"
            numPoints = 12;
            labels = new String[12];
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
            Calendar temp = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
            temp.set(Calendar.DAY_OF_MONTH, 1);
            for (int m = 0; m < 12; m++) {
                temp.set(Calendar.MONTH, m);
                labels[m] = monthFormat.format(temp.getTime());
            }
            revenueData = new float[12];
            
            Calendar currentCal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
            currentCal.setTimeInMillis(now);
            int currentYear = currentCal.get(Calendar.YEAR);
            
            for (FirebaseOrder order : allOrders) {
                if (!isCompletedOrder(order)) continue;
                String createdAt = order.getCreatedAt() != null ? order.getCreatedAt() : order.getDate();
                if (createdAt == null) continue;
                long oTime = DateTimeHelper.toEpochMillis(createdAt);
                Calendar oCal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
                oCal.setTimeInMillis(oTime);
                if (oCal.get(Calendar.YEAR) == currentYear) {
                    int month = oCal.get(Calendar.MONTH);
                    double val = getOrderRevenue(order);
                    revenueData[month] += (float) val;
                }
            }
        }

        LineChart chart = binding.lineChart;
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < numPoints; i++) {
            entries.add(new Entry(i, revenueData[i] / 1000f)); // K VND
        }

        LineData data = chart.getData();
        if (data != null && data.getDataSetCount() > 0) {
            LineDataSet set = (LineDataSet) data.getDataSetByIndex(0);
            set.setValues(entries);
        } else {
            LineDataSet set = new LineDataSet(entries, "Revenue");
            set.setColor(ContextCompat.getColor(this, R.color.chart_revenue_line));
            set.setLineWidth(3f);
            set.setDrawCircles(true);
            set.setCircleColor(ContextCompat.getColor(this, R.color.chart_revenue_line));
            set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set.setDrawValues(false);
            data = new LineData(set);
            chart.setData(data);
        }
        
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(Math.min(numPoints, 7), true);
        
        chart.getData().notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    private void updateBarChart() {
        long now = System.currentTimeMillis();
        long todayStart = getStartOfDay(now);

        int numPoints = 7;
        String[] labels = new String[7];
        float[] salesData;

        // Custom range variables
        int customNumBuckets = 7;
        long[] customBucketStarts = null;

        if ("Today".equals(activeBarRange)) {
            numPoints = 24;
            labels = new String[24];
            for (int h = 0; h < 24; h++) {
                labels[h] = String.format(Locale.US, "%02d:00", h);
            }
            salesData = new float[24];

            for (FirebaseOrder order : allOrders) {
                if (!isCompletedOrder(order)) continue;
                String createdAt = order.getCreatedAt() != null ? order.getCreatedAt() : order.getDate();
                if (createdAt == null) continue;
                long oTime = DateTimeHelper.toEpochMillis(createdAt);
                if (oTime >= todayStart && oTime <= now) {
                    Calendar oCal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
                    oCal.setTimeInMillis(oTime);
                    int hour = oCal.get(Calendar.HOUR_OF_DAY);

                    double val = getOrderRevenue(order);
                    salesData[hour] += (float) val;
                }
            }
        } else if ("Last 7 Days".equals(activeBarRange)) {
            numPoints = 7;
            labels = new String[7];
            long[] dayStarts = new long[7];
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
            for (int i = 0; i < 7; i++) {
                long dStart = todayStart - (6 - i) * 24 * 3600 * 1000L;
                dayStarts[i] = dStart;
                labels[i] = dayFormat.format(new Date(dStart));
            }
            salesData = new float[7];

            long startRange = todayStart - 6 * 24 * 3600 * 1000L;
            for (FirebaseOrder order : allOrders) {
                if (!isCompletedOrder(order)) continue;
                String createdAt = order.getCreatedAt() != null ? order.getCreatedAt() : order.getDate();
                if (createdAt == null) continue;
                long oTime = DateTimeHelper.toEpochMillis(createdAt);
                if (oTime >= startRange && oTime <= now) {
                    long oDayStart = getStartOfDay(oTime);
                    for (int i = 0; i < 7; i++) {
                        if (dayStarts[i] == oDayStart) {
                            double val = getOrderRevenue(order);
                            salesData[i] += (float) val;
                            break;
                        }
                    }
                }
            }
        } else if ("Last 30 Days".equals(activeBarRange)) {
            numPoints = 30;
            labels = new String[30];
            long[] dayStarts = new long[30];
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
            for (int i = 0; i < 30; i++) {
                long dStart = todayStart - (29 - i) * 24 * 3600 * 1000L;
                dayStarts[i] = dStart;
                labels[i] = dateFormat.format(new Date(dStart));
            }
            salesData = new float[30];

            long startRange = todayStart - 29 * 24 * 3600 * 1000L;
            for (FirebaseOrder order : allOrders) {
                if (!isCompletedOrder(order)) continue;
                String createdAt = order.getCreatedAt() != null ? order.getCreatedAt() : order.getDate();
                if (createdAt == null) continue;
                long oTime = DateTimeHelper.toEpochMillis(createdAt);
                if (oTime >= startRange && oTime <= now) {
                    long oDayStart = getStartOfDay(oTime);
                    for (int i = 0; i < 30; i++) {
                        if (dayStarts[i] == oDayStart) {
                            double val = getOrderRevenue(order);
                            salesData[i] += (float) val;
                            break;
                        }
                    }
                }
            }
        } else if ("Last 90 Days".equals(activeBarRange)) {
            numPoints = 12;
            labels = new String[12];
            long[] weekStarts = new long[12];
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
            for (int i = 0; i < 12; i++) {
                long wStart = todayStart - (11 - i) * 7 * 24 * 3600 * 1000L;
                weekStarts[i] = wStart;
                labels[i] = dateFormat.format(new Date(wStart));
            }
            salesData = new float[12];

            long startRange = todayStart - 89 * 24 * 3600 * 1000L;
            for (FirebaseOrder order : allOrders) {
                if (!isCompletedOrder(order)) continue;
                String createdAt = order.getCreatedAt() != null ? order.getCreatedAt() : order.getDate();
                if (createdAt == null) continue;
                long oTime = DateTimeHelper.toEpochMillis(createdAt);
                if (oTime >= startRange && oTime <= now) {
                    for (int i = 0; i < 12; i++) {
                        long wStart = weekStarts[i];
                        long wEnd = (i == 11) ? (now + 1000) : weekStarts[i + 1];
                        if (oTime >= wStart && oTime < wEnd) {
                            double val = getOrderRevenue(order);
                            salesData[i] += (float) val;
                            break;
                        }
                    }
                }
            }
        } else if ("Year to Date".equals(activeBarRange)) {
            numPoints = 12;
            labels = new String[12];
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
            Calendar temp = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
            temp.set(Calendar.DAY_OF_MONTH, 1);
            for (int m = 0; m < 12; m++) {
                temp.set(Calendar.MONTH, m);
                labels[m] = monthFormat.format(temp.getTime());
            }
            salesData = new float[12];

            Calendar currentCal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
            currentCal.setTimeInMillis(now);
            int currentYear = currentCal.get(Calendar.YEAR);

            for (FirebaseOrder order : allOrders) {
                if (!isCompletedOrder(order)) continue;
                String createdAt = order.getCreatedAt() != null ? order.getCreatedAt() : order.getDate();
                if (createdAt == null) continue;
                long oTime = DateTimeHelper.toEpochMillis(createdAt);
                Calendar oCal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
                oCal.setTimeInMillis(oTime);
                if (oCal.get(Calendar.YEAR) == currentYear) {
                    int month = oCal.get(Calendar.MONTH);
                    double val = getOrderRevenue(order);
                    salesData[month] += (float) val;
                }
            }
        } else { // "Custom"
            long currentStart = getStartOfDay(customStartMillis);
            Calendar endCal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
            endCal.setTimeInMillis(customEndMillis);
            endCal.set(Calendar.HOUR_OF_DAY, 23);
            endCal.set(Calendar.MINUTE, 59);
            endCal.set(Calendar.SECOND, 59);
            endCal.set(Calendar.MILLISECOND, 999);
            long currentEnd = endCal.getTimeInMillis();

            long durationDays = (currentEnd - currentStart) / (24 * 3600 * 1000L) + 1;
            if (durationDays <= 31) {
                numPoints = (int) durationDays;
                labels = new String[numPoints];
                customBucketStarts = new long[numPoints];
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
                for (int i = 0; i < numPoints; i++) {
                    long dStart = currentStart + i * 24 * 3600 * 1000L;
                    customBucketStarts[i] = dStart;
                    labels[i] = sdf.format(new Date(dStart));
                }
                salesData = new float[numPoints];

                for (FirebaseOrder order : allOrders) {
                    if (!isCompletedOrder(order)) continue;
                    String createdAt = order.getCreatedAt() != null ? order.getCreatedAt() : order.getDate();
                    if (createdAt == null) continue;
                    long oTime = DateTimeHelper.toEpochMillis(createdAt);
                    if (oTime >= currentStart && oTime <= currentEnd) {
                        long oDayStart = getStartOfDay(oTime);
                        for (int i = 0; i < numPoints; i++) {
                            if (customBucketStarts[i] == oDayStart) {
                                double val = getOrderRevenue(order);
                                salesData[i] += (float) val;
                                break;
                            }
                        }
                    }
                }
            } else {
                numPoints = 12;
                labels = new String[12];
                customBucketStarts = new long[12];
                long step = (currentEnd - currentStart) / 12;
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
                for (int i = 0; i < 12; i++) {
                    long bStart = currentStart + i * step;
                    customBucketStarts[i] = bStart;
                    labels[i] = sdf.format(new Date(bStart));
                }
                salesData = new float[12];

                for (FirebaseOrder order : allOrders) {
                    if (!isCompletedOrder(order)) continue;
                    String createdAt = order.getCreatedAt() != null ? order.getCreatedAt() : order.getDate();
                    if (createdAt == null) continue;
                    long oTime = DateTimeHelper.toEpochMillis(createdAt);
                    if (oTime >= currentStart && oTime <= currentEnd) {
                        for (int i = 0; i < 12; i++) {
                            long bStart = customBucketStarts[i];
                            long bEnd = (i == 11) ? (currentEnd + 1000) : customBucketStarts[i + 1];
                            if (oTime >= bStart && oTime < bEnd) {
                                double val = getOrderRevenue(order);
                                salesData[i] += (float) val;
                                break;
                            }
                        }
                    }
                }
            }
        }

        BarChart chart = binding.barChart;
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < numPoints; i++) {
            entries.add(new BarEntry(i, salesData[i] / 1000f));
        }

        BarData data = chart.getData();
        if (data != null && data.getDataSetCount() > 0) {
            BarDataSet set = (BarDataSet) data.getDataSetByIndex(0);
            set.setValues(entries);
        } else {
            BarDataSet set = new BarDataSet(entries, "Sales");
            set.setColor(ContextCompat.getColor(this, R.color.chart_bar_color));
            set.setDrawValues(false);
            data = new BarData(set);
            chart.setData(data);
        }

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(Math.min(numPoints, 7), true);

        chart.getData().notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    private void updateDonutChartWithData(java.util.Map<String, Float> categorySales, double totalRevenue) {
        PieChart chart = binding.pieChart;
        List<PieEntry> entries = new ArrayList<>();
        
        // Define mapping from Firebase category names to display names and colors
        String[] categories = {
                getString(R.string.firebase_category_best_sellers),
                getString(R.string.firebase_category_fruit_tea),
                getString(R.string.firebase_category_pure_tea),
                getString(R.string.firebase_category_new_arrivals),
                getString(R.string.firebase_category_tea_latte),
                getString(R.string.firebase_category_milk_tea)
        };
        
        float total = 0;
        for (String cat : categories) {
            Float catVal = categorySales.get(cat);
            float val = catVal != null ? catVal : 0f;
            entries.add(new PieEntry(val, cat));
            total += val;
        }

        PieDataSet dataSet = (PieDataSet) chart.getData().getDataSetByIndex(0);
        dataSet.setValues(entries);
        
        chart.setCenterText(getCenterText((float) totalRevenue));
        chart.getData().notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    private SpannableString getCenterText(float total) {
        String totalStr = new DecimalFormat("#,###.#").format(total / 1000.0) + "K";
        SpannableString s = new SpannableString(totalStr + "\nVND");
        s.setSpan(new RelativeSizeSpan(1.8f), 0, totalStr.length(), 0);
        s.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, totalStr.length(), 0);
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.text_primary)), 0, totalStr.length(), 0);
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.text_secondary)), totalStr.length(), s.length(), 0);
        s.setSpan(new RelativeSizeSpan(0.8f), totalStr.length(), s.length(), 0);
        return s;
    }

    private void setupStatCards() {
        setupStatCard(binding.cardRevenue.getRoot(), getString(R.string.stat_revenue_title), "0đ", "0%", getString(R.string.stat_mom), R.drawable.chart, R.color.stat_revenue_start, R.color.stat_revenue_end);
        setupStatCard(binding.cardOrders.getRoot(), getString(R.string.stat_orders_title), "0", "0%", getString(R.string.stat_mom), R.drawable.cart, R.color.stat_orders_start, R.color.stat_orders_end);
        setupStatCard(binding.cardSold.getRoot(), getString(R.string.stat_sold_title), "0", "0%", getString(R.string.stat_mom), R.drawable.coins, R.color.stat_sold_start, R.color.stat_sold_end);

        // Cập nhật icon cho Products tại đây (Ví dụ dùng box1 hoặc bag1)
        setupStatCard(binding.cardProducts.getRoot(), getString(R.string.stat_products_title), "0", "", getString(R.string.stat_active_trading), R.drawable.coffee, R.color.stat_products_start, R.color.stat_products_end);

        binding.cardProducts.ivTrendIcon.setVisibility(View.GONE);
    }

    private void setupStatCard(View cardView, String title, String value, String delta, String label, int iconRes, int startColor, int endColor) {
        ((android.widget.TextView)cardView.findViewById(R.id.tvStatTitle)).setText(title);
        ((android.widget.TextView)cardView.findViewById(R.id.tvStatValue)).setText(value);
        ((android.widget.TextView)cardView.findViewById(R.id.tvStatDelta)).setText(delta);
        ((android.widget.TextView)cardView.findViewById(R.id.tvStatLabel)).setText(label);
        ((android.widget.ImageView)cardView.findViewById(R.id.ivStatIcon)).setImageResource(iconRes);

        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[] { ContextCompat.getColor(this, startColor), ContextCompat.getColor(this, endColor) });
        gd.setCornerRadius(getResources().getDimension(R.dimen.stat_card_corner));
        cardView.findViewById(R.id.viewStatBg).setBackground(gd);
    }

    private void setupLineChart() {
        LineChart chart = binding.lineChart;
        List<Entry> revenue = new ArrayList<>();
        for (int i = 0; i < 7; i++) revenue.add(new Entry(i, 0));

        LineDataSet set1 = new LineDataSet(revenue, "Revenue");
        set1.setColor(ContextCompat.getColor(this, R.color.chart_revenue_line));
        set1.setLineWidth(3f);
        set1.setDrawCircles(true);
        set1.setCircleColor(ContextCompat.getColor(this, R.color.chart_revenue_line));
        set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set1.setDrawValues(false);

        chart.setData(new LineData(set1));
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(new String[]{"M", "T", "W", "T", "F", "S", "S"}));
        chart.getAxisRight().setEnabled(false);
        chart.invalidate();
    }

    private void setupBarChart() {
        BarChart chart = binding.barChart;
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 7; i++) entries.add(new BarEntry(i, 0f));

        BarDataSet set = new BarDataSet(entries, "Sales");
        set.setColor(ContextCompat.getColor(this, R.color.chart_bar_color));
        set.setDrawValues(false);

        chart.setData(new BarData(set));
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(new String[]{"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"}));
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.invalidate();
    }

    private void setupDonutChart() {
        PieChart chart = binding.pieChart;
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(0f, "Hot Drinks"));
        entries.add(new PieEntry(0f, "Fruit Tea"));
        entries.add(new PieEntry(0f, "Pure Tea"));
        entries.add(new PieEntry(0f, "New Drinks"));
        entries.add(new PieEntry(0f, "Latte Tea"));
        entries.add(new PieEntry(0f, "Milk Tea"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                ContextCompat.getColor(this, R.color.pie_hot_drinks),
                ContextCompat.getColor(this, R.color.pie_fruit_tea),
                ContextCompat.getColor(this, R.color.pie_pure_tea),
                ContextCompat.getColor(this, R.color.pie_new_drinks),
                ContextCompat.getColor(this, R.color.pie_latte_tea),
                ContextCompat.getColor(this, R.color.pie_milk_tea)
        );
        dataSet.setDrawValues(false);
        dataSet.setSliceSpace(3f);

        chart.setData(new PieData(dataSet));
        chart.setHoleRadius(75f);
        chart.setTransparentCircleRadius(0f);
        chart.setDrawEntryLabels(false);
        chart.setCenterText(getCenterText(0));
        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.invalidate();

        setupLegendItem(binding.legendHotDrinks.getRoot(), R.string.legend_hot_drinks, R.color.pie_hot_drinks);
        setupLegendItem(binding.legendFruitTea.getRoot(), R.string.legend_fruit_tea, R.color.pie_fruit_tea);
        setupLegendItem(binding.legendPureTea.getRoot(), R.string.legend_pure_tea, R.color.pie_pure_tea);
        setupLegendItem(binding.legendNewDrinks.getRoot(), R.string.legend_new_drinks, R.color.pie_new_drinks);
        setupLegendItem(binding.legendLatteTea.getRoot(), R.string.legend_latte_tea, R.color.pie_latte_tea);
        setupLegendItem(binding.legendMilkTea.getRoot(), R.string.legend_milk_tea, R.color.pie_milk_tea);
    }

    private void setupLegendItem(View view, int textRes, int colorRes) {
        ((android.widget.TextView)view.findViewById(R.id.tvLabel)).setText(getString(textRes));
        view.findViewById(R.id.viewDot).setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, colorRes)));
    }

    private void setupRecentOrders() {
        binding.rvRecentOrders.setLayoutManager(new LinearLayoutManager(this));
    }
}
