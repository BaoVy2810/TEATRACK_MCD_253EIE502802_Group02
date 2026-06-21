package com.teatrack_mcd_253eie502802_group02.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.Agency;

import java.util.List;

public class ClientAgencyAdapter extends RecyclerView.Adapter<ClientAgencyAdapter.AgencyViewHolder> {

    private final Context context;
    private final List<Agency> agencyList;

    public ClientAgencyAdapter(Context context, List<Agency> agencyList) {
        this.context = context;
        this.agencyList = agencyList;
    }

    @NonNull
    @Override
    public AgencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_agency_client, parent, false);
        return new AgencyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AgencyViewHolder holder, int position) {
        Agency agency = agencyList.get(position);

        holder.tvAgencyName.setText(agency.getName());
        holder.tvAgencyAddress.setText(agency.getAddress());
        
        String phoneText = context.getString(R.string.str_agency_phone) + ": " + agency.getPhone();
        holder.tvAgencyPhone.setText(phoneText);

        String imageUrl = agency.getImage();
        if (imageUrl != null && imageUrl.startsWith("http")) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.mipmap.logo_ngo_gia)
                    .error(R.mipmap.logo_ngo_gia)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.ivAgency);
        } else if (imageUrl != null && !imageUrl.isEmpty()) {
            String resourceName = imageUrl.contains(".") ? imageUrl.substring(0, imageUrl.lastIndexOf(".")) : imageUrl;
            int resId = context.getResources().getIdentifier(resourceName, "mipmap", context.getPackageName());
            if (resId == 0) {
                resId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
            }

            if (resId != 0) {
                Glide.with(context).load(resId).centerCrop().into(holder.ivAgency);
            } else {
                holder.ivAgency.setImageResource(R.mipmap.logo_ngo_gia);
            }
        } else {
            holder.ivAgency.setImageResource(R.mipmap.logo_ngo_gia);
        }

        holder.itemView.setOnClickListener(v -> {
            String mapUrl = agency.getMapEmbed();
            if (mapUrl != null && !mapUrl.isEmpty()) {
                try {
                    if (mapUrl.contains("maps/embed")) {
                        mapUrl = mapUrl.replace("maps/embed", "maps");
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl));
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context, "Link bản đồ không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Chi nhánh chưa cập nhật vị trí bản đồ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return agencyList != null ? agencyList.size() : 0;
    }

    public void updateList(List<Agency> newList) {
        this.agencyList.clear();
        this.agencyList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class AgencyViewHolder extends RecyclerView.ViewHolder {
        TextView tvAgencyName, tvAgencyAddress, tvAgencyPhone;
        ImageView ivAgency;

        public AgencyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAgencyName = itemView.findViewById(R.id.tvAgencyName);
            tvAgencyAddress = itemView.findViewById(R.id.tvAgencyAddress);
            tvAgencyPhone = itemView.findViewById(R.id.tvAgencyPhone);
            ivAgency = itemView.findViewById(R.id.ivAgency);
        }
    }
}
