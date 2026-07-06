package com.teatrack_mcd_253eie502802_group02.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.Branch;

import java.util.List;

public class BranchAnalysisAdapter extends RecyclerView.Adapter<BranchAnalysisAdapter.ViewHolder> {

    private final List<Branch> branches;

    public BranchAnalysisAdapter(List<Branch> branches) {
        this.branches = branches;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_branch_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Branch branch = branches.get(position);
        holder.tvBranchAddress.setText(branch.getAddress());
        holder.tvBranchOrders.setText(String.valueOf(branch.getNumOrders()));
        holder.tvBranchRevenue.setText(branch.getRevenue());

        boolean cyanRow = position % 2 == 1;
        boolean lastRow = position == getItemCount() - 1;
        int backgroundRes;
        if (cyanRow) {
            backgroundRes = lastRow ? R.drawable.bg_branch_row_cyan_bottom : R.drawable.bg_branch_row_cyan;
        } else {
            backgroundRes = lastRow ? R.drawable.bg_branch_row_cream_bottom : R.drawable.bg_branch_row_cream;
        }
        int textColor = ContextCompat.getColor(
                holder.itemView.getContext(),
                cyanRow ? R.color.branch_table_white_text : R.color.branch_table_blue_text
        );

        holder.itemView.setBackgroundResource(backgroundRes);
        holder.tvBranchAddress.setTextColor(textColor);
        holder.tvBranchOrders.setTextColor(textColor);
        holder.tvBranchRevenue.setTextColor(textColor);
    }

    @Override
    public int getItemCount() {
        return branches.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBranchAddress, tvBranchOrders, tvBranchRevenue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBranchAddress = itemView.findViewById(R.id.tvBranchAddress);
            tvBranchOrders = itemView.findViewById(R.id.tvBranchOrders);
            tvBranchRevenue = itemView.findViewById(R.id.tvBranchRevenue);
        }
    }
}
