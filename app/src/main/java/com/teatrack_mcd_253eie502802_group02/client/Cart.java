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

import androidx.activity.EdgeToEdge;
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
import com.teatrack_mcd_253eie502802_group02.data.CartManager;
import com.teatrack_mcd_253eie502802_group02.data.OrderCheckoutFlow;
import com.teatrack_mcd_253eie502802_group02.model.CartItem;
import com.teatrack_mcd_253eie502802_group02.shared.ui.CartBadgeHelper;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;

import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Cart extends BaseActivity implements CartManager.CartChangeListener {

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

        tvItemsSelected = findViewById(R.id.tvItemsSelected);
        tvCartHeaderBadge = findViewById(R.id.tvCartHeaderBadge);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvTotal = findViewById(R.id.tvTotal);
        tvSelectedPayment = findViewById(R.id.tvSelectedPayment);
        tvSelectedBranchAddress = findViewById(R.id.tvSelectedBranchAddress);
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

        if (btnConfirmOrder != null) {
            btnConfirmOrder.setOnClickListener(v -> handleConfirmOrder());
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
            view.setPadding(0, systemBars.top, 0, 0);
            if (layoutCartFooter != null) {
                int bottomPadding = systemBars.bottom + dp(16);
                layoutCartFooter.setPadding(
                        layoutCartFooter.getPaddingLeft(),
                        layoutCartFooter.getPaddingTop(),
                        layoutCartFooter.getPaddingRight(),
                        bottomPadding
                );
            }
            return insets;
        });
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
            paymentOverlayScrim.setOnClickListener(v -> hidePaymentPicker());
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
        if (tvCashOnHandLabel == null) return;
        tvCashOnHandLabel.setTextColor(ContextCompat.getColor(this,
                selected ? R.color.on_surface : R.color.secondary));
        tvCashOnHandLabel.setTypeface(tvCashOnHandLabel.getTypeface(),
                selected ? Typeface.BOLD : Typeface.NORMAL);
    }

    private void highlightBankHeader(boolean selected) {
        if (tvCashInBankLabel == null) return;
        tvCashInBankLabel.setTextColor(ContextCompat.getColor(this,
                selected ? R.color.on_surface : R.color.secondary));
        tvCashInBankLabel.setTypeface(tvCashInBankLabel.getTypeface(),
                selected ? Typeface.BOLD : Typeface.NORMAL);
    }

    private void highlightBankOption(View optionRow, boolean selected) {
        if (!(optionRow instanceof ViewGroup)) {
            return;
        }
        ViewGroup group = (ViewGroup) optionRow;
        TextView label = null;
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof TextView) {
                label = (TextView) child;
                break;
            }
        }
        if (label == null) {
            return;
        }
        label.setTextColor(ContextCompat.getColor(this,
                selected ? R.color.brand_blue : R.color.secondary));
        label.setTypeface(label.getTypeface(), selected ? Typeface.BOLD : Typeface.NORMAL);
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

        if (layoutCartFooter != null) {
            layoutCartFooter.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            layoutCartFooter.setPadding(0, 0, 0, 0);
        }

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

        if (layoutCartFooter != null) {
            layoutCartFooter.setBackgroundResource(R.drawable.bg_cart_footer);
            layoutCartFooter.setPadding(
                    footerPaddingStart,
                    footerPaddingTop,
                    footerPaddingEnd,
                    footerPaddingBottom
            );
        }
    }    private void handleConfirmOrder() {
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
        if (tvSubtotal != null) {
            tvSubtotal.setText(formatPrice(subtotal));
        }
        if (tvTotal != null) {
            tvTotal.setText(formatPrice(subtotal));
        }
    }

    private void updateHeaderBadge(int count, boolean visible) {
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
