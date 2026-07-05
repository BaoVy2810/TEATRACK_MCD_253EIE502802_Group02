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

    public static final String DB_URL =
            "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";
    private static final String NODE = "products";

    private final DatabaseReference productsRef;

    // ── Public interfaces ──────────────────────────────────────────────────────
    public interface ProductsCallback {
        void onSuccess(List<Product> products);
        void onError(String message);
    }

    public interface ProductCallback {
        void onSuccess(Product product);
        void onError(String message);
    }

    // ── Constructor ────────────────────────────────────────────────────────────
    public FirebaseProductRepository() {
        productsRef = FirebaseDatabase.getInstance(DB_URL).getReference(NODE);
    }

    /** Called at app start to warm up the disk cache. */
    public void prefetch() {
        productsRef.keepSynced(true);
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    public void getAllProducts(ProductsCallback callback) {
        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Product> products = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Product product = child.getValue(Product.class);
                    if (product == null) continue;
                    if (product.getId() == null || product.getId().isEmpty()) {
                        product.setId(child.getKey());
                    }
                    products.add(product);
                }
                callback.onSuccess(products);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void getProductsByCategory(String category, ProductsCallback callback) {
        getAllProducts(new ProductsCallback() {
            @Override
            public void onSuccess(List<Product> all) {
                List<Product> filtered = new ArrayList<>();
                for (Product p : all) {
                    if (category.equals(p.getCategory())) filtered.add(p);
                }
                callback.onSuccess(filtered);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void getProductByName(String name, ProductCallback callback) {
        getAllProducts(new ProductsCallback() {
            @Override
            public void onSuccess(List<Product> all) {
                for (Product p : all) {
                    if (name.equals(p.getName())) {
                        callback.onSuccess(p);
                        return;
                    }
                }
                callback.onError("Product not found: " + name);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void getProductById(String id, ProductCallback callback) {
        if (id == null || id.isEmpty()) {
            callback.onError("Product id is empty");
            return;
        }
        productsRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Product product = snapshot.getValue(Product.class);
                if (product == null) {
                    callback.onError("Product not found: " + id);
                    return;
                }
                product.setId(snapshot.getKey());
                callback.onSuccess(product);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}
