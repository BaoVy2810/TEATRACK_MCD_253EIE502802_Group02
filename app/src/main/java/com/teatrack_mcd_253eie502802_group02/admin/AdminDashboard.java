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

import androidx.appcompat.app.AppCompatActivity;
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
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;
import com.teatrack_mcd_253eie502802_group02.adapter.BranchAnalysisAdapter;
import com.teatrack_mcd_253eie502802_group02.adapter.RecentOrderAdapter;
import com.teatrack_mcd_253eie502802_group02.databinding.ActivityAdminDashboardBinding;
import com.teatrack_mcd_253eie502802_group02.model.Branch;
import com.teatrack_mcd_253eie502802_group02.model.Order;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AdminDashboard extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;
    private DatabaseReference mDatabase;
    private List<Order> allOrders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mDatabase = FirebaseDatabase.getInstance().getReference();

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
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                String startDate = sdf.format(new Date(selection.first));
                String endDate = sdf.format(new Date(selection.second));
                
                String rangeLabel = startDate + " - " + endDate;
                binding.btnBarChartRange.setText(rangeLabel);
                
                // Cập nhật dữ liệu dựa trên khoảng ngày đã chọn
                filterAndDisplayData("Custom: " + rangeLabel);
            }
        });
    }

    private void setupPickerItem(View root, int viewId, String rangeName, String currentRange, PopupWindow popup) {
        View itemView = root.findViewById(viewId);
        if (itemView == null) return;
        
        boolean isActive = rangeName.equalsIgnoreCase(currentRange);

        android.widget.TextView tv = null;
        android.widget.ImageView iv = null;
        
        if (viewId == R.id.btnToday) { tv = root.findViewById(R.id.tvToday); iv = root.findViewById(R.id.icToday); }
        else if (viewId == R.id.btnLast7Days) { tv = root.findViewById(R.id.tvLast7Days); iv = root.findViewById(R.id.icLast7Days); }
        else if (viewId == R.id.btnLast30Days) { tv = root.findViewById(R.id.tvLast30Days); iv = root.findViewById(R.id.icLast30Days); }
        else if (viewId == R.id.btnLast90Days) { tv = root.findViewById(R.id.tvLast90Days); iv = root.findViewById(R.id.icLast90Days); }
        else if (viewId == R.id.btnYearToDate) { tv = root.findViewById(R.id.tvYearToDate); iv = root.findViewById(R.id.icYearToDate); }

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
            binding.btnBarChartRange.setText(rangeName);
            popup.dismiss();
            fetchBarChartData(rangeName);
            filterAndDisplayData(rangeName);
        });
    }

    private void fetchBarChartData(String range) {
        Toast.makeText(this, "Updating chart for: " + range, Toast.LENGTH_SHORT).show();
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
        filterAndDisplayData(activeBtn.getText().toString());
    }

    private void setupHeader() {
        View header = findViewById(R.id.headerAdmin);
        if (header != null) {
            header.findViewById(R.id.btn_profile).setOnClickListener(v -> startActivity(new Intent(this, AdminProfile.class)));
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
            Class<?> destination = null;
            if (id == R.id.nav_dashboard) return;
            if (id == R.id.nav_products) destination = AdminProduct.class;
            else if (id == R.id.nav_orders) destination = AdminOrders.class;
            else if (id == R.id.nav_account) destination = AdminAccount.class;
            else if (id == R.id.nav_promotion) destination = AdminPromotion.class;
            // Add other mappings as needed

            if (destination != null) {
                startActivity(new Intent(this, destination));
            }
        });
    }

    private void fetchDashboardData() {
        mDatabase.child("branches").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Branch> branches = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Branch branch = data.getValue(Branch.class);
                    if (branch != null) branches.add(branch);
                }
                binding.rvBranchAnalysis.setLayoutManager(new LinearLayoutManager(AdminDashboard.this));
                binding.rvBranchAnalysis.setAdapter(new BranchAnalysisAdapter(branches));
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });

        mDatabase.child("products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                binding.cardProducts.tvStatValue.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });

        mDatabase.child("orders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                allOrders.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Order order = data.getValue(Order.class);
                    if (order != null) allOrders.add(0, order);
                }
                filterAndDisplayData("Monthly");
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void filterAndDisplayData(String range) {
        double totalRevenue = 0;
        int productsSold = 0;
        List<Order> filteredOrders = new ArrayList<>();

        for (Order order : allOrders) {
            boolean matches = false;
            String meta = order.getMeta();
            if (meta == null) continue;

            if (range.equalsIgnoreCase("Today") || range.equalsIgnoreCase("Today_Toggle")) {
                matches = meta.contains("min") || meta.contains("hour");
            } else if (range.equalsIgnoreCase("Weekly") || range.equalsIgnoreCase("Last 7 Days")) {
                matches = meta.contains("min") || meta.contains("hour") || (meta.contains("day") && !meta.contains("30"));
            } else {
                matches = true;
            }

            if (matches) {
                filteredOrders.add(order);
                if (meta.contains("•")) {
                    try {
                        String priceStr = meta.substring(meta.lastIndexOf("•") + 1).trim()
                                .replace("đ", "").replace(".", "");
                        totalRevenue += Double.parseDouble(priceStr);
                        productsSold++;
                    } catch (Exception e) {}
                }
            }
        }

        DecimalFormat df = new DecimalFormat("#,###");
        binding.cardRevenue.tvStatValue.setText(df.format(totalRevenue) + "đ");
        binding.cardOrders.tvStatValue.setText(String.valueOf(filteredOrders.size()));
        binding.cardSold.tvStatValue.setText(String.valueOf(productsSold));

        List<Order> displayList = filteredOrders.subList(0, Math.min(filteredOrders.size(), 5));
        binding.rvRecentOrders.setAdapter(new RecentOrderAdapter(displayList));
        updateLineChartWithData(totalRevenue);
    }

    private void updateLineChartWithData(double totalRevenue) {
        if (binding.lineChart.getData() != null && binding.lineChart.getData().getDataSetCount() > 0) {
            LineDataSet set1 = (LineDataSet) binding.lineChart.getData().getDataSetByIndex(0);
            List<Entry> entries = new ArrayList<>();
            float base = (float) (totalRevenue / 1000000.0);
            entries.add(new Entry(0, base * 0.4f));
            entries.add(new Entry(1, base * 0.7f));
            entries.add(new Entry(2, base * 0.6f));
            entries.add(new Entry(3, base * 1.1f));
            entries.add(new Entry(4, base * 0.9f));
            entries.add(new Entry(5, base));
            set1.setValues(entries);
            binding.lineChart.getData().notifyDataChanged();
            binding.lineChart.notifyDataSetChanged();
            binding.lineChart.invalidate();
        }
    }

    private void setupStatCards() {
        setupStatCard(binding.cardRevenue.getRoot(), getString(R.string.stat_revenue_title), "0đ", "+5.2%", getString(R.string.stat_mom), R.drawable.chart, R.color.stat_revenue_start, R.color.stat_revenue_end);
        setupStatCard(binding.cardOrders.getRoot(), getString(R.string.stat_orders_title), "0", "+5.2%", getString(R.string.stat_mom), R.drawable.cart, R.color.stat_orders_start, R.color.stat_orders_end);
        setupStatCard(binding.cardSold.getRoot(), getString(R.string.stat_sold_title), "0", "+5.2%", getString(R.string.stat_mom), R.drawable.coins, R.color.stat_sold_start, R.color.stat_sold_end);
        
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
        revenue.add(new Entry(0, 10)); revenue.add(new Entry(1, 15)); revenue.add(new Entry(2, 45));
        revenue.add(new Entry(3, 55)); revenue.add(new Entry(4, 30)); revenue.add(new Entry(5, 10));

        List<Entry> expenses = new ArrayList<>();
        expenses.add(new Entry(0, 5)); expenses.add(new Entry(1, 8)); expenses.add(new Entry(2, 7));
        expenses.add(new Entry(3, 10)); expenses.add(new Entry(4, 8)); expenses.add(new Entry(5, 12));

        LineDataSet set1 = new LineDataSet(revenue, "Revenue");
        set1.setColor(ContextCompat.getColor(this, R.color.chart_revenue_line));
        set1.setLineWidth(3f);
        set1.setDrawCircles(false);
        set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineDataSet set2 = new LineDataSet(expenses, "Expenses");
        set2.setColor(ContextCompat.getColor(this, R.color.chart_expense_line));
        set2.setLineWidth(2f);
        set2.setDrawCircles(false);
        set2.enableDashedLine(10f, 5f, 0f);

        chart.setData(new LineData(set1, set2));
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);
        chart.invalidate();
    }

    private void setupBarChart() {
        BarChart chart = binding.barChart;
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 100)); entries.add(new BarEntry(1, 150));
        entries.add(new BarEntry(2, 220)); entries.add(new BarEntry(3, 130));
        entries.add(new BarEntry(4, 250)); entries.add(new BarEntry(5, 180));
        entries.add(new BarEntry(6, 120));

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
        entries.add(new PieEntry(20f, "Hot Drinks"));
        entries.add(new PieEntry(15f, "Fruit Tea"));
        entries.add(new PieEntry(25f, "Pure Tea"));
        entries.add(new PieEntry(10f, "New Drinks"));
        entries.add(new PieEntry(15f, "Latte Tea"));
        entries.add(new PieEntry(15f, "Milk Tea"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{
                ContextCompat.getColor(this, R.color.pie_hot_drinks),
                ContextCompat.getColor(this, R.color.pie_fruit_tea),
                ContextCompat.getColor(this, R.color.pie_pure_tea),
                ContextCompat.getColor(this, R.color.pie_new_drinks),
                ContextCompat.getColor(this, R.color.pie_latte_tea),
                ContextCompat.getColor(this, R.color.pie_milk_tea)
        });
        dataSet.setDrawValues(false);
        dataSet.setSliceSpace(3f);

        chart.setData(new PieData(dataSet));
        chart.setHoleRadius(75f);
        chart.setTransparentCircleRadius(0f);
        chart.setDrawEntryLabels(false);
        chart.setCenterText(getCenterText());
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

    private SpannableString getCenterText() {
        SpannableString s = new SpannableString("951.0K\nVND");
        s.setSpan(new RelativeSizeSpan(1.8f), 0, 6, 0);
        s.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, 6, 0);
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.text_primary)), 0, 6, 0);
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.text_secondary)), 6, s.length(), 0);
        s.setSpan(new RelativeSizeSpan(0.8f), 6, s.length(), 0);
        return s;
    }

    private void setupLegendItem(View view, int textRes, int colorRes) {
        ((android.widget.TextView)view.findViewById(R.id.tvLabel)).setText(getString(textRes));
        view.findViewById(R.id.viewDot).setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, colorRes)));
    }

    private void setupRecentOrders() {
        binding.rvRecentOrders.setLayoutManager(new LinearLayoutManager(this));
    }
}
