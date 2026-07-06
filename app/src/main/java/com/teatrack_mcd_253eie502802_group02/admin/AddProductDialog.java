package com.teatrack_mcd_253eie502802_group02.admin;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.Product;
import com.teatrack_mcd_253eie502802_group02.util.CloudinaryHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class AddProductDialog {

    private final Context context;
    private final List<String> categories;
    private Uri selectedImageUri;
    private Uri cameraImageUri;
    private ImageView ivMainPreview;
    private View llPlaceholder;
    private String uploadedImageUrl = "logo_ngo_gia.png"; // Mặc định
    private final DecimalFormat priceFormatter = new DecimalFormat("#,###");
    private String selectedCategory;

    public AddProductDialog(Context context, List<String> categories) {
        this.context = context;
        this.categories = categories;
        if (categories != null && !categories.isEmpty()) {
            selectedCategory = categories.get(0).equals(context.getString(R.string.filter_all)) && categories.size() > 1 
                    ? categories.get(1) : categories.get(0);
        } else {
            selectedCategory = context.getString(R.string.pure_tea);
        }
    }

    public void show() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_product);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.92);
            dialog.getWindow().setAttributes(params);
            dialog.getWindow().setWindowAnimations(android.R.style.Animation_Dialog);
        }

        EditText etProductId = dialog.findViewById(R.id.etProductId);
        EditText etProductName = dialog.findViewById(R.id.etProductName);
        TextView tvCategorySelect = dialog.findViewById(R.id.tvCategorySelect);
        CheckBox cbVisible = dialog.findViewById(R.id.cbVisible);
        CheckBox cbSpecial = dialog.findViewById(R.id.cbSpecial);
        EditText etPriceM = dialog.findViewById(R.id.etPriceM);
        EditText etPriceL = dialog.findViewById(R.id.etPriceL);
        EditText etVipPriceM = dialog.findViewById(R.id.etVipPriceM);
        EditText etVipPriceL = dialog.findViewById(R.id.etVipPriceL);
        EditText etProductInfo = dialog.findViewById(R.id.etProductInfo);
        EditText etProductDesc = dialog.findViewById(R.id.etProductDesc);
        ImageButton btnClose = dialog.findViewById(R.id.btnClose);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancel);
        MaterialButton btnAdd = dialog.findViewById(R.id.btnAdd);

        // Setup Price Formatters
        setupPriceFormatting(etPriceM);
        setupPriceFormatting(etPriceL);
        setupPriceFormatting(etVipPriceM);
        setupPriceFormatting(etVipPriceL);
        setupNestedDescriptionScroll(etProductDesc);

        // Default image for new product
        setupImageUpload(dialog);

        // Setup Category Select
        if (tvCategorySelect != null) {
            tvCategorySelect.setText(selectedCategory);
            tvCategorySelect.setOnClickListener(v -> showCategoryPopup(v));
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAdd.setOnClickListener(v -> {
            String id = etProductId.getText().toString().trim();
            String name = etProductName.getText().toString().trim();

            if (id.isEmpty() || name.isEmpty()) {
                Toast.makeText(context, "Please fill in ID and Name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedImageUri != null) {
                uploadImageAndSaveProduct(dialog, id, name, selectedCategory, cbVisible, cbSpecial,
                        etPriceM, etPriceL, etVipPriceM, etVipPriceL, etProductInfo, etProductDesc);
            } else {
                saveToFirebase(id, name, selectedCategory, cbVisible, cbSpecial,
                        etPriceM, etPriceL, etVipPriceM, etVipPriceL, etProductInfo, etProductDesc, uploadedImageUrl);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showCategoryPopup(View anchor) {
        List<String> popupCats = new ArrayList<>(categories);
        popupCats.remove(context.getString(R.string.filter_all));
        if (popupCats.isEmpty()) {
            popupCats.add(context.getString(R.string.pure_tea));
            popupCats.add(context.getString(R.string.milk_tea));
            popupCats.add(context.getString(R.string.tea_latte));
            popupCats.add(context.getString(R.string.fruit_tea));
        }

        View popupView = LayoutInflater.from(context).inflate(R.layout.dialog_category_selector, null);
        PopupWindow popupWindow = new PopupWindow(popupView,
                anchor.getWidth(),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(10);

        RecyclerView rvCategories = popupView.findViewById(R.id.rvCategoryList);
        if (rvCategories != null) {
            rvCategories.setLayoutManager(new LinearLayoutManager(context));
            rvCategories.setAdapter(new CategoryDialogAdapter(popupCats, selectedCategory, category -> {
                selectedCategory = category;
                ((TextView) anchor).setText(selectedCategory);
                popupWindow.dismiss();
            }));
        }

        popupWindow.showAsDropDown(anchor, 0, 5);
    }

    private void setupImageUpload(Dialog dialog) {
        int[] imageSlotIds = {R.id.imgSlot1, R.id.imgSlot2, R.id.imgSlot3, R.id.imgSlot4};
        View slotView = dialog.findViewById(imageSlotIds[0]);
        if (slotView != null) {
            ivMainPreview = slotView.findViewById(R.id.ivProductImage);
            llPlaceholder = slotView.findViewById(R.id.llUploadPlaceholder);

            slotView.setOnClickListener(v -> showImageSourceDialog());
        }
    }

    private void showImageSourceDialog() {
        String[] options = {"Camera", "Gallery"};
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Select Image Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, 1001);
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraImageUri = createImageUri();
        if (cameraImageUri != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, 1002);
            }
        }
    }

    private Uri createImageUri() {
        java.io.File storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        java.io.File imageFile = new java.io.File(storageDir, "temp_product_" + System.currentTimeMillis() + ".jpg");
        return androidx.core.content.FileProvider.getUriForFile(context,
                context.getPackageName() + ".fileprovider", imageFile);
    }

    // Xử lý ảnh từ Gallery
    public void handleImageResult(Uri uri) {
        if (uri != null) {
            selectedImageUri = uri;
            if (ivMainPreview != null && llPlaceholder != null) {
                llPlaceholder.setVisibility(View.GONE);
                ivMainPreview.setVisibility(View.VISIBLE);
                Glide.with(context).load(uri).into(ivMainPreview);
            }
        }
    }

    // Xử lý ảnh từ Camera
    public void handleCameraResult() {
        if (cameraImageUri != null) {
            handleImageResult(cameraImageUri);
        }
    }

    private void uploadImageAndSaveProduct(Dialog dialog, String id, String name, String category,
                                           CheckBox cbVisible, CheckBox cbSpecial, EditText etPriceM,
                                           EditText etPriceL, EditText etVipPriceM, EditText etVipPriceL,
                                           EditText etProductInfo, EditText etProductDesc) {

        Toast.makeText(context, "Uploading image...", Toast.LENGTH_SHORT).show();

        MediaManager.get().upload(selectedImageUri)
                .unsigned(CloudinaryHelper.UPLOAD_PRESET)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}
                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        uploadedImageUrl = (String) resultData.get("secure_url");
                        saveToFirebase(id, name, category, cbVisible, cbSpecial,
                                etPriceM, etPriceL, etVipPriceM, etVipPriceL, etProductInfo, etProductDesc, uploadedImageUrl);
                        dialog.dismiss();
                    }
                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(context, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void saveToFirebase(String id, String name, String category, CheckBox cbVisible,
                                CheckBox cbSpecial, EditText etPriceM, EditText etPriceL,
                                EditText etVipPriceM, EditText etVipPriceL, EditText etProductInfo,
                                EditText etProductDesc, String imageUrl) {

        Product newProduct = new Product(
                id, id, name, category,
                parsePrice(etPriceM), parsePrice(etPriceL),
                parsePrice(etVipPriceM), parsePrice(etVipPriceL),
                etProductInfo.getText().toString().trim(),
                etProductDesc.getText().toString().trim(),
                imageUrl, cbVisible.isChecked(), cbSpecial.isChecked()
        );

        String collectionPath = context.getString(R.string.firebase_collection_products);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(collectionPath).child(id);
        ref.setValue(newProduct).addOnSuccessListener(aVoid -> {
            Toast.makeText(context, "Product added successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupDefaultImage(Dialog dialog) {
        // Method replaced by setupImageUpload
    }

    private void setupPriceFormatting(EditText et) {
        et.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    et.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[^\\d]", "");
                    if (!cleanString.isEmpty()) {
                        try {
                            double parsed = Double.parseDouble(cleanString);
                            String formatted = priceFormatter.format(parsed).replace(',', '.');
                            current = formatted;
                            et.setText(formatted);
                            et.setSelection(formatted.length());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    } else {
                        current = "";
                        et.setText("");
                    }

                    et.addTextChangedListener(this);
                }
            }
        });
    }

    private void setupNestedDescriptionScroll(EditText editText) {
        editText.setVerticalScrollBarEnabled(false);
        editText.setOverScrollMode(View.OVER_SCROLL_NEVER);
        editText.setOnTouchListener((view, event) -> {
            boolean canScroll = view.canScrollVertically(-1) || view.canScrollVertically(1);
            if (canScroll && view.getParent() != null) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
            }
            if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                    && view.getParent() != null) {
                view.getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });
    }

    private int parsePrice(EditText et) {
        String val = et.getText().toString().trim().replace(".", "");
        try {
            return val.isEmpty() ? 0 : Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
