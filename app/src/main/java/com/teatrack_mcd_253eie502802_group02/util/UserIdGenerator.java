package com.teatrack_mcd_253eie502802_group02.util;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

/**
 * Tạo ID người dùng theo quy luật CS01, CS02, CS03...
 * Quét toàn bộ Users node, tìm số lớn nhất, cộng thêm 1.
 */
public class UserIdGenerator {

    public interface Callback {
        void onGenerated(String userId);
        void onError(String message);
    }

    /**
     * @param usersRef  DatabaseReference trỏ tới node "Users"
     * @param callback  trả về ID mới (ví dụ "CS08") hoặc lỗi
     */
    public static void next(DatabaseReference usersRef, Callback callback) {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int max = 0;
                for (DataSnapshot child : snapshot.getChildren()) {
                    String key = child.getKey();
                    if (key != null && key.matches("CS\\d+")) {
                        try {
                            int num = Integer.parseInt(key.substring(2));
                            if (num > max) max = num;
                        } catch (NumberFormatException ignored) {}
                    }
                }
                // Zero-pad tối thiểu 2 chữ số: CS08, CS09, CS10, CS11...
                String newId = String.format(Locale.US, "CS%02d", max + 1);
                callback.onGenerated(newId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}
