package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.adapter.RelatedBlogAdapter;
import com.teatrack_mcd_253eie502802_group02.model.Blog;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;

import java.util.ArrayList;
import java.util.List;

public class BlogDetail extends AppCompatActivity {

    private TextView txtDate, txtHeading, txtContent, tvTopTitle;
    private ImageView imgSingle, imgGalleryLeft, imgGalleryRight1, imgGalleryRight2;
    private View btnBack, btnShare;
    private LinearLayout layoutGallery;
    private RecyclerView rvRelatedBlogs;
    private DatabaseReference mDatabase;
    private String blogId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blog_detail);
        setupMainInsets();

        // Lấy ID bài viết từ Intent
        blogId = getIntent().getStringExtra("blog_id");
        if (blogId == null) {
            Toast.makeText(this, R.string.msg_under_development, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        mDatabase = FirebaseDatabase.getInstance().getReference("blogs");
        
        loadBlogDetail();
        loadRelatedBlogs();
        
        setupHeader();
        setupNavBar();
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
        tvTopTitle = findViewById(R.id.tvTopTitle);
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);

        txtDate = findViewById(R.id.txtDetailDate);
        txtHeading = findViewById(R.id.txtDetailHeading);
        txtContent = findViewById(R.id.txtDetailContent);
        
        imgSingle = findViewById(R.id.imgDetailSingle);
        imgGalleryLeft = findViewById(R.id.imgGalleryLeft);
        imgGalleryRight1 = findViewById(R.id.imgGalleryRight1);
        imgGalleryRight2 = findViewById(R.id.imgGalleryRight2);
        layoutGallery = findViewById(R.id.layoutDetailGallery);
        
        rvRelatedBlogs = findViewById(R.id.rvRelatedBlogs);
        rvRelatedBlogs.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Xóa text mặc định ban đầu
        if (txtDate != null) txtDate.setText("");
        if (txtHeading != null) txtHeading.setText("");
        if (txtContent != null) txtContent.setText("");
    }

    private void loadBlogDetail() {
        mDatabase.child(blogId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Blog blog = snapshot.getValue(Blog.class);
                if (blog != null) {
                    if (blog.getId() == null || blog.getId().isEmpty()) {
                        blog.setId(snapshot.getKey());
                    }
                    displayBlog(blog);
                } else {
                    Toast.makeText(BlogDetail.this, "Không tìm thấy nội dung bài viết", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BlogDetail.this, "Lỗi kết nối Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayBlog(Blog blog) {
        if (tvTopTitle != null) tvTopTitle.setText(blog.getTitle());
        txtDate.setText(blog.getDate());
        
        // Xử lý tiêu đề chính (Heading)
        String heading = (blog.getHeading() != null) ? blog.getHeading() : blog.getTitle();
        txtHeading.setText(Html.fromHtml(heading, Html.FROM_HTML_MODE_COMPACT));
        
        // Áp dụng màu sắc nếu có từ Firebase
        if (blog.getHeadingColor() != null) {
            try {
                txtHeading.setTextColor(Color.parseColor(blog.getHeadingColor()));
            } catch (Exception ignored) {}
        }

        // Xử lý hiển thị hình ảnh (Single vs Gallery)
        if ("gallery".equals(blog.getLayoutType()) && blog.getImages() != null && blog.getImages().size() >= 2) {
            layoutGallery.setVisibility(View.VISIBLE);
            imgSingle.setVisibility(View.GONE);
            
            loadImage(blog.getImages().get(0), imgGalleryLeft);
            loadImage(blog.getImages().get(1), imgGalleryRight1);
            if (blog.getImages().size() >= 3) {
                loadImage(blog.getImages().get(2), imgGalleryRight2);
            } else {
                loadImage(blog.getImages().get(1), imgGalleryRight2);
            }
        } else {
            layoutGallery.setVisibility(View.GONE);
            imgSingle.setVisibility(View.VISIBLE);
            loadImage(blog.getImage() != null ? blog.getImage() : blog.getDisplayImage(), imgSingle);
        }

        // Hiển thị nội dung chính bài viết (Hỗ trợ định dạng HTML)
        if (blog.getContent() != null) {
            txtContent.setText(Html.fromHtml(blog.getContent(), Html.FROM_HTML_MODE_COMPACT));
        }
    }

    private void loadImage(String imageSource, ImageView imageView) {
        if (imageSource == null || imageSource.trim().isEmpty()) return;
        String source = imageSource.trim();
        
        if (source.startsWith("http")) {
            Glide.with(this).load(source)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(imageView);
        } else {
            // Remove file extension and standardize name
            String name = source;
            int dot = name.lastIndexOf('.');
            if (dot > 0) {
                name = name.substring(0, dot);
            }
            name = name.replace(".", "_").replace("-", "_");
            
            int resId = getResources().getIdentifier(name, "mipmap", getPackageName());
            if (resId == 0) {
                resId = getResources().getIdentifier(name, "drawable", getPackageName());
            }

            Glide.with(this)
                    .load(resId != 0 ? resId : R.drawable.ic_launcher_background)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(imageView);
        }
    }

    private void loadRelatedBlogs() {
        // Tải tối đa 10 bài viết liên quan
        mDatabase.limitToFirst(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Blog> relatedList = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Blog blog = postSnapshot.getValue(Blog.class);
                    if (blog != null) {
                        if (blog.getId() == null || blog.getId().isEmpty()) {
                            blog.setId(postSnapshot.getKey());
                        }
                        if (!blog.getId().equals(blogId)) {
                            relatedList.add(blog);
                        }
                    }
                }
                RelatedBlogAdapter adapter = new RelatedBlogAdapter(BlogDetail.this, relatedList);
                rvRelatedBlogs.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupHeader() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        if (btnShare != null) {
            btnShare.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String title = (txtHeading != null) ? txtHeading.getText().toString() : "Blog";
                intent.putExtra(Intent.EXTRA_TEXT, "Check out this blog: " + title);
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
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}