package com.teatrack_mcd_253eie502802_group02.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.teatrack_mcd_253eie502802_group02.R;
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_blog_card, parent, false);
        return new BlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        Blog blog = blogList.get(position);

        // Lấy tiêu đề từ trường 'heading' theo Firebase của bạn
        holder.txtTitle.setText(blog.getHeading());
        holder.txtDate.setText(blog.getDate());

        // Load ảnh: Hỗ trợ cả URL và tên resource drawable
        String imageSource = blog.getDisplayImage();
        
        if (imageSource != null && (imageSource.startsWith("http://") || imageSource.startsWith("https://"))) {
            // Nếu là URL (Firebase Storage hoặc web)
            Glide.with(context)
                    .load(imageSource)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.imgThumbnail);
        } else if (imageSource != null) {
            // Nếu là tên file (ví dụ: "blog_1.jpg"), ta bỏ phần mở rộng để lấy trong drawable
            String resourceName = imageSource;
            if (resourceName.contains(".")) {
                resourceName = resourceName.substring(0, resourceName.lastIndexOf("."));
            }
            
            int resId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
            Glide.with(context)
                    .load(resId != 0 ? resId : R.drawable.ic_launcher_background)
                    .into(holder.imgThumbnail);
        } else {
            holder.imgThumbnail.setImageResource(R.drawable.ic_launcher_background);
        }

        // Click → mở BlogDetail
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BlogDetail.class);
            intent.putExtra("blog_id", blog.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail;
        TextView txtTitle, txtDate;

        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgBlogThumbnail);
            txtTitle = itemView.findViewById(R.id.txtBlogTitle);
            txtDate = itemView.findViewById(R.id.txtBlogDate);
        }
    }
}