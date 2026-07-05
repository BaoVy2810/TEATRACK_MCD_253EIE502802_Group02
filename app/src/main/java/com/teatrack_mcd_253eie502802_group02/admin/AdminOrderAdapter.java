package com.teatrack_mcd_253eie502802_group02.admin;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.FirebaseOrder;
import com.teatrack_mcd_253eie502802_group02.model.FirebaseOrderItem;
import com.teatrack_mcd_253eie502802_group02.util.OrderStatusBadgeHelper;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.ViewHolder> {

    public static final String ACTION_CONFIRM       = "confirm";
    public static final String ACTION_VIEW_DETAIL   = "view_detail";
    public static final String ACTION_CANCEL        = "cancel";
    public static final String ACTION_UPDATE_STATUS = "update_status";

    private static final String STATUS_PENDING    = "pending";
    private static final String STATUS_PROCESSING = "processing";
    private static final String STATUS_READY      = "ready";
    private static final String STATUS_SHIPPING   = "shipping";
    private static final String STATUS_COMPLETED  = "completed";
    private static final String STATUS_CANCELLED  = "cancelled";

    private static final String DB_URL =
            "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";

    // In-memory image cache
    private static final Map<String, String> imageCache = new HashMap<>();

    public interface ActionCallback {
        void onAction(FirebaseOrder order, String action);
    }

    private final Context context;
    private List<FirebaseOrder> orders;
    private final ActionCallback callback;

    public AdminOrderAdapter(Context context, List<FirebaseOrder> orders, ActionCallback callback) {
        this.context  = context;
        this.orders   = orders;
        this.callback = callback;
    }

    public void updateData(List<FirebaseOrder> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_admin_order_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        FirebaseOrder order = orders.get(position);
        String status = safe(order.getStatus());

        // Order ID
        String orderId = safe(order.getOrderId());
        h.tvOrderId.setText(orderId.isEmpty() ? safe(order.getId()) : "#" + orderId);

        // Date
        String rawDate = safe(order.getDate());
        if (rawDate.isEmpty()) rawDate = safe(order.getCreatedAt());
        h.tvOrderDate.setText(formatDate(rawDate));

        // Payment
        h.tvPaymentMethod.setText(safe(order.getPaymentMethod(), "—"));

        // Customer
        h.tvCustomerName.setText(safe(order.getCustomerName(), "—"));
        h.tvCustomerPhone.setText(safe(order.getCustomerPhone(), "—"));

        // Item count
        List<FirebaseOrderItem> items = order.getItems();
        int count = (items != null) ? items.size() : 0;
        h.tvItemCount.setText(count + " " + context.getString(R.string.str_item_unit));

        // Total
        h.tvOrderTotal.setText(formatVnd(order.getTotal()));

        // Branch address — prefer branchAddress, fall back to agencyId for legacy orders
        String branch = safe(order.getBranchAddress());
        if (branch.isEmpty()) branch = safe(order.getAgencyId());
        h.tvOrderBranch.setText(branch.isEmpty() ? "—" : branch);

        // Status badge
        OrderStatusBadgeHelper.apply(h.tvStatusBadge, status);

        // Stepper
        applyStepperProgress(h, status);

        // Action buttons
        configureActionButtons(h, order);

        // Thumbnail — first item image
        if (items != null && !items.isEmpty()) {
            loadProductImage(h.ivOrderThumbnail, items.get(0));
        }
    }

    // ── Stepper ──────────────────────────────────────────────────────────────

    private void applyStepperProgress(ViewHolder h, String status) {
        int activeStep = getActiveStep(status);

        FrameLayout[] circles = {h.stepCircle1, h.stepCircle2, h.stepCircle3, h.stepCircle4, h.stepCircle5};
        ImageView[]   imgs    = {h.stepImg1, h.stepImg2, h.stepImg3, h.stepImg4, h.stepImg5};
        View[]        lines   = {h.stepLine1, h.stepLine2, h.stepLine3, h.stepLine4};
        TextView[]    labels  = {h.stepLabel1, h.stepLabel2, h.stepLabel3, h.stepLabel4, h.stepLabel5};

        int colorActive        = context.getColor(R.color.brand_blue);
        int colorInactive      = 0xFFBDBDBD;
        int colorLabelInactive = 0xFF999999;

        for (int i = 0; i < circles.length; i++) {
            boolean done    = (i + 1) <= activeStep;
            boolean current = (i + 1) == activeStep;

            circles[i].setBackgroundResource(
                    done ? R.drawable.bg_step_circle_active : R.drawable.bg_step_circle_inactive);

            imgs[i].setColorFilter(done
                    ? context.getColor(R.color.white)
                    : colorInactive);

            labels[i].setTextColor(current ? colorActive : colorLabelInactive);
            labels[i].setTypeface(null, current
                    ? android.graphics.Typeface.BOLD
                    : android.graphics.Typeface.NORMAL);
        }

        for (int i = 0; i < lines.length; i++) {
            boolean filled = (i + 2) <= activeStep;
            lines[i].setScaleX(filled ? 1f : 0f);
        }
    }

    private int getActiveStep(String status) {
        switch (status) {
            case STATUS_PENDING:    return 1;
            case STATUS_PROCESSING: return 2;
            case STATUS_READY:      return 3;
            case STATUS_SHIPPING:   return 4;
            case STATUS_COMPLETED:  return 5;
            default:                return 0; // cancelled or unknown → all inactive
        }
    }

    // ── Action buttons ───────────────────────────────────────────────────────

    private void configureActionButtons(ViewHolder h, FirebaseOrder order) {
        // Reset all
        h.btnConfirmOrder.setVisibility(View.GONE);
        h.btnViewDetail.setVisibility(View.GONE);
        h.btnCancelOrder.setVisibility(View.GONE);
        h.btnUpdateStatus.setVisibility(View.GONE);
        h.layoutActions.setVisibility(View.VISIBLE);

        switch (safe(order.getStatus())) {
            case STATUS_PENDING:
                h.btnConfirmOrder.setVisibility(View.VISIBLE);
                h.btnViewDetail.setVisibility(View.VISIBLE);
                h.btnCancelOrder.setVisibility(View.VISIBLE);
                break;
            case STATUS_PROCESSING:
            case STATUS_READY:
                h.btnViewDetail.setVisibility(View.VISIBLE);
                h.btnCancelOrder.setVisibility(View.VISIBLE);
                break;
            case STATUS_SHIPPING:
                h.btnViewDetail.setVisibility(View.VISIBLE);
                h.btnUpdateStatus.setVisibility(View.VISIBLE);
                break;
            case STATUS_COMPLETED:
                h.btnViewDetail.setVisibility(View.VISIBLE);
                break;
            case STATUS_CANCELLED:
                h.layoutActions.setVisibility(View.GONE);
                break;
        }

        h.btnConfirmOrder.setOnClickListener(v -> callback.onAction(order, ACTION_CONFIRM));
        h.btnViewDetail.setOnClickListener(v -> callback.onAction(order, ACTION_VIEW_DETAIL));
        h.btnCancelOrder.setOnClickListener(v -> callback.onAction(order, ACTION_CANCEL));
        h.btnUpdateStatus.setOnClickListener(v -> callback.onAction(order, ACTION_UPDATE_STATUS));
    }

    // ── Image loading ─────────────────────────────────────────────────────────

    private void loadProductImage(ImageView iv, FirebaseOrderItem item) {
        String storedImage = item.getImage();
        if (!TextUtils.isEmpty(storedImage)) {
            applyImage(iv, storedImage);
            return;
        }

        String productId = item.getProductId();
        if (!TextUtils.isEmpty(productId)) {
            String cacheKey = "id:" + productId;
            iv.setTag(cacheKey);
            String cached = imageCache.get(cacheKey);
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
                            if (!TextUtils.isEmpty(val)) {
                                imageCache.put(cacheKey, val);
                                if (cacheKey.equals(iv.getTag())) applyImage(iv, val);
                            } else {
                                fetchImageByName(iv, item.getProductName());
                            }
                        }
                        @Override public void onCancelled(@NonNull DatabaseError e) {}
                    });
            return;
        }

        fetchImageByName(iv, item.getProductName());
    }

    private void fetchImageByName(ImageView iv, String productName) {
        if (TextUtils.isEmpty(productName)) { showPlaceholder(iv); return; }
        String cacheKey = "name:" + productName;
        iv.setTag(cacheKey);
        String cached = imageCache.get(cacheKey);
        if (cached != null) { applyImage(iv, cached); return; }

        FirebaseDatabase.getInstance(DB_URL)
                .getReference("products")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String found = "";
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String name = child.child("name").getValue(String.class);
                            if (productName.equals(name)) {
                                String img = child.child("image").getValue(String.class);
                                if (img != null) found = img;
                                break;
                            }
                        }
                        imageCache.put(cacheKey, found);
                        if (cacheKey.equals(iv.getTag())) applyImage(iv, found);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    private void applyImage(ImageView iv, String imageValue) {
        if (TextUtils.isEmpty(imageValue)) { showPlaceholder(iv); return; }
        if (imageValue.startsWith("http://") || imageValue.startsWith("https://")) {
            Glide.with(context)
                    .load(imageValue)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .placeholder(R.mipmap.logo_ngo_gia)
                    .error(R.mipmap.logo_ngo_gia)
                    .centerCrop()
                    .into(iv);
        } else {
            String key = imageValue.toLowerCase().replaceAll("\\.[^.]+$", "");
            int resId = context.getResources().getIdentifier(key, "mipmap", context.getPackageName());
            Glide.with(context)
                    .load(resId != 0 ? resId : R.mipmap.logo_ngo_gia)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .centerCrop()
                    .into(iv);
        }
    }

    private void showPlaceholder(ImageView iv) {
        Glide.with(context).load(R.mipmap.logo_ngo_gia).centerCrop().into(iv);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String formatDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "—";
        for (String pattern : new String[]{
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd HH:mm:ss"
        }) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                Date date = sdf.parse(rawDate);
                if (date != null) {
                    return new SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.ENGLISH).format(date);
                }
            } catch (ParseException ignored) {}
        }
        return rawDate;
    }

    private static String formatVnd(int amount) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(amount) + "₫";
    }

    private static String safe(String s) {
        return s != null ? s : "";
    }

    private static String safe(String s, String fallback) {
        return (s != null && !s.isEmpty()) ? s : fallback;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView     tvOrderId, tvStatusBadge, tvOrderDate, tvPaymentMethod;
        TextView     tvCustomerName, tvCustomerPhone, tvItemCount, tvOrderTotal, tvOrderBranch;
        ImageView    ivOrderThumbnail;
        MaterialButton btnConfirmOrder, btnViewDetail, btnCancelOrder, btnUpdateStatus;
        ViewGroup    layoutActions;

        FrameLayout  stepCircle1, stepCircle2, stepCircle3, stepCircle4, stepCircle5;
        ImageView    stepImg1, stepImg2, stepImg3, stepImg4, stepImg5;
        View         stepLine1, stepLine2, stepLine3, stepLine4;
        TextView     stepLabel1, stepLabel2, stepLabel3, stepLabel4, stepLabel5;

        ViewHolder(@NonNull View v) {
            super(v);
            tvOrderId       = v.findViewById(R.id.tvOrderId);
            tvStatusBadge   = v.findViewById(R.id.tvStatusBadge);
            tvOrderDate     = v.findViewById(R.id.tvOrderDate);
            tvPaymentMethod = v.findViewById(R.id.tvPaymentMethod);
            tvCustomerName  = v.findViewById(R.id.tvCustomerName);
            tvCustomerPhone = v.findViewById(R.id.tvCustomerPhone);
            tvItemCount     = v.findViewById(R.id.tvItemCount);
            tvOrderTotal    = v.findViewById(R.id.tvOrderTotal);
            tvOrderBranch   = v.findViewById(R.id.tvOrderBranch);
            ivOrderThumbnail = v.findViewById(R.id.ivOrderThumbnail);
            btnConfirmOrder  = v.findViewById(R.id.btnConfirmOrder);
            btnViewDetail    = v.findViewById(R.id.btnViewDetail);
            btnCancelOrder   = v.findViewById(R.id.btnCancelOrder);
            btnUpdateStatus  = v.findViewById(R.id.btnUpdateStatus);
            layoutActions    = v.findViewById(R.id.layoutActions);

            stepCircle1 = v.findViewById(R.id.stepCircle1);
            stepCircle2 = v.findViewById(R.id.stepCircle2);
            stepCircle3 = v.findViewById(R.id.stepCircle3);
            stepCircle4 = v.findViewById(R.id.stepCircle4);
            stepCircle5 = v.findViewById(R.id.stepCircle5);
            stepImg1    = v.findViewById(R.id.stepImg1);
            stepImg2    = v.findViewById(R.id.stepImg2);
            stepImg3    = v.findViewById(R.id.stepImg3);
            stepImg4    = v.findViewById(R.id.stepImg4);
            stepImg5    = v.findViewById(R.id.stepImg5);
            stepLine1   = v.findViewById(R.id.stepLine1);
            stepLine2   = v.findViewById(R.id.stepLine2);
            stepLine3   = v.findViewById(R.id.stepLine3);
            stepLine4   = v.findViewById(R.id.stepLine4);
            stepLabel1  = v.findViewById(R.id.stepLabel1);
            stepLabel2  = v.findViewById(R.id.stepLabel2);
            stepLabel3  = v.findViewById(R.id.stepLabel3);
            stepLabel4  = v.findViewById(R.id.stepLabel4);
            stepLabel5  = v.findViewById(R.id.stepLabel5);
        }
    }
}
