package com.teatrack_mcd_253eie502802_group02.client;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
    private String currentStatus = "";

    private View stepCircle1, stepCircle2, stepCircle3, stepCircle4, stepCircle5;
    private ImageView stepImg1, stepImg2, stepImg3, stepImg4, stepImg5;
    private View stepLine1, stepLine2, stepLine3, stepLine4;
    private TextView stepLabel1, stepLabel2, stepLabel3, stepLabel4, stepLabel5;
    private TextView txtOrderId, txtShipperStatus;
    private TextView txtRecipient, txtAddress, txtPayment;
    private FrameLayout mapContainer;
    private WebView webViewMap;
    private String currentMapUrl = null;
    private String lastLoadedAddress = null;
    private View deliveryDot;
    private ValueAnimator mapAnimator;

    private final List<View> icons = new ArrayList<>();
    private final List<ImageView> stepImages = new ArrayList<>();
    private final List<View> progressLines = new ArrayList<>();
    private final List<TextView> labels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_tracking);

        initViews();

        orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null) {
            orderId = getIntent().getStringExtra("orderId");
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
        webViewMap = findViewById(R.id.webViewMap);

        txtRecipient = findViewById(R.id.txtRecipient);
        txtAddress = findViewById(R.id.txtAddress);
        txtPayment = findViewById(R.id.txtPayment);

        stepCircle1 = findViewById(R.id.stepCircle1);
        stepCircle2 = findViewById(R.id.stepCircle2);
        stepCircle3 = findViewById(R.id.stepCircle3);
        stepCircle4 = findViewById(R.id.stepCircle4);
        stepCircle5 = findViewById(R.id.stepCircle5);
        icons.add(stepCircle1); icons.add(stepCircle2); icons.add(stepCircle3);
        icons.add(stepCircle4); icons.add(stepCircle5);

        stepImg1 = findViewById(R.id.stepImg1);
        stepImg2 = findViewById(R.id.stepImg2);
        stepImg3 = findViewById(R.id.stepImg3);
        stepImg4 = findViewById(R.id.stepImg4);
        stepImg5 = findViewById(R.id.stepImg5);
        stepImages.add(stepImg1); stepImages.add(stepImg2); stepImages.add(stepImg3);
        stepImages.add(stepImg4); stepImages.add(stepImg5);

        stepLine1 = findViewById(R.id.stepLine1);
        stepLine2 = findViewById(R.id.stepLine2);
        stepLine3 = findViewById(R.id.stepLine3);
        stepLine4 = findViewById(R.id.stepLine4);
        progressLines.add(stepLine1); progressLines.add(stepLine2);
        progressLines.add(stepLine3); progressLines.add(stepLine4);

        stepLabel1 = findViewById(R.id.stepLabel1);
        stepLabel2 = findViewById(R.id.stepLabel2);
        stepLabel3 = findViewById(R.id.stepLabel3);
        stepLabel4 = findViewById(R.id.stepLabel4);
        stepLabel5 = findViewById(R.id.stepLabel5);
        labels.add(stepLabel1); labels.add(stepLabel2); labels.add(stepLabel3);
        labels.add(stepLabel4); labels.add(stepLabel5);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (orderId != null && repository != null) {
            setupFirebaseListener();
        }
    }

    private void setupFirebaseListener() {
        Log.d("MapDebug", "setupFirebaseListener called, orderId: " + orderId);
        orderRef = repository.listenToOrder(orderId, null);
        statusListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("MapDebug", "onDataChange fired, exists: " + snapshot.exists() + ", key: " + snapshot.getKey());
                if (snapshot.exists()) {
                    String status = snapshot.child("status").getValue(String.class);
                    Log.d("MapDebug", "status value: " + status);
                    if (status != null) {
                        // Reset map nếu status thay đổi
                        if (!status.equals(currentStatus)) {
                            lastLoadedAddress = null;
                            currentMapUrl = null;
                        }
                        currentStatus = status;

                        updateStepper(status);
                        updateStatusLabel(status);
                        handleMapSimulation(status);
                        updateDeliveryInfo(snapshot);

                        String address = snapshot.child("customerAddress").getValue(String.class);
                        if (address != null) {
                            loadAgencyMapByAddress(address);
                        }
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
        animateStepper(currentIndex);

        for (int i = 0; i < icons.size(); i++) {
            TextView label = labels.get(i);
            if (i <= currentIndex) {
                label.setAlpha(1.0f);
                label.setTextColor(Color.parseColor("#1A1A1A"));
                label.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                label.setAlpha(0.5f);
                label.setTextColor(Color.parseColor("#999999"));
                label.setTypeface(null, android.graphics.Typeface.NORMAL);
            }
        }
    }

    private void animateStepper(int newStepIndex) {
        for (int i = 0; i < icons.size(); i++) {
            View circleView = icons.get(i);
            ImageView imgView = stepImages.get(i);
            circleView.animate().cancel();
            long delay = i * 100L;

            if (i <= newStepIndex) {
                circleView.setBackgroundResource(R.drawable.bg_step_circle_active);
                if (imgView != null) {
                    imgView.setColorFilter(Color.WHITE);
                }
                if (i == newStepIndex) {
                    ObjectAnimator scaleX = ObjectAnimator.ofFloat(circleView, "scaleX", 1f, 1.2f, 1f);
                    ObjectAnimator scaleY = ObjectAnimator.ofFloat(circleView, "scaleY", 1f, 1.2f, 1f);
                    AnimatorSet set = new AnimatorSet();
                    set.playTogether(scaleX, scaleY);
                    set.setDuration(600);
                    set.setInterpolator(new OvershootInterpolator(2f));
                    set.setStartDelay(delay);
                    set.start();
                } else {
                    circleView.setScaleX(1f);
                    circleView.setScaleY(1f);
                }
            } else {
                circleView.setBackgroundResource(R.drawable.bg_step_circle_inactive);
                if (imgView != null) {
                    imgView.setColorFilter(Color.parseColor("#BDBDBD"));
                }
                circleView.setScaleX(1f);
                circleView.setScaleY(1f);
            }
        }

        for (int i = 0; i < progressLines.size(); i++) {
            View line = progressLines.get(i);
            line.animate().cancel();
            line.setPivotX(0f);
            long delay = i * 100L;
            if (i < newStepIndex) {
                line.animate().scaleX(1.0f).setDuration(500).setStartDelay(delay).start();
            } else {
                line.animate().scaleX(0f).setDuration(300).setStartDelay(delay).start();
            }
        }
    }

    private int mapStatusToIndex(String status) {
        if (status == null) return 0;
        switch (status.toLowerCase()) {
            case "pending":
            case "confirmed":
                return 0;
            case "processing":
            case "preparing":
                return 1;
            case "ready":
            case "ready for pickup":
                return 2;
            case "shipping":
            case "out for delivery":
                return 3;
            case "completed":
            case "delivered":
                return 4;
            default:
                return 0;
        }
    }

    private void updateStatusLabel(String status) {
        if (status == null) return;
        int statusResId;
        switch (status.toLowerCase()) {
            case "pending":
            case "confirmed":
                statusResId = R.string.order_tracking_pending;
                break;
            case "processing":
            case "preparing":
                statusResId = R.string.order_tracking_processing;
                break;
            case "ready":
            case "ready for pickup":
                statusResId = R.string.order_tracking_ready;
                break;
            case "shipping":
            case "out for delivery":
                statusResId = R.string.order_tracking_shipping;
                break;
            case "completed":
            case "delivered":
                statusResId = R.string.order_tracking_completed;
                break;
            default:
                statusResId = R.string.order_tracking_status_title;
        }
        txtShipperStatus.setText(getString(statusResId));
    }

    private void updateDeliveryInfo(DataSnapshot snapshot) {
        String recipient = snapshot.child("customerName").getValue(String.class);
        String address = snapshot.child("customerAddress").getValue(String.class);
        String payment = snapshot.child("paymentMethod").getValue(String.class);

        if (recipient != null) txtRecipient.setText(recipient);
        if (address != null) txtAddress.setText(address);
        if (payment != null) {
            if ("cash".equalsIgnoreCase(payment)) {
                txtPayment.setText(getString(R.string.order_tracking_cash));
            } else {
                txtPayment.setText(payment);
            }
        }
    }

    private double[] parseLatLngFromEmbedUrl(String embedUrl) {
        try {
            java.util.regex.Pattern lonPattern =
                    java.util.regex.Pattern.compile("!2d([\\d.-]+)");
            java.util.regex.Pattern latPattern =
                    java.util.regex.Pattern.compile("!3d([\\d.-]+)");

            java.util.regex.Matcher lonMatcher = lonPattern.matcher(embedUrl);
            java.util.regex.Matcher latMatcher = latPattern.matcher(embedUrl);

            if (lonMatcher.find() && latMatcher.find()) {
                double lng = Double.parseDouble(lonMatcher.group(1));
                double lat = Double.parseDouble(latMatcher.group(1));
                return new double[]{lat, lng};
            }
        } catch (Exception e) {
            Log.e("OrderTracking", "parseLatLng error: " + e.getMessage());
        }
        return null;
    }

    private String buildMapHtml(double lat, double lng, String branchName, boolean isShipping) {
        return "<!DOCTYPE html><html><head>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<style>"
                + "* { margin:0; padding:0; box-sizing:border-box; }"
                + "body { width:100vw; height:100vh; overflow:hidden; position:relative; }"
                + "iframe { position:absolute; top:0; left:0; width:100%; height:100%; border:none; z-index:1; }"
                + "canvas { position:absolute; top:0; left:0; width:100%; height:100%; z-index:2; pointer-events:none; }"
                + "</style>"
                + "</head><body>"
                + "<iframe src='https://maps.google.com/maps?q=" + lat + "," + lng + "&z=16&output=embed' allowfullscreen></iframe>"
                + "<canvas id='c'></canvas>"
                + "<script>"
                + "const canvas = document.getElementById('c');"
                + "const ctx = canvas.getContext('2d');"
                + "canvas.width = window.innerWidth;"
                + "canvas.height = window.innerHeight;"
                + "const W = canvas.width, H = canvas.height;"
                + "const isShipping = " + isShipping + ";"
                + "const points = ["
                + "  {x: W*0.15, y: H*0.82},"
                + "  {x: W*0.15, y: H*0.55},"
                + "  {x: W*0.45, y: H*0.55},"
                + "  {x: W*0.45, y: H*0.28},"
                + "  {x: W*0.75, y: H*0.28}"
                + "];"
                + "let totalLen = 0;"
                + "const segLens = [];"
                + "for(let i=1;i<points.length;i++){"
                + "  const dx=points[i].x-points[i-1].x, dy=points[i].y-points[i-1].y;"
                + "  const l=Math.sqrt(dx*dx+dy*dy);"
                + "  segLens.push(l); totalLen+=l;"
                + "}"
                + "function getPosAtProgress(p){"
                + "  let dist=p*totalLen, acc=0;"
                + "  for(let i=0;i<segLens.length;i++){"
                + "    if(dist<=acc+segLens[i]){"
                + "      const t=(dist-acc)/segLens[i];"
                + "      return {x:points[i].x+(points[i+1].x-points[i].x)*t,"
                + "              y:points[i].y+(points[i+1].y-points[i].y)*t};"
                + "    }"
                + "    acc+=segLens[i];"
                + "  }"
                + "  return points[points.length-1];"
                + "}"
                + "function drawRoute(progress){"
                + "  ctx.beginPath();"
                + "  ctx.moveTo(points[0].x,points[0].y);"
                + "  for(let i=1;i<points.length;i++) ctx.lineTo(points[i].x,points[i].y);"
                + "  ctx.strokeStyle='rgba(255,255,255,0.3)';"
                + "  ctx.lineWidth=5; ctx.lineCap='round'; ctx.lineJoin='round';"
                + "  ctx.setLineDash([8,6]); ctx.stroke(); ctx.setLineDash([]);"
                + "  if(!isShipping) return;"
                + "  ctx.beginPath();"
                + "  ctx.moveTo(points[0].x,points[0].y);"
                + "  let acc2=0;"
                + "  for(let i=0;i<segLens.length;i++){"
                + "    const dist=progress*totalLen;"
                + "    if(acc2+segLens[i]>=dist){"
                + "      const t=(dist-acc2)/segLens[i];"
                + "      const x=points[i].x+(points[i+1].x-points[i].x)*t;"
                + "      const y=points[i].y+(points[i+1].y-points[i].y)*t;"
                + "      ctx.lineTo(x,y); break;"
                + "    }"
                + "    ctx.lineTo(points[i+1].x,points[i+1].y);"
                + "    acc2+=segLens[i];"
                + "  }"
                + "  ctx.strokeStyle='#FF6B35';"
                + "  ctx.lineWidth=5; ctx.lineCap='round'; ctx.lineJoin='round';"
                + "  ctx.stroke();"
                + "}"
                + "function drawDestination(t){"
                + "  const dest=points[points.length-1];"
                + "  const pulse=1+0.3*Math.sin(t*0.05);"
                + "  ctx.beginPath();"
                + "  ctx.arc(dest.x,dest.y,20*pulse,0,Math.PI*2);"
                + "  ctx.fillStyle='rgba(255,107,53,0.2)'; ctx.fill();"
                + "  ctx.beginPath();"
                + "  ctx.arc(dest.x,dest.y,12,0,Math.PI*2);"
                + "  ctx.fillStyle='#FF6B35'; ctx.fill();"
                + "  ctx.beginPath();"
                + "  ctx.arc(dest.x,dest.y,6,0,Math.PI*2);"
                + "  ctx.fillStyle='#fff'; ctx.fill();"
                + "}"
                + "function drawShipper(pos){"
                + "  ctx.beginPath();"
                + "  ctx.arc(pos.x+2,pos.y+2,16,0,Math.PI*2);"
                + "  ctx.fillStyle='rgba(0,0,0,0.3)'; ctx.fill();"
                + "  ctx.beginPath();"
                + "  ctx.arc(pos.x,pos.y,16,0,Math.PI*2);"
                + "  ctx.fillStyle='#2d2d2d'; ctx.fill();"
                + "  ctx.beginPath();"
                + "  ctx.moveTo(pos.x,pos.y-8);"
                + "  ctx.lineTo(pos.x+6,pos.y+5);"
                + "  ctx.lineTo(pos.x-6,pos.y+5);"
                + "  ctx.closePath();"
                + "  ctx.fillStyle='#fff'; ctx.fill();"
                + "}"
                + "let frame=0, duration=300;"
                + "function loop(){"
                + "  ctx.clearRect(0,0,W,H);"
                + "  const progress = isShipping ? (frame % duration) / duration : 0;"
                + "  drawRoute(progress);"
                + "  drawDestination(frame);"
                + "  const pos = getPosAtProgress(progress);"
                + "  drawShipper(pos);"
                + "  frame++;"
                + "  requestAnimationFrame(loop);"
                + "}"
                + "loop();"
                + "</script>"
                + "</body></html>";

    }

    private void loadAgencyMapByAddress(String pickupAddress) {
        if (pickupAddress == null || pickupAddress.isEmpty() || pickupAddress.equals(lastLoadedAddress)) return;

        com.google.firebase.database.DatabaseReference agenciesRef =
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference("agencies");

        agenciesRef.get().addOnSuccessListener(snapshot -> {
            Log.d("MapDebug", "agencies count: " + snapshot.getChildrenCount());
            for (com.google.firebase.database.DataSnapshot agency : snapshot.getChildren()) {
                String address = agency.child("address").getValue(String.class);
                Log.d("MapDebug", "checking agency: " + agency.getKey() + " | address: " + address);
                if (address != null) {
                    String[] pickupWords = pickupAddress.replaceAll("[^0-9]", " ").trim().split("\\s+");
                    String[] agencyWords = address.replaceAll("[^0-9]", " ").trim().split("\\s+");

                    boolean matched = false;
                    for (String pNum : pickupWords) {
                        if (pNum.length() >= 3) {
                            for (String aNum : agencyWords) {
                                if (pNum.equals(aNum)) {
                                    matched = true;
                                    break;
                                }
                            }
                        }
                        if (matched) break;
                    }

                    if (matched) {
                        Log.d("MapDebug", "MATCH: " + agency.getKey());
                        lastLoadedAddress = pickupAddress;
                        String mapEmbed = agency.child("mapEmbed").getValue(String.class);
                        String branchName = agency.child("name").getValue(String.class);
                        if (branchName == null) branchName = "Chi nhánh";
                        final String finalBranchName = branchName;

                        if (mapEmbed != null && !mapEmbed.equals(currentMapUrl)) {
                            currentMapUrl = mapEmbed;
                            double[] latLng = parseLatLngFromEmbedUrl(mapEmbed);

                            if (latLng != null) {
                                final boolean shipping = "shipping".equalsIgnoreCase(currentStatus)
                                        || "out for delivery".equalsIgnoreCase(currentStatus);
                                runOnUiThread(() -> {
                                    configureWebView();
                                    String html = buildMapHtml(latLng[0], latLng[1], finalBranchName, shipping);
                                    Log.d("MapDebug", "latLng: " + latLng[0] + ", " + latLng[1]);
                                    Log.d("MapDebug", "html length: " + html.length());
                                    Log.d("MapDebug", "webViewMap null? " + (webViewMap == null));
                                    webViewMap.loadDataWithBaseURL(
                                            "https://openstreetmap.org",
                                            html,
                                            "text/html",
                                            "UTF-8",
                                            null
                                    );
                                });
                            }
                        }
                        break;
                    }
                }
            }
        }).addOnFailureListener(e ->
                Log.e("OrderTracking", "loadAgencyMapByAddress error: " + e.getMessage())
        );
    }

    private void configureWebView() {
        WebSettings settings = webViewMap.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        webViewMap.setWebChromeClient(new WebChromeClient());
        webViewMap.setWebViewClient(new WebViewClient());
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