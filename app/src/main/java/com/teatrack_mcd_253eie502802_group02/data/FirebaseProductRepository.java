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

    public interface ProductCallback {
        void onSuccess(Product product);

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
                    Product product = parseProduct(child);
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

    public void getAllProducts(ProductsCallback callback) {
        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Product> products = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Product product = parseProduct(child);
                    if (product != null) {
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

    public void getProductByName(String name, ProductCallback callback) {
        productsRef.orderByChild("name")
                .equalTo(name)
                .limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Product product = parseProduct(child);
                            if (product != null) {
                                callback.onSuccess(product);
                                return;
                            }
                        }
                        callback.onError("Product not found");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    private Product parseProduct(DataSnapshot snapshot) {
        Product product = snapshot.getValue(Product.class);
        if (product == null) {
            return null;
        }

        // Firebase schema uses "price" for size M.
        product.setPrice(readLong(snapshot, "price"));
        product.setPriceL(readLong(snapshot, "priceL"));
        product.setVipPriceM(readLong(snapshot, "vipPriceM"));
        product.setVipPriceL(readLong(snapshot, "vipPriceL"));
        product.setImages(readImages(snapshot));
        return product;
    }

    private List<String> readImages(DataSnapshot snapshot) {
        List<String> images = new ArrayList<>();
        DataSnapshot imagesNode = snapshot.child("images");
        if (imagesNode.exists()) {
            for (DataSnapshot child : imagesNode.getChildren()) {
                Object value = child.getValue();
                if (value == null) {
                    continue;
                }
                String image = String.valueOf(value).trim();
                if (!image.isEmpty()) {
                    images.add(image);
                }
            }
        }
        if (images.isEmpty()) {
            Object singleImage = snapshot.child("image").getValue();
            if (singleImage != null) {
                String image = String.valueOf(singleImage).trim();
                if (!image.isEmpty()) {
                    images.add(image);
                }
            }
        }
        return images;
    }

    private long readLong(DataSnapshot snapshot, String key) {
        Object raw = snapshot.child(key).getValue();
        if (raw == null) {
            return 0L;
        }
        if (raw instanceof Number) {
            return ((Number) raw).longValue();
        }
        String text = String.valueOf(raw).replaceAll("[^0-9]", "");
        if (text.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }
}
