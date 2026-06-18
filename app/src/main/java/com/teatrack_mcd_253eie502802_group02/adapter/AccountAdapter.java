package com.teatrack_mcd_253eie502802_group02.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.databinding.ItemAccountBinding;
import com.teatrack_mcd_253eie502802_group02.model.User;

import java.util.ArrayList;
import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    public interface AccountActionListener {
        void onView(User user);
        void onEdit(User user);
        void onDelete(User user);
    }

    private final Context context;
    private final AccountActionListener listener;
    private final List<User> users = new ArrayList<>();

    public AccountAdapter(Context context, AccountActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAccountBinding binding = ItemAccountBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AccountViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void submitList(List<User> newUsers) {
        users.clear();
        users.addAll(newUsers);
        notifyDataSetChanged();
    }

    class AccountViewHolder extends RecyclerView.ViewHolder {
        private final ItemAccountBinding binding;

        AccountViewHolder(ItemAccountBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(User user) {
            binding.tvInitial.setText(getInitial(user.getFullName(), user.getUsername()));
            binding.tvAccountId.setText(safe(user.getId()));
            binding.tvFullName.setText(safe(user.getFullName()));
            binding.tvUsername.setText("@" + safe(user.getUsername()));
            binding.tvEmail.setText(safe(user.getEmail()));
            binding.tvPhone.setText(safe(user.getPhoneNumber()).isEmpty() ? "N/A" : user.getPhoneNumber());
            binding.tvAddress.setText(safe(user.getAddress()).isEmpty() ? "N/A" : user.getAddress());
            binding.tvCreatedAt.setText(safe(user.getCreatedAt()));

            bindRole(user.getRole());
            bindStatus(user.getStatus());

            binding.btnView.setOnClickListener(v -> listener.onView(user));
            binding.btnEdit.setOnClickListener(v -> listener.onEdit(user));
            binding.btnDelete.setOnClickListener(v -> listener.onDelete(user));
        }

        private void bindRole(String role) {
            binding.tvRole.setText(safe(role));
            binding.tvRole.setTypeface(Typeface.DEFAULT, "Customer Vip".equalsIgnoreCase(role) ? Typeface.BOLD : Typeface.NORMAL);
            binding.tvRole.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
            binding.tvRole.setBackground(null);
        }

        private void bindStatus(String status) {
            binding.tvStatus.setText(safe(status));

            int textColor;
            int bgColor;
            if ("Locked".equalsIgnoreCase(status)) {
                textColor = ContextCompat.getColor(context, R.color.danger);
                bgColor = ContextCompat.getColor(context, R.color.danger_bg);
            } else if ("Inactive".equalsIgnoreCase(status)) {
                textColor = ContextCompat.getColor(context, R.color.text_secondary);
                bgColor = ContextCompat.getColor(context, R.color.divider);
            } else {
                textColor = ContextCompat.getColor(context, R.color.success);
                bgColor = ContextCompat.getColor(context, R.color.success_bg);
            }

            binding.tvStatus.setTextColor(textColor);
            binding.tvStatus.setBackground(makeBadgeBackground(bgColor, textColor));
        }

        private GradientDrawable makeBadgeBackground(int bgColor, int strokeColor) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(bgColor);
            drawable.setCornerRadius(context.getResources().getDisplayMetrics().density * 999);
            drawable.setStroke(1, adjustAlpha(strokeColor, 0.35f));
            return drawable;
        }

        private int adjustAlpha(int color, float factor) {
            int alpha = Math.round(android.graphics.Color.alpha(color) * factor);
            return (color & 0x00FFFFFF) | (alpha << 24);
        }

        private String getInitial(String fullName, String username) {
            String source = !safe(fullName).isEmpty() ? fullName : username;
            source = safe(source).trim();
            return source.isEmpty() ? "?" : source.substring(0, 1).toUpperCase();
        }

        private String safe(String value) {
            return value == null ? "" : value;
        }
    }
}
