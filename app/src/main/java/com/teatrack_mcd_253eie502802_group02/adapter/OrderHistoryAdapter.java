package com.teatrack_mcd_253eie502802_group02.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.FirebaseOrder;
import com.teatrack_mcd_253eie502802_group02.util.OrderStatusBadgeHelper;
import com.teatrack_mcd_253eie502802_group02.util.PriceFormatHelper;
import com.teatrack_mcd_253eie502802_group02.model.FirebaseOrderItem;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {

    public interface OnOrderClickListener {
        void onViewDetails(FirebaseOrder order);
    }

    private static final String DB_URL =
            "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";

    private final Context context;
    private final List<FirebaseOrder> displayList = new ArrayList<>();
    private OnOrderClickListener listener;

    public OrderHistoryAdapter(Context context) {
        this.context = context;
    }

    public void setListener(OnOrderClickListener listener) {
        this.listener = listener;
    }

    public void setOrders(List<FirebaseOrder> orders) {
        displayList.clear();
        displayList.addAll(orders);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_order_history_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        FirebaseOrder order = displayList.get(position);

        // Order ID
        h.tvOrderId.setText(context.getString(R.string.str_order_id_prefix, order.getOrderId()));

        // Date/time
        h.tvDate.setText(formatDate(order.getCreatedAt()));

        // Item count
        int count = order.getItems() != null ? order.getItems().size() : 0;
        h.tvItemCount.setText(context.getResources().getQuantityString(
                R.plurals.str_item_count, count, count));

        // Branch — agency name from Firebase agencies node
        loadBranchName(h.tvBranch, order);

        // Total
        h.tvTotal.setText(formatPrice(order.getTotal()));

        // Status badge
        OrderStatusBadgeHelper.apply(h.tvStatus, order.getStatus() != null ? order.getStatus() : "");

        // Thumbnail — use first item's product image if available, else placeholder
        loadThumbnail(h.ivThumbnail, order);

        // Clicks
        h.btnViewDetails.setOnClickListener(v -> {
            if (listener != null) listener.onViewDetails(order);
        });
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onViewDetails(order);
        });
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    // Cache: cacheKey (productId or productName) → image string from Firebase
    private static final Map<String, String> productImageCache = new HashMap<>();
    private static final Map<String, String> agencyNameCache = new HashMap<>();
    private static boolean agencyIndexLoaded = false;
    private static final Map<String, String> agencyNameByAddress = new HashMap<>();

    private void loadBranchName(TextView tv, FirebaseOrder order) {
        String agencyId = order.getAgencyId();
        String branch = order.getBranchAddress();
        if (branch == null || branch.isEmpty()) {
            branch = order.getCustomerAddress();
        }
        final String fallbackAddress = branch;

        if (agencyId != null && !agencyId.isEmpty()) {
            tv.setTag("agency:" + agencyId);
            String cached = agencyNameCache.get(agencyId);
            if (cached != null) {
                tv.setText(cached.isEmpty() ? fallbackBranchLabel(fallbackAddress) : cached);
                return;
            }
            FirebaseDatabase.getInstance(DB_URL)
                    .getReference("agencies")
                    .child(agencyId)
                    .child("name")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String name = snapshot.getValue(String.class);
                            String resolved = name != null ? name.trim() : "";
                            agencyNameCache.put(agencyId, resolved);
                            if (!("agency:" + agencyId).equals(tv.getTag())) return;
                            tv.setText(resolved.isEmpty()
                                    ? fallbackBranchLabel(fallbackAddress)
                                    : resolved);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            if (!("agency:" + agencyId).equals(tv.getTag())) return;
                            tv.setText(fallbackBranchLabel(fallbackAddress));
                        }
                    });
            return;
        }

        resolveBranchNameByAddress(tv, fallbackAddress);
    }

    private void resolveBranchNameByAddress(TextView tv, String branchAddress) {
        if (branchAddress == null || branchAddress.isEmpty()) {
            tv.setTag(null);
            tv.setText(context.getString(R.string.str_branch_unknown));
            return;
        }

        String addressKey = normalizeAddressKey(branchAddress);
        tv.setTag("address:" + addressKey);

        if (agencyIndexLoaded) {
            String name = agencyNameByAddress.get(addressKey);
            tv.setText(name != null && !name.isEmpty() ? name : branchAddress);
            return;
        }

        tv.setText(branchAddress);
        FirebaseDatabase.getInstance(DB_URL)
                .getReference("agencies")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        agencyNameByAddress.clear();
                        agencyNameCache.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String id = child.getKey();
                            String name = child.child("name").getValue(String.class);
                            String address = child.child("address").getValue(String.class);
                            if (id != null && name != null) {
                                agencyNameCache.put(id, name.trim());
                            }
                            if (address != null && name != null) {
                                agencyNameByAddress.put(normalizeAddressKey(address), name.trim());
                            }
                        }
                        agencyIndexLoaded = true;
                        if (!("address:" + addressKey).equals(tv.getTag())) return;
                        String name = agencyNameByAddress.get(addressKey);
                        tv.setText(name != null && !name.isEmpty() ? name : branchAddress);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (!("address:" + addressKey).equals(tv.getTag())) return;
                        tv.setText(branchAddress);
                    }
                });
    }

    private String fallbackBranchLabel(String branchAddress) {
        if (branchAddress != null && !branchAddress.isEmpty()) return branchAddress;
        return context.getString(R.string.str_branch_unknown);
    }

    private static String normalizeAddressKey(String address) {
        return address.toLowerCase(Locale.US)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private void loadThumbnail(ImageView iv, FirebaseOrder order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            showPlaceholder(iv);
            return;
        }

        FirebaseOrderItem first = order.getItems().get(0);

        // Fast path: image filename stored on the order item
        String storedImage = first.getImage();
        if (storedImage != null && !storedImage.isEmpty()) {
            applyImage(iv, storedImage);
            return;
        }

        // Path 2: fetch by productId
        String productId = first.getProductId();
        if (productId != null && !productId.isEmpty()) {
            String cacheKey = "id:" + productId;
            iv.setTag(cacheKey);
            String cached = productImageCache.get(cacheKey);
            if (cached != null) {
                applyImage(iv, cached);
                return;
            }
            showPlaceholder(iv);
            FirebaseDatabase.getInstance(DB_URL)
                    .getReference("products")
                    .child(productId)
                    .child("image")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String val = snapshot.getValue(String.class);
                            if (val != null && !val.isEmpty()) {
                                productImageCache.put(cacheKey, val);
                                if (cacheKey.equals(iv.getTag())) applyImage(iv, val);
                            } else {
                                // productId lookup returned empty — try by name
                                fetchImageByName(iv, first.getProductName());
                            }
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                    });
            return;
        }

        // Path 3: productId missing — search all products by name
        fetchImageByName(iv, first.getProductName());
    }

    private void fetchImageByName(ImageView iv, String productName) {
        if (productName == null || productName.isEmpty()) {
            showPlaceholder(iv);
            return;
        }
        String cacheKey = "name:" + productName;
        iv.setTag(cacheKey);
        String cached = productImageCache.get(cacheKey);
        if (cached != null) {
            applyImage(iv, cached);
            return;
        }
        FirebaseDatabase.getInstance(DB_URL)
                .getReference("products")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String found = null;
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String name = child.child("name").getValue(String.class);
                            if (productName.equals(name)) {
                                found = child.child("image").getValue(String.class);
                                break;
                            }
                        }
                        String val = (found != null) ? found : "";
                        productImageCache.put(cacheKey, val);
                        if (cacheKey.equals(iv.getTag())) applyImage(iv, val);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void applyImage(ImageView iv, String imageValue) {
        if (imageValue == null || imageValue.isEmpty()) {
            showPlaceholder(iv);
            return;
        }
        if (imageValue.startsWith("http://") || imageValue.startsWith("https://")) {
            Glide.with(context)
                    .load(imageValue)
                    .placeholder(R.mipmap.logo_ngo_gia)
                    .error(R.mipmap.logo_ngo_gia)
                    .centerCrop()
                    .into(iv);
        } else {
            String key = imageValue.toLowerCase().replaceAll("\\.[^.]+$", "");
            int resId = context.getResources().getIdentifier(key, "mipmap", context.getPackageName());
            Glide.with(context)
                    .load(resId != 0 ? resId : R.mipmap.logo_ngo_gia)
                    .centerCrop()
                    .into(iv);
        }
    }

    private void showPlaceholder(ImageView iv) {
        Glide.with(context).load(R.mipmap.logo_ngo_gia).centerCrop().into(iv);
    }

    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "";
        // Sử dụng Locale hiện tại của ứng dụng thay vì hardcode Locale.ENGLISH
        Locale currentLocale = context.getResources().getConfiguration().getLocales().get(0);
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            input.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            Date date = input.parse(isoDate);
            if (date == null) return isoDate;
            SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy • hh:mm a", currentLocale);
            return output.format(date);
        } catch (ParseException e) {
            try {
                SimpleDateFormat input2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                input2.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                Date date = input2.parse(isoDate);
                if (date == null) return isoDate;
                SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy • hh:mm a", currentLocale);
                return output.format(date);
            } catch (ParseException ex) {
                return isoDate;
            }
        }
    }

    private String formatPrice(int amount) {
        return PriceFormatHelper.formatVnd(amount);
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvOrderId;
        final TextView tvStatus;
        final ImageView ivThumbnail;
        final TextView tvDate;
        final TextView tvItemCount;
        final TextView tvBranch;
        final TextView tvTotal;
        final TextView btnViewDetails;

        ViewHolder(@NonNull View v) {
            super(v);
            tvOrderId     = v.findViewById(R.id.tvOrderId);
            tvStatus      = v.findViewById(R.id.tvStatus);
            ivThumbnail   = v.findViewById(R.id.ivThumbnail);
            tvDate        = v.findViewById(R.id.tvDate);
            tvItemCount   = v.findViewById(R.id.tvItemCount);
            tvBranch      = v.findViewById(R.id.tvBranch);
            tvTotal       = v.findViewById(R.id.tvTotal);
            btnViewDetails = v.findViewById(R.id.btnViewDetails);
        }
    }
}
