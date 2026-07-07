package com.teatrack_mcd_253eie502802_group02.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.teatrack_mcd_253eie502802_group02.model.ContactRequest;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class ContactSampleSeeder {

    private static final String MARKER_KEY = "contact_seed_01";
    private static final String MARKER_KEY_BATCH_2 = "contact_seed_11";

    private ContactSampleSeeder() {
    }

    public static void ensureSamples(@NonNull DatabaseReference contactsRef, @Nullable Runnable onComplete) {
        seedIfEmpty(contactsRef, () -> seedMoreIfEmpty(contactsRef, onComplete));
    }

    public static void seedIfEmpty(@NonNull DatabaseReference contactsRef, @Nullable Runnable onComplete) {
        contactsRef.child(MARKER_KEY).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (onComplete != null) {
                        onComplete.run();
                    }
                    return;
                }
                seedBatch(contactsRef, buildBatch1Rows(), onComplete);
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }

    public static void seedMoreIfEmpty(@NonNull DatabaseReference contactsRef, @Nullable Runnable onComplete) {
        contactsRef.child(MARKER_KEY_BATCH_2).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (onComplete != null) {
                        onComplete.run();
                    }
                    return;
                }

                Map<String, Object> updates = new HashMap<>();
                updates.put("contact_seed_10/read", true);
                putContacts(updates, buildBatch2Rows());
                contactsRef.updateChildren(updates).addOnCompleteListener(task -> {
                    if (onComplete != null) {
                        onComplete.run();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }

    private static void seedBatch(@NonNull DatabaseReference contactsRef, Object[][] rows,
                                  @Nullable Runnable onComplete) {
        Map<String, Object> updates = new HashMap<>();
        putContacts(updates, rows);
        contactsRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    private static void putContacts(@NonNull Map<String, Object> updates, Object[][] rows) {
        for (Object[] row : rows) {
            ContactRequest request = new ContactRequest(
                    (String) row[0],
                    (String) row[1],
                    (String) row[2],
                    (String) row[3],
                    (String) row[4],
                    (String) row[5],
                    (String) row[6],
                    (String) row[7],
                    (Integer) row[8],
                    (Boolean) row[9],
                    (String) row[10]
            );
            updates.put((String) row[0], request);
        }
    }

    private static Object[][] buildBatch1Rows() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        return new Object[][]{
                {"contact_seed_01", "Nguyễn Minh Anh", "minhanh@gmail.com", "0901234567",
                        "TeaTrack Ngô Gia - Quận 1", "praise",
                        "Trà sữa Ngô Gia hôm qua rất thơm, nhân viên phục vụ nhiệt tình!",
                        daysAgo(calendar, sdf, 1, 10, 30), 1, false, ""},
                {"contact_seed_02", "Trần Bảo Ngọc", "baongoc@email.com", "0912345678",
                        "TeaTrack Ngô Gia - Quận 3", "praise",
                        "Mình rất thích trà trái cây nhiệt đới, vị chua ngọt vừa phải.",
                        daysAgo(calendar, sdf, 2, 14, 15), 2, true,
                        "Cảm ơn bạn đã ủng hộ Ngô Gia. Hẹn gặp lại bạn sớm nhé!"},
                {"contact_seed_03", "Lê Hoàng Phúc", "hoangphuc@gmail.com", "0923456789",
                        "TeaTrack Ngô Gia - Quận 7", "suggest",
                        "Mong shop có thêm topping pudding và size 700ml cho team đi làm.",
                        daysAgo(calendar, sdf, 1, 16, 45), 1, false, ""},
                {"contact_seed_04", "Phạm Thu Hà", "thuha@email.com", "0934567890",
                        "TeaTrack Ngô Gia - Thủ Đức", "suggest",
                        "Nên mở thêm khung giờ giao hàng buổi tối sau 20h.",
                        daysAgo(calendar, sdf, 3, 9, 20), 2, true,
                        "Đã ghi nhận đề xuất và chuyển bộ phận vận hành xem xét."},
                {"contact_seed_05", "Hồng Hạnh", "honghanh@gmail.com", "0945678901",
                        "TeaTrack Ngô Gia - Quận 10", "complain",
                        "Đơn #HTNG-102 giao trễ hơn 40 phút, trà không còn đá.",
                        daysAgo(calendar, sdf, 0, 11, 5), 1, false, ""},
                {"contact_seed_06", "Võ Quốc Bình", "quocbinh@email.com", "0956789012",
                        "TeaTrack Ngô Gia - Bình Thạnh", "complain",
                        "Nhân viên quên topping trân châu trong đơn mang đi.",
                        daysAgo(calendar, sdf, 2, 18, 50), 1, true, ""},
                {"contact_seed_07", "Đặng Kim Liên", "kimlien@gmail.com", "0967890123",
                        "TeaTrack Ngô Gia - Quận 5", "complain",
                        "App không cập nhật trạng thái đơn sau khi thanh toán MoMo.",
                        daysAgo(calendar, sdf, 4, 13, 40), 2, true,
                        "Lỗi đồng bộ đã được xử lý. Bạn vui lòng cập nhật app lên bản mới nhất."},
                {"contact_seed_08", "Bùi Gia Hân", "giahan@email.com", "0978901234",
                        "TeaTrack Ngô Gia - Tân Bình", "other",
                        "Mình muốn hợp tác trưng bày sản phẩm tại sự kiện trường học.",
                        daysAgo(calendar, sdf, 1, 8, 15), 1, false, ""},
                {"contact_seed_09", "Ngô Thành Đạt", "thanhdat@gmail.com", "0989012345",
                        "TeaTrack Ngô Gia - Phú Nhuận", "other",
                        "Xin thông tin tuyển part-time cuối tuần tại chi nhánh này.",
                        daysAgo(calendar, sdf, 5, 15, 0), 2, true,
                        "HR sẽ liên hệ qua email trong 2-3 ngày làm việc."},
                {"contact_seed_10", "Trịnh Lan Vy", "lanvy@email.com", "0990123456",
                        "TeaTrack Ngô Gia - Quận 1", "suggest",
                        "Có thể thêm tuỳ chọn ít đường 10% cho khách ăn kiêng không ạ?",
                        daysAgo(calendar, sdf, 0, 19, 25), 2, true,
                        "Đã bổ sung mức đường 10% trên menu từ tuần sau."},
        };
    }

    private static Object[][] buildBatch2Rows() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        return new Object[][]{
                {"contact_seed_11", "Lý Thanh Tùng", "thanhtung@gmail.com", "0902111222",
                        "TeaTrack Ngô Gia - Quận 2", "praise",
                        "Không gian quán sạch sẽ, nhạc nhẹ rất dễ chịu khi làm việc.",
                        daysAgo(calendar, sdf, 0, 9, 10), 1, false, ""},
                {"contact_seed_12", "Mai Quỳnh Anh", "quynhanh@email.com", "0913222333",
                        "TeaTrack Ngô Gia - Gò Vấp", "suggest",
                        "Nên có combo 2 ly giảm 15% cho nhóm bạn đi học.",
                        daysAgo(calendar, sdf, 2, 12, 35), 2, true,
                        "Combo nhóm 2 ly đã được triển khai từ thứ Hai tuần này."},
                {"contact_seed_13", "Huỳnh Đức An", "ducan@gmail.com", "0924333444",
                        "TeaTrack Ngô Gia - Quận 6", "complain",
                        "Ly bị rò nước khi mang đi, túi giấy ướt sũng.",
                        daysAgo(calendar, sdf, 0, 14, 50), 1, false, ""},
                {"contact_seed_14", "Phan Ngọc Linh", "ngoclinh@email.com", "0935444555",
                        "TeaTrack Ngô Gia - Quận 8", "other",
                        "Cho mình xin menu PDF để in standee sự kiện công ty.",
                        daysAgo(calendar, sdf, 1, 17, 20), 1, false, ""},
                {"contact_seed_15", "Đỗ Minh Khôi", "minhkhoi@gmail.com", "0946555666",
                        "TeaTrack Ngô Gia - Quận 1", "praise",
                        "Chương trình tích điểm đổi voucher rất hợp lý, mình sẽ giới thiệu bạn bè.",
                        daysAgo(calendar, sdf, 3, 20, 5), 2, true,
                        "Cảm ơn bạn! Nhớ dùng mã giới thiệu bạn bè để nhận thêm ưu đãi nhé."},
        };
    }

    private static String daysAgo(Calendar calendar, SimpleDateFormat sdf,
                                  int days, int hour, int minute) {
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.DAY_OF_YEAR, -days);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return sdf.format(calendar.getTime());
    }
}
