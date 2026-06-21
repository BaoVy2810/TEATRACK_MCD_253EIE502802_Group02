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
import androidx.appcompat.app.AppCompatActivity;
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

    private TextView txtBreadcrumbTitle, txtDate, txtHeading, txtSubheading, txtContent;
    private ImageView imgSingle, imgGalleryLeft, imgGalleryRight1, imgGalleryRight2;
    private LinearLayout layoutGallery;
    private RecyclerView rvRelatedBlogs;
    private DatabaseReference mDatabase;
    private String blogId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_detail);

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

    private void initViews() {
        txtBreadcrumbTitle = findViewById(R.id.txtDetailBreadcrumbTitle);
        txtDate = findViewById(R.id.txtDetailDate);
        txtHeading = findViewById(R.id.txtDetailHeading);
        txtSubheading = findViewById(R.id.txtDetailSubheading);
        txtContent = findViewById(R.id.txtDetailContent);
        
        imgSingle = findViewById(R.id.imgDetailSingle);
        imgGalleryLeft = findViewById(R.id.imgGalleryLeft);
        imgGalleryRight1 = findViewById(R.id.imgGalleryRight1);
        imgGalleryRight2 = findViewById(R.id.imgGalleryRight2);
        layoutGallery = findViewById(R.id.layoutDetailGallery);
        
        rvRelatedBlogs = findViewById(R.id.rvRelatedBlogs);
        rvRelatedBlogs.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Xóa text mặc định ban đầu
        txtBreadcrumbTitle.setText("");
        txtDate.setText("");
        txtHeading.setText("");
        txtSubheading.setText("");
        txtContent.setText("");
    }

    private void loadBlogDetail() {
        mDatabase.child(blogId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Blog blog = snapshot.getValue(Blog.class);
                if (blog != null) {
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
        txtBreadcrumbTitle.setText(blog.getTitle());
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

        // Xử lý tiêu đề phụ (Subheading)
        if (blog.getSubheading() != null && !blog.getSubheading().isEmpty()) {
            txtSubheading.setVisibility(View.VISIBLE);
            txtSubheading.setText(blog.getSubheading());
            if (blog.getHeadingColor() != null) {
                try {
                    txtSubheading.setTextColor(Color.parseColor(blog.getHeadingColor()));
                } catch (Exception ignored) {}
            }
        } else {
            txtSubheading.setVisibility(View.GONE);
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
        if (imageSource == null || imageSource.isEmpty()) return;
        if (imageSource.startsWith("http://") || imageSource.startsWith("https://")) {
            Glide.with(this).load(imageSource)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(imageView);
        } else {
            // Bỏ phần mở rộng, thay dấu "." còn lại bằng "_" → lookup mipmap
            String name = imageSource.contains(".")
                    ? imageSource.substring(0, imageSource.lastIndexOf("."))
                    : imageSource;
            name = name.replace(".", "_");
            int resId = getResources().getIdentifier(name, "mipmap", getPackageName());
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
                    if (blog != null && !blog.getId().equals(blogId)) {
                        relatedList.add(blog);
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
            if (id == R.id.nav_promotion) {
                // Quay về danh sách blog
                Intent intent = new Intent(this, BlogGeneral.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return;
            }

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
        Intent intent = new Intent(this, BlogGeneral.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}