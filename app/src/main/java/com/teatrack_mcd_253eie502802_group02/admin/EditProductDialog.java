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
import android.view.LayoutInflater;
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
import com.teatrack_mcd_253eie502802_group02.util.ProductImageHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EditProductDialog {

    private final Context context;
    private final Product product;
    private final List<String> categories;
    private Uri selectedImageUri;
    private Uri cameraImageUri;
    private ImageView ivMainPreview;
    private View llPlaceholder;
    private String uploadedImageUrl;
    private String selectedCategory;
    private final DecimalFormat priceFormatter = new DecimalFormat("#,###");

    public EditProductDialog(Context context, Product product, List<String> categories) {
        this.context = context;
        this.product = product;
        this.categories = categories;
        this.uploadedImageUrl = product.getImage();
        this.selectedCategory = product.getCategory();
    }

    public void show() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_product);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.92);
            dialog.getWindow().setAttributes(params);
            dialog.getWindow().setWindowAnimations(android.R.style.Animation_Dialog);
        }

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
        MaterialButton btnEdit = dialog.findViewById(R.id.btnEdit);

        // Fill Data
        etProductName.setText(product.getName());
        cbVisible.setChecked(product.isVisible());
        cbSpecial.setChecked(product.isSpecial());
        
        etPriceM.setText(formatInitialPrice(product.getPrice()));
        etPriceL.setText(formatInitialPrice(product.getPriceL()));
        etVipPriceM.setText(formatInitialPrice(product.getVipPriceM()));
        etVipPriceL.setText(formatInitialPrice(product.getVipPriceL()));

        etProductInfo.setText(product.getDescription());
        etProductDesc.setText(product.getDetail());

        setupPriceFormatting(etPriceM);
        setupPriceFormatting(etPriceL);
        setupPriceFormatting(etVipPriceM);
        setupPriceFormatting(etVipPriceL);

        setupImageUpload(dialog);

        // Setup Category Select
        if (tvCategorySelect != null) {
            tvCategorySelect.setText(selectedCategory);
            tvCategorySelect.setOnClickListener(v -> showCategoryPopup(v));
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnEdit.setOnClickListener(v -> {
            String name = etProductName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(context, "Product name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            product.setName(name);
            product.setCategory(selectedCategory);
            product.setVisible(cbVisible.isChecked());
            product.setSpecial(cbSpecial.isChecked());
            product.setPrice(parsePrice(etPriceM));
            product.setPriceL(parsePrice(etPriceL));
            product.setVipPriceM(parsePrice(etVipPriceM));
            product.setVipPriceL(parsePrice(etVipPriceL));
            product.setDescription(etProductInfo.getText().toString().trim());
            product.setDetail(etProductDesc.getText().toString().trim());

            if (selectedImageUri != null) {
                uploadImageAndSaveProduct(dialog);
            } else {
                product.setImage(uploadedImageUrl);
                saveToFirebase(dialog);
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
        List<String> productImages = product.getImages();

        for (int i = 0; i < imageSlotIds.length; i++) {
            final int index = i;
            View slotView = dialog.findViewById(imageSlotIds[i]);
            if (slotView == null) continue;

            ImageView ivPreview = slotView.findViewById(R.id.ivProductImage);
            View placeholder = slotView.findViewById(R.id.llUploadPlaceholder);

            if (i == 0) {
                ivMainPreview = ivPreview;
                llPlaceholder = placeholder;
            }

            // Hiển thị ảnh cũ nếu có
            if (productImages != null && index < productImages.size()) {
                String imgUrl = productImages.get(index);
                if (imgUrl != null && !imgUrl.isEmpty()) {
                    placeholder.setVisibility(View.GONE);
                    ivPreview.setVisibility(View.VISIBLE);
                    ProductImageHelper.loadFromUrl(ivPreview, imgUrl);
                }
            } else if (i == 0 && (product.getImage() != null || product.getImageRes() != 0)) {
                // Fallback cho ảnh chính cũ (trường image)
                placeholder.setVisibility(View.GONE);
                ivPreview.setVisibility(View.VISIBLE);
                ProductImageHelper.load(ivPreview, product);
            }

            slotView.setOnClickListener(v -> {
                if (index == 0) {
                    showImageSourceDialog();
                } else {
                    Toast.makeText(context, "Currently only support editing the main image", Toast.LENGTH_SHORT).show();
                }
            });
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
            ((Activity) context).startActivityForResult(intent, 1003); // Use 1003 for Edit Gallery
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraImageUri = createImageUri();
        if (cameraImageUri != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, 1004); // Use 1004 for Edit Camera
            }
        }
    }

    private Uri createImageUri() {
        java.io.File storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        java.io.File imageFile = new java.io.File(storageDir, "temp_product_edit_" + System.currentTimeMillis() + ".jpg");
        return androidx.core.content.FileProvider.getUriForFile(context,
                context.getPackageName() + ".fileprovider", imageFile);
    }

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

    public void handleCameraResult() {
        if (cameraImageUri != null) {
            handleImageResult(cameraImageUri);
        }
    }

    private void uploadImageAndSaveProduct(Dialog dialog) {
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
                        product.setImage(uploadedImageUrl);
                        saveToFirebase(dialog);
                    }
                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(context, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void saveToFirebase(Dialog dialog) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("products").child(product.getId());
        ref.setValue(product).addOnSuccessListener(aVoid -> {
            Toast.makeText(context, "Product updated successfully", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }).addOnFailureListener(e -> Toast.makeText(context, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String formatInitialPrice(int price) {
        if (price <= 0) return "";
        return priceFormatter.format(price).replace(',', '.');
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

    private int parsePrice(EditText et) {
        String val = et.getText().toString().trim().replace(".", "");
        try {
            return val.isEmpty() ? 0 : Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
