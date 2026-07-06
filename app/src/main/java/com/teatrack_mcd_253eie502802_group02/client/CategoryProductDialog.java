package com.teatrack_mcd_253eie502802_group02.client;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.teatrack_mcd_253eie502802_group02.MainActivity;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.adapter.CategoryProductAdapter;
import com.teatrack_mcd_253eie502802_group02.model.Product;
import com.teatrack_mcd_253eie502802_group02.util.ProductImageHelper;

import java.util.List;

public class CategoryProductDialog extends DialogFragment {

    private List<Product> categoryProducts;
    private int currentIndex = 0;
    private String selectedCategory;

    private ImageView imgHero;
    private RecyclerView rvProducts;
    private CategoryProductAdapter adapter;

    public static CategoryProductDialog newInstance(List<Product> products, int startIndex, String selectedCategory) {
        CategoryProductDialog dialog = new CategoryProductDialog();
        dialog.categoryProducts = products;
        dialog.currentIndex = startIndex;
        dialog.selectedCategory = selectedCategory;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_category_products, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            int horizontalMargin = (int) (20 * getResources().getDisplayMetrics().density);
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().getDecorView().setPadding(
                    horizontalMargin, 0, horizontalMargin, 0);
        }

        bindViews(view);
        return dialog;
    }

    private void bindViews(View view) {
        imgHero = view.findViewById(R.id.imgCategoryHero);
        rvProducts = view.findViewById(R.id.rvCategoryProducts);
        ImageView btnClose = view.findViewById(R.id.btnCloseDialog);
        ImageView btnPrev = view.findViewById(R.id.btnPrevProduct);
        ImageView btnNext = view.findViewById(R.id.btnNextProduct);

        rvProducts.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        adapter = new CategoryProductAdapter(categoryProducts, position -> {
            selectProduct(position);
            openProductDetail(categoryProducts.get(position));
        });
        rvProducts.setAdapter(adapter);

        btnClose.setOnClickListener(v -> dismiss());

        btnPrev.setOnClickListener(v -> {
            if (categoryProducts == null || categoryProducts.isEmpty()) {
                return;
            }
            selectProduct((currentIndex - 1 + categoryProducts.size()) % categoryProducts.size());
        });

        btnNext.setOnClickListener(v -> {
            if (categoryProducts == null || categoryProducts.isEmpty()) {
                return;
            }
            selectProduct((currentIndex + 1) % categoryProducts.size());
        });

        imgHero.setOnClickListener(v -> openMenuForSelectedCategory());
        applyInitialHeroImage();
        adapter.setSelectedPosition(currentIndex);
        rvProducts.scrollToPosition(currentIndex);
    }

    private void selectProduct(int position) {
        if (categoryProducts == null || categoryProducts.isEmpty()) {
            return;
        }
        currentIndex = position;
        Product product = categoryProducts.get(currentIndex);
        ProductImageHelper.load(imgHero, product);
        adapter.setSelectedPosition(currentIndex);
        rvProducts.smoothScrollToPosition(currentIndex);
    }

    private void openMenuForSelectedCategory() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_SELECTED_TAB, MainActivity.TAB_MENU);
        if (selectedCategory != null) {
            intent.putExtra(MainActivity.EXTRA_MENU_CATEGORY, selectedCategory);
        }
        startActivity(intent);
        dismiss();
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(requireContext(), ProductDetail.class);
        intent.putExtra("name", product.getName());
        intent.putExtra("priceM", String.valueOf(product.getPrice()));
        intent.putExtra("priceL", String.valueOf(product.getPriceL()));
        intent.putExtra("vipM", String.valueOf(product.getVipPriceM()));
        intent.putExtra("vipL", String.valueOf(product.getVipPriceL()));
        ProductImageHelper.putDetailExtras(intent, requireContext(), product);
        if (product.getId() != null && !product.getId().isEmpty()) {
            intent.putExtra("productId", product.getId());
        }
        startActivity(intent);
        dismiss();
    }

    private void applyInitialHeroImage() {
        if (imgHero == null) {
            return;
        }
        int heroRes = resolveCategoryHeroRes();
        if (heroRes != 0) {
            imgHero.setImageResource(heroRes);
            return;
        }
        if (categoryProducts != null && !categoryProducts.isEmpty() && currentIndex < categoryProducts.size()) {
            ProductImageHelper.load(imgHero, categoryProducts.get(currentIndex));
        }
    }

    private int resolveCategoryHeroRes() {
        if (selectedCategory == null || getContext() == null) {
            return 0;
        }
        if (selectedCategory.equals(getString(R.string.firebase_category_pure_tea))) {
            return R.mipmap.tra_yakult;
        }
        if (selectedCategory.equals(getString(R.string.firebase_category_tea_latte))) {
            return R.mipmap.tra_latte;
        }
        if (selectedCategory.equals(getString(R.string.firebase_category_milk_tea))) {
            return R.mipmap.tra_sua;
        }
        if (selectedCategory.equals(getString(R.string.firebase_category_new_arrivals))) {
            return R.mipmap.thuc_uong_moi;
        }
        if (selectedCategory.equals(getString(R.string.firebase_category_best_sellers))) {
            return R.mipmap.cac_mon_hot;
        }
        if (selectedCategory.equals(getString(R.string.firebase_category_fruit_tea))) {
            return R.mipmap.tra_dai_loan;
        }
        return 0;
    }
}
