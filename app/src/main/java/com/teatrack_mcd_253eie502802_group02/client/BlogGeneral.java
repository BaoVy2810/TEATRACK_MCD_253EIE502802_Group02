package com.teatrack_mcd_253eie502802_group02.client;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

import java.util.ArrayList;
import java.util.List;

public class BlogGeneral extends AppCompatActivity {

    private RecyclerView rvBlogList;
    private ProgressBar progressBar;
    private List<Blog> allBlogs = new ArrayList<>();
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blog_general);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvBlogList = findViewById(R.id.rvBlogList);
        progressBar = findViewById(R.id.progressBar);

        rvBlogList.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo Firebase - Trỏ vào node 'blogs'
        mDatabase = FirebaseDatabase.getInstance().getReference("blogs");

        loadBlogsFromFirebase();
    }

    private void loadBlogsFromFirebase() {
        progressBar.setVisibility(View.VISIBLE);
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
                
                if (!allBlogs.isEmpty()) {
                    BlogAdapter adapter = new BlogAdapter(BlogGeneral.this, allBlogs);
                    rvBlogList.setAdapter(adapter);
                } else {
                    Toast.makeText(BlogGeneral.this, "Không có dữ liệu bài viết", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Log.e("Firebase", "Error: " + error.getMessage());
                Toast.makeText(BlogGeneral.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}