package com.teatrack_mcd_253eie502802_group02.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.ContactRequest;
import com.teatrack_mcd_253eie502802_group02.util.FeedbackTopicHelper;

import java.util.List;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ComplaintViewHolder> {

    private List<ContactRequest> contactList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ContactRequest contact);
    }

    public ComplaintAdapter(List<ContactRequest> contactList, OnItemClickListener listener) {
        this.contactList = contactList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_complaint, parent, false);
        return new ComplaintViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintViewHolder holder, int position) {
        ContactRequest contact = contactList.get(position);
        
        holder.tvUserName.setText(contact.getFullname());
        holder.tvUserContact.setText(contact.getEmail() + " | " + contact.getPhone());
        
        String content = contact.getContent();
        if (content != null && content.length() > 100) {
            content = content.substring(0, 97) + "...";
        }
        holder.tvContent.setText(content);
        holder.tvCreatedAt.setText(contact.getTime() != null ? contact.getTime() : "");
        holder.tvBranchAddress.setText(contact.getBranch() != null ? contact.getBranch() : "");
        FeedbackTopicHelper.applyTopicBadgeV2(holder.tvTopicBadge, contact.getTopic());

        // Status mapping: 1 -> Pending, 2 -> Resolved
        if (contact.getStatus() == 1) {
            holder.tvStatus.setText("Pending");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
            holder.tvStatus.setTextColor(Color.parseColor("#F59E0B"));
        } else if (contact.getStatus() == 2) {
            holder.tvStatus.setText("Resolved");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_delivered);
            holder.tvStatus.setTextColor(Color.parseColor("#065F46"));
        }

        if (contact.getNote() != null && !contact.getNote().isEmpty()) {
            holder.layoutAdminReply.setVisibility(View.VISIBLE);
            holder.tvAdminReply.setText(contact.getNote());
        } else {
            holder.layoutAdminReply.setVisibility(View.GONE);
        }

        holder.btnReply.setText(contact.getStatus() == 1 ? "Handle" : "View");
        holder.btnReply.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(contact);
            }
        });
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(contact);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    static class ComplaintViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvStatus, tvTopicBadge, tvUserContact, tvContent, tvCreatedAt, tvBranchAddress, tvAdminReply;
        LinearLayout layoutAdminReply;
        MaterialButton btnReply;

        public ComplaintViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTopicBadge = itemView.findViewById(R.id.tvTopicBadge);
            tvUserContact = itemView.findViewById(R.id.tvUserContact);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            tvBranchAddress = itemView.findViewById(R.id.tvBranchAddress);
            tvAdminReply = itemView.findViewById(R.id.tvAdminReply);
            layoutAdminReply = itemView.findViewById(R.id.layoutAdminReply);
            btnReply = itemView.findViewById(R.id.btnReply);
        }
    }
}
