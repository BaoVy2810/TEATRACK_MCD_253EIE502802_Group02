package com.teatrack_mcd_253eie502802_group02.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.admin.AdminAgency;
import com.teatrack_mcd_253eie502802_group02.model.Agency;

import java.util.List;

public class AgencyAdapter extends RecyclerView.Adapter<AgencyAdapter.AgencyViewHolder> {

    private final Context context;
    private final List<Agency> agencyList;
    private final DatabaseReference databaseReference;

    public AgencyAdapter(Context context, List<Agency> agencyList) {
        this.context = context;
        this.agencyList = agencyList;
        String firebaseUrl = "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";
        this.databaseReference = FirebaseDatabase.getInstance(firebaseUrl).getReference("agencies");
    }

    @NonNull
    @Override
    public AgencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_agency, parent, false);
        return new AgencyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AgencyViewHolder holder, int position) {
        Agency agency = agencyList.get(position);

        // 1. Hiển thị thông tin lên giao diện chữ
        holder.tvAgencyId.setText("ID: " + agency.getId());
        holder.tvAgencyName.setText(agency.getName());
        holder.tvAgencyAddress.setText(agency.getAddress());
        holder.tvAgencyPhone.setText("SĐT: " + agency.getPhone());

        // 2. Hiển thị trạng thái ẩn/hiện dữ liệu
        if (agency.isVisible()) {
            holder.btnVisibility.setImageResource(R.drawable.eye);
        } else {
            holder.btnVisibility.setImageResource(R.drawable.hide);
        }

        // Xử lý đổi trạng thái ẩn/hiện trực tiếp lên Firebase
        holder.btnVisibility.setOnClickListener(v -> {
            boolean newStatus = !agency.isVisible();
            databaseReference.child(agency.getId()).child("visible").setValue(newStatus)
                    .addOnSuccessListener(aVoid -> {
                        String message = newStatus ? "Chi nhánh đang hiển thị" : "Đã ẩn chi nhánh";
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    });
        });

        // 3. Sử dụng Glide hiển thị hình ảnh từ Firebase (Kiểm tra chuỗi link hợp lệ)
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
            // Xử lý nếu là tên file trong mipmap hoặc drawable (vd: "chi_nhanh1.png" hoặc "chi_nhanh1")
            String resourceName = imageUrl;
            if (resourceName.contains(".")) {
                resourceName = resourceName.substring(0, resourceName.lastIndexOf("."));
            }

            // Thử tìm trong mipmap trước (theo kết quả tìm kiếm thực tế)
            int resId = context.getResources().getIdentifier(resourceName, "mipmap", context.getPackageName());
            if (resId == 0) {
                // Nếu không thấy, thử tìm trong drawable
                resId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
            }

            if (resId != 0) {
                Glide.with(context)
                        .load(resId)
                        .placeholder(R.mipmap.logo_ngo_gia)
                        .error(R.mipmap.logo_ngo_gia)
                        .centerCrop()
                        .into(holder.ivAgency);
            } else {
                holder.ivAgency.setImageResource(R.mipmap.logo_ngo_gia);
            }
        } else {
            holder.ivAgency.setImageResource(R.mipmap.logo_ngo_gia);
        }

        // 4. Xử lý sự kiện nhấn vào item để điều hướng mở Google Maps
        View.OnClickListener mapClickListener = v -> {
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
        };

        if (holder.cardAgency != null) {
            holder.cardAgency.setOnClickListener(mapClickListener);
        } else {
            holder.itemView.setOnClickListener(mapClickListener);
        }

        // 5. Xử lý sự kiện nút SỬA điều hướng gọi trực tiếp sang hàm xử lý tập trung của AdminAgency
        holder.btnEdit.setOnClickListener(v -> {
            if (context instanceof AdminAgency) {
                ((AdminAgency) context).showEditAgencyDialog(agency);
            }
        });

        // 6. Xử lý sự kiện nút XÓA gọi phương thức xóa an toàn có Dialog xác nhận từ AdminAgency
        holder.btnDelete.setOnClickListener(v -> {
            if (context instanceof AdminAgency) {
                ((AdminAgency) context).deleteAgency(agency.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return agencyList != null ? agencyList.size() : 0;
    }

    /**
     * Cập nhật danh sách mới cho Adapter khi tìm kiếm dữ liệu thực tế
     */
    public void updateList(List<Agency> newList) {
        this.agencyList.clear();
        this.agencyList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class AgencyViewHolder extends RecyclerView.ViewHolder {
        TextView tvAgencyId, tvAgencyName, tvAgencyAddress, tvAgencyPhone;
        ImageView ivAgency;
        ImageButton btnEdit, btnDelete, btnVisibility;
        CardView cardAgency;

        public AgencyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAgencyId = itemView.findViewById(R.id.tvAgencyId);
            tvAgencyName = itemView.findViewById(R.id.tvAgencyName);
            tvAgencyAddress = itemView.findViewById(R.id.tvAgencyAddress);
            tvAgencyPhone = itemView.findViewById(R.id.tvAgencyPhone);
            ivAgency = itemView.findViewById(R.id.ivAgency);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnVisibility = itemView.findViewById(R.id.btnVisibility);
            cardAgency = itemView.findViewById(R.id.cardAgency);
        }
    }
}