package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.app.DatePickerDialog;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.adapter.CartItemAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.data.CartManager;
import com.teatrack_mcd_253eie502802_group02.data.OrderCheckoutFlow;
import com.teatrack_mcd_253eie502802_group02.model.User;
import com.teatrack_mcd_253eie502802_group02.model.CartItem;
import com.teatrack_mcd_253eie502802_group02.shared.ui.CartBadgeHelper;

import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Cart extends BaseActivity implements CartManager.CartChangeListener {
    private View cardRecipientEditor;
    private com.google.android.material.materialswitch.MaterialSwitch switchSaveDetails;
    private android.widget.EditText etRecipientName;
    private android.widget.EditText etRecipientPhone;
    private android.widget.EditText etRecipientAddress;
    private TextView tvPickupDate;
    private View tvCustomTimeToggle;
    private View layoutCustomTime;
    private android.widget.NumberPicker pickerHour;
    private android.widget.NumberPicker pickerMinute;
    private android.widget.CalendarView calendarViewCustomTime;
    private int customPickupHour = 8;
    private int customPickupMinute = 0;
    private android.widget.GridLayout gridPickupTimes;
    private View layoutRecipientEditorFooter;
    private View cardPaymentPickerContent;
    private String selectedPickupTime = "";
    private String selectedPickupDate = "";
    private String customPickupTime = "";
    private int navBarInsetBottom;
    private static final int PAYMENT_CASH_ON_HAND = 0;
    private static final int PAYMENT_CASH_IN_BANK = 1;
    private static final int PAYMENT_MOMO = 2;
    private static final int PAYMENT_ZALOPAY = 3;
    private static final int PAYMENT_EWALLET = 4;
    private static final int REQUEST_PAYMENT = 101;
    private final List<CartItem> cartItems = new ArrayList<>();
    private CartItemAdapter adapter;
    private TextView tvItemsSelected;
    private TextView tvCartHeaderBadge;
    private TextView tvEmptyCart;
    private TextView tvSubtotal;
    private TextView tvTotal;
    private TextView tvSelectedPayment;
    private TextView tvSelectedBranchAddress;
    private TextView tvRecipientDetails;
    private TextView tvOrderPickupTime;
    private EditText etNote;
    private ImageView ivSelectedPaymentIcon;
    private View cardItemRemoved;
    private View cartRoot;
    private View layoutCartFooter;
    private View paymentOverlayScrim;
    private View cardOrderSummary;
    private View cardPaymentPicker;
    private View cardVoucherPicker;
    private android.widget.LinearLayout layoutVoucherList;
    private android.widget.EditText etVoucherCode;
    private View layoutVoucherAdd;
    private View layoutVoucherApplied;
    private TextView tvVoucherValue;
    private TextView tvVoucherSummary;
    private com.teatrack_mcd_253eie502802_group02.model.Promotion appliedVoucher = null;
    private final java.util.List<com.teatrack_mcd_253eie502802_group02.model.Promotion> availableVouchers = new java.util.ArrayList<>();
    private final java.util.List<com.teatrack_mcd_253eie502802_group02.model.Agency> availableBranches = new java.util.ArrayList<>();
    private View cardBranchPicker;
    private android.widget.LinearLayout layoutBranchOptions;
    private int selectedBranchIndex = -1;
    private String selectedBranchId = "";
    private View layoutBankOptions;
    private View rowCashInBankHeader;
    private View btnConfirmOrder;
    private TextView tvTerms;
    private TextView tvCashOnHandLabel;
    private TextView tvCashInBankLabel;
    private View optionMoMo;
    private View optionZaloPay;
    private View optionEWallet;
    private View dragHandle;
    private int footerPaddingStart;
    private int footerPaddingTop;
    private int footerPaddingEnd;
    private int footerPaddingBottom;
    private int selectedPaymentMethod = PAYMENT_CASH_ON_HAND;
    private final Handler bannerHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        cartRoot = findViewById(R.id.cartRoot);
        layoutCartFooter = findViewById(R.id.layoutCartFooter);
        paymentOverlayScrim = findViewById(R.id.paymentOverlayScrim);
        cardOrderSummary = findViewById(R.id.cardOrderSummary);
        cardPaymentPicker = findViewById(R.id.cardPaymentPicker);
        cardVoucherPicker = findViewById(R.id.cardVoucherPicker);
        layoutVoucherList = findViewById(R.id.layoutVoucherList);
        etVoucherCode = findViewById(R.id.etVoucherCode);
        layoutVoucherAdd = findViewById(R.id.layoutVoucherAdd);
        layoutVoucherApplied = findViewById(R.id.layoutVoucherApplied);
        tvVoucherValue = findViewById(R.id.tvVoucherValue);
        tvVoucherSummary = findViewById(R.id.tvVoucherSummary);
        cardBranchPicker = findViewById(R.id.cardBranchPicker);
        layoutBranchOptions = findViewById(R.id.layoutBranchOptions);
        setupWindowInsets();
        tvOrderPickupTime = findViewById(R.id.tvOrderPickupTime);
        tvItemsSelected = findViewById(R.id.tvItemsSelected);
        tvCartHeaderBadge = findViewById(R.id.tvCartHeaderBadge);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvTotal = findViewById(R.id.tvTotal);
        tvSelectedPayment = findViewById(R.id.tvSelectedPayment);
        tvSelectedBranchAddress = findViewById(R.id.tvSelectedBranchAddress);
        tvOrderPickupTime = findViewById(R.id.tvOrderPickupTime);
        tvRecipientDetails = findViewById(R.id.tvRecipientDetails);
        etNote = findViewById(R.id.etNote);
        ivSelectedPaymentIcon = findViewById(R.id.ivSelectedPaymentIcon);
        cardItemRemoved = findViewById(R.id.cardItemRemoved);
        RecyclerView rvCartItems = findViewById(R.id.rvCartItems);
        TextView tvTermsView = findViewById(R.id.tvTerms);
        tvTerms = tvTermsView;
        TextView tvTotalLabel = findViewById(R.id.tvTotalLabel);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);

        applyExtraBoldTypeface(tvTotalLabel, tvTotal);

        View btnBack = findViewById(R.id.btnCartBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        dragHandle = findViewById(R.id.dragHandle);
        if (layoutCartFooter != null) {
            footerPaddingStart = layoutCartFooter.getPaddingStart();
            footerPaddingTop = layoutCartFooter.getPaddingTop();
            footerPaddingEnd = layoutCartFooter.getPaddingEnd();
            footerPaddingBottom = layoutCartFooter.getPaddingBottom();
        }

        CartBadgeHelper.setup(this);

        setupTermsLink(tvTermsView);
        setupPaymentPicker();
        setupBranchPicker();
        setupVoucherPicker();
        setupRecipientEditor();

        if (btnConfirmOrder != null) {
            btnConfirmOrder.setOnClickListener(v -> {
                String details = tvRecipientDetails != null ? tvRecipientDetails.getText().toString().trim() : "";
                if (details.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin người nhận!", Toast.LENGTH_SHORT).show();
                    showRecipientEditor();
                    return;
                }
                handleConfirmOrder();
            });
        }

        adapter = new CartItemAdapter(cartItems, new CartItemAdapter.CartItemActionListener() {
            @Override
            public void onQuantityChanged(int position, int newQuantity) {
                CartManager.getInstance().updateQuantity(position, newQuantity);
            }

            @Override
            public void onRemove(int position) {
                CartManager.getInstance().removeItem(position);
                showRemovedBanner();
            }
        });

        if (rvCartItems != null) {
            rvCartItems.setLayoutManager(new LinearLayoutManager(this));
            rvCartItems.setAdapter(adapter);
        }

        CartManager.getInstance().addListener(this);
        refreshCartUi();
        View cardOrderSummary = findViewById(R.id.cardOrderSummary);
        View dragHandle = findViewById(R.id.dragHandle);

        if (dragHandle != null && cardOrderSummary != null) {
            dragHandle.setOnClickListener(v -> toggleSummary(cardOrderSummary));
            cardOrderSummary.setOnClickListener(v -> toggleSummary(cardOrderSummary));
        }
        switchSaveDetails = findViewById(R.id.switchSaveDetails);
        if (switchSaveDetails != null) {
            switchSaveDetails.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    loadSavedRecipientInfo();
                    loadUserRecipientFallback();
                    saveCurrentRecipientToPrefs();
                } else {
                    Toast.makeText(this, "Đã tắt tự động lưu thông tin", Toast.LENGTH_SHORT).show();
                }
            });
        }

        loadSavedRecipientInfo();
        loadUserRecipientFallback();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CartBadgeHelper.updateBadge(this);
    }

    private void toggleSummary(View cardOrderSummary) {
        if (cardOrderSummary.getVisibility() == View.VISIBLE) {
            cardOrderSummary.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> cardOrderSummary.setVisibility(View.GONE))
                    .start();
        } else {
            cardOrderSummary.setVisibility(View.VISIBLE);
            cardOrderSummary.setAlpha(0f);
            cardOrderSummary.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start();
        }
    }

    @Override
    protected void onDestroy() {
        CartManager.getInstance().removeListener(this);
        bannerHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onCartChanged() {
        refreshCartUi();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PAYMENT && resultCode == RESULT_OK && data != null) {
            int method = data.getIntExtra("selected", PAYMENT_CASH_ON_HAND);
            selectPaymentMethod(method);
        }
    }
    private void setupWindowInsets() {
        if (cartRoot == null) {
            return;
        }
        ViewCompat.setOnApplyWindowInsetsListener(cartRoot, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            navBarInsetBottom = systemBars.bottom;
            view.setPadding(0, systemBars.top, 0, 0);
            restoreFooterPadding();
            View scrollView = findViewById(R.id.nestedScrollView);
            if (scrollView != null) {
                scrollView.setPadding(0, 0, 0, dp(8));
            }
            return insets;
        });
        ViewCompat.requestApplyInsets(cartRoot);
        WindowInsetsCompat currentInsets = ViewCompat.getRootWindowInsets(cartRoot);
        if (currentInsets != null) {
            navBarInsetBottom = currentInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            restoreFooterPadding();
        }
    }

    private void restoreFooterPadding() {
        if (layoutCartFooter == null) {
            return;
        }
        layoutCartFooter.setBackgroundResource(R.drawable.bg_cart_footer);
        layoutCartFooter.setPadding(
                footerPaddingStart,
                footerPaddingTop,
                footerPaddingEnd,
                footerPaddingBottom + navBarInsetBottom
        );
    }

    private void applyOverlayFooterPadding() {
        if (layoutCartFooter == null) {
            return;
        }
        layoutCartFooter.setBackgroundResource(R.drawable.bg_cart_footer);
        layoutCartFooter.setPadding(0, 0, 0, navBarInsetBottom);
    }

    private void applyExtraBoldTypeface(TextView... textViews) {
        Typeface typeface = ResourcesCompat.getFont(this, R.font.inter_semibold);
        if (typeface == null) {
            return;
        }
        for (TextView textView : textViews) {
            if (textView != null) {
                textView.setTypeface(typeface, Typeface.BOLD);
            }
        }
    }

    private void setupTermsLink(TextView tvTerms) {
        if (tvTerms == null) {
            return;
        }
        String prefix = getString(R.string.cart_terms_prefix);
        String link = getString(R.string.cart_terms_link);
        SpannableString spannable = new SpannableString(prefix + " " + link);

        int linkColor = ContextCompat.getColor(this, R.color.brand_blue);
        spannable.setSpan(new ForegroundColorSpan(linkColor), prefix.length(), spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), prefix.length(), spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                openProfileTab();
            }
            @Override
            public void updateDrawState(android.text.TextPaint ds) {
                ds.setColor(linkColor);
                ds.setUnderlineText(false);
            }
        }, prefix.length(), spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvTerms.setText(spannable);
        tvTerms.setMovementMethod(LinkMovementMethod.getInstance());
        tvTerms.setHighlightColor(android.graphics.Color.TRANSPARENT);
    }

    private void setupPaymentPicker() {
        View btnPaymentChange = findViewById(R.id.btnPaymentChange);
        View optionCashOnHand = findViewById(R.id.optionCashOnHand);
        rowCashInBankHeader = findViewById(R.id.rowCashInBankHeader);
        layoutBankOptions = findViewById(R.id.layoutBankOptions);
        tvCashOnHandLabel = findViewById(R.id.tvCashOnHandLabel);
        tvCashInBankLabel = findViewById(R.id.tvCashInBankLabel);
        optionMoMo = findViewById(R.id.optionMoMo);
        optionZaloPay = findViewById(R.id.optionZaloPay);
        optionEWallet = findViewById(R.id.optionEWallet);

        if (btnPaymentChange != null) {
            btnPaymentChange.setOnClickListener(v -> showPaymentPicker());
        }
        if (paymentOverlayScrim != null) {
            paymentOverlayScrim.setOnClickListener(v -> {
                if (cardPaymentPicker != null && cardPaymentPicker.getVisibility() == View.VISIBLE) {
                    hidePaymentPicker();
                } else if (cardVoucherPicker != null && cardVoucherPicker.getVisibility() == View.VISIBLE) {
                    hideVoucherPicker();
                } else if (cardBranchPicker != null && cardBranchPicker.getVisibility() == View.VISIBLE) {
                    hideBranchPicker();
                } else if (cardRecipientEditor != null && cardRecipientEditor.getVisibility() == View.VISIBLE) {
                    hideRecipientEditor();
                }
            });
        }
        if (cardPaymentPicker != null) {
            cardPaymentPicker.setClickable(true);
        }
        if (optionCashOnHand != null) {
            optionCashOnHand.setOnClickListener(v -> selectPaymentMethod(PAYMENT_CASH_ON_HAND));
        }
        if (rowCashInBankHeader != null) {
            rowCashInBankHeader.setOnClickListener(v -> toggleBankOptions());
        }
        if (optionMoMo != null) optionMoMo.setOnClickListener(v -> selectPaymentMethod(PAYMENT_MOMO));
        if (optionZaloPay != null) optionZaloPay.setOnClickListener(v -> selectPaymentMethod(PAYMENT_ZALOPAY));
        if (optionEWallet != null) optionEWallet.setOnClickListener(v -> selectPaymentMethod(PAYMENT_EWALLET));

        updatePaymentUi();
    }

    private void toggleBankOptions() {
        if (layoutBankOptions == null) return;
        boolean show = layoutBankOptions.getVisibility() != View.VISIBLE;
        layoutBankOptions.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            highlightBankHeader(true);
            highlightCashOnHand(false);
        }
    }

    private void highlightCashOnHand(boolean selected) {
        if (tvCashOnHandLabel == null) {
            return;
        }
        tvCashOnHandLabel.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        tvCashOnHandLabel.setTextColor(ContextCompat.getColor(this, R.color.on_surface));
    }

    private void highlightBankHeader(boolean selected) {
        if (tvCashInBankLabel == null) {
            return;
        }
        tvCashInBankLabel.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        tvCashInBankLabel.setTextColor(ContextCompat.getColor(this, R.color.on_surface));
    }

    private void highlightBankOption(View optionRow, boolean selected) {
        if (!(optionRow instanceof ViewGroup)) {
            return;
        }
        ViewGroup group = (ViewGroup) optionRow;
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof TextView) {
                highlightPaymentLabel((TextView) child, selected);
                break;
            }
        }
    }

    private void highlightPaymentLabel(TextView label, boolean selected) {
        if (label == null) {
            return;
        }
        label.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        label.setTextColor(ContextCompat.getColor(this, R.color.on_surface));
    }

    private void selectPaymentMethod(int method) {
        selectedPaymentMethod = method;
        updatePaymentUi();
        hidePaymentPicker();
    }

    private void updatePaymentUi() {
        String methodName = "";
        int iconRes = R.drawable.ic_cash_coins;

        boolean isCashOnHand = selectedPaymentMethod == PAYMENT_CASH_ON_HAND;
        boolean isBankMethod = selectedPaymentMethod >= PAYMENT_CASH_IN_BANK;

        highlightCashOnHand(isCashOnHand);
        highlightBankHeader(isBankMethod);
        highlightBankOption(optionZaloPay, selectedPaymentMethod == PAYMENT_ZALOPAY);
        highlightBankOption(optionMoMo, selectedPaymentMethod == PAYMENT_MOMO);
        highlightBankOption(optionEWallet, selectedPaymentMethod == PAYMENT_EWALLET);

        if (layoutBankOptions != null && !isBankMethod) {
            layoutBankOptions.setVisibility(View.GONE);
        }

        switch (selectedPaymentMethod) {
            case PAYMENT_CASH_ON_HAND:
                methodName = getString(R.string.cart_cash_on_hand);
                iconRes = R.drawable.ic_cash_coins;
                break;
            case PAYMENT_CASH_IN_BANK:
                methodName = getString(R.string.cart_cash_in_bank);
                iconRes = R.drawable.ic_cash_dollar;
                break;
            case PAYMENT_MOMO:
                methodName = getString(R.string.payment_momo);
                iconRes = R.drawable.momo;
                break;
            case PAYMENT_ZALOPAY:
                methodName = getString(R.string.payment_zalopay);
                iconRes = R.drawable.zalopay;
                break;
            case PAYMENT_EWALLET:
                methodName = getString(R.string.payment_ewallet);
                iconRes = R.drawable.ewallet;
                break;
        }

        if (tvSelectedPayment != null) tvSelectedPayment.setText(methodName);
        if (ivSelectedPaymentIcon != null) ivSelectedPaymentIcon.setImageResource(iconRes);
    }

    private void showPaymentPicker() {
        if (cardOrderSummary != null) cardOrderSummary.setVisibility(View.GONE);
        if (cardPaymentPicker != null) cardPaymentPicker.setVisibility(View.VISIBLE);
        if (paymentOverlayScrim != null) paymentOverlayScrim.setVisibility(View.VISIBLE);
        if (btnConfirmOrder != null) btnConfirmOrder.setVisibility(View.GONE);
        if (tvTerms != null) tvTerms.setVisibility(View.GONE);
        if (dragHandle != null) dragHandle.setVisibility(View.GONE);

        applyOverlayFooterPadding();

        if (selectedPaymentMethod >= PAYMENT_CASH_IN_BANK && layoutBankOptions != null) {
            layoutBankOptions.setVisibility(View.VISIBLE);
        }
        updatePaymentUi();
    }
    private void setupBranchPicker() {
        View btnChangeBranch = findViewById(R.id.btnChangeBranch);
        if (btnChangeBranch != null) {
            btnChangeBranch.setOnClickListener(v -> showBranchPicker());
        }
        loadBranchesFromFirebase();
    }

    private void loadBranchesFromFirebase() {
        FirebaseDatabase.getInstance().getReference("agencies")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        availableBranches.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            com.teatrack_mcd_253eie502802_group02.model.Agency agency =
                                    child.getValue(com.teatrack_mcd_253eie502802_group02.model.Agency.class);
                            if (agency != null) {
                                agency.setId(child.getKey());
                                if (agency.isVisible()) {
                                    availableBranches.add(agency);
                                }
                            }
                        }
                        buildBranchRows();
                        updateBranchUi();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void buildBranchRows() {
        if (layoutBranchOptions == null) return;
        layoutBranchOptions.removeAllViews();

        android.util.TypedValue rippleValue = new android.util.TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, rippleValue, true);
        int rippleResId = rippleValue.resourceId;

        for (int i = 0; i < availableBranches.size(); i++) {
            final int idx = i;
            com.teatrack_mcd_253eie502802_group02.model.Agency agency = availableBranches.get(i);
            String code = agency.getName();
            String addr = agency.getAddress();

            android.widget.LinearLayout row = new android.widget.LinearLayout(this);
            row.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(dp(20), dp(16), dp(20), dp(16));
            if (rippleResId != 0) row.setBackgroundResource(rippleResId);
            row.setClickable(true);
            row.setFocusable(true);
            row.setTag(idx);

            android.widget.ImageView icon = new android.widget.ImageView(this);
            android.widget.LinearLayout.LayoutParams iconParams =
                    new android.widget.LinearLayout.LayoutParams(dp(32), dp(32));
            icon.setLayoutParams(iconParams);
            icon.setImageResource(R.drawable.ic_store);
            icon.setColorFilter(ContextCompat.getColor(this, R.color.brand_blue));

            android.widget.LinearLayout textCol = new android.widget.LinearLayout(this);
            textCol.setOrientation(android.widget.LinearLayout.VERTICAL);
            android.widget.LinearLayout.LayoutParams textParams =
                    new android.widget.LinearLayout.LayoutParams(0,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            textParams.setMarginStart(dp(14));
            textCol.setLayoutParams(textParams);

            TextView tvCode = new TextView(this);
            tvCode.setText(code);
            tvCode.setTextSize(14f);
            tvCode.setTypeface(tvCode.getTypeface(), Typeface.BOLD);

            TextView tvAddr = new TextView(this);
            tvAddr.setText(addr);
            tvAddr.setTextSize(12f);
            tvAddr.setTextColor(ContextCompat.getColor(this, R.color.secondary));

            textCol.addView(tvCode);
            textCol.addView(tvAddr);
            row.addView(icon);
            row.addView(textCol);

            row.setOnClickListener(v -> selectBranch(idx));
            layoutBranchOptions.addView(row);

            if (i < availableBranches.size() - 1) {
                View divider = new View(this);
                android.widget.LinearLayout.LayoutParams dp1 =
                        new android.widget.LinearLayout.LayoutParams(
                                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
                divider.setLayoutParams(dp1);
                divider.setBackgroundColor(ContextCompat.getColor(this, R.color.outline_variant));
                layoutBranchOptions.addView(divider);
            }
        }
        
        // Initial selection if empty
        if (selectedBranchIndex == -1 && !availableBranches.isEmpty()) {
            selectedBranchIndex = 0;
            selectedBranchId = availableBranches.get(0).getId();
        }
    }

    private void selectBranch(int idx) {
        selectedBranchIndex = idx;
        if (idx >= 0 && idx < availableBranches.size()) {
            selectedBranchId = availableBranches.get(idx).getId();
        }
        updateBranchUi();
        hideBranchPicker();
    }

    private void updateBranchUi() {
        if (tvSelectedBranchAddress != null && selectedBranchIndex >= 0 && selectedBranchIndex < availableBranches.size()) {
            com.teatrack_mcd_253eie502802_group02.model.Agency agency = availableBranches.get(selectedBranchIndex);
            tvSelectedBranchAddress.setText(agency.getAddress());
        }
        if (layoutBranchOptions == null) return;
        for (int i = 0; i < layoutBranchOptions.getChildCount(); i++) {
            View child = layoutBranchOptions.getChildAt(i);
            if (!(child.getTag() instanceof Integer)) continue;
            int idx = (int) child.getTag();
            boolean sel = idx == selectedBranchIndex;
            if (sel) {
                child.setBackgroundResource(R.drawable.bg_light_blue_rounded_12);
            } else {
                android.util.TypedValue rv = new android.util.TypedValue();
                getTheme().resolveAttribute(android.R.attr.selectableItemBackground, rv, true);
                if (rv.resourceId != 0) child.setBackgroundResource(rv.resourceId);
                else child.setBackground(null);
            }
            if (child instanceof android.widget.LinearLayout) {
                android.widget.LinearLayout row = (android.widget.LinearLayout) child;
                for (int j = 0; j < row.getChildCount(); j++) {
                    View inner = row.getChildAt(j);
                    if (inner instanceof android.widget.LinearLayout) {
                        android.widget.LinearLayout tc = (android.widget.LinearLayout) inner;
                        for (int k = 0; k < tc.getChildCount(); k++) {
                            if (tc.getChildAt(k) instanceof TextView) {
                                TextView tv = (TextView) tc.getChildAt(k);
                                tv.setTextColor(ContextCompat.getColor(this,
                                        sel ? R.color.brand_blue : (k == 0 ? R.color.on_surface : R.color.secondary)));
                            }
                        }
                    }
                }
            }
        }
    }

    private void showBranchPicker() {
        if (cardOrderSummary != null) cardOrderSummary.setVisibility(View.GONE);
        if (cardBranchPicker != null) cardBranchPicker.setVisibility(View.VISIBLE);
        if (paymentOverlayScrim != null) paymentOverlayScrim.setVisibility(View.VISIBLE);
        if (btnConfirmOrder != null) btnConfirmOrder.setVisibility(View.GONE);
        if (tvTerms != null) tvTerms.setVisibility(View.GONE);
        if (dragHandle != null) dragHandle.setVisibility(View.GONE);
        updateBranchUi();
        applyOverlayFooterPadding();
    }

    private void hideBranchPicker() {
        if (cardBranchPicker != null) cardBranchPicker.setVisibility(View.GONE);
        if (paymentOverlayScrim != null) paymentOverlayScrim.setVisibility(View.GONE);
        if (cardOrderSummary != null) {
            cardOrderSummary.setAlpha(1f);
            cardOrderSummary.setVisibility(View.VISIBLE);
        }
        if (btnConfirmOrder != null) btnConfirmOrder.setVisibility(View.VISIBLE);
        if (tvTerms != null) tvTerms.setVisibility(View.VISIBLE);
        if (dragHandle != null) dragHandle.setVisibility(View.VISIBLE);
        restoreFooterPadding();
    }

    private void hidePaymentPicker() {
        if (cardPaymentPicker != null) cardPaymentPicker.setVisibility(View.GONE);
        if (paymentOverlayScrim != null) paymentOverlayScrim.setVisibility(View.GONE);
        if (cardOrderSummary != null) {
            cardOrderSummary.setAlpha(1f);
            cardOrderSummary.setVisibility(View.VISIBLE);
        }
        if (btnConfirmOrder != null) btnConfirmOrder.setVisibility(View.VISIBLE);
        if (tvTerms != null) tvTerms.setVisibility(View.VISIBLE);
        if (dragHandle != null) dragHandle.setVisibility(View.VISIBLE);
        if (layoutBankOptions != null) layoutBankOptions.setVisibility(View.GONE);

        restoreFooterPadding();
    }
    private void handleConfirmOrder() {
        if (CartManager.getInstance().getItems().isEmpty()) {
            return;
        }
        String branchAddress = tvSelectedBranchAddress != null ? tvSelectedBranchAddress.getText().toString() : "";
        String recipientDetails = tvRecipientDetails != null ? tvRecipientDetails.getText().toString() : "";
        String note = etNote != null ? etNote.getText().toString() : "";

        if (selectedPaymentMethod == PAYMENT_CASH_ON_HAND) {
            if (btnConfirmOrder != null) {
                btnConfirmOrder.setEnabled(false);
            }
            Toast.makeText(this, R.string.order_saving, Toast.LENGTH_SHORT).show();
            OrderCheckoutFlow.placeOrderAndOpenCheckout(
                    this,
                    selectedPaymentMethod,
                    branchAddress,
                    recipientDetails,
                    note,
                    false,
                    btnConfirmOrder
            );
        } else {
            Intent intent = new Intent(this, Payment.class);
            intent.putExtra("method", selectedPaymentMethod);
            intent.putExtra("pickupAddress", branchAddress);
            intent.putExtra("recipientDetails", recipientDetails);
            intent.putExtra("note", note);
            startActivity(intent);
        }
    }

    private void openProfileTab() {
        Intent intent = new Intent(this, PolicyandTermActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void refreshCartUi() {
        cartItems.clear();
        cartItems.addAll(CartManager.getInstance().getItems());
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        int totalQty = CartManager.getInstance().getTotalQuantity();
        boolean hasItems = totalQty > 0;

        if (tvItemsSelected != null) {
            tvItemsSelected.setText(getString(R.string.cart_items_selected_format, totalQty));
        }
        updateHeaderBadge(totalQty, hasItems);
        if (tvEmptyCart != null) {
            tvEmptyCart.setVisibility(hasItems ? View.GONE : View.VISIBLE);
        }

        int subtotal = CartManager.getInstance().getSubtotal();
        int discount = 0;
        if (appliedVoucher != null) {
            discount = (int) appliedVoucher.getValue();
        }
        if (tvSubtotal != null) {
            tvSubtotal.setText(formatPrice(subtotal));
        }
        if (tvTotal != null) {
            tvTotal.setText(formatPrice(subtotal - discount));
        }
    }
    // ─── Voucher Picker ───────────────────────────────────────────────────────

    private void setupVoucherPicker() {
        if (layoutVoucherAdd != null) {
            layoutVoucherAdd.setOnClickListener(v -> showVoucherPicker());
        }
        if (layoutVoucherApplied != null) {
            layoutVoucherApplied.setOnClickListener(v -> showVoucherPicker());
        }

        View btnConfirm = (cardVoucherPicker != null) ? cardVoucherPicker.findViewById(R.id.btnConfirmVoucherCode) : null;
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                if (etVoucherCode == null) return;
                String code = etVoucherCode.getText().toString().trim();
                if (code.isEmpty()) {
                    Toast.makeText(this, R.string.cart_voucher_enter_code, Toast.LENGTH_SHORT).show();
                    return;
                }
                validateAndApplyCode(code);
            });
        }
        if (etVoucherCode != null) {
            etVoucherCode.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    buildVoucherRows(s == null ? "" : s.toString().trim());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
        loadVouchersFromFirebase();
    }

    private void loadVouchersFromFirebase() {
        android.content.SharedPreferences loginPrefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String userId = loginPrefs.getString("userId", null);
        com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("vouchers")
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        availableVouchers.clear();
                        for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                            com.teatrack_mcd_253eie502802_group02.model.Promotion p =
                                    child.getValue(com.teatrack_mcd_253eie502802_group02.model.Promotion.class);
                            if (p != null) {
                                p.setId(child.getKey());
                                if (p.getCode() == null || p.getCode().trim().isEmpty()) {
                                    p.setCode(child.getKey());
                                }
                                availableVouchers.add(p);
                            }
                        }
                        buildVoucherRows();
                    }
                    @Override
                    public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                        buildVoucherRows();
                    }
                });
    }

    private void buildVoucherRows() {
        String filter = etVoucherCode != null ? etVoucherCode.getText().toString().trim() : "";
        buildVoucherRows(filter);
    }

    private void buildVoucherRows(String filterQuery) {
        if (layoutVoucherList == null) return;
        layoutVoucherList.removeAllViews();

        List<com.teatrack_mcd_253eie502802_group02.model.Promotion> filtered = new ArrayList<>();
        for (com.teatrack_mcd_253eie502802_group02.model.Promotion voucher : availableVouchers) {
            if (matchesVoucherFilter(voucher, filterQuery, false)) {
                filtered.add(voucher);
            }
        }

        if (filtered.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(filterQuery.isEmpty()
                    ? getString(R.string.cart_voucher_empty)
                    : getString(R.string.cart_voucher_not_found, filterQuery));
            empty.setTextColor(ContextCompat.getColor(this, R.color.secondary));
            empty.setGravity(android.view.Gravity.CENTER);
            empty.setPadding(dp(16), dp(24), dp(16), dp(24));
            layoutVoucherList.addView(empty);
            return;
        }
        int subtotal = com.teatrack_mcd_253eie502802_group02.data.CartManager.getInstance().getSubtotal();
        android.util.TypedValue ripple = new android.util.TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, ripple, true);

        for (com.teatrack_mcd_253eie502802_group02.model.Promotion v : filtered) {
            boolean eligible = subtotal >= v.getMinSubtotal();

            android.widget.LinearLayout row = new android.widget.LinearLayout(this);
            row.setOrientation(android.widget.LinearLayout.VERTICAL);

            // Main row: icon + content + action
            android.widget.LinearLayout mainRow = new android.widget.LinearLayout(this);
            mainRow.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            mainRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
            if (ripple.resourceId != 0) mainRow.setBackgroundResource(ripple.resourceId);
            mainRow.setClickable(true);
            mainRow.setFocusable(true);
            mainRow.setPadding(dp(16), dp(12), dp(16), dp(12));

            // Icon circle
            FrameLayout iconCircle = new FrameLayout(this);
            android.widget.LinearLayout.LayoutParams iconCircleParams =
                    new android.widget.LinearLayout.LayoutParams(dp(44), dp(44));
            iconCircle.setLayoutParams(iconCircleParams);
            iconCircle.setBackgroundResource(R.drawable.bg_light_blue_rounded_12);
            android.widget.ImageView ivIcon = new android.widget.ImageView(this);
            FrameLayout.LayoutParams ivParams = new FrameLayout.LayoutParams(dp(24), dp(24));
            ivParams.gravity = android.view.Gravity.CENTER;
            ivIcon.setLayoutParams(ivParams);
            ivIcon.setImageResource(R.drawable.ic_local_offer);
            ivIcon.setColorFilter(ContextCompat.getColor(this, R.color.brand_blue));
            iconCircle.addView(ivIcon);

            // Content column
            android.widget.LinearLayout content = new android.widget.LinearLayout(this);
            content.setOrientation(android.widget.LinearLayout.VERTICAL);
            android.widget.LinearLayout.LayoutParams contentParams =
                    new android.widget.LinearLayout.LayoutParams(0,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            contentParams.setMarginStart(dp(12));
            content.setLayoutParams(contentParams);

            // PRIMARY BOLD TEXT: voucherID (v.getId())
            TextView tvId = new TextView(this);
            tvId.setText(v.getId());
            tvId.setTextSize(16f);
            tvId.setTypeface(null, Typeface.BOLD);
            tvId.setTextColor(ContextCompat.getColor(this, R.color.on_surface));
            content.addView(tvId);

            // Secondary text: Title or Description
            TextView tvTitle = new TextView(this);
            tvTitle.setText(v.getTitle().isEmpty() ? v.getDescription() : v.getTitle());
            tvTitle.setTextSize(13f);
            tvTitle.setTextColor(ContextCompat.getColor(this, R.color.secondary));
            content.addView(tvTitle);

            if (v.getExpiry() != null && !v.getExpiry().isEmpty()) {
                TextView tvExp = new TextView(this);
                tvExp.setText("HSD: " + v.getExpiry());
                tvExp.setTextSize(12f);
                tvExp.setTextColor(ContextCompat.getColor(this, R.color.secondary));
                content.addView(tvExp);
            }

            // Action button
            TextView btnAction = new TextView(this);
            btnAction.setText(eligible ? getString(R.string.cart_voucher_use_now) : getString(R.string.cart_voucher_conditions));
            btnAction.setTextSize(14f);
            btnAction.setTypeface(null, Typeface.BOLD);
            btnAction.setTextColor(ContextCompat.getColor(this, R.color.brand_blue));
            android.widget.LinearLayout.LayoutParams btnParams =
                    new android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
            btnParams.setMarginStart(dp(8));
            btnAction.setLayoutParams(btnParams);

            mainRow.addView(iconCircle);
            mainRow.addView(content);
            mainRow.addView(btnAction);

            row.addView(mainRow);

            if (!eligible) {
                // Show condition info bar
                android.widget.LinearLayout infoBar = new android.widget.LinearLayout(this);
                infoBar.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                infoBar.setGravity(android.view.Gravity.CENTER_VERTICAL);
                infoBar.setBackgroundColor(0xFFFFF3CD);
                infoBar.setPadding(dp(16), dp(8), dp(16), dp(8));
                android.widget.LinearLayout.LayoutParams infoParams =
                        new android.widget.LinearLayout.LayoutParams(
                                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                infoBar.setLayoutParams(infoParams);
                
                android.widget.ImageView ivInfo = new android.widget.ImageView(this);
                android.widget.LinearLayout.LayoutParams infoIconParams =
                        new android.widget.LinearLayout.LayoutParams(dp(14), dp(14));
                ivInfo.setLayoutParams(infoIconParams);
                ivIcon.setImageResource(R.drawable.ic_local_offer);
                ivInfo.setColorFilter(0xFFB8860B);
                
                TextView tvInfo = new TextView(this);
                android.widget.LinearLayout.LayoutParams tvInfoParams =
                        new android.widget.LinearLayout.LayoutParams(
                                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                tvInfoParams.setMarginStart(dp(6));
                tvInfo.setLayoutParams(tvInfoParams);
                String minStr = formatPrice((int) v.getMinSubtotal());
                tvInfo.setText(getString(R.string.cart_voucher_min_order, minStr));
                tvInfo.setTextSize(12f);
                tvInfo.setTextColor(0xFF7A5800);
                tvInfo.setMaxLines(2);
                
                infoBar.addView(ivInfo);
                infoBar.addView(tvInfo);
                row.addView(infoBar);
            }

            if (eligible) {
                mainRow.setOnClickListener(view -> {
                    applyVoucher(v);
                    hideVoucherPicker();
                });
            } else {
                mainRow.setOnClickListener(view -> {
                    android.widget.Toast.makeText(this,
                            "Cần đơn tối thiểu " + formatPrice((int)v.getMinSubtotal()),
                            android.widget.Toast.LENGTH_SHORT).show();
                });
            }

            layoutVoucherList.addView(row);

            // Divider
            View divider = new View(this);
            divider.setBackgroundColor(ContextCompat.getColor(this, R.color.outline_variant));
            android.widget.LinearLayout.LayoutParams divParams =
                    new android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1);
            // We can use dp(1) for height
            divParams.height = dp(1);
            layoutVoucherList.addView(divider, divParams);
        }
    }


    private void applyVoucher(com.teatrack_mcd_253eie502802_group02.model.Promotion voucher) {
        appliedVoucher = voucher;
        int discount = (int) voucher.getValue();
        if (tvVoucherValue != null) tvVoucherValue.setText("-" + formatPrice(discount));
        if (tvVoucherSummary != null) tvVoucherSummary.setText("-" + formatPrice(discount));
        if (layoutVoucherAdd != null) layoutVoucherAdd.setVisibility(View.GONE);
        if (layoutVoucherApplied != null) layoutVoucherApplied.setVisibility(View.VISIBLE);
        refreshCartUi();
    }

    private void clearVoucher() {
        appliedVoucher = null;
        if (tvVoucherValue != null) tvVoucherValue.setText("");
        if (tvVoucherSummary != null) tvVoucherSummary.setText("0đ");
        if (layoutVoucherAdd != null) layoutVoucherAdd.setVisibility(View.VISIBLE);
        if (layoutVoucherApplied != null) layoutVoucherApplied.setVisibility(View.GONE);
    }

    private void validateAndApplyCode(String code) {
        com.teatrack_mcd_253eie502802_group02.model.Promotion matched = findVoucherByCode(code);
        if (matched == null) {
            Toast.makeText(this, R.string.cart_voucher_invalid, Toast.LENGTH_SHORT).show();
            return;
        }
        int subtotal = com.teatrack_mcd_253eie502802_group02.data.CartManager.getInstance().getSubtotal();
        if (subtotal < matched.getMinSubtotal()) {
            Toast.makeText(this,
                    getString(R.string.cart_voucher_min_order, formatPrice((int) matched.getMinSubtotal())),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        applyVoucher(matched);
        hideVoucherPicker();
    }

    private com.teatrack_mcd_253eie502802_group02.model.Promotion findVoucherByCode(String code) {
        for (com.teatrack_mcd_253eie502802_group02.model.Promotion voucher : availableVouchers) {
            if (matchesVoucherFilter(voucher, code, true)) {
                return voucher;
            }
        }
        return null;
    }

    private boolean matchesVoucherFilter(
            com.teatrack_mcd_253eie502802_group02.model.Promotion voucher,
            String query,
            boolean exactMatch) {
        if (query == null || query.isEmpty()) {
            return true;
        }
        String normalizedQuery = query.trim().toUpperCase(Locale.ROOT);
        String id = voucher.getId() == null ? "" : voucher.getId().trim().toUpperCase(Locale.ROOT);
        String voucherCode = voucher.getCode() == null ? "" : voucher.getCode().trim().toUpperCase(Locale.ROOT);
        String title = voucher.getTitle() == null ? "" : voucher.getTitle().trim().toUpperCase(Locale.ROOT);

        if (exactMatch) {
            return normalizedQuery.equals(id) || normalizedQuery.equals(voucherCode);
        }
        return id.contains(normalizedQuery)
                || voucherCode.contains(normalizedQuery)
                || title.contains(normalizedQuery);
    }

    private void showVoucherPicker() {
        String filter = etVoucherCode != null ? etVoucherCode.getText().toString().trim() : "";
        buildVoucherRows(filter);
        if (cardOrderSummary != null) cardOrderSummary.setVisibility(View.GONE);
        if (cardVoucherPicker != null) cardVoucherPicker.setVisibility(View.VISIBLE);
        if (paymentOverlayScrim != null) paymentOverlayScrim.setVisibility(View.VISIBLE);
        if (btnConfirmOrder != null) btnConfirmOrder.setVisibility(View.GONE);
        if (tvTerms != null) tvTerms.setVisibility(View.GONE);
        if (dragHandle != null) dragHandle.setVisibility(View.GONE);
        applyOverlayFooterPadding();
    }

    private void hideVoucherPicker() {
        if (cardVoucherPicker != null) cardVoucherPicker.setVisibility(View.GONE);
        if (paymentOverlayScrim != null) paymentOverlayScrim.setVisibility(View.GONE);
        if (cardOrderSummary != null) {
            cardOrderSummary.setAlpha(1f);
            cardOrderSummary.setVisibility(View.VISIBLE);
        }
        if (btnConfirmOrder != null) btnConfirmOrder.setVisibility(View.VISIBLE);
        if (tvTerms != null) tvTerms.setVisibility(View.VISIBLE);
        if (dragHandle != null) dragHandle.setVisibility(View.VISIBLE);
        restoreFooterPadding();
    }

    private void setupRecipientEditor() {
        cardRecipientEditor = findViewById(R.id.cardRecipientEditor);
        etRecipientName = findViewById(R.id.etRecipientName);
        etRecipientPhone = findViewById(R.id.etRecipientPhone);
        etRecipientAddress = findViewById(R.id.etRecipientAddress);
        tvPickupDate = findViewById(R.id.tvPickupDate);
        tvCustomTimeToggle = findViewById(R.id.tvCustomTimeToggle);
        layoutCustomTime = findViewById(R.id.layoutCustomTime);
        pickerHour = findViewById(R.id.pickerHour);
        pickerMinute = findViewById(R.id.pickerMinute);
        calendarViewCustomTime = findViewById(R.id.calendarViewCustomTime);
        gridPickupTimes = findViewById(R.id.gridPickupTimes);
        layoutRecipientEditorFooter = findViewById(R.id.layoutRecipientEditorFooter);
        cardPaymentPickerContent = findViewById(R.id.cardPaymentPickerContent);

        // Ensure default date before setting up slots
        if (selectedPickupDate == null || selectedPickupDate.isEmpty()) {
            Calendar today = Calendar.getInstance();
            selectedPickupDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                    today.get(Calendar.DAY_OF_MONTH),
                    today.get(Calendar.MONTH) + 1,
                    today.get(Calendar.YEAR));
        }

        setupPickupTimeSlots();
        setupPickupDatePicker();
        setupCustomTimeRequest();

        View btnEdit    = findViewById(R.id.btnEditRecipientDetail);
        View btnCancel  = findViewById(R.id.btnCancelRecipientEdit);
        View btnConfirm = findViewById(R.id.btnConfirmRecipientEdit);

        if (btnEdit != null)
            btnEdit.setOnClickListener(v -> showRecipientEditor());
        if (btnCancel != null)
            btnCancel.setOnClickListener(v -> hideRecipientEditor());
        if (btnConfirm != null)
            btnConfirm.setOnClickListener(v -> confirmRecipientEdit());
    }

    private void showRecipientEditor() {
        if (tvRecipientDetails != null) {
            String current = tvRecipientDetails.getText().toString().trim();
            if (!current.isEmpty()) {
                String[] lines = current.split("\n");
                if (lines.length >= 1) {
                    String[] namePhone = lines[0].split("\\|");
                    if (namePhone.length >= 1 && etRecipientName != null) etRecipientName.setText(namePhone[0].trim());
                    if (namePhone.length >= 2 && etRecipientPhone != null) etRecipientPhone.setText(namePhone[1].trim());
                }
                if (lines.length >= 2 && etRecipientAddress != null) {
                    etRecipientAddress.setText(lines[1].trim());
                }
            }
        }
        if (etRecipientAddress != null && etRecipientAddress.getText().toString().isEmpty()) {
            android.content.SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            etRecipientAddress.setText(pref.getString("saved_address", ""));
        }
        highlightSavedPickupTime();
        if (tvPickupDate != null && !selectedPickupDate.isEmpty()) {
            tvPickupDate.setText(selectedPickupDate);
        }
        if (layoutCustomTime != null) {
            layoutCustomTime.setVisibility(customPickupTime.isEmpty() ? View.GONE : View.VISIBLE);
        }
        if (!customPickupTime.isEmpty()) {
            restorePickerValues();
        }

        // Ẩn order summary & confirm button, giống showPaymentPicker
        if (cardOrderSummary != null) cardOrderSummary.setVisibility(View.GONE);
        if (cardRecipientEditor != null) cardRecipientEditor.setVisibility(View.VISIBLE);
        if (paymentOverlayScrim != null) paymentOverlayScrim.setVisibility(View.VISIBLE);
        if (btnConfirmOrder != null) btnConfirmOrder.setVisibility(View.GONE);
        if (tvTerms != null) tvTerms.setVisibility(View.GONE);
        if (dragHandle != null) dragHandle.setVisibility(View.GONE);

        applyOverlayFooterPadding();
    }

    private void hideRecipientEditor() {
        if (cardRecipientEditor != null) cardRecipientEditor.setVisibility(View.GONE);
        if (paymentOverlayScrim != null) paymentOverlayScrim.setVisibility(View.GONE);
        if (cardOrderSummary != null) {
            cardOrderSummary.setAlpha(1f);
            cardOrderSummary.setVisibility(View.VISIBLE);
        }
        if (btnConfirmOrder != null) btnConfirmOrder.setVisibility(View.VISIBLE);
        if (tvTerms != null) tvTerms.setVisibility(View.VISIBLE);
        if (dragHandle != null) dragHandle.setVisibility(View.VISIBLE);

        restoreFooterPadding();
    }

    private void confirmRecipientEdit() {
        String name = etRecipientName != null ? etRecipientName.getText().toString().trim() : "";
        String phone = etRecipientPhone != null ? etRecipientPhone.getText().toString().trim() : "";
        String address = etRecipientAddress != null ? etRecipientAddress.getText().toString().trim() : "";
        if (layoutCustomTime != null && layoutCustomTime.getVisibility() == View.VISIBLE) {
            buildCustomPickupTime();
        }

        boolean hasError = false;
        if (name.isEmpty()) {
            if (etRecipientName != null) etRecipientName.setError("Required");
            hasError = true;
        } else {
            if (etRecipientName != null) etRecipientName.setError(null);
        }
        if (phone.isEmpty()) {
            if (etRecipientPhone != null) etRecipientPhone.setError("Required");
            hasError = true;
        } else {
            if (etRecipientPhone != null) etRecipientPhone.setError(null);
        }
        if (address.isEmpty()) {
            if (etRecipientAddress != null) etRecipientAddress.setError("Required");
            hasError = true;
        } else {
            if (etRecipientAddress != null) etRecipientAddress.setError(null);
        }
        if (hasError) return;

        updateRecipientDisplay(name, phone, address, selectedPickupTime, selectedPickupDate, customPickupTime);
        if (switchSaveDetails != null && switchSaveDetails.isChecked()) {
            getSharedPreferences("UserPrefs", MODE_PRIVATE).edit()
                    .putString("saved_name", name)
                    .putString("saved_phone", phone)
                    .putString("saved_address", address)
                    .putString("saved_pickup_time", selectedPickupTime)
                    .putString("saved_pickup_date", selectedPickupDate)
                    .putString("saved_custom_pickup_time", customPickupTime)
                    .apply();
        }
        hideRecipientEditor();
    }

    private void saveCurrentRecipientToPrefs() {
        if (tvRecipientDetails == null) return;
        String details = tvRecipientDetails.getText().toString().trim();
        if (details.isEmpty()) return;

        String name = "";
        String phone = "";
        String address = "";

        String[] lines = details.split("\n");
        if (lines.length >= 1) {
            String[] parts = lines[0].split("\\|");
            if (parts.length >= 1) name = parts[0].trim();
            if (parts.length >= 2) phone = parts[1].trim();
        }
        if (lines.length >= 2) {
            address = lines[1].trim();
        }

        android.content.SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        pref.edit()
                .putString("saved_name", name)
                .putString("saved_phone", phone)
                .putString("saved_address", address)
                .putString("saved_pickup_time", selectedPickupTime)
                .putString("saved_pickup_date", selectedPickupDate)
                .putString("saved_custom_pickup_time", customPickupTime)
                .apply();
    }

    private void updateRecipientDisplay(String name, String phone, String address, String pickupTime,
                                        String pickupDate, String customTime) {
        if (tvRecipientDetails != null) {
            StringBuilder display = new StringBuilder();
            if (name != null && !name.isEmpty()) {
                display.append(name);
            }
            if (phone != null && !phone.isEmpty()) {
                if (display.length() > 0) display.append(" | ");
                display.append(phone);
            }
            if (address != null && !address.isEmpty()) {
                if (display.length() > 0) display.append("\n");
                display.append(address);
            }
            tvRecipientDetails.setText(display.toString());
        }
        String timeLabel = (customTime != null && !customTime.isEmpty()) ? customTime : pickupTime;

        if (tvOrderPickupTime != null) {
            if (pickupDate != null && !pickupDate.isEmpty() && timeLabel != null && !timeLabel.isEmpty()) {
                tvOrderPickupTime.setText(pickupDate + " " + timeLabel);
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                tvOrderPickupTime.setText(sdf.format(new java.util.Date()));
            }
        }
    }

    private static final String[][] PICKUP_SLOT_DATA = {
        {"09:00 AM", "Early Bird", "false"},
        {"10:30 AM", "Morning Tea", "false"},
        {"12:15 PM", "Midday", "false"},
        {"02:30 PM", "Afternoon", "false"},
        {"04:45 PM", "Closing", "false"},
        {"06:00 PM", "Full", "true"},
    };

    private void setupPickupTimeSlots() {
        if (gridPickupTimes == null) {
            return;
        }
        gridPickupTimes.removeAllViews();
        
        Calendar now = Calendar.getInstance();
        boolean dateIsToday = isToday(selectedPickupDate);
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);

        for (String[] slotData : PICKUP_SLOT_DATA) {
            String timeStr = slotData[0];
            String label = slotData[1];
            boolean isFullInConfig = Boolean.parseBoolean(slotData[2]);
            
            // Check if slot is in the past
            boolean isPast = false;
            if (dateIsToday) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                    Calendar slotTime = Calendar.getInstance();
                    slotTime.setTime(sdf.parse(timeStr));
                    
                    int slotHour = slotTime.get(Calendar.HOUR_OF_DAY);
                    int slotMinute = slotTime.get(Calendar.MINUTE);
                    
                    if (slotHour < currentHour || (slotHour == currentHour && slotMinute <= currentMinute)) {
                        isPast = true;
                    }
                } catch (Exception ignored) {}
            }

            boolean isDisabled = isFullInConfig || isPast;

            android.widget.LinearLayout card = new android.widget.LinearLayout(this);
            card.setOrientation(android.widget.LinearLayout.VERTICAL);
            card.setGravity(android.view.Gravity.CENTER);
            card.setPadding(dp(10), dp(14), dp(10), dp(14));
            card.setTag(timeStr);
            card.setTag(R.id.tvCustomTimeToggle, label);

            android.widget.GridLayout.LayoutParams params = new android.widget.GridLayout.LayoutParams();
            params.width = 0;
            params.height = android.widget.GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f);
            params.setMargins(dp(4), dp(4), dp(4), dp(4));
            card.setLayoutParams(params);

            TextView tvTime = new TextView(this);
            tvTime.setText(timeStr);
            tvTime.setGravity(android.view.Gravity.CENTER);
            tvTime.setTextSize(14f);
            tvTime.setTypeface(tvTime.getTypeface(), Typeface.BOLD);

            TextView tvLabel = new TextView(this);
            tvLabel.setGravity(android.view.Gravity.CENTER);
            tvLabel.setTextSize(12f);

            if (isDisabled) {
                card.setBackgroundResource(R.drawable.bg_edittext_rounded);
                card.setAlpha(0.5f);
                android.text.SpannableString strikeTime = new android.text.SpannableString(timeStr);
                strikeTime.setSpan(new android.text.style.StrikethroughSpan(), 0, timeStr.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvTime.setText(strikeTime);
                tvTime.setTextColor(ContextCompat.getColor(this, R.color.secondary));
                tvLabel.setText(isPast ? "Expired" : label);
                tvLabel.setTextColor(ContextCompat.getColor(this, R.color.secondary));
            } else {
                card.setBackgroundResource(R.drawable.bg_edittext_rounded);
                tvTime.setTextColor(ContextCompat.getColor(this, R.color.on_surface));
                tvLabel.setText(label);
                tvLabel.setTextColor(ContextCompat.getColor(this, R.color.secondary));
                card.setOnClickListener(v -> selectPickupTimeSlot(timeStr, card));
            }

            card.addView(tvTime);
            card.addView(tvLabel);
            gridPickupTimes.addView(card);
        }
    }

    private void selectPickupTimeSlot(String slot, View selectedView) {
        selectedPickupTime = slot;
        customPickupTime = "";
        if (layoutCustomTime != null) {
            layoutCustomTime.setVisibility(View.GONE);
        }
        for (int i = 0; i < gridPickupTimes.getChildCount(); i++) {
            View child = gridPickupTimes.getChildAt(i);
            boolean selected = child == selectedView;
            Object labelTag = child.getTag(R.id.tvCustomTimeToggle);
            boolean isFull = "Full".equals(labelTag);
            if (isFull) continue;
            child.setBackgroundResource(selected ? R.drawable.bg_light_blue_rounded_12 : R.drawable.bg_edittext_rounded);
            child.setAlpha(1f);
            if (child instanceof android.widget.LinearLayout) {
                android.widget.LinearLayout card = (android.widget.LinearLayout) child;
                for (int j = 0; j < card.getChildCount(); j++) {
                    View inner = card.getChildAt(j);
                    if (inner instanceof TextView) {
                        TextView tv = (TextView) inner;
                        if (j == 0) {
                            tv.setText((String) child.getTag());
                            tv.setTextColor(ContextCompat.getColor(this,
                                    selected ? R.color.brand_blue : R.color.on_surface));
                        } else {
                            Object origLabel = child.getTag(R.id.tvCustomTimeToggle);
                            String labelText = origLabel != null ? origLabel.toString() : tv.getText().toString();
                            tv.setText(selected ? "Selected" : labelText);
                            tv.setTextColor(ContextCompat.getColor(this,
                                    selected ? R.color.brand_blue : R.color.secondary));
                        }
                    }
                }
            }
        }
    }

    private void highlightSavedPickupTime() {
        if (gridPickupTimes == null || selectedPickupTime == null || selectedPickupTime.isEmpty()) {
            return;
        }
        for (int i = 0; i < gridPickupTimes.getChildCount(); i++) {
            View child = gridPickupTimes.getChildAt(i);
            if (selectedPickupTime.equals(child.getTag())) {
                selectPickupTimeSlot(selectedPickupTime, child);
                break;
            }
        }
    }

    private void setupPickupDatePicker() {
        if (tvPickupDate == null) {
            return;
        }
        if (selectedPickupDate == null || selectedPickupDate.isEmpty()) {
            Calendar today = Calendar.getInstance();
            selectedPickupDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                    today.get(Calendar.DAY_OF_MONTH),
                    today.get(Calendar.MONTH) + 1,
                    today.get(Calendar.YEAR));
        }
        tvPickupDate.setText(selectedPickupDate);

        tvPickupDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    R.style.CustomMaterialCalendar,
                    (view, year, month, dayOfMonth) -> {
                        selectedPickupDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                                dayOfMonth, month + 1, year);
                        tvPickupDate.setText(selectedPickupDate);
                        // Re-validate everything when date changes
                        validateCustomTimePickers();
                        setupPickupTimeSlots();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            // Disable past dates
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dialog.show();
        });
    }

    private void setupCustomTimeRequest() {
        if (tvCustomTimeToggle == null || layoutCustomTime == null) {
            return;
        }
        tvCustomTimeToggle.setOnClickListener(v -> {
            boolean show = layoutCustomTime.getVisibility() != View.VISIBLE;
            layoutCustomTime.setVisibility(show ? View.VISIBLE : View.GONE);
            if (show) {
                clearPickupSlotSelection();
                validateCustomTimePickers();
            }
        });
        if (pickerHour != null) {
            pickerHour.setMinValue(0);
            pickerHour.setMaxValue(23);
            pickerHour.setValue(customPickupHour);
            pickerHour.setFormatter(value -> String.format(Locale.getDefault(), "%02d", value));
            pickerHour.setOnValueChangedListener((picker, oldVal, newVal) -> {
                customPickupHour = newVal;
                validateCustomTimePickers();
            });
        }
        if (pickerMinute != null) {
            pickerMinute.setMinValue(0);
            pickerMinute.setMaxValue(59);
            pickerMinute.setValue(customPickupMinute);
            pickerMinute.setFormatter(value -> String.format(Locale.getDefault(), "%02d", value));
            pickerMinute.setOnValueChangedListener((picker, oldVal, newVal) -> {
                customPickupMinute = newVal;
                validateCustomTimePickers();
            });
        }
        if (calendarViewCustomTime != null) {
            // Set min date for calendar view
            calendarViewCustomTime.setMinDate(System.currentTimeMillis() - 1000);
            calendarViewCustomTime.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                selectedPickupDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                        dayOfMonth, month + 1, year);
                if (tvPickupDate != null) {
                    tvPickupDate.setText(selectedPickupDate);
                }
                // Reset pickers to 0 when date changes to future, or validate if today
                if (!isToday(selectedPickupDate)) {
                    if (pickerHour != null) {
                        pickerHour.setMinValue(0);
                    }
                    if (pickerMinute != null) {
                        pickerMinute.setMinValue(0);
                    }
                }
                validateCustomTimePickers();
                setupPickupTimeSlots();
            });
        }
    }

    private void validateCustomTimePickers() {
        Calendar now = Calendar.getInstance();
        boolean today = isToday(selectedPickupDate);
        
        if (today) {
            int currentHour = now.get(Calendar.HOUR_OF_DAY);
            int currentMin = now.get(Calendar.MINUTE);
            
            if (pickerHour != null) {
                pickerHour.setMinValue(currentHour);
                if (pickerHour.getValue() < currentHour) {
                    pickerHour.setValue(currentHour);
                    customPickupHour = currentHour;
                }
            }
            
            if (pickerMinute != null) {
                if (customPickupHour == currentHour) {
                    pickerMinute.setMinValue(currentMin);
                    if (pickerMinute.getValue() < currentMin) {
                        pickerMinute.setValue(currentMin);
                        customPickupMinute = currentMin;
                    }
                } else {
                    pickerMinute.setMinValue(0);
                }
            }
        } else {
            if (pickerHour != null) {
                pickerHour.setMinValue(0);
            }
            if (pickerMinute != null) {
                pickerMinute.setMinValue(0);
            }
        }
        buildCustomPickupTime();
    }

    private boolean isToday(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return false;
        Calendar today = Calendar.getInstance();
        String todayStr = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                today.get(Calendar.DAY_OF_MONTH),
                today.get(Calendar.MONTH) + 1,
                today.get(Calendar.YEAR));
        return dateStr.equals(todayStr);
    }

    private void buildCustomPickupTime() {
        customPickupTime = String.format(Locale.getDefault(), "%02d:%02d", customPickupHour, customPickupMinute);
    }

    private void restorePickerValues() {
        if (customPickupTime == null || customPickupTime.isEmpty()) return;
        String[] parts = customPickupTime.split(":");
        if (parts.length == 2) {
            try {
                customPickupHour = Integer.parseInt(parts[0]);
                customPickupMinute = Integer.parseInt(parts[1]);
                if (pickerHour != null) pickerHour.setValue(customPickupHour);
                if (pickerMinute != null) pickerMinute.setValue(customPickupMinute);
            } catch (NumberFormatException ignored) {}
        }
    }

    private void clearPickupSlotSelection() {
        selectedPickupTime = "";
        if (gridPickupTimes == null) {
            return;
        }
        for (int i = 0; i < gridPickupTimes.getChildCount(); i++) {
            View child = gridPickupTimes.getChildAt(i);
            child.setBackgroundResource(R.drawable.bg_edittext_rounded);
            if (child instanceof android.widget.LinearLayout) {
                android.widget.LinearLayout card = (android.widget.LinearLayout) child;
                for (int j = 0; j < card.getChildCount(); j++) {
                    View inner = card.getChildAt(j);
                    if (inner instanceof TextView) {
                        ((TextView) inner).setTextColor(ContextCompat.getColor(this,
                                j == 0 ? R.color.on_surface : R.color.secondary));
                    }
                }
            }
        }
    }

    private void loadUserRecipientFallback() {
        android.content.SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        if (!pref.getString("saved_name", "").isEmpty()) {
            return;
        }
        android.content.SharedPreferences loginPrefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String userId = loginPrefs.getString("userId", null);
        if (userId == null || userId.isEmpty()) {
            return;
        }
        FirebaseDatabase.getInstance().getReference("Users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            return;
                        }
                        User user = snapshot.getValue(User.class);
                        if (user == null) {
                            return;
                        }
                        String name = user.getFullName();
                        String phone = user.getPhoneNumber();
                        if ((name == null || name.isEmpty()) && (phone == null || phone.isEmpty())) {
                            return;
                        }
                        String safeName = name == null ? "" : name;
                        String safePhone = phone == null ? "" : phone;
                        String safeAddress = user.getAddress() == null ? "" : user.getAddress();
                        updateRecipientDisplay(safeName, safePhone, safeAddress, "", "", "");
                        if (switchSaveDetails != null && switchSaveDetails.isChecked()) {
                            getSharedPreferences("UserPrefs", MODE_PRIVATE).edit()
                                    .putString("saved_name", safeName)
                                    .putString("saved_phone", safePhone)
                                    .putString("saved_address", safeAddress)
                                    .apply();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }    private void updateHeaderBadge(int count, boolean visible) {
        if (tvCartHeaderBadge == null) {
            return;
        }
        if (visible && count > 0) {
            tvCartHeaderBadge.setVisibility(View.VISIBLE);
            tvCartHeaderBadge.setText(count > 99 ? "99+" : String.valueOf(count));
        } else {
            tvCartHeaderBadge.setVisibility(View.GONE);
        }
    }
    private void loadSavedRecipientInfo() {
        android.content.SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedName = pref.getString("saved_name", "");
        String savedPhone = pref.getString("saved_phone", "");
        String savedAddress = pref.getString("saved_address", "");
        selectedPickupTime = pref.getString("saved_pickup_time", "");
        selectedPickupDate = pref.getString("saved_pickup_date", "");
        customPickupTime = pref.getString("saved_custom_pickup_time", "");

        if (!savedName.isEmpty() || !savedPhone.isEmpty() || !savedAddress.isEmpty()) {
            updateRecipientDisplay(savedName, savedPhone, savedAddress, selectedPickupTime, selectedPickupDate, customPickupTime);
        }
        if (tvPickupDate != null && !selectedPickupDate.isEmpty()) {
            tvPickupDate.setText(selectedPickupDate);
        }
        if (!customPickupTime.isEmpty()) {
            restorePickerValues();
        }
    }
    private void showRemovedBanner() {
        if (cardItemRemoved == null) {
            return;
        }
        cardItemRemoved.setVisibility(View.VISIBLE);
        bannerHandler.removeCallbacksAndMessages(null);
        bannerHandler.postDelayed(() -> {
            if (cardItemRemoved != null) {
                cardItemRemoved.setVisibility(View.GONE);
            }
        }, 2500);
    }

    private String formatPrice(int price) {
        return String.format(Locale.US, "%,d", price).replace(',', '.') + "đ";
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

}
