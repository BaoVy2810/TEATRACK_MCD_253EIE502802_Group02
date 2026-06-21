package com.teatrack_mcd_253eie502802_group02.admin;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.adapter.BlogAdapter;
import com.teatrack_mcd_253eie502802_group02.model.Blog;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AdminBlog extends AppCompatActivity {

    private EditText etSearchBlog;
    private RecyclerView rvBlogList;
    private Button btnAddBlog;
    private TextView tvEmptyState;

    private static final int[] NAV_ITEM_IDS = {
            R.id.nav_dashboard,
            R.id.nav_products,
            R.id.nav_orders,
            R.id.nav_account,
            R.id.nav_forum,
            R.id.nav_branch,
            R.id.nav_feedbacks,
            R.id.nav_promotion
    };

    private BlogAdapter blogAdapter;
    private List<Blog> blogList;
    private List<Blog> fullBlogList;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private EditText currentImageEditText;
    private ShapeableImageView currentPreviewImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_blog);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            if (currentPreviewImage != null) {
                                currentPreviewImage.setImageURI(imageUri);
                                currentPreviewImage.setVisibility(View.VISIBLE);
                            }
                            uploadImageToFirebase(imageUri);
                        }
                    }
                }
        );

        initViews();
        setupRecyclerView();
        initData();
        setupNavBar();
        setupHeader();

        btnAddBlog.setOnClickListener(v -> showAddBlogDialog());
        setupSearch();
    }

    private void setupHeader() {
        View btnProfile = findViewById(R.id.btn_profile);
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> startActivity(new Intent(this, AdminProfile.class)));
        }
    }

    private void setupNavBar() {
        NavBarHelper.setupNavBar(this, NAV_ITEM_IDS, R.id.nav_forum, v -> {
            int id = v.getId();
            Class<?> destination = null;
            if (id == R.id.nav_dashboard) destination = AdminDashboard.class;
            else if (id == R.id.nav_products) destination = AdminProduct.class;
            else if (id == R.id.nav_orders) destination = AdminOrders.class;
            else if (id == R.id.nav_account) destination = AdminAccount.class;
            else if (id == R.id.nav_forum) return; // Already here
            else if (id == R.id.nav_branch) destination = AdminAgency.class;
            else if (id == R.id.nav_feedbacks) destination = AdminComplaints.class;
            else if (id == R.id.nav_promotion) destination = AdminPromotion.class;

            if (destination != null) {
                startActivity(new Intent(this, destination));
                finish();
            }
        });
    }

    private void showAddBlogDialog() {
        showBlogDialog(null);
    }

    public void showEditBlogDialog(Blog blog) {
        showBlogDialog(blog);
    }

    private void showBlogDialog(Blog blog) {
        Dialog dialog = new Dialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_blog_admin, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        ImageButton btnCloseDialog = view.findViewById(R.id.btnCloseDialog);
        EditText etBlogTitle = view.findViewById(R.id.etBlogTitle);
        EditText etBlogContent = view.findViewById(R.id.etBlogContent);
        EditText etBlogImage = view.findViewById(R.id.etBlogImage);
        Spinner spBlogCategory = view.findViewById(R.id.spBlogCategory);
        SwitchMaterial swPublished = view.findViewById(R.id.swPublished);
        com.google.android.material.button.MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        com.google.android.material.button.MaterialButton btnSubmit = view.findViewById(R.id.btnSubmit);
        com.google.android.material.button.MaterialButton btnChooseImage = view.findViewById(R.id.btnChooseBlogImage);
        ShapeableImageView ivPreview = view.findViewById(R.id.ivBlogPreview);

        // Setup Category Spinner
        String[] categories = {
                getString(R.string.pure_tea),
                getString(R.string.tea_latte),
                getString(R.string.milk_tea),
                getString(R.string.fruit_tea),
                getString(R.string.str_category_news),
                getString(R.string.str_category_events)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBlogCategory.setAdapter(adapter);

        if (blog == null) {
            tvDialogTitle.setText(R.string.str_add_blog);
            btnSubmit.setText(R.string.str_btn_create);
        } else {
            tvDialogTitle.setText(R.string.str_edit_blog);
            btnSubmit.setText(R.string.str_btn_save);
            etBlogTitle.setText(blog.getTitle() != null ? blog.getTitle() : blog.getHeading());
            etBlogContent.setText(blog.getContent());
            etBlogImage.setText(blog.getThumbnailImage() != null ? blog.getThumbnailImage() : blog.getImage());
            swPublished.setChecked("published".equalsIgnoreCase(blog.getStatus()));
            
            // Set category selection
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(blog.getCategory())) {
                    spBlogCategory.setSelection(i);
                    break;
                }
            }

            String displayImg = blog.getThumbnailImage() != null && !blog.getThumbnailImage().isEmpty() 
                    ? blog.getThumbnailImage() : blog.getImage();

            if (displayImg != null && !displayImg.isEmpty()) {
                ivPreview.setVisibility(View.VISIBLE);
                if (displayImg.startsWith("http")) {
                    Glide.with(this).load(displayImg).into(ivPreview);
                } else {
                    int resId = getResources().getIdentifier(displayImg.replace(".png", "").replace(".jpg", ""), "mipmap", getPackageName());
                    if (resId == 0) resId = getResources().getIdentifier(displayImg.replace(".png", "").replace(".jpg", ""), "drawable", getPackageName());
                    if (resId != 0) ivPreview.setImageResource(resId);
                }
            }
        }

        btnCloseDialog.setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnChooseImage.setOnClickListener(v -> pickImage(etBlogImage, ivPreview));

        btnSubmit.setOnClickListener(v -> {
            String title = etBlogTitle.getText().toString().trim();
            String content = etBlogContent.getText().toString().trim();
            String image = etBlogImage.getText().toString().trim();
            String category = spBlogCategory.getSelectedItem().toString();
            String status = swPublished.isChecked() ? "published" : "draft";

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, R.string.str_fill_required, Toast.LENGTH_SHORT).show();
                return;
            }

            if (blog == null && image.isEmpty()) {
                Toast.makeText(this, R.string.str_image_required, Toast.LENGTH_SHORT).show();
                return;
            }

            Blog blogToSave = (blog == null) ? new Blog() : blog;
            blogToSave.setTitle(title);
            blogToSave.setHeading(title); 
            blogToSave.setContent(content);
            blogToSave.setImage(image);
            blogToSave.setThumbnailImage(image); 
            blogToSave.setCategory(category);
            blogToSave.setStatus(status);
            
            if (blog == null) {
                blogToSave.setLayoutType("standard"); 
                blogToSave.setHeadingColor("#0088FF");
                String currentTimestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(new Date());
                blogToSave.setDate(currentTimestamp);
                
                String id = databaseReference.push().getKey();
                blogToSave.setId(id);
                
                databaseReference.child(id).setValue(blogToSave).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, R.string.str_add_success, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
            } else {
                databaseReference.child(blog.getId()).setValue(blogToSave).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, R.string.str_update_success, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
            }
        });

        dialog.show();
    }

    public void deleteBlog(Blog blog) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_delete_confirm);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.88);
            dialog.getWindow().setAttributes(params);
        }

        TextView tvTitle = dialog.findViewById(R.id.tvDeleteTitle);
        if (tvTitle != null) tvTitle.setText(R.string.str_delete_blog);

        TextView tvMessage = dialog.findViewById(R.id.tvDeleteMessage);
        String title = blog.getTitle();
        if (title == null || title.isEmpty()) title = blog.getHeading();
        
        String html = getString(R.string.str_delete_confirm) + " <b>" + title + "</b>?";
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tvMessage.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvMessage.setText(Html.fromHtml(html));
        }

        dialog.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnConfirmDelete).setOnClickListener(v -> {
            databaseReference.child(blog.getId()).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AdminBlog.this, R.string.str_delete_success, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private void setupSearch() {
        etSearchBlog.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String text) {
        List<Blog> filteredList = new ArrayList<>();
        String query = text.toLowerCase();
        for (Blog item : fullBlogList) {
            boolean matchesTitle = item.getTitle() != null && item.getTitle().toLowerCase().contains(query);
            boolean matchesHeading = item.getHeading() != null && item.getHeading().toLowerCase().contains(query);
            if (matchesTitle || matchesHeading) {
                filteredList.add(item);
            }
        }
        blogList.clear();
        blogList.addAll(filteredList);
        blogAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void pickImage(EditText targetEditText, ShapeableImageView previewImageView) {
        this.currentImageEditText = targetEditText;
        this.currentPreviewImage = previewImageView;
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadImageToFirebase(Uri imageUri) {
        String fileName = "blog_images/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference fileRef = storageReference.child(fileName);
        Toast.makeText(this, R.string.str_uploading_image, Toast.LENGTH_SHORT).show();
        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                if (currentImageEditText != null) {
                    currentImageEditText.setText(uri.toString());
                }
                Toast.makeText(AdminBlog.this, R.string.str_upload_success, Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> Toast.makeText(AdminBlog.this, getString(R.string.str_upload_error, e.getMessage()), Toast.LENGTH_SHORT).show());
    }

    private void initViews() {
        etSearchBlog = findViewById(R.id.etSearchBlog);
        rvBlogList = findViewById(R.id.rvBlogList);
        btnAddBlog = findViewById(R.id.btnAddBlog);
        tvEmptyState = findViewById(R.id.tvEmptyState);
    }

    private void setupRecyclerView() {
        rvBlogList.setLayoutManager(new LinearLayoutManager(this));
        blogList = new ArrayList<>();
        fullBlogList = new ArrayList<>();
        blogAdapter = new BlogAdapter(this, blogList);
        rvBlogList.setAdapter(blogAdapter);
    }

    private void initData() {
        databaseReference = FirebaseDatabase.getInstance().getReference("blogs");
        storageReference = FirebaseStorage.getInstance().getReference();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                blogList.clear();
                fullBlogList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Blog blog = dataSnapshot.getValue(Blog.class);
                    if (blog != null) {
                        blogList.add(blog);
                        fullBlogList.add(blog);
                    }
                }
                blogAdapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AdminBlog.this, getString(R.string.str_error, error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState() {
        if (blogList.isEmpty()) {
            rvBlogList.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvBlogList.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }
    }
}
