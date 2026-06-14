package com.teatrack_mcd_253eie502802_group02.data;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.model.Product;

import java.util.ArrayList;
import java.util.List;

public class FirebaseProductRepository {

    public interface ProductsCallback {
        void onSuccess(List<Product> products);

        void onError(String message);
    }

    private final DatabaseReference productsRef;

    public FirebaseProductRepository() {
        productsRef = FirebaseDatabase.getInstance().getReference("products");
    }

    public void getProductsByCategory(String category, ProductsCallback callback) {
        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Product> products = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Product product = child.getValue(Product.class);
                    if (product != null && category.equals(product.getCategory())) {
                        products.add(product);
                    }
                }
                callback.onSuccess(products);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}
