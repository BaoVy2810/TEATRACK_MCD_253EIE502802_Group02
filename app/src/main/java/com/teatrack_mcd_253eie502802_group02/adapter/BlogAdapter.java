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

        // Binding dữ liệu: Ưu tiên title cho danh sách tổng quát
        holder.txtTitle.setText(blog.getTitle());
        holder.txtDate.setText(blog.getDate());
        
        // Hiển thị mô tả ngắn (snippet)
        if (blog.getDescription() != null && !blog.getDescription().isEmpty()) {
            holder.txtDesc.setVisibility(View.VISIBLE);
            holder.txtDesc.setText(blog.getDescription());
        } else {
            holder.txtDesc.setVisibility(View.GONE);
        }

        // Tải hình ảnh (Thumbnail)
        String imageSource = blog.getDisplayImage();
        if (imageSource != null && (imageSource.startsWith("http://") || imageSource.startsWith("https://"))) {
            Glide.with(context)
                    .load(imageSource)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.imgThumbnail);
        } else if (imageSource != null) {
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

        // CHỨC NĂNG: Bấm vào thẻ để mở BlogDetail tương ứng
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BlogDetail.class);
            intent.putExtra("blog_id", blog.getId()); // Truyền đúng ID bài viết
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail;
        TextView txtTitle, txtDate, txtDesc;

        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgBlogThumbnail);
            txtTitle = itemView.findViewById(R.id.txtBlogTitle);
            txtDate = itemView.findViewById(R.id.txtBlogDate);
            txtDesc = itemView.findViewById(R.id.txtBlogDesc);
        }
    }
}