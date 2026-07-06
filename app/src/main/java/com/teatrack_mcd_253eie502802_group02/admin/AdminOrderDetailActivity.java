package com.teatrack_mcd_253eie502802_group02.admin;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.FirebaseOrder;
import com.teatrack_mcd_253eie502802_group02.model.FirebaseOrderItem;
import com.teatrack_mcd_253eie502802_group02.util.FirebaseOrderHelper;
import com.teatrack_mcd_253eie502802_group02.util.OrderItemDisplayHelper;
import com.teatrack_mcd_253eie502802_group02.util.OrderStatusBadgeHelper;
import com.teatrack_mcd_253eie502802_group02.util.PaymentMethodBadgeHelper;
import com.teatrack_mcd_253eie502802_group02.util.PriceFormatHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminOrderDetailActivity extends AppCompatActivity {

    private static final String DB_URL =
            "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";

    private static final String STATUS_PENDING    = "pending";
    private static final String STATUS_PROCESSING = "processing";
    private static final String STATUS_READY      = "ready";
    private static final String STATUS_SHIPPING   = "shipping";
    private static final String STATUS_COMPLETED  = "completed";
    private static final String STATUS_CANCELLED  = "cancelled";

    // In-memory image cache
    private static final Map<String, String> imageCache = new HashMap<>();

    // ── Views ──────────────────────────────────────────────────────────────────

    private ImageView    ivAdminOrderThumbnail;
    private TextView     tvAdminOrderId;
    private TextView     tvAdminOrderDate;
    private TextView     tvAdminPaymentChip;
    private TextView     tvAdminStatusBadge;
    private TextView     tvAdminItemCount;
    private TextView     tvAdminTotal;

    // Stepper
    private FrameLayout  stepCircle1, stepCircle2, stepCircle3, stepCircle4, stepCircle5;
    private ImageView    stepImg1, stepImg2, stepImg3, stepImg4, stepImg5;
    private View         stepLine1, stepLine2, stepLine3, stepLine4;
    private TextView     stepLabel1, stepLabel2, stepLabel3, stepLabel4, stepLabel5;

    // Items
    private LinearLayout llAdminOrderItems;

    // Customer
    private TextView     tvDeliveryName;
    private TextView     tvDeliveryPhone;
    private TextView     tvDeliveryAddress;
    private TextView     tvDeliveryPayment;
    private TextView     tvDeliveryBranch;
    private TextView     tvDeliveryStatus;

    // Payment
    private TextView     tvPaySubtotal;
    private TextView     tvPayDiscount;
    private TextView     tvPayShipping;
    private TextView     tvPayTotal;

    // Actions
    private MaterialButton btnAdminUpdateStatus;
    private MaterialButton btnAdminDeleteOrder;

    private FirebaseOrder currentOrder;
    private DatabaseReference orderRef;
    private ValueEventListener orderListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_detail);
        com.teatrack_mcd_253eie502802_group02.shared.ui.AdminInsetsHelper.apply(this);

        currentOrder = (FirebaseOrder) getIntent().getSerializableExtra("order");
        if (currentOrder == null) { finish(); return; }

        bindViews();
        setupTopBar();
        setupActionButtons();
        populateOrder(currentOrder);
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachOrderListener();
    }

    @Override
    protected void onStop() {
        detachOrderListener();
        super.onStop();
    }

    private void attachOrderListener() {
        String key = FirebaseOrderHelper.resolveFirebaseKey(currentOrder);
        if (key == null || key.isEmpty()) return;

        detachOrderListener();
        orderRef = FirebaseDatabase.getInstance(DB_URL).getReference("orders").child(key);
        orderListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                FirebaseOrder fresh = snapshot.getValue(FirebaseOrder.class);
                if (fresh == null) return;
                fresh.setId(snapshot.getKey());
                if (fresh.getOrderId() == null || fresh.getOrderId().isEmpty()) {
                    fresh.setOrderId(snapshot.getKey());
                }
                currentOrder = fresh;
                populateOrder(fresh);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminOrderDetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        orderRef.addValueEventListener(orderListener);
    }

    private void detachOrderListener() {
        if (orderRef != null && orderListener != null) {
            orderRef.removeEventListener(orderListener);
        }
        orderRef = null;
        orderListener = null;
    }

    // ── Bind ──────────────────────────────────────────────────────────────────

    private void bindViews() {
        ivAdminOrderThumbnail = findViewById(R.id.ivAdminOrderThumbnail);
        tvAdminOrderId        = findViewById(R.id.tvAdminOrderId);
        tvAdminOrderDate      = findViewById(R.id.tvAdminOrderDate);
        tvAdminPaymentChip    = findViewById(R.id.tvAdminPaymentChip);
        tvAdminStatusBadge    = findViewById(R.id.tvAdminStatusBadge);
        tvAdminItemCount      = findViewById(R.id.tvAdminItemCount);
        tvAdminTotal          = findViewById(R.id.tvAdminTotal);

        stepCircle1 = findViewById(R.id.stepCircle1);
        stepCircle2 = findViewById(R.id.stepCircle2);
        stepCircle3 = findViewById(R.id.stepCircle3);
        stepCircle4 = findViewById(R.id.stepCircle4);
        stepCircle5 = findViewById(R.id.stepCircle5);
        stepImg1    = findViewById(R.id.stepImg1);
        stepImg2    = findViewById(R.id.stepImg2);
        stepImg3    = findViewById(R.id.stepImg3);
        stepImg4    = findViewById(R.id.stepImg4);
        stepImg5    = findViewById(R.id.stepImg5);
        stepLine1   = findViewById(R.id.stepLine1);
        stepLine2   = findViewById(R.id.stepLine2);
        stepLine3   = findViewById(R.id.stepLine3);
        stepLine4   = findViewById(R.id.stepLine4);
        stepLabel1  = findViewById(R.id.stepLabel1);
        stepLabel2  = findViewById(R.id.stepLabel2);
        stepLabel3  = findViewById(R.id.stepLabel3);
        stepLabel4  = findViewById(R.id.stepLabel4);
        stepLabel5  = findViewById(R.id.stepLabel5);

        llAdminOrderItems  = findViewById(R.id.llAdminOrderItems);

        tvDeliveryName     = findViewById(R.id.tvDeliveryName);
        tvDeliveryPhone    = findViewById(R.id.tvDeliveryPhone);
        tvDeliveryAddress  = findViewById(R.id.tvDeliveryAddress);
        tvDeliveryPayment  = findViewById(R.id.tvDeliveryPayment);
        tvDeliveryBranch   = findViewById(R.id.tvDeliveryBranch);
        tvDeliveryStatus   = findViewById(R.id.tvDeliveryStatus);

        tvPaySubtotal      = findViewById(R.id.tvPaySubtotal);
        tvPayDiscount      = findViewById(R.id.tvPayDiscount);
        tvPayShipping      = findViewById(R.id.tvPayShipping);
        tvPayTotal         = findViewById(R.id.tvPayTotal);

        btnAdminUpdateStatus = findViewById(R.id.btnAdminUpdateStatus);
        btnAdminDeleteOrder  = findViewById(R.id.btnAdminDeleteOrder);
    }

    private void setupTopBar() {
        TextView tvTopTitle = findViewById(R.id.tvTopTitle);
        if (tvTopTitle != null) {
            tvTopTitle.setText(R.string.str_order_details_title);
        }
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }
    }

    // ── Action buttons ────────────────────────────────────────────────────────

    private void setupActionButtons() {
        btnAdminUpdateStatus.setOnClickListener(v -> showStatusDialog());
        btnAdminDeleteOrder.setOnClickListener(v -> showDeleteDialog());
    }

    private void showStatusDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_status, null);
        dialog.setContentView(dialogView);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.88),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        RadioGroup     rgStatus     = dialogView.findViewById(R.id.rgStatus);
        RadioButton    rbPending    = dialogView.findViewById(R.id.rbPending);
        RadioButton    rbProcessing = dialogView.findViewById(R.id.rbProcessing);
        RadioButton    rbReady      = dialogView.findViewById(R.id.rbReady);
        RadioButton    rbShipping   = dialogView.findViewById(R.id.rbShipping);
        RadioButton    rbCompleted  = dialogView.findViewById(R.id.rbCompleted);
        RadioButton    rbCancelled  = dialogView.findViewById(R.id.rbCancelled);
        MaterialButton btnCancel    = dialogView.findViewById(R.id.btnDialogCancel);
        MaterialButton btnConfirm   = dialogView.findViewById(R.id.btnDialogConfirm);

        RadioButton[]  allRbs   = {rbPending, rbProcessing, rbReady, rbShipping, rbCompleted, rbCancelled};
        String[]       statuses = {STATUS_PENDING, STATUS_PROCESSING, STATUS_READY,
                STATUS_SHIPPING, STATUS_COMPLETED, STATUS_CANCELLED};
        int[]          iconRes  = {R.drawable.pending_tab, R.drawable.processing_tab,
                R.drawable.ready_tab, R.drawable.shipping_tab, R.drawable.finished_tab, R.drawable.ic_status_cancelled};
        int            colorBlue = 0xFF0088FF;
        int            colorText = 0xFF1C1C1E;

        // Apply icons — all #0088FF tint
        for (int i = 0; i < allRbs.length; i++) {
            Drawable icon = AppCompatResources.getDrawable(this, iconRes[i]);
            if (icon != null) {
                icon = DrawableCompat.wrap(icon.mutate());
                DrawableCompat.setTint(icon, colorBlue);
                int sz = dpToPx(iconRes[i] == R.drawable.ic_status_cancelled ? 20 : 18);
                icon.setBounds(0, 0, sz, sz);
                allRbs[i].setCompoundDrawables(icon, null, null, null);
            }
            allRbs[i].setTextColor(colorText);
        }

        // Helper: refresh highlight on all options
        Runnable refreshHighlight = () -> {
            int checkedId = rgStatus.getCheckedRadioButtonId();
            for (RadioButton rb : allRbs) {
                if (rb.getId() == checkedId) {
                    rb.setBackground(AppCompatResources.getDrawable(this, R.drawable.bg_status_option_selected));
                    rb.setTextColor(colorBlue);
                    rb.setTypeface(null, android.graphics.Typeface.BOLD);
                } else {
                    rb.setBackgroundResource(android.R.color.transparent);
                    rb.setTextColor(colorText);
                    rb.setTypeface(null, android.graphics.Typeface.NORMAL);
                }
            }
        };

        // Pre-select current status
        String cur = safe(currentOrder.getStatus());
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equals(cur)) { allRbs[i].setChecked(true); break; }
        }
        refreshHighlight.run();

        rgStatus.setOnCheckedChangeListener((group, checkedId) -> refreshHighlight.run());

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            int checkedId = rgStatus.getCheckedRadioButtonId();
            String newStatus = null;
            if      (checkedId == R.id.rbPending)    newStatus = STATUS_PENDING;
            else if (checkedId == R.id.rbProcessing) newStatus = STATUS_PROCESSING;
            else if (checkedId == R.id.rbReady)      newStatus = STATUS_READY;
            else if (checkedId == R.id.rbShipping)   newStatus = STATUS_SHIPPING;
            else if (checkedId == R.id.rbCompleted)  newStatus = STATUS_COMPLETED;
            else if (checkedId == R.id.rbCancelled)  newStatus = STATUS_CANCELLED;

            if (newStatus == null) { dialog.dismiss(); return; }

            String finalStatus = newStatus;
            String key = safe(FirebaseOrderHelper.resolveFirebaseKey(currentOrder));
            if (key.isEmpty()) { dialog.dismiss(); return; }

            FirebaseDatabase.getInstance(DB_URL)
                    .getReference("orders")
                    .child(key)
                    .child("status")
                    .setValue(finalStatus)
                    .addOnSuccessListener(unused -> {
                        currentOrder.setStatus(finalStatus);
                        populateOrder(currentOrder);
                        
                        // Loyalty Points Logic
                        if (STATUS_COMPLETED.equals(finalStatus) && !currentOrder.isPointsAwarded()) {
                            awardPointsToUser(currentOrder);
                        }
                        
                        Toast.makeText(this, "Status updated", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        });

        dialog.show();
    }

    private void awardPointsToUser(FirebaseOrder order) {
        String userId = order.getUserId();
        if (userId == null || userId.isEmpty()) return;

        DatabaseReference userPointsRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference("Users").child(userId).child("points");

        userPointsRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Long points = currentData.getValue(Long.class);
                if (points == null) points = 0L;
                currentData.setValue(points + 1);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                if (committed) {
                    // Mark order as points awarded
                    FirebaseDatabase.getInstance(DB_URL)
                            .getReference("orders")
                            .child(order.getId())
                            .child("pointsAwarded")
                            .setValue(true);
                    order.setPointsAwarded(true);

                    // Add to PointsHistory
                    DatabaseReference historyRef = FirebaseDatabase.getInstance(DB_URL)
                            .getReference("PointsHistory").child(userId).push();
                    
                    Map<String, Object> historyEntry = new HashMap<>();
                    historyEntry.put("orderId", order.getOrderId() != null ? order.getOrderId() : order.getId());
                    historyEntry.put("pointsChange", 1);
                    historyEntry.put("createdAt", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
                    historyEntry.put("type", "earn");
                    historyEntry.put("description", "Tích điểm từ đơn hàng #" + (order.getOrderId() != null ? order.getOrderId() : order.getId()));
                    
                    historyRef.setValue(historyEntry);
                }
            }
        });
    }

    private void showDeleteDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        View dialogView = android.view.LayoutInflater.from(this)
                .inflate(R.layout.dialog_delete_confirm, null);
        dialog.setContentView(dialogView);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvTitle   = dialogView.findViewById(R.id.tvDeleteTitle);
        TextView tvMessage = dialogView.findViewById(R.id.tvDeleteMessage);
        tvTitle.setText(R.string.modal_delete_title);
        String orderId = currentOrder != null && currentOrder.getOrderId() != null
                ? currentOrder.getOrderId() : "";
        String fullMessage = "Order <font color='#0088ff'><b>#" + orderId + "</b></font> will be permanently deleted.";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tvMessage.setText(android.text.Html.fromHtml(fullMessage, android.text.Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvMessage.setText(android.text.Html.fromHtml(fullMessage));
        }

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnConfirmDelete).setOnClickListener(v -> {
            String key = safe(FirebaseOrderHelper.resolveFirebaseKey(currentOrder));
            if (key.isEmpty()) { dialog.dismiss(); return; }
            FirebaseDatabase.getInstance(DB_URL)
                    .getReference("orders")
                    .child(key)
                    .removeValue()
                    .addOnSuccessListener(unused -> {
                        dialog.dismiss();
                        Toast.makeText(this, "Order deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        dialog.dismiss();
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
    }

    // ── Populate ──────────────────────────────────────────────────────────────

    private void populateOrder(FirebaseOrder order) {
        populateHeader(order);
        applyStepperProgress(safe(order.getStatus()));
        populateItems(order.getItems());
        populateCustomerCard(order);
        populatePaymentCard(order);
    }

    private void populateHeader(FirebaseOrder order) {
        // Thumbnail
        List<FirebaseOrderItem> items = order.getItems();
        if (items != null && !items.isEmpty()) {
            loadProductImage(ivAdminOrderThumbnail, items.get(0));
        }

        // Order ID
        String orderId = safe(order.getOrderId());
        tvAdminOrderId.setText(orderId.isEmpty() ? safe(order.getId()) : "#" + orderId);

        // Date
        String rawDate = safe(order.getDate());
        if (rawDate.isEmpty()) rawDate = safe(order.getCreatedAt());
        tvAdminOrderDate.setText(formatDate(rawDate));

        // Payment chip
        PaymentMethodBadgeHelper.apply(tvAdminPaymentChip, order.getPaymentMethod());

        // Status badge
        OrderStatusBadgeHelper.apply(tvAdminStatusBadge, safe(order.getStatus()));

        // Item count + total
        int count = (items != null) ? items.size() : 0;
        tvAdminItemCount.setText(count + (count == 1 ? " item" : " items"));
        tvAdminTotal.setText(formatVnd(order.getTotal()));
    }

    private void populateItems(List<FirebaseOrderItem> items) {
        llAdminOrderItems.removeAllViews();
        if (items == null || items.isEmpty()) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        float density = getResources().getDisplayMetrics().density;
        int gapBetweenItems = (int) (8 * density);
        int gapBeforePayment = (int) (12 * density);

        for (int i = 0; i < items.size(); i++) {
            FirebaseOrderItem item = items.get(i);
            View row = inflater.inflate(R.layout.item_order_detail_line, llAdminOrderItems, false);

            ImageView ivImage = row.findViewById(R.id.imgOrderItem);
            TextView tvName = row.findViewById(R.id.tvOrderItemName);
            TextView tvCustom = row.findViewById(R.id.tvOrderItemConfig);
            TextView tvToppin = row.findViewById(R.id.tvOrderItemToppings);
            TextView tvTotal = row.findViewById(R.id.tvOrderItemLineTotal);
            TextView tvQty = row.findViewById(R.id.tvOrderItemQtyUnit);

            loadProductImage(ivImage, item);

            tvName.setText(safe(item.getProductName()));

            tvCustom.setText(OrderItemDisplayHelper.formatConfigLine(
                    this,
                    item.getSize(),
                    item.getSugar(),
                    item.getIce()
            ));

            String toppingsBlock = OrderItemDisplayHelper.formatToppingsBlock(
                    this,
                    safe(item.getToppings())
            );
            if (!toppingsBlock.isEmpty()) {
                tvToppin.setVisibility(View.VISIBLE);
                tvToppin.setText(toppingsBlock);
            } else {
                tvToppin.setVisibility(View.GONE);
            }

            tvTotal.setText(formatVnd(item.getLineTotal()));
            tvQty.setText("x" + item.getQuantity() + " • " + formatVnd(item.getUnitPrice()) + "/sp");

            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) row.getLayoutParams();
            if (lp == null) {
                lp = new ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            lp.bottomMargin = (i == items.size() - 1) ? gapBeforePayment : gapBetweenItems;
            row.setLayoutParams(lp);

            llAdminOrderItems.addView(row);
        }
    }

    private void populateCustomerCard(FirebaseOrder order) {
        tvDeliveryName.setText(safe(order.getCustomerName(), "—"));
        tvDeliveryPhone.setText(safe(order.getCustomerPhone(), "—"));
        tvDeliveryAddress.setText(safe(order.getCustomerAddress(), "—"));
        PaymentMethodBadgeHelper.apply(tvDeliveryPayment, order.getPaymentMethod());
        String branchAddr = safe(order.getBranchAddress());
        if (branchAddr.isEmpty()) branchAddr = safe(order.getAgencyId());
        tvDeliveryBranch.setText(branchAddr.isEmpty() ? "—" : branchAddr);
        OrderStatusBadgeHelper.apply(tvDeliveryStatus, safe(order.getStatus()));
    }

    private void populatePaymentCard(FirebaseOrder order) {
        tvPaySubtotal.setText(formatVnd(order.getSubtotal()));
        tvPayShipping.setText(formatVnd(order.getShipping()));

        int discount = order.getDiscount();
        if (discount > 0) {
            tvPayDiscount.setText("-" + formatVnd(discount));
        } else {
            tvPayDiscount.setText("0đ");
        }

        tvPayTotal.setText(formatVnd(order.getTotal()));
    }

    // ── Stepper ───────────────────────────────────────────────────────────────

    private void applyStepperProgress(String status) {
        int activeStep = getActiveStep(status);

        FrameLayout[] circles = {stepCircle1, stepCircle2, stepCircle3, stepCircle4, stepCircle5};
        ImageView[]   imgs    = {stepImg1, stepImg2, stepImg3, stepImg4, stepImg5};
        View[]        lines   = {stepLine1, stepLine2, stepLine3, stepLine4};
        TextView[]    labels  = {stepLabel1, stepLabel2, stepLabel3, stepLabel4, stepLabel5};

        int colorActive        = getColor(R.color.brand_blue);
        int colorInactive      = 0xFFBDBDBD;
        int colorLabelInactive = 0xFF999999;

        for (int i = 0; i < circles.length; i++) {
            boolean done    = (i + 1) <= activeStep;
            boolean current = (i + 1) == activeStep;

            circles[i].setBackgroundResource(
                    done ? R.drawable.bg_step_circle_active : R.drawable.bg_step_circle_inactive);

            imgs[i].setColorFilter(done ? getColor(R.color.white) : colorInactive);

            labels[i].setTextColor(current ? colorActive : colorLabelInactive);
            labels[i].setTypeface(null, current
                    ? android.graphics.Typeface.BOLD
                    : android.graphics.Typeface.NORMAL);
        }

        for (int i = 0; i < lines.length; i++) {
            lines[i].setScaleX((i + 2) <= activeStep ? 1f : 0f);
        }
    }

    private int getActiveStep(String status) {
        switch (status) {
            case STATUS_PENDING:    return 1;
            case STATUS_PROCESSING: return 2;
            case STATUS_READY:      return 3;
            case STATUS_SHIPPING:   return 4;
            case STATUS_COMPLETED:  return 5;
            default:                return 0;
        }
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
            if (cached != null) { applyImage(iv, cached); return; }
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
            Glide.with(this)
                    .load(imageValue)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .placeholder(R.mipmap.logo_ngo_gia)
                    .error(R.mipmap.logo_ngo_gia)
                    .centerCrop()
                    .into(iv);
        } else {
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
        return PriceFormatHelper.formatVnd(amount);
    }

    private static String safe(String s) {
        return s != null ? s : "";
    }

    private static String safe(String s, String fallback) {
        return (s != null && !s.isEmpty()) ? s : fallback;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
