package com.teatrack_mcd_253eie502802_group02.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
    private TabLayout tabFilter;
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
        
        // Setup TabLayout for filtering (assuming it exists in layout, if not we'll need to check)
        tabFilter = findViewById(R.id.tabFilter); 
        if (tabFilter != null) {
            tabFilter.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    filterList(tab.getPosition());
                }
                @Override public void onTabUnselected(TabLayout.Tab tab) {}
                @Override public void onTabReselected(TabLayout.Tab tab) {}
            });
        }

        adapter = new ComplaintAdapter(displayList, this::showDetailDialog);
        rvComplaints.setAdapter(adapter);
    }

    private void setupFirebase() {
        String dbUrl = "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";
        mDatabase = FirebaseDatabase.getInstance(dbUrl).getReference("contacts");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allContacts.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ContactRequest contact = ds.getValue(ContactRequest.class);
                    if (contact != null) {
                        allContacts.add(contact);
                    }
                }
                
                // Sort by time descending
                Collections.sort(allContacts, (o1, o2) -> {
                    try {
                        Date d1 = sdf.parse(o1.getTime());
                        Date d2 = sdf.parse(o2.getTime());
                        return d2.compareTo(d1);
                    } catch (ParseException e) {
                        return 0;
                    }
                });

                filterList(tabFilter != null ? tabFilter.getSelectedTabPosition() : 0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminComplaints.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterList(int position) {
        displayList.clear();
        if (position == 0) { // All
            displayList.addAll(allContacts);
        } else if (position == 1) { // Pending (status 1)
            for (ContactRequest c : allContacts) {
                if (c.getStatus() == 1) displayList.add(c);
            }
        } else if (position == 2) { // Resolved (status 2)
            for (ContactRequest c : allContacts) {
                if (c.getStatus() == 2) displayList.add(c);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showDetailDialog(ContactRequest contact) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_admin_reply, null);

        TextView tvInfo = new TextView(this);
        tvInfo.setPadding(50, 40, 50, 10);
        tvInfo.setTextSize(14);
        tvInfo.setTextColor(getResources().getColor(android.R.color.black));
        tvInfo.setText(String.format("From: %s\nPhone: %s\nEmail: %s\nBranch: %s\nTopic: %s\nTime: %s\n\nContent: %s",
                contact.getFullname(), contact.getPhone(), contact.getEmail(),
                contact.getBranch(), contact.getTopic(), contact.getTime(), contact.getContent()));

        EditText etNote = view.findViewById(R.id.etReply);
        etNote.setText(contact.getNote());

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.addView(tvInfo);
        layout.addView(view);

        builder.setTitle("Feedback Details")
               .setView(layout)
               .setPositiveButton(contact.getStatus() == 1 ? "Resolve" : "Save", (dialog, which) -> {
                   String note = etNote.getText().toString().trim();
                   submitUpdate(contact, note, 2);
               })
               .setNegativeButton("Close", null)
               .show();
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
