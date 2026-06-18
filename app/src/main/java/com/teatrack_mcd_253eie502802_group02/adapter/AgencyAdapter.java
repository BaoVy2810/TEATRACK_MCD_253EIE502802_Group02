package com.teatrack_mcd_253eie502802_group02.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.Agency;

import java.util.List;

public class AgencyAdapter
        extends RecyclerView.Adapter<AgencyAdapter.ViewHolder> {

    private List<Agency> agencyList;
    private OnAgencyClickListener listener;

    public interface OnAgencyClickListener {
        void onAgencyClick(Agency agency);
    }

    public AgencyAdapter(List<Agency> agencyList,
                         OnAgencyClickListener listener) {

        this.agencyList = agencyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_agency,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        Agency agency = agencyList.get(position);

        holder.txtName.setText(agency.getName());
        holder.txtAddress.setText(agency.getAddress());
        holder.txtPhone.setText(agency.getPhone());

        holder.itemView.setOnClickListener(v ->
                listener.onAgencyClick(agency)
        );

        /*
         TODO
         Glide/Picasso load image

         Glide.with(holder.itemView.getContext())
              .load(agency.getImage())
              .into(holder.imgAgency);
        */
    }

    @Override
    public int getItemCount() {
        return agencyList.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        ImageView imgAgency;
        TextView txtName;
        TextView txtAddress;
        TextView txtPhone;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgAgency = itemView.findViewById(R.id.imgAgency);
            txtName = itemView.findViewById(R.id.txtName);
            txtAddress = itemView.findViewById(R.id.txtAddress);
            txtPhone = itemView.findViewById(R.id.txtPhone);
        }
    }
}