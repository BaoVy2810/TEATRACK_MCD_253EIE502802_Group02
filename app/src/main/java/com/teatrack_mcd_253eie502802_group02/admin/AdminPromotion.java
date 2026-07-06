package com.teatrack_mcd_253eie502802_group02.admin;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.adapter.AdminPromotionAdapter;
import com.teatrack_mcd_253eie502802_group02.model.Promotion;
import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;
import com.teatrack_mcd_253eie502802_group02.util.CloudinaryHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class AdminPromotion extends BaseActivity {

    private RecyclerView rvPromotionList;
    private AdminPromotionAdapter adapter;
    private List<Promotion> promotionList;
    private List<Promotion> filteredList;
    private DatabaseReference databaseReference;
    private EditText etSearch;
    private View layoutEmptyState;
    private View fabAddPromotion;
    private Uri selectedImageUri;
    private ImageView ivDialogPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_promotion);
        com.teatrack_mcd_253eie502802_group02.shared.ui.AdminInsetsHelper.apply(this);

        CloudinaryHelper.init(this);
        initViews();
        setupFirebase();
        setupSearch();
        setupBottomNavigation();
        setupHeader();

        View fab = findViewById(R.id.fabAddPromotion);
        if (fab != null) {
            fab.setOnTouchListener(new View.OnTouchListener() {
                private float initialX, initialY, initialTouchX, initialTouchY;
                private static final int CLICK_THRESHOLD = 10;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = v.getX();
                            initialY = v.getY();
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            v.setX(initialX + (event.getRawX() - initialTouchX));
                            v.setY(initialY + (event.getRawY() - initialTouchY));
                            return true;

                        case MotionEvent.ACTION_UP:
                            float diffX = Math.abs(event.getRawX() - initialTouchX);
                            float diffY = Math.abs(event.getRawY() - initialTouchY);
                            if (diffX < CLICK_THRESHOLD && diffY < CLICK_THRESHOLD) {
                                showPromotionDialog(null);
                            }
                            return true;
                    }
                    return false;
                }
            });
        }

        // PHẦN B: SEEDING DATA AN TOÀN - Long press vào tiêu đề để kích hoạt
        TextView txtTitle = findViewById(R.id.txtPromotionManagement);
        txtTitle.setOnLongClickListener(v -> {
            seedVouchers();
            return true;
        });
    }

    private void seedVouchers() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("vouchers");
        
        // Kiểm tra xem đã seed chưa bằng cách tìm mã OPENING50
        ref.orderByChild("code").equalTo("OPENING50").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(AdminPromotion.this, "Data already seeded!", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                String[][] data = {
                    {"OPENING50", "Khai Trương Hồng Phát", "Giảm 50% tổng đơn, tối đa 30k. Chào mừng chi nhánh mới!", "percent", "50", "30000", "0", "2024-12-31", "https://images.unsplash.com/photo-1544787210-22bb8306386c?q=80&w=800&auto=format&fit=crop"},
                    {"MILKTEA10", "Tín Đồ Trà Sữa", "Giảm 10k cho mỗi ly Trà Sữa trong đơn hàng.", "per_item", "10000", "0", "40000", "2024-06-30", "https://images.unsplash.com/photo-1594631252845-29fc4586bd92?q=80&w=800&auto=format&fit=crop"},
                    {"FREESHIP", "Giao Hàng Miễn Phí", "Giảm trực tiếp 15k phí vận chuyển cho đơn từ 50k.", "amount", "15000", "0", "50000", "2024-12-31", "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?q=80&w=800&auto=format&fit=crop"},
                    {"MEMDAY", "Ngày Hội Thành Viên", "Giảm 20% tối đa 50k cho khách hàng thân thiết vào thứ 2.", "percent", "20", "50000", "100000", "2024-12-31", "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?q=80&w=800&auto=format&fit=crop"},
                    {"TOPPING5", "Thêm Topping Thêm Vui", "Giảm 5k cho mỗi phần Topping thêm vào.", "per_item", "5000", "0", "30000", "2024-08-15", "https://images.unsplash.com/photo-1597318181409-cf44d05b2ba7?q=80&w=800&auto=format&fit=crop"},
                    {"NIGHTTEA", "Trà Đêm Tỉnh Táo", "Giảm 20k cho đơn hàng sau 20h tối.", "amount", "20000", "0", "80000", "2024-07-20", "https://images.unsplash.com/photo-1517646287270-a5a9ca602e5c?q=80&w=800&auto=format&fit=crop"},
                    {"COMBO2", "Tiệc Trà Cặp Đôi", "Giảm 15% tối đa 25k khi mua từ 2 ly bất kỳ.", "percent", "15", "25000", "60000", "2024-09-30", "https://images.unsplash.com/photo-1512568400610-62da28bc8a13?q=80&w=800&auto=format&fit=crop"},
                    {"WEEKEND", "Cuối Tuần Rực Rỡ", "Tặng voucher 10k cho mọi đơn hàng thứ 7 và CN.", "amount", "10000", "0", "0", "2024-10-31", "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?q=80&w=800&auto=format&fit=crop"},
                    {"NEWUSER", "Chào Bạn Mới", "Giảm 30% cho đơn hàng đầu tiên, tối đa 20k.", "percent", "30", "20000", "0", "2025-01-01", "https://images.unsplash.com/photo-1541167760496-162955ed8a9f?q=80&w=800&auto=format&fit=crop"},
                    {"TEATIME", "Giờ Vàng Thưởng Trà", "Giảm 7k mỗi ly trong khung giờ 14h - 17h.", "per_item", "7000", "0", "50000", "2024-11-30", "https://images.unsplash.com/photo-1594631252845-29fc4586bd92?q=80&w=800&auto=format&fit=crop"}
                };

                for (String[] row : data) {
                    String id = ref.push().getKey();
                    Promotion p = new Promotion();
                    p.setId(id);
                    p.setCode(row[0]);
                    p.setTitle(row[1]);
                    p.setDescription(row[2]);
                    p.setType(row[3]);
                    p.setValue(Double.parseDouble(row[4]));
                    p.setMax(Double.parseDouble(row[5]));
                    p.setMinSubtotal(Double.parseDouble(row[6]));
                    p.setExpiry(row[7]);
                    p.setImage(row[8]);
                    p.setImageSourceType("remote");
                    p.setIsActive(true);
                    p.setCategory("Global");
                    if (id != null) ref.child(id).setValue(p);
                }
                Toast.makeText(AdminPromotion.this, "Seeding 10 vouchers successfully!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminPromotion.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        rvPromotionList = findViewById(R.id.rvPromotionList);
        etSearch = findViewById(R.id.etSearch);
        etSearch.setHint(R.string.str_promotion_search);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        fabAddPromotion = findViewById(R.id.fabAddPromotion);
        setupEmptyStateContent();

        promotionList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new AdminPromotionAdapter(filteredList, new AdminPromotionAdapter.OnPromotionActionListener() {
            @Override
            public void onEdit(Promotion promotion) {
                showPromotionDialog(promotion);
            }

            @Override
            public void onDelete(Promotion promotion) {
                confirmDelete(promotion);
            }
        });

        rvPromotionList.setLayoutManager(new LinearLayoutManager(this));
        rvPromotionList.setAdapter(adapter);
    }

    private void setupBottomNavigation() {
        int[] navItemIds = {
                R.id.nav_dashboard,
                R.id.nav_products,
                R.id.nav_orders,
                R.id.nav_account,
                R.id.nav_forum,
                R.id.nav_branch,
                R.id.nav_feedbacks,
                R.id.nav_promotion
        };

        NavBarHelper.setupNavBar(this, navItemIds, R.id.nav_promotion, v -> {
            int id = v.getId();
            if (id == R.id.nav_promotion) return;

            Class<?> destination = null;
            if (id == R.id.nav_dashboard) destination = AdminDashboard.class;
            else if (id == R.id.nav_products) destination = AdminProduct.class;
            else if (id == R.id.nav_orders) destination = AdminOrders.class;
            else if (id == R.id.nav_account) destination = AdminAccount.class;
            else if (id == R.id.nav_forum) destination = AdminBlog.class;
            else if (id == R.id.nav_branch) destination = AdminAgency.class;
            else if (id == R.id.nav_feedbacks) destination = AdminComplaints.class;

            if (destination != null) {
                NavBarHelper.navigateWithoutTransition(this, destination);
            }
        });
    }

    private void setupHeader() {
        com.teatrack_mcd_253eie502802_group02.shared.ui.HeaderMenuHelper.setupProfileMenu(this);
        View header = findViewById(R.id.layout_header);
        if (header != null) {
            View btnNotif = header.findViewById(R.id.btn_notification);
            if (btnNotif != null) {
                btnNotif.setOnClickListener(v -> Toast.makeText(this, "Opening Notifications...", Toast.LENGTH_SHORT).show());
            }
        }
    }

    private void setupFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference("vouchers");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                promotionList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Promotion promotion = data.getValue(Promotion.class);
                    if (promotion != null) {
                        promotionList.add(promotion);
                    }
                }
                filter(etSearch.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminPromotion.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String text) {
        filteredList.clear();
        if (text.isEmpty()) {
            filteredList.addAll(promotionList);
        } else {
            String query = text.toLowerCase().trim();
            for (Promotion p : promotionList) {
                if (p.getCode().toLowerCase().contains(query) || p.getDescription().toLowerCase().contains(query)) {
                    filteredList.add(p);
                }
            }
        }
        adapter.notifyDataSetChanged();
        updateEmptyState(filteredList.isEmpty());
    }

    private void updateEmptyState(boolean empty) {
        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        }
        if (rvPromotionList != null) {
            rvPromotionList.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
    }

    private void setupEmptyStateContent() {
        if (layoutEmptyState == null) return;
        ImageView icon = layoutEmptyState.findViewById(R.id.ivEmptyIcon);
        TextView title = layoutEmptyState.findViewById(R.id.tvEmptyTitle);
        TextView desc = layoutEmptyState.findViewById(R.id.tvEmptyDesc);
        icon.setImageResource(R.drawable.coupon3);
        title.setText(R.string.empty_promotions_title);
        desc.setText(R.string.empty_promotions_desc);
    }

    private void showPromotionDialog(Promotion promotion) {
        Dialog dialog = new Dialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_promotion_admin, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText etCode = view.findViewById(R.id.etPromotionCode);
        EditText etDesc = view.findViewById(R.id.etPromotionDesc);
        View typeSelectRow = view.findViewById(R.id.typeSelectRow);
        TextView tvTypeSelect = view.findViewById(R.id.tvTypeSelect);
        EditText etValue = view.findViewById(R.id.etPromotionValue);
        EditText etMinSubtotal = view.findViewById(R.id.etMinSubtotal);
        EditText etMaxDiscount = view.findViewById(R.id.etMaxDiscount);
        TextView tvMaxDiscountLabel = view.findViewById(R.id.tvMaxDiscountLabel);
        
        TextView btnSourceLocal = view.findViewById(R.id.btnSourceLocal);
        TextView btnSourceRemote = view.findViewById(R.id.btnSourceRemote);
        LinearLayout layoutLocal = view.findViewById(R.id.layoutLocalImage);
        LinearLayout layoutRemote = view.findViewById(R.id.layoutRemoteImage);
        EditText etImageUrl = view.findViewById(R.id.etImageUrl);
        Button btnSelectLocal = view.findViewById(R.id.btnSelectLocalImage);
        ivDialogPreview = view.findViewById(R.id.ivPromotionPreview);

        TextView tvPreviewTitle = view.findViewById(R.id.tvPreviewTitle);
        TextView tvPreviewDiscount = view.findViewById(R.id.tvPreviewDiscount);
        TextView tvPreviewMinOrder = view.findViewById(R.id.tvPreviewMinOrder);

        com.google.android.material.switchmaterial.SwitchMaterial swActive = view.findViewById(R.id.swPromotionActive);
        com.google.android.material.button.MaterialButton btnSave = view.findViewById(R.id.btnSavePromotion);
        com.google.android.material.button.MaterialButton btnCancel = view.findViewById(R.id.btnCancelPromotion);
        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        ImageButton btnCloseDialog = view.findViewById(R.id.btnCloseDialog);

        if (btnCloseDialog != null) {
            btnCloseDialog.setOnClickListener(v -> dialog.dismiss());
        }

        // Promotion type dropdown
        final String[] typeLabels = {
                getString(R.string.str_type_amount),
                getString(R.string.str_type_percent),
                getString(R.string.str_type_per_item)
        };
        final int[] selectedTypeIndex = {0};

        Runnable applyTypeSelection = () -> {
            tvTypeSelect.setText(typeLabels[selectedTypeIndex[0]]);
            if (selectedTypeIndex[0] == 1) {
                tvMaxDiscountLabel.setVisibility(View.VISIBLE);
                etMaxDiscount.setVisibility(View.VISIBLE);
            } else {
                tvMaxDiscountLabel.setVisibility(View.GONE);
                etMaxDiscount.setVisibility(View.GONE);
            }
            updatePreview(etCode, selectedTypeIndex[0], etValue, etMinSubtotal, etMaxDiscount,
                    tvPreviewTitle, tvPreviewDiscount, tvPreviewMinOrder);
        };

        typeSelectRow.setOnClickListener(v -> showTypeOptionPopup(v, Arrays.asList(typeLabels),
                typeLabels[selectedTypeIndex[0]], choice -> {
                    for (int i = 0; i < typeLabels.length; i++) {
                        if (typeLabels[i].equals(choice)) {
                            selectedTypeIndex[0] = i;
                            break;
                        }
                    }
                    applyTypeSelection.run();
                }));

        // Image source toggle (dashboard pill style)
        final int[] selectedImageSource = {R.id.btnSourceLocal};
        Runnable applyImageSourceToggle = () -> {
            boolean isLocal = selectedImageSource[0] == R.id.btnSourceLocal;
            updateImageSourceToggleState(btnSourceLocal, btnSourceRemote, isLocal ? btnSourceLocal : btnSourceRemote);
            layoutLocal.setVisibility(isLocal ? View.VISIBLE : View.GONE);
            layoutRemote.setVisibility(isLocal ? View.GONE : View.VISIBLE);
        };
        btnSourceLocal.setOnClickListener(v -> {
            selectedImageSource[0] = R.id.btnSourceLocal;
            applyImageSourceToggle.run();
        });
        btnSourceRemote.setOnClickListener(v -> {
            selectedImageSource[0] = R.id.btnSourceRemote;
            applyImageSourceToggle.run();
        });
        applyImageSourceToggle.run();

        btnSelectLocal.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1001);
        });

        // Live Preview Logic
        TextWatcher previewWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePreview(etCode, selectedTypeIndex[0], etValue, etMinSubtotal, etMaxDiscount,
                        tvPreviewTitle, tvPreviewDiscount, tvPreviewMinOrder);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        etCode.addTextChangedListener(previewWatcher);
        etValue.addTextChangedListener(previewWatcher);
        etMinSubtotal.addTextChangedListener(previewWatcher);
        etMaxDiscount.addTextChangedListener(previewWatcher);
        
        etImageUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (selectedImageSource[0] == R.id.btnSourceRemote && !s.toString().isEmpty()) {
                    Glide.with(AdminPromotion.this).load(s.toString()).into(ivDialogPreview);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        if (promotion != null) {
            tvTitle.setText(R.string.str_edit_promotion);
            btnSave.setText(R.string.btn_edit);
            btnSave.setIcon(ContextCompat.getDrawable(this, R.drawable.edit2));
            etCode.setText(promotion.getCode());
            etDesc.setText(promotion.getDescription());
            if ("percent".equals(promotion.getType())) {
                selectedTypeIndex[0] = 1;
                etMaxDiscount.setText(String.valueOf(promotion.getMax()));
            } else if ("per_item".equals(promotion.getType())) {
                selectedTypeIndex[0] = 2;
            } else {
                selectedTypeIndex[0] = 0;
            }
            etValue.setText(String.valueOf(promotion.getValue()));
            etMinSubtotal.setText(String.valueOf(promotion.getMinSubtotal()));
            swActive.setChecked(promotion.getIsActive());

            if ("remote".equals(promotion.getImageSourceType())) {
                selectedImageSource[0] = R.id.btnSourceRemote;
                etImageUrl.setText(promotion.getImage());
                Glide.with(this).load(promotion.getImage()).into(ivDialogPreview);
            } else {
                selectedImageSource[0] = R.id.btnSourceLocal;
                // For local, we might just show the existing image URL if it's already on Cloudinary
                if (promotion.getImage() != null && !promotion.getImage().isEmpty()) {
                    Glide.with(this).load(promotion.getImage()).into(ivDialogPreview);
                }
            }
        } else {
            tvTitle.setText(R.string.str_add_promotion);
            btnSave.setText(R.string.btn_add);
            btnSave.setIcon(ContextCompat.getDrawable(this, R.drawable.plus));
        }

        applyTypeSelection.run();
        applyImageSourceToggle.run();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String code = etCode.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String valueStr = etValue.getText().toString().trim();
            String minStr = etMinSubtotal.getText().toString().trim();
            String type = "amount";
            int selection = selectedTypeIndex[0];
            if (selection == 1) type = "percent";
            else if (selection == 2) type = "per_item";

            if (code.isEmpty()) {
                etCode.setError(getString(R.string.str_error_code_empty));
                return;
            }

            if (desc.isEmpty() || valueStr.isEmpty()) {
                Toast.makeText(this, R.string.str_fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            double value = Double.parseDouble(valueStr);
            double min = minStr.isEmpty() ? 0 : Double.parseDouble(minStr);
            double max = etMaxDiscount.getText().toString().isEmpty() ? 0 : Double.parseDouble(etMaxDiscount.getText().toString());

            String imageSourceType = (selectedImageSource[0] == R.id.btnSourceLocal) ? "local" : "remote";
            String currentImageUrl = (imageSourceType.equals("remote")) ? etImageUrl.getText().toString() : (promotion != null ? promotion.getImage() : "");

            if (imageSourceType.equals("local") && selectedImageUri != null) {
                uploadAndSave(dialog, promotion, code, desc, min, max, type, value, imageSourceType);
            } else {
                savePromotion(dialog, promotion, code, desc, min, max, type, value, currentImageUrl, imageSourceType, swActive.isChecked());
            }
        });

        dialog.show();
    }

    private String formatPrice(double price) {
        return String.format(Locale.US, "%,.0f", price).replace(',', '.') + "đ";
    }

    private void showTypeOptionPopup(View anchor, List<String> options, String selected,
                                     java.util.function.Consumer<String> listener) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.dialog_category_selector, null);
        popupView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        int minWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.45);
        int anchorWidth = anchor.getWidth() > 0 ? anchor.getWidth() : 0;
        int popupWidth = Math.max(Math.max(anchorWidth, popupView.getMeasuredWidth()), minWidth);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                popupWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(20);

        RecyclerView rvCategories = popupView.findViewById(R.id.rvCategoryList);
        if (rvCategories != null) {
            rvCategories.setLayoutManager(new LinearLayoutManager(this));
            rvCategories.setAdapter(new CategoryDialogAdapter(options, selected, choice -> {
                listener.accept(choice);
                popupWindow.dismiss();
            }));
        }
        popupWindow.showAsDropDown(anchor, 0, 10);
    }

    private void updatePreview(EditText etCode, int typeIndex, EditText etValue, EditText etMinSubtotal, EditText etMaxDiscount,
                              TextView tvTitle, TextView tvDiscount, TextView tvMinOrder) {
        String code = etCode.getText().toString().trim();
        String valStr = etValue.getText().toString().trim();
        String minStr = etMinSubtotal.getText().toString().trim();
        
        tvTitle.setText(code.isEmpty() ? "PROMO CODE" : code);
        
        if (!valStr.isEmpty()) {
            try {
                double value = Double.parseDouble(valStr);
                int selection = typeIndex;
                
                if (selection == 1) { // Percent
                    String desc = getString(R.string.str_desc_percent, (int)value);
                    String maxStr = etMaxDiscount.getText().toString().trim();
                    if (!maxStr.isEmpty()) {
                        desc += " " + getString(R.string.str_desc_max, formatPrice(Double.parseDouble(maxStr)));
                    }
                    tvDiscount.setText(desc);
                } else if (selection == 2) { // Per Item
                    tvDiscount.setText(getString(R.string.str_desc_per_item, formatPrice(value)));
                } else { // Amount
                    tvDiscount.setText(getString(R.string.str_desc_amount, formatPrice(value)));
                }
            } catch (Exception e) {
                tvDiscount.setText(getString(R.string.str_preview_empty));
            }
        } else {
            tvDiscount.setText(getString(R.string.str_preview_empty));
        }

        if (!minStr.isEmpty()) {
            try {
                tvMinOrder.setText(getString(R.string.str_preview_min_order, formatPrice(Double.parseDouble(minStr))));
            } catch (Exception e) {
                tvMinOrder.setText("");
            }
        } else {
            tvMinOrder.setText("");
        }
    }

    private void uploadAndSave(Dialog dialog, Promotion promotion, String code, String desc, double min, double max, 
                              String type, double value, String imageSourceType) {
        Toast.makeText(this, R.string.str_uploading_image, Toast.LENGTH_SHORT).show();
        MediaManager.get().upload(selectedImageUri)
                .unsigned(CloudinaryHelper.UPLOAD_PRESET)
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        savePromotion(dialog, promotion, code, desc, min, max, type, value, imageUrl, imageSourceType, true);
                    }
                    @Override public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(AdminPromotion.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void savePromotion(Dialog dialog, Promotion promotion, String code, String desc, double min, double max, 
                               String type, double value, String imageUrl, String imageSourceType, boolean isActive) {
        String id = (promotion == null) ? databaseReference.push().getKey() : promotion.getId();
        Promotion newPromotion = new Promotion(id, code, desc, min, type, value);
        newPromotion.setMax(max);
        newPromotion.setImage(imageUrl);
        newPromotion.setImageSourceType(imageSourceType);
        newPromotion.setIsActive(isActive);

        if (id != null) {
            databaseReference.child(id).setValue(newPromotion)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, promotion == null ? R.string.str_add_promotion_success : R.string.str_update_promotion_success, Toast.LENGTH_SHORT).show();
                        selectedImageUri = null;
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, getString(R.string.str_error, e.getMessage()), Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (ivDialogPreview != null) {
                ivDialogPreview.setVisibility(View.VISIBLE);
                Glide.with(this).load(selectedImageUri).into(ivDialogPreview);
            }
        }
    }

    private void confirmDelete(Promotion promotion) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_delete_confirm);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            dialog.getWindow().setAttributes(params);
        }

        TextView tvTitle = dialog.findViewById(R.id.tvDeleteTitle);
        if (tvTitle != null) tvTitle.setText(R.string.modal_delete_title);

        TextView tvMessage = dialog.findViewById(R.id.tvDeleteMessage);
        String code = promotion.getCode();
        String fullMessage = "The promotion <font color='#0088ff'><b>" + android.text.TextUtils.htmlEncode(code) + "</b></font> will be permanently deleted.";
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tvMessage.setText(Html.fromHtml(fullMessage, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvMessage.setText(Html.fromHtml(fullMessage));
        }

        dialog.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnConfirmDelete).setOnClickListener(v -> {
            databaseReference.child(promotion.getId()).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AdminPromotion.this, R.string.str_delete_promotion_success, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        });

        dialog.show();
    }

    private void updateImageSourceToggleState(TextView btnLocal, TextView btnRemote, TextView activeBtn) {
        TextView[] buttons = {btnLocal, btnRemote};
        int activeBlue = ContextCompat.getColor(this, R.color.brand_blue);
        for (TextView btn : buttons) {
            if (btn == activeBtn) {
                btn.setBackgroundResource(R.drawable.bg_toggle_item_active_white);
                btn.setTextColor(activeBlue);
            } else {
                btn.setBackgroundResource(R.drawable.bg_toggle_item_inactive_transparent);
                btn.setTextColor(Color.WHITE);
            }
        }
    }
}
