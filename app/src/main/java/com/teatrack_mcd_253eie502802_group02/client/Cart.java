package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
    private com.google.android.material.textfield.TextInputEditText etRecipientName;
    private com.google.android.material.textfield.TextInputEditText etRecipientPhone;
    private TextView tvRecipientPickupTime;
    private TextView tvPickupDate;
    private TextView tvCustomTimeToggle;
    private com.google.android.material.textfield.TextInputLayout layoutCustomTime;
    private com.google.android.material.textfield.TextInputEditText etCustomTimeRequest;
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
        setupRecipientEditor();

        if (btnConfirmOrder != null) {
            btnConfirmOrder.setOnClickListener(v -> {
                String name = etRecipientName.getText().toString().trim();
                String phone = etRecipientPhone.getText().toString().trim();

                if (name.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin người nhận!", Toast.LENGTH_SHORT).show();
                    cardRecipientEditor.setVisibility(View.VISIBLE);
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
        View layoutSummaryCollapsible = findViewById(R.id.layoutSummaryCollapsible);
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
                    Toast.makeText(this, "Thông tin thanh toán sẽ được lưu cho lần sau", Toast.LENGTH_SHORT).show();
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
        applyOverlayBottomInset(layoutRecipientEditorFooter, dp(16));
        applyOverlayBottomInset(cardPaymentPickerContent, dp(8));
    }

    private void applyOverlayBottomInset(View target, int basePadding) {
        if (target == null) {
            return;
        }
        target.setPadding(
                target.getPaddingLeft(),
                target.getPaddingTop(),
                target.getPaddingRight(),
                basePadding + navBarInsetBottom
        );
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
                } else {
                    hideRecipientEditor();
                }
            });        }
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
        int color = ContextCompat.getColor(this, selected ? R.color.on_surface : R.color.on_surface);
        tvCashOnHandLabel.setTextColor(color);
    }

    private void highlightBankHeader(boolean selected) {
        if (tvCashInBankLabel == null) {
            return;
        }
        tvCashInBankLabel.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        int color = ContextCompat.getColor(this, selected ? R.color.on_surface : R.color.on_surface);
        tvCashInBankLabel.setTextColor(color);
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
        }label.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        int color = ContextCompat.getColor(this, selected ? R.color.on_surface : R.color.on_surface);
        label.setTextColor(color);
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
        boolean isSaveEnabled = (switchSaveDetails != null && switchSaveDetails.isChecked());
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
        if (tvSubtotal != null) {
            tvSubtotal.setText(formatPrice(subtotal));
        }
        if (tvTotal != null) {
            tvTotal.setText(formatPrice(subtotal));
        }
    }
    private void setupRecipientEditor() {
        cardRecipientEditor = findViewById(R.id.cardRecipientEditor);
        etRecipientName = findViewById(R.id.etRecipientName);
        etRecipientPhone = findViewById(R.id.etRecipientPhone);
        tvPickupDate = findViewById(R.id.tvPickupDate);
        tvCustomTimeToggle = findViewById(R.id.tvCustomTimeToggle);
        layoutCustomTime = findViewById(R.id.layoutCustomTime);
        etCustomTimeRequest = findViewById(R.id.etCustomTimeRequest);
        gridPickupTimes = findViewById(R.id.gridPickupTimes);
        layoutRecipientEditorFooter = findViewById(R.id.layoutRecipientEditorFooter);
        cardPaymentPickerContent = findViewById(R.id.cardPaymentPickerContent);
        setupPickupTimeSlots();
        setupPickupDatePicker();
        setupCustomTimeRequest();

        View btnEdit    = findViewById(R.id.btnEditRecipientDetail);
        View btnClose   = findViewById(R.id.btnCloseRecipientEditor);
        View btnCancel  = findViewById(R.id.btnCancelRecipientEdit);
        View btnConfirm = findViewById(R.id.btnConfirmRecipientEdit);

        if (btnEdit != null)
            btnEdit.setOnClickListener(v -> showRecipientEditor());
        if (btnClose != null)
            btnClose.setOnClickListener(v -> hideRecipientEditor());
        if (btnCancel != null)
            btnCancel.setOnClickListener(v -> hideRecipientEditor());
        if (btnConfirm != null)
            btnConfirm.setOnClickListener(v -> confirmRecipientEdit());
    }

    private void showRecipientEditor() {
        if (tvRecipientDetails != null && etRecipientName != null && etRecipientPhone != null) {
            CharSequence detailsSeq = tvRecipientDetails.getText();
            if (detailsSeq != null) {
                String current = detailsSeq.toString();
                String[] parts = current.split("\\|");
                if (parts.length >= 1) etRecipientName.setText(parts[0].trim());
                if (parts.length >= 2) etRecipientPhone.setText(parts[1].trim());
            }
        }
        highlightSavedPickupTime();
        if (tvPickupDate != null && !selectedPickupDate.isEmpty()) {
            tvPickupDate.setText(selectedPickupDate);
        }
        if (etCustomTimeRequest != null) {
            etCustomTimeRequest.setText(customPickupTime);
        }
        if (layoutCustomTime != null) {
            layoutCustomTime.setVisibility(customPickupTime.isEmpty() ? View.GONE : View.VISIBLE);
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
        customPickupTime = etCustomTimeRequest != null ? etCustomTimeRequest.getText().toString().trim() : "";

        if (!name.isEmpty() || !phone.isEmpty()) {
            updateRecipientDisplay(name, phone, selectedPickupTime, selectedPickupDate, customPickupTime);
            if (switchSaveDetails != null && switchSaveDetails.isChecked()) {
                android.content.SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                pref.edit()
                        .putString("saved_name", name)
                        .putString("saved_phone", phone)
                        .putString("saved_pickup_time", selectedPickupTime)
                        .putString("saved_pickup_date", selectedPickupDate)
                        .putString("saved_custom_pickup_time", customPickupTime)
                        .apply();
            }
        }
        hideRecipientEditor();
    }

    private void updateRecipientDisplay(String name, String phone, String pickupTime,
                                        String pickupDate, String customTime) {
        if (tvRecipientDetails != null) {
            String display = name;
            if (!phone.isEmpty()) {
                display = name.isEmpty() ? phone : name + " | " + phone;
            }
            tvRecipientDetails.setText(display);
        }
        String timeLabel = (customTime != null && !customTime.isEmpty()) ? customTime : pickupTime;
        String pickupLabel = "";
        if (pickupDate != null && !pickupDate.isEmpty() && timeLabel != null && !timeLabel.isEmpty()) {
            pickupLabel = getString(R.string.cart_pickup_display_with_date, pickupDate, timeLabel);
        } else if (timeLabel != null && !timeLabel.isEmpty()) {
            pickupLabel = getString(R.string.cart_pickup_display, timeLabel);
        } else if (pickupDate != null && !pickupDate.isEmpty()) {
            pickupLabel = pickupDate;
        }

        if (tvOrderPickupTime != null) {
            if (pickupDate != null && !pickupDate.isEmpty() && timeLabel != null && !timeLabel.isEmpty()) {
                tvOrderPickupTime.setText(pickupDate + " " + timeLabel);
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                tvOrderPickupTime.setText(sdf.format(new java.util.Date()));
            }
        }
        if (tvRecipientPickupTime != null) {
            if (!pickupLabel.isEmpty()) {
                tvRecipientPickupTime.setText(pickupLabel);
                tvRecipientPickupTime.setVisibility(View.VISIBLE);
            } else {
                tvRecipientPickupTime.setVisibility(View.GONE);
            }
        }
    }

    private void setupPickupTimeSlots() {
        if (gridPickupTimes == null) {
            return;
        }
        gridPickupTimes.removeAllViews();
        String[] slots = getResources().getStringArray(R.array.cart_pickup_time_slots);
        for (String slot : slots) {
            TextView slotView = new TextView(this);
            android.widget.GridLayout.LayoutParams params = new android.widget.GridLayout.LayoutParams();
            params.width = 0;
            params.height = android.widget.GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f);
            params.setMargins(dp(4), dp(4), dp(4), dp(4));
            slotView.setLayoutParams(params);
            slotView.setText(slot);
            slotView.setGravity(android.view.Gravity.CENTER);
            slotView.setPadding(dp(10), dp(12), dp(10), dp(12));
            slotView.setTextSize(13f);
            slotView.setTypeface(slotView.getTypeface(), Typeface.BOLD);
            slotView.setBackgroundResource(R.drawable.bg_edittext_rounded);
            slotView.setTextColor(ContextCompat.getColor(this, R.color.on_surface));
            slotView.setOnClickListener(v -> selectPickupTimeSlot(slot, slotView));
            gridPickupTimes.addView(slotView);
        }
    }

    private void selectPickupTimeSlot(String slot, TextView selectedView) {
        selectedPickupTime = slot;
        customPickupTime = "";
        if (etCustomTimeRequest != null) {
            etCustomTimeRequest.setText("");
        }
        if (layoutCustomTime != null) {
            layoutCustomTime.setVisibility(View.GONE);
        }
        for (int i = 0; i < gridPickupTimes.getChildCount(); i++) {
            View child = gridPickupTimes.getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                boolean selected = tv == selectedView;
                tv.setBackgroundResource(selected ? R.drawable.bg_light_blue_rounded_12 : R.drawable.bg_edittext_rounded);
                tv.setTextColor(ContextCompat.getColor(this,
                        selected ? R.color.brand_blue : R.color.on_surface));
            }
        }
    }

    private void highlightSavedPickupTime() {
        if (gridPickupTimes == null || selectedPickupTime == null || selectedPickupTime.isEmpty()) {
            return;
        }
        for (int i = 0; i < gridPickupTimes.getChildCount(); i++) {
            View child = gridPickupTimes.getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                if (selectedPickupTime.equals(tv.getText().toString())) {
                    selectPickupTimeSlot(selectedPickupTime, tv);
                    break;
                }
            }
        }
    }

    private void setupPickupDatePicker() {
        if (tvPickupDate == null) {
            return;
        }
        if (selectedPickupDate.isEmpty()) {
            Calendar today = Calendar.getInstance();
            selectedPickupDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                    today.get(Calendar.DAY_OF_MONTH),
                    today.get(Calendar.MONTH) + 1,
                    today.get(Calendar.YEAR));
            tvPickupDate.setText(selectedPickupDate);
        }
        tvPickupDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        selectedPickupDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                                dayOfMonth, month + 1, year);
                        tvPickupDate.setText(selectedPickupDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
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
            if (show && etCustomTimeRequest != null) {
                etCustomTimeRequest.requestFocus();
            }
        });
        if (etCustomTimeRequest != null) {
            etCustomTimeRequest.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    clearPickupSlotSelection();
                }
            });
        }
    }

    private void clearPickupSlotSelection() {
        selectedPickupTime = "";
        if (gridPickupTimes == null) {
            return;
        }
        for (int i = 0; i < gridPickupTimes.getChildCount(); i++) {
            View child = gridPickupTimes.getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                tv.setBackgroundResource(R.drawable.bg_edittext_rounded);
                tv.setTextColor(ContextCompat.getColor(this, R.color.on_surface));
                tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
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
                        if (tvRecipientDetails != null
                                && (tvRecipientDetails.getText() == null
                                || tvRecipientDetails.getText().toString().trim().isEmpty()
                                || tvRecipientDetails.getText().toString().contains("Nguyễn Ba Đù"))) {
                            updateRecipientDisplay(
                                    name == null ? "" : name,
                                    phone == null ? "" : phone,
                                    "",
                                    "",
                                    ""
                            );
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
        selectedPickupTime = pref.getString("saved_pickup_time", "");
        selectedPickupDate = pref.getString("saved_pickup_date", "");
        customPickupTime = pref.getString("saved_custom_pickup_time", "");

        if (!savedName.isEmpty() || !savedPhone.isEmpty()) {
            updateRecipientDisplay(savedName, savedPhone, selectedPickupTime, selectedPickupDate, customPickupTime);
        }
        if (tvPickupDate != null && !selectedPickupDate.isEmpty()) {
            tvPickupDate.setText(selectedPickupDate);
        }
        if (etCustomTimeRequest != null && !customPickupTime.isEmpty()) {
            etCustomTimeRequest.setText(customPickupTime);
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
