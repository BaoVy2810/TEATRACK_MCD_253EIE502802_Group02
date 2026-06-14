package com.teatrack_mcd_253eie502802_group02.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.NewsItem;

import java.util.List;

public class NewsCardAdapter extends RecyclerView.Adapter<NewsCardAdapter.NewsViewHolder> {

    private final List<NewsItem> newsItems;

    public NewsCardAdapter(List<NewsItem> newsItems) {
        this.newsItems = newsItems;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news_card, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem item = newsItems.get(position);
        holder.imgNews.setImageResource(item.getImageRes());
        holder.tvNewsTitle.setText(item.getTitle());
        holder.tvNewsDate.setText(item.getDateRange());
    }

    @Override
    public int getItemCount() {
        return newsItems.size();
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView imgNews;
        TextView tvNewsTitle, tvNewsDate;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            imgNews = itemView.findViewById(R.id.imgNews);
            tvNewsTitle = itemView.findViewById(R.id.tvNewsTitle);
            tvNewsDate = itemView.findViewById(R.id.tvNewsDate);
        }
    }
}
