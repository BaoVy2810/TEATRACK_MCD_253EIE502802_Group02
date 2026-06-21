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

public class RelatedBlogAdapter extends RecyclerView.Adapter<RelatedBlogAdapter.RelatedViewHolder> {

    private final Context context;
    private final List<Blog> blogList;

    public RelatedBlogAdapter(Context context, List<Blog> blogList) {
        this.context = context;
        this.blogList = blogList;
    }

    @NonNull
    @Override
    public RelatedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_related_blog, parent, false);
        return new RelatedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RelatedViewHolder holder, int position) {
        Blog blog = blogList.get(position);

        holder.txtTitle.setText(blog.getTitle());
        holder.txtDate.setText(blog.getDate());
        holder.txtDesc.setText(blog.getDescription());

        String imageSource = blog.getDisplayImage();
        Glide.with(context)
                .load(resolveImageRes(context, imageSource))
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.imgThumbnail);

        View.OnClickListener clickListener = v -> {
            Intent intent = new Intent(context, BlogDetail.class);
            intent.putExtra("blog_id", blog.getId());
            context.startActivity(intent);
        };

        holder.itemView.setOnClickListener(clickListener);
        holder.btnViewDetail.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }

    private static int resolveImageRes(Context context, String filename) {
        if (filename == null || filename.isEmpty()) return R.drawable.ic_launcher_background;
        if (filename.startsWith("http://") || filename.startsWith("https://")) return 0;
        String name = filename.contains(".")
                ? filename.substring(0, filename.lastIndexOf("."))
                : filename;
        name = name.replace(".", "_");
        int resId = context.getResources().getIdentifier(name, "mipmap", context.getPackageName());
        return resId != 0 ? resId : R.drawable.ic_launcher_background;
    }

    public static class RelatedViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail;
        TextView txtTitle, txtDate, txtDesc, btnViewDetail;

        public RelatedViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgRelatedThumbnail);
            txtTitle = itemView.findViewById(R.id.txtRelatedTitle);
            txtDate = itemView.findViewById(R.id.txtRelatedDate);
            txtDesc = itemView.findViewById(R.id.txtRelatedDesc);
            btnViewDetail = itemView.findViewById(R.id.btnViewDetail);
        }
    }
}