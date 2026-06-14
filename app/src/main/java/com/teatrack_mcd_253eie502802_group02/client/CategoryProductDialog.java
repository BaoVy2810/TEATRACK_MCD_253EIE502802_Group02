package com.teatrack_mcd_253eie502802_group02.client;

import android.app.Dialog;
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

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.adapter.CategoryProductAdapter;
import com.teatrack_mcd_253eie502802_group02.model.Product;

import java.util.List;

/**
 * Dialog shown when the user taps an item_beverage_category on the homepage.
 * Shows a large hero image of the current product plus a horizontal list
 * of related products in the same category, with prev/next navigation.
 */
public class CategoryProductDialog extends DialogFragment {

    private List<Product> categoryProducts;
    private int currentIndex = 0;

    private ImageView imgHero;
    private RecyclerView rvProducts;
    private CategoryProductAdapter adapter;

    public static CategoryProductDialog newInstance(List<Product> products, int startIndex) {
        CategoryProductDialog dialog = new CategoryProductDialog();
        dialog.categoryProducts = products;
        dialog.currentIndex = startIndex;
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
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
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
            currentIndex = position;
            updateHero();
        });
        rvProducts.setAdapter(adapter);

        btnClose.setOnClickListener(v -> dismiss());

        btnPrev.setOnClickListener(v -> {
            if (categoryProducts == null || categoryProducts.isEmpty()) return;
            currentIndex = (currentIndex - 1 + categoryProducts.size()) % categoryProducts.size();
            updateHero();
        });

        btnNext.setOnClickListener(v -> {
            if (categoryProducts == null || categoryProducts.isEmpty()) return;
            currentIndex = (currentIndex + 1) % categoryProducts.size();
            updateHero();
        });

        updateHero();
    }

    private void updateHero() {
        if (categoryProducts == null || categoryProducts.isEmpty()) return;
        Product product = categoryProducts.get(currentIndex);
        imgHero.setImageResource(product.getImageRes());
        adapter.setSelectedPosition(currentIndex);
    }
}