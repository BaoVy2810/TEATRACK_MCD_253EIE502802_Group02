package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.data.CartManager;
import com.teatrack_mcd_253eie502802_group02.model.CartItem;
import com.teatrack_mcd_253eie502802_group02.model.FirebaseOrder;
import com.teatrack_mcd_253eie502802_group02.model.FirebaseOrderItem;
import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderDetails extends BaseActivity {

    private static final String EXTRA_ORDER = "extra_order";
    private static final String DB_URL =
            "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";

    // In-memory image cache shared for the session
    private static final Map<String, String> imageCache = new HashMap<>();

    public static Intent newIntent(Context context, FirebaseOrder order) {
        Intent intent = new Intent(context, OrderDetails.class);
        intent.putExtra(EXTRA_ORDER, order);
        return intent;
    }

    // ── Views ────────────────────────────────────────────────────────────────

    // Header card
    private ImageView ivOrderThumbnail;
    private TextView tvDetailOrderId;
    private TextView tvDetailDate;
    private TextView tvPaymentChip;
    private TextView tvDetailStatusBadge;
    private TextView tvDetailItemCount;
    private TextView tvDetailTotal;
    private View vAddressDivider;
    private LinearLayout llDetailAddress;
    private TextView tvDetailAddress;

    // Items card
    private LinearLayout llOrderItems;

    // Delivery card
    private TextView tvDeliveryName;
    private TextView tvDeliveryPhone;
    private TextView tvDeliveryAddress;
    private TextView tvDeliveryPayment;
    private TextView tvDeliveryStatus;

    // Payment card
    private TextView tvPaySubtotal;
    private LinearLayout llDiscountRow;
    private TextView tvPayDiscount;
    private TextView tvPayShipping;
    private TextView tvPayTotal;
    private TextView tvTotalSavings;

    // Actions
    private LinearLayout llTwoActions;
    private LinearLayout llSingleAction;
    private MaterialButton btnLeftAction;
    private MaterialButton btnRightAction;
    private MaterialButton btnSingleAction;
    private TextView tvActionHint;

    private FirebaseOrder currentOrder;

    // ── Status constants ──────────────────────────────────────────────────────

    private static final String STATUS_PENDING    = "pending";
    private static final String STATUS_PROCESSING = "processing";
    private static final String STATUS_READY      = "ready";
    private static final String STATUS_SHIPPING   = "shipping";
    private static final String STATUS_COMPLETED  = "completed";
    private static final String STATUS_CANCELLED  = "cancelled";

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_details);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        FirebaseOrder order = (FirebaseOrder) getIntent().getSerializableExtra(EXTRA_ORDER);
        if (order == null) {
            finish();
            return;
        }

        currentOrder = order;
        bindViews();
        setupButtons();
        populateOrder(order);
    }

    // ── Bind ──────────────────────────────────────────────────────────────────

    private void bindViews() {
        ivOrderThumbnail    = findViewById(R.id.ivOrderThumbnail);
        tvDetailOrderId     = findViewById(R.id.tvDetailOrderId);
        tvDetailDate        = findViewById(R.id.tvDetailDate);
        tvPaymentChip       = findViewById(R.id.tvPaymentChip);
        tvDetailStatusBadge = findViewById(R.id.tvDetailStatusBadge);
        tvDetailItemCount   = findViewById(R.id.tvDetailItemCount);
        tvDetailTotal       = findViewById(R.id.tvDetailTotal);
        vAddressDivider     = findViewById(R.id.vAddressDivider);
        llDetailAddress     = findViewById(R.id.llDetailAddress);
        tvDetailAddress     = findViewById(R.id.tvDetailAddress);

        llOrderItems = findViewById(R.id.llOrderItems);

        tvDeliveryName    = findViewById(R.id.tvDeliveryName);
        tvDeliveryPhone   = findViewById(R.id.tvDeliveryPhone);
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);
        tvDeliveryPayment = findViewById(R.id.tvDeliveryPayment);
        tvDeliveryStatus  = findViewById(R.id.tvDeliveryStatus);

        tvPaySubtotal  = findViewById(R.id.tvPaySubtotal);
        llDiscountRow  = findViewById(R.id.llDiscountRow);
        tvPayDiscount  = findViewById(R.id.tvPayDiscount);
        tvPayShipping  = findViewById(R.id.tvPayShipping);
        tvPayTotal     = findViewById(R.id.tvPayTotal);
        tvTotalSavings = findViewById(R.id.tvTotalSavings);

        llTwoActions    = findViewById(R.id.llTwoActions);
        llSingleAction  = findViewById(R.id.llSingleAction);
        btnLeftAction   = findViewById(R.id.btnLeftAction);
        btnRightAction  = findViewById(R.id.btnRightAction);
        btnSingleAction = findViewById(R.id.btnSingleAction);
        tvActionHint    = findViewById(R.id.tvActionHint);
    }

    private void setupButtons() {
    }

    // ── Populate ─────────────────────────────────────────────────────────────

    private void populateOrder(FirebaseOrder order) {
        populateHeader(order);
        populateItems(order.getItems());
        populateDelivery(order);
        populatePayment(order);
        populateActions(order.getStatus());
    }

    private void populateHeader(FirebaseOrder order) {
        String status = safe(order.getStatus());

        // Thumbnail — same logic as OrderHistoryAdapter
        List<FirebaseOrderItem> items = order.getItems();
        if (items != null && !items.isEmpty()) {
            loadProductImage(ivOrderThumbnail, items.get(0));
        }

        // Order ID
        String orderId = safe(order.getOrderId());
        tvDetailOrderId.setText(orderId.isEmpty() ? safe(order.getId()) : "#" + orderId);

        // Date — formatted same as OrderHistoryAdapter
        String rawDate = safe(order.getDate());
        if (rawDate.isEmpty()) rawDate = safe(order.getCreatedAt());
        tvDetailDate.setText(formatDate(rawDate));

        // Payment chip — neutral blue style, different from status badges
        String payment = safe(order.getPaymentMethod());
        tvPaymentChip.setText(payment.isEmpty() ? "—" : payment);

        // Status badge
        applyStatusBadge(tvDetailStatusBadge, status);

        // Item count
        int count = (items != null) ? items.size() : 0;
        tvDetailItemCount.setText(count + (count == 1 ? " item" : " items"));

        // Total
        tvDetailTotal.setText(formatVnd(order.getTotal()));

        // Address row
        String addr = safe(order.getCustomerAddress());
        if (!addr.isEmpty()) {
            vAddressDivider.setVisibility(View.VISIBLE);
            llDetailAddress.setVisibility(View.VISIBLE);
            tvDetailAddress.setText(addr);
        }
    }

    private void populateItems(List<FirebaseOrderItem> items) {
        llOrderItems.removeAllViews();
        if (items == null || items.isEmpty()) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < items.size(); i++) {
            FirebaseOrderItem item = items.get(i);

            View row = inflater.inflate(R.layout.item_order_detail_product, llOrderItems, false);

            ImageView ivImage = row.findViewById(R.id.ivItemImage);
            TextView tvName   = row.findViewById(R.id.tvItemName);
            TextView tvCustom = row.findViewById(R.id.tvItemCustomizations);
            TextView tvToppin = row.findViewById(R.id.tvItemToppings);
            TextView tvTotal  = row.findViewById(R.id.tvItemLineTotal);
            TextView tvQty    = row.findViewById(R.id.tvItemQtyUnit);

            // Image — same logic as adapter
            loadProductImage(ivImage, item);

            // Name
            tvName.setText(safe(item.getProductName()));

            // Size / sugar / ice
            String size  = safe(item.getSize());
            String sugar = safe(item.getSugar());
            String ice   = safe(item.getIce());
            StringBuilder custom = new StringBuilder();
            if (!size.isEmpty())  { custom.append("Size ").append(size); }
            if (!sugar.isEmpty()) {
                if (custom.length() > 0) custom.append("  •  ");
                custom.append(sugar).append(" sugar");
            }
            if (!ice.isEmpty()) {
                if (custom.length() > 0) custom.append("  •  ");
                custom.append(ice).append(" ice");
            }
            tvCustom.setText(custom.toString());

            // Toppings
            String toppings = safe(item.getToppings());
            if (!toppings.isEmpty()) {
                tvToppin.setVisibility(View.VISIBLE);
                tvToppin.setText("+ " + toppings);
            }

            // Price
            tvTotal.setText(formatVnd(item.getLineTotal()));
            tvQty.setText("x" + item.getQuantity() + "  •  " + formatVnd(item.getUnitPrice()) + "/sp");

            llOrderItems.addView(row);

            // Divider between items (not after last)
            if (i < items.size() - 1) {
                View divider = new View(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divider.setLayoutParams(lp);
                divider.setBackgroundColor(0xFFF3F4F6);
                llOrderItems.addView(divider);
            }
        }
    }

    private void populateDelivery(FirebaseOrder order) {
        tvDeliveryName.setText(safe(order.getCustomerName(), "—"));
        tvDeliveryPhone.setText(safe(order.getCustomerPhone(), "—"));
        tvDeliveryAddress.setText(safe(order.getCustomerAddress(), "—"));
        tvDeliveryPayment.setText(safe(order.getPaymentMethod(), "—"));
        applyStatusBadge(tvDeliveryStatus, safe(order.getStatus()));
    }

    private void populatePayment(FirebaseOrder order) {
        tvPaySubtotal.setText(formatVnd(order.getSubtotal()));

        int discount = order.getDiscount();
        if (discount > 0) {
            llDiscountRow.setVisibility(View.VISIBLE);
            tvPayDiscount.setText("−" + formatVnd(discount));
            tvTotalSavings.setVisibility(View.VISIBLE);
            tvTotalSavings.setText(formatVnd(discount) + " saved");
        }

        int shipping = order.getShipping();
        tvPayShipping.setText(shipping == 0
                ? getString(R.string.str_free_shipping)
                : formatVnd(shipping));

        tvPayTotal.setText(formatVnd(order.getTotal()));
    }

    private void populateActions(String status) {
        status = safe(status);

        switch (status) {
            case STATUS_PENDING:
            case STATUS_PROCESSING:
            case STATUS_READY:
            case STATUS_SHIPPING: {
                llTwoActions.setVisibility(View.VISIBLE);
                btnLeftAction.setText(R.string.str_btn_track_order);
                btnLeftAction.setIconResource(R.drawable.ic_track_order_order_detail);
                btnRightAction.setText(R.string.str_btn_contact_support);
                btnRightAction.setIconResource(R.drawable.ic_contact_support_order_detail);
                btnLeftAction.setOnClickListener(v -> launchTrackOrder());
                btnRightAction.setOnClickListener(v ->
                        Toast.makeText(this, R.string.str_coming_soon, Toast.LENGTH_SHORT).show());
                tvActionHint.setText(R.string.str_hint_active_order);
                break;
            }
            case STATUS_COMPLETED: {
                llTwoActions.setVisibility(View.VISIBLE);
                btnLeftAction.setText(R.string.str_btn_reorder);
                btnLeftAction.setIconResource(R.drawable.ic_reorder_order_detail);
                btnRightAction.setText(R.string.str_btn_contact_support);
                btnRightAction.setIconResource(R.drawable.ic_contact_support_order_detail);
                btnLeftAction.setOnClickListener(v -> launchReorder());
                btnRightAction.setOnClickListener(v ->
                        Toast.makeText(this, R.string.str_coming_soon, Toast.LENGTH_SHORT).show());
                tvActionHint.setText(R.string.str_hint_completed_order);
                break;
            }
            case STATUS_CANCELLED: {
                llSingleAction.setVisibility(View.VISIBLE);
                btnSingleAction.setText(R.string.str_btn_order_again);
                btnSingleAction.setIconResource(R.drawable.ic_reorder_order_detail);
                btnSingleAction.setOnClickListener(v -> launchReorder());
                tvActionHint.setText(R.string.str_hint_cancelled_order);
                break;
            }
            default:
                break;
        }
    }

    private void launchTrackOrder() {
        String orderId = safe(currentOrder != null ? currentOrder.getOrderId() : "");
        if (orderId.isEmpty()) orderId = safe(currentOrder != null ? currentOrder.getId() : "");
        Intent intent = new Intent(this, OrderTracking.class);
        intent.putExtra("orderId", orderId);
        startActivity(intent);
    }

    private void launchReorder() {
        if (currentOrder == null || currentOrder.getItems() == null
                || currentOrder.getItems().isEmpty()) {
            Toast.makeText(this, R.string.str_coming_soon, Toast.LENGTH_SHORT).show();
            return;
        }

        CartManager cart = CartManager.getInstance();
        cart.clear();

        for (FirebaseOrderItem src : currentOrder.getItems()) {
            CartItem item = new CartItem();
            item.setProductId(safe(src.getProductId()));
            item.setProductName(safe(src.getProductName()));
            item.setImage(safe(src.getImage()));
            item.setSize(safe(src.getSize(), "M"));
            item.setSugar(safe(src.getSugar(), "Medium"));
            item.setIce(safe(src.getIce(), "Medium"));
            item.setQuantity(Math.max(1, src.getQuantity()));
            item.setUnitPrice(src.getUnitPrice());
            item.setVipUnitPrice(0);

            // Restore toppings as display-only (price already in unitPrice)
            String toppingStr = safe(src.getToppings());
            if (!toppingStr.isEmpty()) {
                for (String part : toppingStr.split(",")) {
                    part = part.trim();
                    if (part.isEmpty()) continue;
                    // Parse "Name x2" or just "Name"
                    int qty = 1;
                    String name = part;
                    int xIdx = part.lastIndexOf(" x");
                    if (xIdx > 0) {
                        try {
                            qty = Integer.parseInt(part.substring(xIdx + 2).trim());
                            name = part.substring(0, xIdx).trim();
                        } catch (NumberFormatException ignored) {}
                    }
                    item.addTopping(name, 0, qty);
                }
            }

            cart.addItem(item);
        }

        startActivity(new Intent(this, Cart.class));
    }

    // ── Image loading (mirrors OrderHistoryAdapter logic) ─────────────────────

    private void loadProductImage(ImageView iv, FirebaseOrderItem item) {
        // Fast path: image URL/filename stored on item
        String storedImage = item.getImage();
        if (!TextUtils.isEmpty(storedImage)) {
            applyImage(iv, storedImage);
            return;
        }

        // Fetch by productId
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

        // Fallback: search by name
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
            Glide.with(this)
                    .load(imageValue)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .placeholder(R.mipmap.logo_ngo_gia)
                    .error(R.mipmap.logo_ngo_gia)
                    .centerCrop()
                    .into(iv);
        } else {
            // Local mipmap resource by filename
            String key = imageValue.toLowerCase().replaceAll("\\.[^.]+$", "");
            int resId = getResources().getIdentifier(key, "mipmap", getPackageName());
            Glide.with(this)
                    .load(resId != 0 ? resId : R.mipmap.logo_ngo_gia)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .centerCrop()
                    .into(iv);
        }
    }

    private void showPlaceholder(ImageView iv) {
        Glide.with(this).load(R.mipmap.logo_ngo_gia).centerCrop().into(iv);
    }

    // ── Status badge ──────────────────────────────────────────────────────────

    private void applyStatusBadge(TextView badge, String status) {
        int bgRes;
        String label;
        int textColor;

        switch (status) {
            case STATUS_PENDING:
                bgRes = R.drawable.bg_status_pending;
                label = getString(R.string.str_status_pending);
                textColor = 0xFF854D0E;
                break;
            case STATUS_PROCESSING:
                bgRes = R.drawable.bg_status_processing;
                label = getString(R.string.str_status_processing);
                textColor = 0xFF1E40AF;
                break;
            case STATUS_READY:
                bgRes = R.drawable.bg_status_ready;
                label = getString(R.string.str_status_ready);
                textColor = 0xFF5B21B6;
                break;
            case STATUS_SHIPPING:
                bgRes = R.drawable.bg_status_shipping;
                label = getString(R.string.str_status_shipping);
                textColor = 0xFFC2410C;
                break;
            case STATUS_COMPLETED:
            case "delivered":
                bgRes = R.drawable.bg_status_delivered;
                label = getString(R.string.str_status_completed);
                textColor = 0xFF065F46;
                break;
            case STATUS_CANCELLED:
                bgRes = R.drawable.bg_status_cancelled;
                label = getString(R.string.str_status_cancelled);
                textColor = 0xFF6B7280;
                break;
            default:
                bgRes = R.drawable.bg_status_pending;
                label = status.isEmpty() ? "—" : status;
                textColor = 0xFF6B7280;
                break;
        }

        badge.setText(label);
        badge.setTextColor(textColor);
        badge.setBackgroundResource(bgRes);

        int iconRes = statusIconRes(status);
        if (iconRes != 0) {
            Drawable icon = AppCompatResources.getDrawable(this, iconRes);
            if (icon != null) {
                icon = DrawableCompat.wrap(icon.mutate());
                DrawableCompat.setTint(icon, textColor);
                int size = dpToPx(12);
                icon.setBounds(0, 0, size, size);
                badge.setCompoundDrawables(icon, null, null, null);
                badge.setCompoundDrawablePadding(dpToPx(4));
            }
        } else {
            badge.setCompoundDrawables(null, null, null, null);
        }
    }

    private int statusIconRes(String status) {
        switch (status) {
            case STATUS_PENDING:    return R.drawable.pending_tab;
            case STATUS_PROCESSING: return R.drawable.processing_tab;
            case STATUS_READY:      return R.drawable.ready_tab;
            case STATUS_SHIPPING:   return R.drawable.shipping_tab;
            case STATUS_COMPLETED:
            case "delivered":       return R.drawable.finished_tab;
            default:                return 0;
        }
    }

    // ── Date formatting (same as OrderHistoryAdapter) ─────────────────────────

    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "—";
        // Try full ISO with milliseconds
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
                    SimpleDateFormat out = new SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.ENGLISH);
                    return out.format(date);
                }
            } catch (ParseException ignored) {}
        }
        return rawDate; // fallback: show as-is
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private static String safe(String s) {
        return s != null ? s : "";
    }

    private static String safe(String s, String fallback) {
        return (s != null && !s.isEmpty()) ? s : fallback;
    }

    private static String formatVnd(int amount) {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return nf.format(amount) + "₫";
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
