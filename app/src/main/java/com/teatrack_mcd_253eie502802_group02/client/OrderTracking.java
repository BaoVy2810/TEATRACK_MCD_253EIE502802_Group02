package com.teatrack_mcd_253eie502802_group02.client;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
                        if (!status.equals(currentStatus)) {
                            lastLoadedAddress = null;
                            currentMapUrl = null;
                        }
                        currentStatus = status;

                        updateStepper(status);
                        updateStatusLabel(status);
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

    private String buildMapHtml(double lat, double lng, boolean isShipping, boolean isCompleted) {
        return "<!DOCTYPE html><html><head>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<style>"
                + "* { margin:0; padding:0; box-sizing:border-box; }"
                + "body { width:100vw; height:100vh; overflow:hidden; position:relative; background:#f5f0e8; }"
                + "iframe { position:absolute; top:0; left:0; width:100%; height:100%; border:none; z-index:1; }"
                + "canvas { position:absolute; top:0; left:0; width:100%; height:100%; z-index:2; pointer-events:none; }"
                + "</style>"
                + "</head><body>"
                + "<iframe src='https://maps.google.com/maps?q=" + lat + "," + lng + "&z=16&output=embed' allowfullscreen></iframe>"
                + "<canvas id='c'></canvas>"
                + "<script>"
                + "const canvas = document.getElementById('c');"
                + "const ctx = canvas.getContext('2d');"
                + "function resize() { canvas.width = window.innerWidth; canvas.height = window.innerHeight; }"
                + "resize();"
                + "window.addEventListener('resize', resize);"
                + "const isShipping = " + isShipping + ";"
                + "const isCompleted = " + isCompleted + ";"
                + "const CENTER_LAT = " + lat + ";"
                + "const CENTER_LNG = " + lng + ";"
                + "const ZOOM = 16;"

                // Mercator projection: lat/lng → world pixel
                + "function lngLatToWorld(lat, lng) {"
                + "  const TILE = 256;"
                + "  const scale = TILE * Math.pow(2, ZOOM);"
                + "  const x = (lng + 180) / 360 * scale;"
                + "  const sinLat = Math.sin(lat * Math.PI / 180);"
                + "  const y = (0.5 - Math.log((1 + sinLat) / (1 - sinLat)) / (4 * Math.PI)) * scale;"
                + "  return {x, y};"
                + "}"

                // Convert lat/lng → canvas pixel relative to center
                + "function lngLatToCanvas(lat, lng) {"
                + "  const W = canvas.width, H = canvas.height;"
                + "  const center = lngLatToWorld(CENTER_LAT, CENTER_LNG);"
                + "  const pt = lngLatToWorld(lat, lng);"
                + "  return { x: W/2 + (pt.x - center.x), y: H/2 + (pt.y - center.y) };"
                + "}"

                // Route: điểm đầu = vị trí chi nhánh trên map, sau đó offset mô phỏng
                + "function getPoints() {"
                + "  const W = canvas.width, H = canvas.height;"
                + "  const origin = lngLatToCanvas(CENTER_LAT, CENTER_LNG);"
                + "  const ox = origin.x, oy = origin.y;"
                + "  return ["
                + "    {x: ox,            y: oy},"
                + "    {x: ox,            y: oy - H*0.18},"
                + "    {x: ox + W*0.25,   y: oy - H*0.18},"
                + "    {x: ox + W*0.40,   y: oy - H*0.35}"
                + "  ];"
                + "}"

                // Compute segment lengths + total
                + "function buildSegments(points) {"
                + "  let total = 0; const segs = [];"
                + "  for (let i = 1; i < points.length; i++) {"
                + "    const dx = points[i].x - points[i-1].x, dy = points[i].y - points[i-1].y;"
                + "    const l = Math.sqrt(dx*dx + dy*dy);"
                + "    segs.push(l); total += l;"
                + "  }"
                + "  return {segs, total};"
                + "}"

                // Position along route at progress 0..1
                + "function posAtProgress(points, segs, total, p) {"
                + "  let dist = p * total, acc = 0;"
                + "  for (let i = 0; i < segs.length; i++) {"
                + "    if (dist <= acc + segs[i]) {"
                + "      const t = (dist - acc) / segs[i];"
                + "      return { x: points[i].x + (points[i+1].x - points[i].x) * t,"
                + "               y: points[i].y + (points[i+1].y - points[i].y) * t };"
                + "    }"
                + "    acc += segs[i];"
                + "  }"
                + "  return points[points.length - 1];"
                + "}"

                // Draw full route: xám khi chưa shipping, xám + xanh #0088FF khi shipping
                // Vẽ path cong tại các góc gấp khúc (quadraticCurveTo bo góc)
                + "function buildCurvedPath(ctx, points, r) {"
                + "  ctx.moveTo(points[0].x, points[0].y);"
                + "  for (let i = 1; i < points.length - 1; i++) {"
                + "    const prev = points[i-1], cur = points[i], next = points[i+1];"
                + "    const d1 = Math.sqrt((cur.x-prev.x)**2+(cur.y-prev.y)**2);"
                + "    const d2 = Math.sqrt((next.x-cur.x)**2+(next.y-cur.y)**2);"
                + "    const t1 = Math.min(r, d1/2) / d1;"
                + "    const t2 = Math.min(r, d2/2) / d2;"
                + "    const bx = cur.x - (cur.x-prev.x)*t1, by = cur.y - (cur.y-prev.y)*t1;"
                + "    const ex = cur.x + (next.x-cur.x)*t2, ey = cur.y + (next.y-cur.y)*t2;"
                + "    ctx.lineTo(bx, by);"
                + "    ctx.quadraticCurveTo(cur.x, cur.y, ex, ey);"
                + "  }"
                + "  const last = points[points.length-1];"
                + "  ctx.lineTo(last.x, last.y);"
                + "}"

                + "function drawRoute(points, segs, total, progress) {"
                + "  const r = 28;"
                // Nền xám toàn bộ route
                + "  ctx.beginPath();"
                + "  buildCurvedPath(ctx, points, r);"
                + "  ctx.strokeStyle = '#CCCCCC';"
                + "  ctx.lineWidth = 3;"
                + "  ctx.lineCap = 'round';"
                + "  ctx.lineJoin = 'round';"
                + "  ctx.setLineDash([8, 5]);"
                + "  ctx.stroke();"
                + "  ctx.setLineDash([]);"
                // Phần đã đi qua — clip theo progress, chỉ vẽ khi isShipping
                + "  if (!isShipping || progress <= 0) return;"
                + "  let acc = 0, drawn = false;"
                + "  const targetDist = progress * total;"
                + "  const partialPoints = [points[0]];"
                + "  for (let i = 0; i < segs.length; i++) {"
                + "    if (acc + segs[i] >= targetDist) {"
                + "      const t = (targetDist - acc) / segs[i];"
                + "      partialPoints.push({"
                + "        x: points[i].x + (points[i+1].x - points[i].x) * t,"
                + "        y: points[i].y + (points[i+1].y - points[i].y) * t"
                + "      });"
                + "      drawn = true; break;"
                + "    }"
                + "    partialPoints.push(points[i+1]);"
                + "    acc += segs[i];"
                + "  }"
                + "  if (!drawn) partialPoints.push(points[points.length-1]);"
                + "  ctx.beginPath();"
                + "  buildCurvedPath(ctx, partialPoints, r);"
                + "  ctx.strokeStyle = '#0088FF';"
                + "  ctx.lineWidth = 3;"
                + "  ctx.lineCap = 'round';"
                + "  ctx.lineJoin = 'round';"
                + "  ctx.stroke();"
                + "}"

                // Destination pin
                + "function drawDestinationPin(x, y, t) {"
                + "  const pulse = 1 + 0.15 * Math.sin(t * 0.06);"
                + "  ctx.beginPath();"
                + "  ctx.arc(x, y, 26 * pulse, 0, Math.PI*2);"
                + "  ctx.fillStyle = 'rgba(0,136,255,0.15)';"
                + "  ctx.fill();"
                + "  ctx.beginPath();"
                + "  ctx.arc(x, y, 17, 0, Math.PI*2);"
                + "  ctx.fillStyle = '#0088FF';"
                + "  ctx.shadowColor = 'rgba(0,136,255,0.5)';"
                + "  ctx.shadowBlur = 8;"
                + "  ctx.fill();"
                + "  ctx.shadowBlur = 0;"
                + "  const px = x, py = y - 2;"
                + "  ctx.beginPath();"
                + "  ctx.arc(px, py - 3, 5, 0, Math.PI*2);"
                + "  ctx.fillStyle = '#fff';"
                + "  ctx.fill();"
                + "  ctx.beginPath();"
                + "  ctx.moveTo(px - 4, py - 3);"
                + "  ctx.quadraticCurveTo(px - 4, py + 2, px, py + 7);"
                + "  ctx.quadraticCurveTo(px + 4, py + 2, px + 4, py - 3);"
                + "  ctx.fillStyle = '#0088FF';"
                + "  ctx.fill();"
                + "  ctx.beginPath();"
                + "  ctx.arc(px, py - 3, 5, 0, Math.PI*2);"
                + "  ctx.fillStyle = '#fff';"
                + "  ctx.fill();"
                + "  ctx.beginPath();"
                + "  ctx.arc(px, py - 3, 2, 0, Math.PI*2);"
                + "  ctx.fillStyle = '#0088FF';"
                + "  ctx.fill();"
                + "}"

                // Shipper navigation icon
                + "function drawShipperIcon(x, y, angle) {"
                + "  ctx.beginPath();"
                + "  ctx.arc(x, y, 22, 0, Math.PI*2);"
                + "  ctx.fillStyle = 'rgba(0,136,255,0.2)';"
                + "  ctx.fill();"
                + "  ctx.beginPath();"
                + "  ctx.arc(x, y, 15, 0, Math.PI*2);"
                + "  ctx.fillStyle = '#0088FF';"
                + "  ctx.shadowColor = 'rgba(0,0,0,0.25)';"
                + "  ctx.shadowBlur = 6;"
                + "  ctx.fill();"
                + "  ctx.shadowBlur = 0;"
                + "  ctx.save();"
                + "  ctx.translate(x, y);"
                + "  ctx.rotate(angle);"
                + "  ctx.beginPath();"
                + "  ctx.moveTo(0, -7);"
                + "  ctx.lineTo(5, 5);"
                + "  ctx.lineTo(0, 2);"
                + "  ctx.lineTo(-5, 5);"
                + "  ctx.closePath();"
                + "  ctx.fillStyle = '#fff';"
                + "  ctx.fill();"
                + "  ctx.restore();"
                + "}"

                // Angle of travel
                + "function angleAtProgress(points, segs, total, p) {"
                + "  const p2 = Math.min(p + 0.01, 1);"
                + "  const a = posAtProgress(points, segs, total, p);"
                + "  const b = posAtProgress(points, segs, total, p2);"
                + "  return Math.atan2(b.y - a.y, b.x - a.x) + Math.PI / 2;"
                + "}"

                + "let frame = 0;"
                + "const DURATION = 360;"
                + "function loop() {"
                + "  ctx.clearRect(0, 0, canvas.width, canvas.height);"
                + "  const points = getPoints();"
                + "  const {segs, total} = buildSegments(points);"
                + "  const progress = isCompleted ? 1 : (isShipping ? (frame % DURATION) / DURATION : 0);"
                + "  drawRoute(points, segs, total, progress);"
                + "  const dest = points[points.length - 1];"
                + "  drawDestinationPin(dest.x, dest.y, frame);"
                + "  if (isShipping) {"
                + "    const pos = posAtProgress(points, segs, total, progress);"
                + "    const angle = angleAtProgress(points, segs, total, progress);"
                + "    drawShipperIcon(pos.x, pos.y, angle);"
                + "  }"
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

                        if (mapEmbed != null && !mapEmbed.equals(currentMapUrl)) {
                            currentMapUrl = mapEmbed;
                            double[] latLng = parseLatLngFromEmbedUrl(mapEmbed);

                            if (latLng != null) {
                                runOnUiThread(() -> {
                                    configureWebView();
                                    boolean isShipping = "shipping".equalsIgnoreCase(currentStatus)
                                            || "out for delivery".equalsIgnoreCase(currentStatus);
                                    boolean isCompleted = "completed".equalsIgnoreCase(currentStatus)
                                            || "delivered".equalsIgnoreCase(currentStatus);
                                    String html = buildMapHtml(latLng[0], latLng[1], isShipping, isCompleted);
                                    Log.d("MapDebug", "latLng: " + latLng[0] + ", " + latLng[1]);
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
        webViewMap.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, android.webkit.WebResourceRequest request) {
                return true; // block tất cả link
            }
        });
        webViewMap.setOnTouchListener((v, event) -> true); // disable touch interaction
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (orderRef != null && statusListener != null) {
            orderRef.removeEventListener(statusListener);
        }
    }

    public void btnBackHome(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}