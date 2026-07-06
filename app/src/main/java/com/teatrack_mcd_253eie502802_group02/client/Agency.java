package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.shared.ui.ProfileBackHelper;
import com.teatrack_mcd_253eie502802_group02.adapter.ClientAgencyAdapter;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;

import java.util.ArrayList;
import java.util.List;

import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;

public class Agency extends BaseActivity {

    private static final String DATABASE_URL = "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";
    private EditText etSearch;
    private RecyclerView rvAgencies;
    private ClientAgencyAdapter agencyAdapter;
    private final List<com.teatrack_mcd_253eie502802_group02.model.Agency> agencyList = new ArrayList<>();
    private final List<com.teatrack_mcd_253eie502802_group02.model.Agency> fullAgencyList = new ArrayList<>();
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agency);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        initViews();
        setupRecyclerView();
        setupHeader();
        setupNavBar();
        initFirebase();
        setupSearch();
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        rvAgencies = findViewById(R.id.rvAgencies);
    }

    private void setupRecyclerView() {
        rvAgencies.setLayoutManager(new LinearLayoutManager(this));
        agencyAdapter = new ClientAgencyAdapter(this, agencyList);
        rvAgencies.setAdapter(agencyAdapter);
    }

    private void setupHeader() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        View btnShare = findViewById(R.id.btnShare);
        if (btnShare != null) {
            btnShare.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.str_agency_title));
                startActivity(Intent.createChooser(intent, getString(R.string.blog_detail_share)));
            });
        }
    }

    private void setupNavBar() {
        // Nav bar removed from layout
    }

    private void initFirebase() {
        databaseReference = FirebaseDatabase.getInstance(DATABASE_URL).getReference("agencies");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                agencyList.clear();
                fullAgencyList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    try {
                        com.teatrack_mcd_253eie502802_group02.model.Agency agency = dataSnapshot.getValue(com.teatrack_mcd_253eie502802_group02.model.Agency.class);
                        if (agency != null && agency.isVisible()) {
                            agencyList.add(agency);
                            fullAgencyList.add(agency);
                        }
                    } catch (Exception ignored) {}
                }
                agencyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Agency.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        if (etSearch == null) return;
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String text) {
        String query = text.toLowerCase().trim();
        List<com.teatrack_mcd_253eie502802_group02.model.Agency> filteredList = new ArrayList<>();
        for (com.teatrack_mcd_253eie502802_group02.model.Agency item : fullAgencyList) {
            if ((item.getName() != null && item.getName().toLowerCase().contains(query)) ||
                (item.getAddress() != null && item.getAddress().toLowerCase().contains(query))) {
                filteredList.add(item);
            }
        }
        agencyAdapter.updateList(filteredList);
    }
}
