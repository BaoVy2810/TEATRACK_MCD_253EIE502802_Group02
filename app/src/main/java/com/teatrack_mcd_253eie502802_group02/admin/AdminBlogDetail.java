package com.teatrack_mcd_253eie502802_group02.admin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.Blog;

public class AdminBlogDetail extends AppCompatActivity {

    private TextView tvDate;
    private TextView tvTitle;
    private TextView tvContent;
    private TextView tvStatus;
    private ImageView ivBlogImage;
    private DatabaseReference blogsRef;
    private String blogId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_blog_detail);
        com.teatrack_mcd_253eie502802_group02.shared.ui.AdminInsetsHelper.apply(this);

        blogId = getIntent().getStringExtra("blog_id");
        if (blogId == null || blogId.trim().isEmpty()) {
            Toast.makeText(this, "Blog not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();

        blogsRef = FirebaseDatabase.getInstance().getReference("blogs");
        loadBlog();
    }

    private void initViews() {
        tvDate = findViewById(R.id.tvAdminBlogDate);
        tvTitle = findViewById(R.id.tvAdminBlogTitle);
        tvContent = findViewById(R.id.tvAdminBlogContent);
        tvStatus = findViewById(R.id.tvAdminBlogStatus);
        ivBlogImage = findViewById(R.id.ivAdminBlogImage);

        View btnBack = findViewById(R.id.btnAdminBlogBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> returnToAdminBlogList());
        }
    }

    private void loadBlog() {
        blogsRef.child(blogId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Blog blog = snapshot.getValue(Blog.class);
                if (blog == null) {
                    Toast.makeText(AdminBlogDetail.this, "Blog not found", Toast.LENGTH_SHORT).show();
                    returnToAdminBlogList();
                    return;
                }
                displayBlog(blog);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminBlogDetail.this, getString(R.string.str_error, error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayBlog(Blog blog) {
        String title = nonEmpty(blog.getTitle()) ? blog.getTitle() : blog.getHeading();
        tvTitle.setText(Html.fromHtml(title != null ? title : "", Html.FROM_HTML_MODE_COMPACT));
        tvDate.setText(blog.getDate() != null ? blog.getDate() : "");

        if (tvStatus != null) {
            tvStatus.setVisibility(View.GONE);
        }

        if (nonEmpty(blog.getHeadingColor())) {
            try {
                tvTitle.setTextColor(Color.parseColor(blog.getHeadingColor()));
            } catch (Exception ignored) {
                tvTitle.setTextColor(getColor(R.color.brand_blue));
            }
        }

        String image = blog.getDisplayImage();
        if (nonEmpty(image)) {
            ivBlogImage.setVisibility(View.VISIBLE);
            loadImage(image, ivBlogImage);
        } else {
            ivBlogImage.setVisibility(View.GONE);
        }

        String content = blog.getContent() != null ? blog.getContent() : "";
        tvContent.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT));
    }

    private void loadImage(String source, ImageView target) {
        String imageSource = source.trim();
        if (imageSource.startsWith("http://") || imageSource.startsWith("https://")) {
            Glide.with(this)
                    .load(imageSource)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(target);
            return;
        }

        int dot = imageSource.lastIndexOf('.');
        if (dot > 0) {
            imageSource = imageSource.substring(0, dot);
        }
        imageSource = imageSource.replace(".", "_").replace("-", "_");
        int resId = getResources().getIdentifier(imageSource, "mipmap", getPackageName());
        if (resId == 0) {
            resId = getResources().getIdentifier(imageSource, "drawable", getPackageName());
        }

        Glide.with(this)
                .load(resId != 0 ? resId : R.drawable.ic_launcher_background)
                .placeholder(R.drawable.ic_launcher_background)
                .into(target);
    }

    private boolean nonEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void returnToAdminBlogList() {
        Intent intent = new Intent(this, AdminBlog.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        returnToAdminBlogList();
    }
}
