package com.teatrack_mcd_253eie502802_group02.data;

import android.os.Handler;
import android.os.Looper;

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

    // In-memory cache — shared across all instances, survives activity recreations
    private static volatile List<Product> sMemoryCache = null;

    private final DatabaseReference productsRef;

    public FirebaseProductRepository() {
        productsRef = FirebaseDatabase.getInstance().getReference("products");
        productsRef.keepSynced(true);
    }

    /**
     * Pre-warms the in-memory cache. Call from Application.onCreate() so the
     * cache is ready before the user navigates to the Menu screen.
     */
    public void prefetch() {
        if (sMemoryCache != null && !sMemoryCache.isEmpty()) {
            return; // already cached, nothing to do
        }
        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Product> products = parseAll(snapshot);
                if (!products.isEmpty()) {
                    sMemoryCache = products;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // prefetch is best-effort, ignore errors
            }
        });
    }

    public void getAllProducts(ProductsCallback callback) {
        // If memory cache is populated, deliver it immediately on the main thread,
        // then silently refresh in the background so the next call is always fresh.
        if (sMemoryCache != null && !sMemoryCache.isEmpty()) {
            final List<Product> cached = new ArrayList<>(sMemoryCache);
            new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(cached));
            refreshInBackground();
            return;
        }

        // No in-memory cache yet — fetch from Firebase.
        // With setPersistenceEnabled(true) + keepSynced(true) this hits the local disk
        // cache first (essentially instant after the first run) then updates from network.
        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Product> products = parseAll(snapshot);
                sMemoryCache = new ArrayList<>(products);
                callback.onSuccess(products);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    /** Silently updates the memory cache from Firebase without calling any callback. */
    private void refreshInBackground() {
        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Product> fresh = parseAll(snapshot);
                if (!fresh.isEmpty()) {
                    sMemoryCache = fresh;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // background refresh is best-effort
            }
        });
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

    private List<Product> parseAll(DataSnapshot snapshot) {
        List<Product> products = new ArrayList<>();
        for (DataSnapshot child : snapshot.getChildren()) {
            Product product = parseProduct(child);
            if (product != null) {
                products.add(product);
            }
        }
        return products;
    }

    private Product parseProduct(DataSnapshot snapshot) {
        Product product = snapshot.getValue(Product.class);
        if (product == null) {
            return null;
        }

        // Firebase schema uses "price" for size M.
        product.setPrice((int) readLong(snapshot, "price"));
        product.setPriceL((int) readLong(snapshot, "priceL"));
        product.setVipPriceM((int) readLong(snapshot, "vipPriceM"));
        product.setVipPriceL((int) readLong(snapshot, "vipPriceL"));
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
