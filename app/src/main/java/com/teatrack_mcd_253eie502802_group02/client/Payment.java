package com.teatrack_mcd_253eie502802_group02.client;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.data.OrderCheckoutFlow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Payment extends AppCompatActivity {

    private static final int PAYMENT_CASH_IN_BANK = 1;
    private static final int PAYMENT_MOMO = 2;
    private static final int PAYMENT_ZALOPAY = 3;
    private static final int PAYMENT_EWALLET = 4;

    private int method;
    private String pickupAddress;
    private String agencyId;
    private String recipientDetails;
    private String note;
    private TextView tvTimer, tvTopTitle;
    private View viewScanningLine;
    private ObjectAnimator scanningAnimator;
    private int secondsRemaining = 5;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (secondsRemaining > 0) {
                if (tvTimer != null) {
                    tvTimer.setText(getString(R.string.payment_waiting_format, secondsRemaining));
                }
                secondsRemaining--;
                handler.postDelayed(this, 3000);
            } else {
                completePayment();
            }
        }
    };

    // drawable mipmap name, label
    private final int[][] bankLogos = {
            {R.mipmap.vib, 0}, {R.mipmap.tech, 0}, {R.mipmap.acb, 0},
            {R.mipmap.scb, 0}, {R.mipmap.vcb, 0}, {R.mipmap.bidv, 0},
            {R.mipmap.agri, 0}, {R.mipmap.mb, 0}, {R.mipmap.no, 0},
            {R.mipmap.lp, 0}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        method = getIntent().getIntExtra("method", PAYMENT_MOMO);
        pickupAddress = getIntent().getStringExtra("pickupAddress");
        agencyId = getIntent().getStringExtra("agencyId");
        recipientDetails = getIntent().getStringExtra("recipientDetails");
        note = getIntent().getStringExtra("note");
        findViewById(R.id.btnPaymentBack).setOnClickListener(v -> finish());

        tvTopTitle = findViewById(R.id.tvTopTitle);
        tvTimer = findViewById(R.id.tvTimer);
        viewScanningLine = findViewById(R.id.viewScanningLine);

        MaterialCardView cardQr = findViewById(R.id.cardQr);
        ImageView ivQrCode = findViewById(R.id.ivQrCode);
        TextView tvInstructions = findViewById(R.id.tvPaymentInstructions);
        TextView tvBankTitle = findViewById(R.id.tvBankSupportTitle);
        GridLayout gridBankSupport = findViewById(R.id.gridBankSupport);

        int bgColor;
        int qrRes;
        String instructions;
        String title;
        boolean showBankGrid = false;

        switch (method) {
            case PAYMENT_ZALOPAY:
                bgColor = ContextCompat.getColor(this, R.color.payment_zalopay_blue);
                qrRes = R.mipmap.qrzalo;
                instructions = getString(R.string.payment_instructions_zalopay);
                title = getString(R.string.payment_title_zalopay);
                break;
            case PAYMENT_CASH_IN_BANK:
            case PAYMENT_EWALLET:
                bgColor = ContextCompat.getColor(this, R.color.payment_bank_blue);
                qrRes = R.mipmap.qrbanking;
                instructions = getString(R.string.payment_instructions_bank);
                title = getString(R.string.payment_title_bank);
                showBankGrid = true;
                break;
            case PAYMENT_MOMO:
            default:
                bgColor = ContextCompat.getColor(this, R.color.payment_momo_pink);
                qrRes = R.mipmap.qrcode;
                instructions = getString(R.string.payment_instructions_momo);
                title = getString(R.string.payment_title_momo);
                break;
        }

        tvTopTitle.setText(title);
        cardQr.setCardBackgroundColor(bgColor);
        ivQrCode.setImageResource(qrRes);
        tvInstructions.setText(instructions);

        findViewById(R.id.btnDownloadQr).setOnClickListener(v -> saveQrToGallery(cardQr));
        findViewById(R.id.btnCustomizeQr).setOnClickListener(v -> {
            Intent intent = new Intent(Payment.this, PageNotFound.class);
            startActivity(intent);
        });
        findViewById(R.id.btnShareQr).setOnClickListener(v -> shareQrImage(cardQr));

        if (showBankGrid) {
            tvBankTitle.setVisibility(android.view.View.VISIBLE);
            gridBankSupport.setVisibility(android.view.View.VISIBLE);
            populateBankGrid(gridBankSupport);
        } else {
            tvBankTitle.setVisibility(android.view.View.GONE);
            gridBankSupport.setVisibility(android.view.View.GONE);
        }

        startScanningAnimation();
        handler.post(timerRunnable);
    }

    private void startScanningAnimation() {
        if (viewScanningLine == null) return;

        viewScanningLine.post(() -> {
            float startY = 0f;
            float endY = ((View) viewScanningLine.getParent()).getHeight() - viewScanningLine.getHeight();

            scanningAnimator = ObjectAnimator.ofFloat(viewScanningLine, "translationY", startY, endY);
            scanningAnimator.setDuration(2000);
            scanningAnimator.setRepeatCount(ValueAnimator.INFINITE);
            scanningAnimator.setRepeatMode(ValueAnimator.REVERSE);
            scanningAnimator.setInterpolator(new LinearInterpolator());
            scanningAnimator.start();
        });
    }

    private void populateBankGrid(GridLayout grid) {
        grid.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int[] logo : bankLogos) {
            MaterialCardView item = (MaterialCardView) inflater.inflate(R.layout.item_bank_icon, grid, false);
            ImageView iv = (ImageView) item.getChildAt(0);
            iv.setImageResource(logo[0]);
            grid.addView(item);
        }
    }

    private void completePayment() {
        Toast.makeText(this, "Payment received! Redirecting...", Toast.LENGTH_SHORT).show();
        OrderCheckoutFlow.placeOrderAndOpenCheckout(
                this,
                method,
                pickupAddress,
                agencyId,
                recipientDetails,
                note,
                true,
                null
        );
    }

    private void saveQrToGallery(View view) {
        Bitmap bitmap = createBitmapFromView(view);
        String fileName = "QR_Payment_" + System.currentTimeMillis() + ".png";

        OutputStream fos;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TeaTrack");
                Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = getContentResolver().openOutputStream(imageUri);
            } else {
                File imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File image = new File(imagesDir, fileName);
                fos = new FileOutputStream(image);
            }

            if (fos != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                Toast.makeText(this, "QR Code saved to gallery!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareQrImage(View view) {
        Bitmap bitmap = createBitmapFromView(view);
        try {
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "shared_qr.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);

            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, "Share QR Code via"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to share QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap createBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (scanningAnimator != null) {
            scanningAnimator.cancel();
        }
        super.onDestroy();
    }
}