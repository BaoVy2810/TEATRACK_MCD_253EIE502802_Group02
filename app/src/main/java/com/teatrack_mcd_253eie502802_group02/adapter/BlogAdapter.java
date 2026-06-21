package com.teatrack_mcd_253eie502802_group02.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.admin.AdminBlog;
import com.teatrack_mcd_253eie502802_group02.client.BlogDetail;
import com.teatrack_mcd_253eie502802_group02.model.Blog;

import java.util.List;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.BlogViewHolder> {

    private final Context context;
    private final List<Blog> blogList;

    public BlogAdapter(Context context, List<Blog> blogList) {
        this.context = context;
        this.blogList = blogList;
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_blog_admin, parent, false);
        return new BlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        Blog blog = blogList.get(position);

        // Hiển thị Title hoặc Heading nếu Title rỗng
        String displayTitle = blog.getTitle();
        if (displayTitle == null || displayTitle.isEmpty()) {
            displayTitle = blog.getHeading();
        }
        holder.tvBlogTitle.setText(displayTitle);
        
        holder.tvBlogStatus.setText(blog.getStatus());

        // Update status background based on value
        if ("published".equalsIgnoreCase(blog.getStatus())) {
            holder.tvBlogStatus.setBackgroundResource(R.drawable.bg_status_published);
            holder.tvBlogStatus.setText(context.getString(R.string.str_status_published));
        } else {
            holder.tvBlogStatus.setBackgroundResource(R.drawable.bg_status_draft);
            holder.tvBlogStatus.setText(context.getString(R.string.str_status_draft));
        }

        // Ưu tiên thumbnailImage, nếu không có thì dùng image
        String imageUrl = blog.getThumbnailImage();
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageUrl = blog.getImage();
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (imageUrl.startsWith("http")) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(holder.ivBlogThumbnail);
            } else {
                int resId = context.getResources().getIdentifier(imageUrl.replace(".png", "").replace(".jpg", ""), "mipmap", context.getPackageName());
                if (resId == 0) resId = context.getResources().getIdentifier(imageUrl.replace(".png", "").replace(".jpg", ""), "drawable", context.getPackageName());
                if (resId != 0) {
                    Glide.with(context).load(resId).centerCrop().into(holder.ivBlogThumbnail);
                }
            }
        } else {
            holder.ivBlogThumbnail.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BlogDetail.class);
            intent.putExtra("blog", blog);
            context.startActivity(intent);
        });

        holder.btnEditBlog.setOnClickListener(v -> {
            if (context instanceof AdminBlog) {
                ((AdminBlog) context).showEditBlogDialog(blog);
            }
        });

        holder.btnDeleteBlog.setOnClickListener(v -> {
            if (context instanceof AdminBlog) {
                ((AdminBlog) context).deleteBlog(blog);
            }
        });
    }

    @Override
    public int getItemCount() {
        return blogList != null ? blogList.size() : 0;
    }

    public void updateList(List<Blog> newList) {
        this.blogList.clear();
        this.blogList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        TextView tvBlogTitle, tvBlogStatus;
        ImageView ivBlogThumbnail;
        ImageButton btnEditBlog, btnDeleteBlog;

        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBlogTitle = itemView.findViewById(R.id.tvBlogTitle);
            tvBlogStatus = itemView.findViewById(R.id.tvBlogStatus);
            ivBlogThumbnail = itemView.findViewById(R.id.ivBlogThumbnail);
            btnEditBlog = itemView.findViewById(R.id.btnEditBlog);
            btnDeleteBlog = itemView.findViewById(R.id.btnDeleteBlog);
        }
    }
}
