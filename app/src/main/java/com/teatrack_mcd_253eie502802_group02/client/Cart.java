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
import android.widget.ImageView;
import android.widget.TextView;

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
import com.teatrack_mcd_253eie502802_group02.model.CartItem;
import com.teatrack_mcd_253eie502802_group02.shared.ui.CartBadgeHelper;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Cart extends AppCompatActivity implements CartManager.CartChangeListener {

    private static final int PAYMENT_CASH_ON_HAND = 0;
    private static final int PAYMENT_CASH_IN_BANK = 1;

    private final List<CartItem> cartItems = new ArrayList<>();
    private CartItemAdapter adapter;
    private TextView tvItemsSelected;
    private TextView tvCartHeaderBadge;
    private TextView tvEmptyCart;
    private TextView tvSubtotal;
    private TextView tvTotal;
    private TextView tvSelectedPayment;
    private ImageView ivSelectedPaymentIcon;
    private View cardItemRemoved;
    private View cartRoot;
    private View layoutCartFooter;
    private View overlayPaymentPicker;
    private View paymentPickerBottomContainer;
    private View paymentOverlayScrim;
    private int selectedPaymentMethod = PAYMENT_CASH_ON_HAND;
    private final Handler bannerHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        cartRoot = findViewById(R.id.cartRoot);
        layoutCartFooter = findViewById(R.id.layoutCartFooter);
        overlayPaymentPicker = findViewById(R.id.overlayPaymentPicker);
        paymentPickerBottomContainer = findViewById(R.id.paymentPickerBottomContainer);
        paymentOverlayScrim = findViewById(R.id.paymentOverlayScrim);
        setupWindowInsets();

        tvItemsSelected = findViewById(R.id.tvItemsSelected);
        tvCartHeaderBadge = findViewById(R.id.tvCartHeaderBadge);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvTotal = findViewById(R.id.tvTotal);
        tvSelectedPayment = findViewById(R.id.tvSelectedPayment);
        ivSelectedPaymentIcon = findViewById(R.id.ivSelectedPaymentIcon);
        cardItemRemoved = findViewById(R.id.cardItemRemoved);
        RecyclerView rvCartItems = findViewById(R.id.rvCartItems);
        TextView tvTerms = findViewById(R.id.tvTerms);
        TextView tvTotalLabel = findViewById(R.id.tvTotalLabel);

        applyExtraBoldTypeface(tvTotalLabel, tvTotal);

        View btnBack = findViewById(R.id.btnCartBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        setupTermsLink(tvTerms);
        setupPaymentPicker();

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
                if (paymentPickerBottomContainer != null) {
                    paymentPickerBottomContainer.setPadding(
                            paymentPickerBottomContainer.getPaddingLeft(),
                            paymentPickerBottomContainer.getPaddingTop(),
                            paymentPickerBottomContainer.getPaddingRight(),
                            bottomPadding + dp(64)
                    );
                }
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
        SpannableString spannable = new SpannableString(prefix + link);

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
        View optionCashInBank = findViewById(R.id.optionCashInBank);

        if (btnPaymentChange != null) {
            btnPaymentChange.setOnClickListener(v -> showPaymentPicker());
        }
        if (paymentOverlayScrim != null) {
            paymentOverlayScrim.setOnClickListener(v -> hidePaymentPicker());
        }
        if (paymentPickerBottomContainer != null) {
            paymentPickerBottomContainer.setClickable(true);
        }
        if (optionCashOnHand != null) {
            optionCashOnHand.setOnClickListener(v -> selectPaymentMethod(PAYMENT_CASH_ON_HAND));
        }
        if (optionCashInBank != null) {
            optionCashInBank.setOnClickListener(v -> selectPaymentMethod(PAYMENT_CASH_IN_BANK));
        }
    }

    private void showPaymentPicker() {
        if (overlayPaymentPicker != null) {
            overlayPaymentPicker.setVisibility(View.VISIBLE);
        }
    }

    private void hidePaymentPicker() {
        if (overlayPaymentPicker != null) {
            overlayPaymentPicker.setVisibility(View.GONE);
        }
    }

    private void selectPaymentMethod(int method) {
        selectedPaymentMethod = method;
        if (tvSelectedPayment != null) {
            tvSelectedPayment.setText(method == PAYMENT_CASH_IN_BANK
                    ? R.string.cart_cash_in_bank
                    : R.string.cart_cash_on_hand);
        }
        if (ivSelectedPaymentIcon != null) {
            ivSelectedPaymentIcon.setImageResource(method == PAYMENT_CASH_IN_BANK
                    ? R.drawable.ic_cash_bank
                    : R.drawable.ic_cash);
        }
        hidePaymentPicker();
    }

    private void openProfileTab() {
        Intent intent = new Intent(this, UserProfile.class);
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
