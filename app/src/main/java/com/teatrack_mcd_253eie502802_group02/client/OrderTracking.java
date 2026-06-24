package com.teatrack_mcd_253eie502802_group02.client;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.MainActivity;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.data.FirebaseOrderRepository;

import java.util.ArrayList;
import java.util.List;

public class OrderTracking extends AppCompatActivity {

    private FirebaseOrderRepository repository;
    private DatabaseReference orderRef;
    private ValueEventListener statusListener;
    private String orderId;

    private View dot1, dot2, dot3, dot4, dot5;
    private View line1, line2, line3, line4;
    private TextView txtStatus1, txtStatus2, txtStatus3, txtStatus4, txtStatus5;
    private TextView txtOrderId, txtShipperStatus;
    private FrameLayout mapContainer;
    private View deliveryDot;
    private ValueAnimator mapAnimator;

    private final List<View> dots = new ArrayList<>();
    private final List<View> lines = new ArrayList<>();
    private final List<TextView> statusTexts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_tracking);

        initViews();
        
        orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null) {
            orderId = getIntent().getStringExtra("orderId"); // Fallback for previous turns
        }

        if (orderId != null) {
            txtOrderId.setText(getString(R.string.order_tracking_id_prefix, orderId));
            repository = new FirebaseOrderRepository();
        } else {
            Toast.makeText(this, "Order ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void initViews() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtOrderId = findViewById(R.id.txtOrderId);
        txtShipperStatus = findViewById(R.id.txtShipperStatus);
        mapContainer = findViewById(R.id.mapContainer);

        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        dot4 = findViewById(R.id.dot4);
        dot5 = findViewById(R.id.dot5);
        dots.add(dot1); dots.add(dot2); dots.add(dot3); dots.add(dot4); dots.add(dot5);

        line1 = findViewById(R.id.line1);
        line2 = findViewById(R.id.line2);
        line3 = findViewById(R.id.line3);
        line4 = findViewById(R.id.line4);
        lines.add(line1); lines.add(line2); lines.add(line3); lines.add(line4);

        txtStatus1 = findViewById(R.id.txtStatusConfirmed);
        txtStatus2 = findViewById(R.id.txtStatusPreparing);
        txtStatus3 = findViewById(R.id.txtStatusReady);
        txtStatus4 = findViewById(R.id.txtStatusDelivery);
        txtStatus5 = findViewById(R.id.txtStatusDelivered);
        statusTexts.add(txtStatus1); statusTexts.add(txtStatus2); statusTexts.add(txtStatus3);
        statusTexts.add(txtStatus4); statusTexts.add(txtStatus5);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (orderId != null && repository != null) {
            setupFirebaseListener();
        }
    }

    private void setupFirebaseListener() {
        orderRef = repository.listenToOrder(orderId, null);
        statusListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if (status != null) {
                        updateStepper(status);
                        updateStatusLabel(status);
                        handleMapSimulation(status);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderTracking.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        orderRef.addValueEventListener(statusListener);
    }

    private void updateStepper(String status) {
        int currentIndex = mapStatusToIndex(status);

        for (int i = 0; i < dots.size(); i++) {
            View dot = dots.get(i);
            TextView text = statusTexts.get(i);
            View line = (i < lines.size()) ? lines.get(i) : null;

            if (i <= currentIndex) {
                // Active state
                dot.setAlpha(1.0f);
                dot.setBackgroundResource(R.drawable.bg_stepper_dot_active);
                text.setAlpha(1.0f);
                text.setTextColor(Color.BLACK);
                text.setTypeface(null, android.graphics.Typeface.BOLD);
                if (line != null) {
                    line.setAlpha(1.0f);
                    line.setBackgroundResource(R.drawable.bg_stepper_line); 
                }

                if (i == currentIndex) {
                    animateStep(dot);
                }
            } else {
                // Inactive state
                dot.setAlpha(0.3f);
                dot.setBackgroundResource(R.drawable.bg_stepper_dot);
                text.setAlpha(0.3f);
                text.setTypeface(null, android.graphics.Typeface.NORMAL);
                if (line != null) line.setAlpha(0.3f);
            }
        }
    }

    private int mapStatusToIndex(String status) {
        switch (status.toLowerCase()) {
            case "pending":
            case "confirmed":
                return 0;
            case "processing":
            case "preparing":
                return 1;
            case "ready for pickup":
            case "ready":
                return 2;
            case "shipping":
            case "out for delivery":
                return 3;
            case "delivered":
            case "completed":
                return 4;
            default:
                return 0;
        }
    }

    private void animateStep(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.8f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 1.0f);
        scaleX.setDuration(300);
        scaleY.setDuration(300);
        scaleX.start();
        scaleY.start();
    }

    private void updateStatusLabel(String status) {
        int statusResId;
        switch (status.toLowerCase()) {
            case "pending":
                statusResId = R.string.status_pending;
                break;
            case "confirmed":
                statusResId = R.string.order_tracking_confirmed;
                break;
            case "processing":
            case "preparing":
                statusResId = R.string.order_tracking_preparing;
                break;
            case "ready":
            case "ready for pickup":
                statusResId = R.string.order_tracking_ready_for_pickup;
                break;
            case "shipping":
            case "out for delivery":
                statusResId = R.string.order_tracking_out_for_delivery;
                break;
            case "delivered":
            case "completed":
                statusResId = R.string.order_tracking_delivered;
                break;
            default:
                statusResId = R.string.order_tracking_status_title;
        }
        txtShipperStatus.setText(getString(statusResId));
    }

    private void handleMapSimulation(String status) {
        if ("shipping".equalsIgnoreCase(status) || "out for delivery".equalsIgnoreCase(status)) {
            startMapAnimation();
        } else {
            stopMapAnimation();
        }
    }

    private void startMapAnimation() {
        if (deliveryDot == null) {
            deliveryDot = new View(this);
            int size = (int) (12 * getResources().getDisplayMetrics().density);
            deliveryDot.setLayoutParams(new FrameLayout.LayoutParams(size, size));
            deliveryDot.setBackgroundResource(R.drawable.bg_stepper_dot_active);
            mapContainer.addView(deliveryDot);
        }
        deliveryDot.setVisibility(View.VISIBLE);

        if (mapAnimator != null && mapAnimator.isRunning()) return;

        mapAnimator = ValueAnimator.ofFloat(0f, 1f);
        mapAnimator.setDuration(8000);
        mapAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mapAnimator.setInterpolator(new LinearInterpolator());
        mapAnimator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            int width = mapContainer.getWidth() - deliveryDot.getWidth();
            int height = mapContainer.getHeight() - deliveryDot.getHeight();
            if (width > 0 && height > 0) {
                deliveryDot.setX(width * fraction);
                deliveryDot.setY(height * fraction);
            }
        });
        mapAnimator.start();
    }

    private void stopMapAnimation() {
        if (mapAnimator != null) {
            mapAnimator.cancel();
            mapAnimator = null;
        }
        if (deliveryDot != null) {
            deliveryDot.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (orderRef != null && statusListener != null) {
            orderRef.removeEventListener(statusListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMapAnimation();
    }

    public void btnBackHome(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
