package com.teatrack_mcd_253eie502802_group02.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.adapter.ComplaintAdapter;
import com.teatrack_mcd_253eie502802_group02.model.ContactRequest;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminComplaints extends AppCompatActivity {

    private RecyclerView rvComplaints;
    private ComplaintAdapter adapter;
    private List<ContactRequest> allContacts = new ArrayList<>();
    private List<ContactRequest> displayList = new ArrayList<>();
    private DatabaseReference mDatabase;
    private View tabAll, tabUnread, tabPending, tabResolved;
    private View activeTab;
    private int selectedFilter = 0; // 0: All, 1: Unread, 2: Pending, 3: Resolved
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_complaints);
        com.teatrack_mcd_253eie502802_group02.shared.ui.AdminInsetsHelper.apply(this);

        initViews();
        setupFirebase();
        setupBottomNavigation();
        com.teatrack_mcd_253eie502802_group02.shared.ui.HeaderMenuHelper.setupProfileMenu(this);
    }

    private void initViews() {
        rvComplaints = findViewById(R.id.rvComplaints);
        rvComplaints.setLayoutManager(new LinearLayoutManager(this));

        tabAll = findViewById(R.id.tabAll);
        tabUnread = findViewById(R.id.tabUnread);
        tabPending = findViewById(R.id.tabPending);
        tabResolved = findViewById(R.id.tabResolved);

        activeTab = tabAll;
        initTabLabels();
        setTabActive(tabAll);

        // Click listeners for tabs to filter
        View.OnClickListener tabClick = v -> {
            int id = v.getId();
            if (id == R.id.tabAll) selectedFilter = 0;
            else if (id == R.id.tabUnread) selectedFilter = 1;
            else if (id == R.id.tabPending) selectedFilter = 2;
            else if (id == R.id.tabResolved) selectedFilter = 3;
            
            updateTabSelection();
            filterList();
        };

        if (tabAll != null) tabAll.setOnClickListener(tabClick);
        if (tabUnread != null) tabUnread.setOnClickListener(tabClick);
        if (tabPending != null) tabPending.setOnClickListener(tabClick);
        if (tabResolved != null) tabResolved.setOnClickListener(tabClick);

        adapter = new ComplaintAdapter(displayList, this::showDetailDialog);
        rvComplaints.setAdapter(adapter);
    }

    private void initTabLabels() {
        setTabLabel(tabAll, R.string.str_tab_all);
        setTabLabel(tabUnread, R.string.str_tab_unread);
        setTabLabel(tabPending, R.string.str_tab_pending);
        setTabLabel(tabResolved, R.string.str_tab_resolved);
    }

    private void setTabLabel(View tab, int labelRes) {
        if (tab == null) return;
        TextView label = tab.findViewById(R.id.tvTabLabel);
        if (label != null) {
            label.setText(labelRes);
        }
    }

    private void updateTabSelection() {
        View[] tabs = {tabAll, tabUnread, tabPending, tabResolved};
        int[] filters = {0, 1, 2, 3};

        setTabInactive(activeTab);
        for (int i = 0; i < tabs.length; i++) {
            if (filters[i] == selectedFilter) {
                setTabActive(tabs[i]);
                activeTab = tabs[i];
                break;
            }
        }
    }

    private void setTabActive(View tab) {
        if (tab == null) return;
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
        if (tab == null) return;
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

    private void setTabCount(View tab, int count) {
        if (tab == null) return;
        TextView countView = tab.findViewById(R.id.tvTabCount);
        if (countView != null) {
            countView.setText(String.valueOf(count));
        }
    }

    private void setupFirebase() {
        String dbUrl = "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";
        mDatabase = FirebaseDatabase.getInstance(dbUrl).getReference("contacts");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allContacts.clear();
                int unread = 0;
                int pending = 0;
                int resolved = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    ContactRequest contact = ds.getValue(ContactRequest.class);
                    if (contact != null) {
                        if (contact.get_id() == null) contact.set_id(ds.getKey());
                        allContacts.add(contact);
                        
                        if (!contact.isRead()) unread++;
                        if (contact.getStatus() == 1) pending++;
                        else if (contact.getStatus() == 2) resolved++;
                    }
                }
                
                updateTabCounts(allContacts.size(), unread, pending, resolved);

                // Sort by time descending
                Collections.sort(allContacts, (o1, o2) -> {
                    try {
                        Date d1 = sdf.parse(o1.getTime());
                        Date d2 = sdf.parse(o2.getTime());
                        return d2.compareTo(d1);
                    } catch (Exception e) {
                        return 0;
                    }
                });

                filterList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminComplaints.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTabCounts(int total, int unread, int pending, int resolved) {
        setTabCount(tabAll, total);
        setTabCount(tabUnread, unread);
        setTabCount(tabPending, pending);
        setTabCount(tabResolved, resolved);
    }


    private void filterList() {
        displayList.clear();
        for (ContactRequest c : allContacts) {
            boolean matchesFilter = false;
            switch (selectedFilter) {
                case 0: // All
                    matchesFilter = true;
                    break;
                case 1: // Unread
                    matchesFilter = !c.isRead();
                    break;
                case 2: // Pending
                    matchesFilter = c.getStatus() == 1;
                    break;
                case 3: // Resolved
                    matchesFilter = c.getStatus() == 2;
                    break;
            }

            if (matchesFilter) {
                displayList.add(c);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showDetailDialog(ContactRequest contact) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_admin_reply, null);

        TextView tvName = view.findViewById(R.id.tvDetailUserName);
        TextView tvEmail = view.findViewById(R.id.tvDetailUserEmail);
        TextView tvStatus = view.findViewById(R.id.tvDetailStatus);
        TextView tvTopic = view.findViewById(R.id.tvDetailTopic);
        TextView tvDate = view.findViewById(R.id.tvDetailDate);
        TextView tvContent = view.findViewById(R.id.tvDetailContent);
        EditText etReply = view.findViewById(R.id.etReply);
        View btnClose = view.findViewById(R.id.btnClose);
        View btnCancel = view.findViewById(R.id.btnCancel);
        View btnSend = view.findViewById(R.id.btnSendReply);

        tvName.setText(contact.getFullname());
        tvEmail.setText(contact.getEmail());
        tvTopic.setText(contact.getTopic());
        tvDate.setText(contact.getTime());
        tvContent.setText(contact.getContent());
        etReply.setText(contact.getNote());

        if (contact.getStatus() == 1) {
            tvStatus.setText("Pending");
            tvStatus.setBackgroundResource(R.drawable.bg_badge_pending);
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.badge_pending_text));
        } else {
            tvStatus.setText("Resolved");
            tvStatus.setBackgroundResource(R.drawable.bg_badge_completed);
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.badge_completed_text));
        }

        AlertDialog dialog = builder.setView(view).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSend.setOnClickListener(v -> {
            String note = etReply.getText().toString().trim();
            submitUpdate(contact, note, 2);
            dialog.dismiss();
        });

        dialog.show();
        
        // Mark as read when opened
        if (!contact.isRead()) {
            mDatabase.child(contact.get_id()).child("read").setValue(true);
        }
    }

    private void submitUpdate(ContactRequest contact, String note, int newStatus) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("note", note);
        updates.put("status", newStatus);
        updates.put("read", true);

        mDatabase.child(contact.get_id()).updateChildren(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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

        NavBarHelper.setupNavBar(this, navItemIds, R.id.nav_feedbacks, v -> {
            int id = v.getId();
            if (id == R.id.nav_feedbacks) return;

            Class<?> destination = null;
            if (id == R.id.nav_dashboard) destination = AdminDashboard.class;
            else if (id == R.id.nav_products) destination = AdminProduct.class;
            else if (id == R.id.nav_orders) destination = AdminOrders.class;
            else if (id == R.id.nav_account) destination = AdminAccount.class;
            else if (id == R.id.nav_forum) destination = AdminBlog.class;
            else if (id == R.id.nav_branch) destination = AdminAgency.class;
            else if (id == R.id.nav_promotion) destination = AdminPromotion.class;

            if (destination != null) {
                NavBarHelper.navigateWithoutTransition(this, destination);
            }
        });
    }
}
