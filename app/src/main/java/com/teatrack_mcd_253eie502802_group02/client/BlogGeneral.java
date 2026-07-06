package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

public class BlogGeneral extends BaseActivity {

    private RecyclerView rvBlogList;
    private ProgressBar progressBar;
    private android.widget.TextView tvTopTitle;
    private View btnBack, btnShare;
    private List<Blog> allBlogs = new ArrayList<>();
    private DatabaseReference mDatabase;
    private BlogAdapter adapter;

    private String filterCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blog_general);
        setupMainInsets();

        filterCategory = getIntent().getStringExtra("CATEGORY_FILTER");

        initViews();
        setupHeader();
        setupNavBar();

        // Khởi tạo Firebase - Node 'blogs'
        mDatabase = FirebaseDatabase.getInstance().getReference("blogs");

        loadBlogsFromFirebase();
    }

    private void setupMainInsets() {
        View mainView = findViewById(R.id.main);
        if (mainView == null) {
            return;
        }
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        rvBlogList = findViewById(R.id.rvBlogList);
        progressBar = findViewById(R.id.progressBar);
        tvTopTitle = findViewById(R.id.tvTopTitle);
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        
        if (tvTopTitle != null && filterCategory != null && !filterCategory.isEmpty()) {
            tvTopTitle.setText(filterCategory);
        }

        rvBlogList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BlogAdapter(this, allBlogs);
        rvBlogList.setAdapter(adapter);
    }

    private void setupHeader() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        if (btnShare != null) {
            btnShare.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "Check out these blogs at TeaTrack!");
                startActivity(Intent.createChooser(intent, "Share via"));
            });
        }
    }

    private void setupNavBar() {
        // No NavBar client here as per user request
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
        Log.d("BlogGeneral", "Loading blogs from Firebase, category filter: " + filterCategory);
        
        // Lấy danh sách bài viết từ Firebase
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allBlogs.clear();
                Log.d("BlogGeneral", "DataSnapshot count: " + snapshot.getChildrenCount());
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Blog blog = postSnapshot.getValue(Blog.class);
                    if (blog != null) {
                        // Ensure ID is set from the snapshot key if missing
                        if (blog.getId() == null || blog.getId().isEmpty()) {
                            blog.setId(postSnapshot.getKey());
                        }
                        
                        Log.d("BlogGeneral", "Found blog: " + blog.getTitle() + " (ID: " + blog.getId() + ")");
                        
                        // Check if we filter by category (stored in 'title' field based on current model)
                        boolean matches = false;
                        if (filterCategory == null || filterCategory.trim().isEmpty()) {
                            matches = true;
                        } else if (blog.getTitle() != null) {
                            String title = blog.getTitle().toLowerCase(java.util.Locale.ROOT).trim();
                            String filter = filterCategory.toLowerCase(java.util.Locale.ROOT).trim();
                            // Match if titles are same, or one contains other (e.g. Promotion vs Promotions)
                            matches = title.contains(filter) || filter.contains(title);
                        }

                        if (matches) {
                            allBlogs.add(blog);
                        } else {
                            Log.d("BlogGeneral", "Filtered out: '" + blog.getTitle() + "' != '" + filterCategory + "'");
                        }
                    } else {
                        Log.w("BlogGeneral", "Blog is null for snapshot: " + postSnapshot.getKey());
                    }
                }
                
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                if (allBlogs.isEmpty()) {
                    String msg = (filterCategory == null || filterCategory.isEmpty()) 
                            ? "Hiện chưa có bài viết nào trong Diễn đàn" 
                            : "Không tìm thấy bài viết cho danh mục: " + filterCategory;
                    Toast.makeText(BlogGeneral.this, msg, Toast.LENGTH_SHORT).show();
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