package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.adapter.BlogAdapter;
import com.teatrack_mcd_253eie502802_group02.model.Blog;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;

import java.util.ArrayList;
import java.util.List;

public class BlogGeneral extends AppCompatActivity {

    private RecyclerView rvBlogList;
    private ProgressBar progressBar;
    private List<Blog> allBlogs = new ArrayList<>();
    private DatabaseReference mDatabase;
    private BlogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_general);

        initViews();
        setupHeader();
        setupNavBar();

        // Khởi tạo Firebase - Node 'blogs'
        mDatabase = FirebaseDatabase.getInstance().getReference("blogs");

        loadBlogsFromFirebase();
    }

    private void initViews() {
        rvBlogList = findViewById(R.id.rvBlogList);
        progressBar = findViewById(R.id.progressBar);
        
        rvBlogList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BlogAdapter(this, allBlogs);
        rvBlogList.setAdapter(adapter);
    }

    private void setupHeader() {
        findViewById(R.id.btn_cart).setOnClickListener(v ->
                startActivity(new Intent(this, Cart.class)));

        findViewById(R.id.btn_profile).setOnClickListener(v ->
                startActivity(new Intent(this, UserProfile.class)));
    }

    private void setupNavBar() {
        int[] navItemIds = {
                R.id.nav_home,
                R.id.nav_menu,
                R.id.nav_orders,
                R.id.nav_promotion,
                R.id.nav_profile
        };

        NavBarHelper.setupNavBar(this, navItemIds, R.id.nav_promotion, v -> {
            int id = v.getId();
            if (id == R.id.nav_promotion) return; // đang ở đây rồi

            Intent intent = null;
            if (id == R.id.nav_home)           intent = new Intent(this, Homepage.class);
            else if (id == R.id.nav_menu)      intent = new Intent(this, Menu.class);
            else if (id == R.id.nav_orders)    intent = new Intent(this, OrderHistory.class);
            else if (id == R.id.nav_profile)   intent = new Intent(this, UserProfile.class);

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Homepage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void loadBlogsFromFirebase() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Lấy danh sách bài viết từ Firebase
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allBlogs.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Blog blog = postSnapshot.getValue(Blog.class);
                    if (blog != null) {
                        allBlogs.add(blog);
                    }
                }
                
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                if (allBlogs.isEmpty()) {
                    Toast.makeText(BlogGeneral.this, "Hiện chưa có bài viết nào trong Diễn đàn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Log.e("Firebase", "Error: " + error.getMessage());
                Toast.makeText(BlogGeneral.this, "Lỗi tải dữ liệu Diễn đàn", Toast.LENGTH_SHORT).show();
            }
        });
    }
}