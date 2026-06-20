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

    private static final String DATABASE_URL = "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";
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

        // Khởi tạo Firebase với URL cụ thể cho vùng asia-southeast1
        mDatabase = FirebaseDatabase.getInstance(DATABASE_URL).getReference("blogs");

        loadBlogsFromFirebase();
    }

    private void initViews() {
        rvBlogList = findViewById(R.id.rvBlogList);
        progressBar = findViewById(R.id.progressBar);
        
        rvBlogList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BlogAdapter(this, allBlogs);
        rvBlogList.setAdapter(adapter);
    }

    private String readString(DataSnapshot snapshot, String key) {
        Object val = snapshot.child(key).getValue();
        return val != null ? String.valueOf(val) : "";
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

        // Gán chức năng điều hướng cho Navbar
        NavBarHelper.setupNavBar(this, navItemIds, -1, v -> {
            int id = v.getId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, Homepage.class));
            } else if (id == R.id.nav_menu) {
                startActivity(new Intent(this, Menu.class));
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, OrderHistory.class));
            } else if (id == R.id.nav_promotion) {
                Toast.makeText(this, R.string.str_coming_soon, Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, UserProfile.class));
            }
        });
    }

    private void loadBlogsFromFirebase() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d("FirebaseBlog", "Starting fetch from path: " + mDatabase.toString());
        Toast.makeText(this, "Đang kết nối Firebase...", Toast.LENGTH_SHORT).show();
        
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("FirebaseBlog", "onDataChange triggered. Exists: " + snapshot.exists() + ", Children: " + snapshot.getChildrenCount());
                allBlogs.clear();
                
                if (!snapshot.exists()) {
                    Log.e("FirebaseBlog", "No data found at 'blogs' node");
                }

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    try {
                        Blog blog = postSnapshot.getValue(Blog.class);
                        if (blog != null) {
                            if (blog.getId() == null || blog.getId().isEmpty()) {
                                blog.setId(postSnapshot.getKey());
                            }
                            allBlogs.add(blog);
                        }
                    } catch (Exception e) {
                        Log.e("FirebaseBlog", "Error parsing blog: " + postSnapshot.getKey(), e);
                        // Manual parse fallback
                        Blog manualBlog = new Blog();
                        manualBlog.setId(postSnapshot.getKey());
                        manualBlog.setTitle(readString(postSnapshot, "title"));
                        manualBlog.setHeading(readString(postSnapshot, "heading"));
                        manualBlog.setSubheading(readString(postSnapshot, "subheading"));
                        manualBlog.setContent(readString(postSnapshot, "content"));
                        manualBlog.setDate(readString(postSnapshot, "date"));
                        manualBlog.setImage(readString(postSnapshot, "image"));
                        manualBlog.setThumbnailImage(readString(postSnapshot, "thumbnailImage"));
                        manualBlog.setDescription(readString(postSnapshot, "description"));
                        allBlogs.add(manualBlog);
                    }
                }
                
                Log.d("FirebaseBlog", "Total blogs loaded: " + allBlogs.size());
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                if (allBlogs.isEmpty()) {
                    Toast.makeText(BlogGeneral.this, "Danh sách bài viết trống", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BlogGeneral.this, "Tải thành công " + allBlogs.size() + " bài viết", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Log.e("FirebaseBlog", "Firebase Error: " + error.getMessage() + ", Details: " + error.getDetails());
                Toast.makeText(BlogGeneral.this, "Lỗi Firebase: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
