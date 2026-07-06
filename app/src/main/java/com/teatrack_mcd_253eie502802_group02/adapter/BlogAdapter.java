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
import com.teatrack_mcd_253eie502802_group02.admin.AdminBlogDetail;
import com.teatrack_mcd_253eie502802_group02.client.BlogDetail;
import com.teatrack_mcd_253eie502802_group02.model.Blog;

import java.util.List;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.BlogViewHolder> {

    private static final int TYPE_CLIENT = 0;
    private static final int TYPE_ADMIN  = 1;

    private final Context context;
    private final List<Blog> blogList;
    private final boolean isAdmin;

    /** Client constructor */
    public BlogAdapter(Context context, List<Blog> blogList) {
        this(context, blogList, false);
    }

    /** Admin constructor */
    public BlogAdapter(Context context, List<Blog> blogList, boolean isAdmin) {
        this.context  = context;
        this.blogList = blogList;
        this.isAdmin  = isAdmin;
    }

    @Override
    public int getItemViewType(int position) {
        return isAdmin ? TYPE_ADMIN : TYPE_CLIENT;
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = (viewType == TYPE_ADMIN)
                ? R.layout.item_blog_admin
                : R.layout.item_blog_card;
        View view = LayoutInflater.from(context).inflate(layout, parent, false);
        return new BlogViewHolder(view, viewType == TYPE_ADMIN);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        Blog blog = blogList.get(position);

        if (isAdmin) {
            bindAdmin(holder, blog);
        } else {
            bindClient(holder, blog);
        }
    }

    // ─── Client bind ───────────────────────────────────────────────────────────

    private void bindClient(BlogViewHolder holder, Blog blog) {
        holder.txtTitle.setText(blog.getTitle());
        holder.txtDate.setText(blog.getDate());

        if (blog.getDescription() != null && !blog.getDescription().isEmpty()) {
            holder.txtDesc.setVisibility(View.VISIBLE);
            holder.txtDesc.setText(blog.getDescription());
        } else {
            holder.txtDesc.setVisibility(View.GONE);
        }

        loadImage(blog.getDisplayImage(), holder.imgThumbnail, false);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BlogDetail.class);
            intent.putExtra("blog_id", blog.getId());
            context.startActivity(intent);
        });
    }

    // ─── Admin bind ────────────────────────────────────────────────────────────

    private void bindAdmin(BlogViewHolder holder, Blog blog) {
        String displayTitle = (blog.getTitle() != null && !blog.getTitle().isEmpty())
                ? blog.getTitle()
                : blog.getHeading();
        holder.tvBlogTitle.setText(displayTitle);

        if ("published".equalsIgnoreCase(blog.getStatus())) {
            holder.tvBlogStatus.setBackgroundResource(R.drawable.bg_status_published);
            holder.tvBlogStatus.setText(context.getString(R.string.str_status_published));
        } else {
            holder.tvBlogStatus.setBackgroundResource(R.drawable.bg_status_draft);
            holder.tvBlogStatus.setText(context.getString(R.string.str_status_draft));
        }

        String imageUrl = (blog.getThumbnailImage() != null && !blog.getThumbnailImage().isEmpty())
                ? blog.getThumbnailImage()
                : blog.getImage();
        loadImage(imageUrl, holder.ivBlogThumbnail, true);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminBlogDetail.class);
            intent.putExtra("blog_id", blog.getId());
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

    // ─── Shared image loader ───────────────────────────────────────────────────

    private void loadImage(String source, ImageView target, boolean centerCrop) {
        if (source == null || source.isEmpty()) {
            target.setImageResource(R.drawable.ic_launcher_background);
            return;
        }
        if (source.startsWith("http://") || source.startsWith("https://")) {
            Glide.with(context)
                    .load(source)
                    .placeholder(R.drawable.ic_launcher_background)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(centerCrop
                            ? new com.bumptech.glide.request.RequestOptions().centerCrop()
                            : new com.bumptech.glide.request.RequestOptions())
                    .into(target);
        } else {
            int resId = resolveImageRes(context, source);
            Glide.with(context)
                    .load(resId != 0 ? resId : R.drawable.ic_launcher_background)
                    .apply(centerCrop
                            ? new com.bumptech.glide.request.RequestOptions().centerCrop()
                            : new com.bumptech.glide.request.RequestOptions())
                    .into(target);
        }
    }

    private static int resolveImageRes(Context context, String filename) {
        if (filename == null || filename.trim().isEmpty()) return 0;
        String name = filename.trim();
        int dot = name.lastIndexOf('.');
        if (dot > 0) name = name.substring(0, dot);
        name = name.replace(".", "_").replace("-", "_");
        int resId = context.getResources().getIdentifier(name, "mipmap", context.getPackageName());
        if (resId == 0)
            resId = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        return resId;
    }

    // ─── Utility ───────────────────────────────────────────────────────────────

    @Override
    public int getItemCount() {
        return blogList != null ? blogList.size() : 0;
    }

    public void updateList(List<Blog> newList) {
        blogList.clear();
        blogList.addAll(newList);
        notifyDataSetChanged();
    }

    // ─── ViewHolder ────────────────────────────────────────────────────────────

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        // Client views
        ImageView imgThumbnail;
        TextView txtTitle, txtDate, txtDesc;

        // Admin views
        TextView tvBlogTitle, tvBlogStatus;
        ImageView ivBlogThumbnail;
        ImageButton btnEditBlog, btnDeleteBlog;

        public BlogViewHolder(@NonNull View itemView, boolean isAdmin) {
            super(itemView);
            if (isAdmin) {
                tvBlogTitle     = itemView.findViewById(R.id.tvBlogTitle);
                tvBlogStatus    = itemView.findViewById(R.id.tvBlogStatus);
                ivBlogThumbnail = itemView.findViewById(R.id.ivBlogThumbnail);
                btnEditBlog     = itemView.findViewById(R.id.btnEditBlog);
                btnDeleteBlog   = itemView.findViewById(R.id.btnDeleteBlog);
            } else {
                imgThumbnail = itemView.findViewById(R.id.imgBlogThumbnail);
                txtTitle     = itemView.findViewById(R.id.txtBlogTitle);
                txtDate      = itemView.findViewById(R.id.txtBlogDate);
                txtDesc      = itemView.findViewById(R.id.txtBlogDesc);
            }
        }
    }
}
