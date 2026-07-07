package com.teatrack_mcd_253eie502802_group02.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.teatrack_mcd_253eie502802_group02.model.ContactRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ContactSampleSeeder {

    private static final String MARKER_KEY = "contact_seed_01";
    private static final String MARKER_KEY_BATCH_2 = "contact_seed_11";
    private static final String BRANCH_SYNC_MARKER = "_branch_sync_v2";

    private static final String[] FALLBACK_BRANCH_NAMES = {
            "Ngô Gia - Quận 1",
            "Ngô Gia - Quận 3",
            "Ngô Gia - Quận 7",
            "Ngô Gia - Thủ Đức",
            "Ngô Gia - Quận 10",
            "Ngô Gia - Bình Thạnh",
            "Ngô Gia - Quận 5",
            "Ngô Gia - Tân Bình",
            "Ngô Gia - Phú Nhuận",
            "Ngô Gia - Quận 2",
            "Ngô Gia - Gò Vấp",
            "Ngô Gia - Quận 6",
            "Ngô Gia - Quận 8"
    };

    private ContactSampleSeeder() {
    }

    public static void ensureSamples(@NonNull DatabaseReference contactsRef, @Nullable Runnable onComplete) {
        seedIfEmpty(contactsRef, () -> seedMoreIfEmpty(contactsRef, onComplete));
    }

    public static void syncBranchNamesWithAgencies(
            @NonNull DatabaseReference rootRef,
            @Nullable Runnable onComplete) {
        DatabaseReference contactsRef = rootRef.child("contacts");
        contactsRef.child(BRANCH_SYNC_MARKER).addListenerForSingleValueEvent(
                new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot markerSnapshot) {
                        if (markerSnapshot.exists()
                                && Boolean.TRUE.equals(markerSnapshot.getValue(Boolean.class))) {
                            if (onComplete != null) {
                                onComplete.run();
                            }
                            return;
                        }

                        rootRef.child("agencies").addListenerForSingleValueEvent(
                                new com.google.firebase.database.ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot agenciesSnapshot) {
                                        List<String> agencyNames = extractAgencyNames(agenciesSnapshot);
                                        if (agencyNames.isEmpty()) {
                                            markBranchSyncComplete(contactsRef, onComplete);
                                            return;
                                        }

                                        contactsRef.addListenerForSingleValueEvent(
                                                new com.google.firebase.database.ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot contactsSnapshot) {
                                                        Map<String, Object> updates = new HashMap<>();
                                                        int index = 0;
                                                        for (DataSnapshot child : contactsSnapshot.getChildren()) {
                                                            String key = child.getKey();
                                                            if (key == null || key.startsWith("_")) {
                                                                continue;
                                                            }
                                                            String branch = child.child("branch").getValue(String.class);
                                                            String resolved = resolveAgencyName(branch, agencyNames, index);
                                                            index++;
                                                            if (resolved != null
                                                                    && (branch == null || !resolved.equals(branch.trim()))) {
                                                                updates.put(key + "/branch", resolved);
                                                            }
                                                        }
                                                        updates.put(BRANCH_SYNC_MARKER, true);
                                                        contactsRef.updateChildren(updates).addOnCompleteListener(task -> {
                                                            if (onComplete != null) {
                                                                onComplete.run();
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onCancelled(
                                                            @NonNull com.google.firebase.database.DatabaseError error) {
                                                        if (onComplete != null) {
                                                            onComplete.run();
                                                        }
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

                    @Override
                    public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }
                });
    }

    public static void seedIfEmpty(@NonNull DatabaseReference contactsRef, @Nullable Runnable onComplete) {
        contactsRef.child(MARKER_KEY).addListenerForSingleValueEvent(
                new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if (onComplete != null) {
                                onComplete.run();
                            }
                            return;
                        }

                        DatabaseReference agenciesRef = contactsRef.getRoot().child("agencies");
                        agenciesRef.addListenerForSingleValueEvent(
                                new com.google.firebase.database.ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot agenciesSnapshot) {
                                        List<String> agencyNames = extractAgencyNames(agenciesSnapshot);
                                        seedBatch(contactsRef, buildBatch1Rows(agencyNames), onComplete);
                                    }

                                    @Override
                                    public void onCancelled(
                                            @NonNull com.google.firebase.database.DatabaseError error) {
                                        seedBatch(contactsRef, buildBatch1Rows(null), onComplete);
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

    public static void seedMoreIfEmpty(@NonNull DatabaseReference contactsRef, @Nullable Runnable onComplete) {
        contactsRef.child(MARKER_KEY_BATCH_2).addListenerForSingleValueEvent(
                new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if (onComplete != null) {
                                onComplete.run();
                            }
                            return;
                        }

                        DatabaseReference agenciesRef = contactsRef.getRoot().child("agencies");
                        agenciesRef.addListenerForSingleValueEvent(
                                new com.google.firebase.database.ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot agenciesSnapshot) {
                                        List<String> agencyNames = extractAgencyNames(agenciesSnapshot);
                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("contact_seed_10/read", true);
                                        putContacts(updates, buildBatch2Rows(agencyNames));
                                        contactsRef.updateChildren(updates).addOnCompleteListener(task -> {
                                            if (onComplete != null) {
                                                onComplete.run();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(
                                            @NonNull com.google.firebase.database.DatabaseError error) {
                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("contact_seed_10/read", true);
                                        putContacts(updates, buildBatch2Rows(null));
                                        contactsRef.updateChildren(updates).addOnCompleteListener(task -> {
                                            if (onComplete != null) {
                                                onComplete.run();
                                            }
                                        });
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

    private static void markBranchSyncComplete(
            @NonNull DatabaseReference contactsRef,
            @Nullable Runnable onComplete) {
        contactsRef.child(BRANCH_SYNC_MARKER).setValue(true).addOnCompleteListener(task -> {
            if (onComplete != null) {
                onComplete.run();
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

    @NonNull
    private static List<String> extractAgencyNames(@NonNull DataSnapshot agenciesSnapshot) {
        List<String> names = new ArrayList<>();
        for (DataSnapshot child : agenciesSnapshot.getChildren()) {
            String name = child.child("name").getValue(String.class);
            if (name != null && !name.trim().isEmpty()) {
                names.add(name.trim());
            }
        }
        return names;
    }

    @Nullable
    private static String resolveAgencyName(
            @Nullable String branchRef,
            @NonNull List<String> agencyNames,
            int fallbackIndex) {
        if (branchRef == null || branchRef.trim().isEmpty()) {
            return pickBranch(agencyNames, fallbackIndex);
        }

        String branch = branchRef.trim();
        for (String name : agencyNames) {
            if (branch.equalsIgnoreCase(name)) {
                return name;
            }
        }

        String districtSuffix = extractDistrictSuffix(branch);
        if (districtSuffix != null) {
            String districtKey = normalizeKey(districtSuffix);
            for (String name : agencyNames) {
                if (normalizeKey(name).contains(districtKey)) {
                    return name;
                }
            }
        }

        String branchKey = normalizeKey(branch);
        for (String name : agencyNames) {
            String nameKey = normalizeKey(name);
            if (branchKey.contains(nameKey) || nameKey.contains(branchKey)) {
                return name;
            }
        }

        return pickBranch(agencyNames, fallbackIndex);
    }

    private static String pickBranch(@Nullable List<String> agencyNames, int index) {
        if (agencyNames != null && !agencyNames.isEmpty()) {
            return agencyNames.get(Math.floorMod(index, agencyNames.size()));
        }
        return FALLBACK_BRANCH_NAMES[Math.floorMod(index, FALLBACK_BRANCH_NAMES.length)];
    }

    @Nullable
    private static String extractDistrictSuffix(@NonNull String value) {
        int separator = value.lastIndexOf(" - ");
        if (separator >= 0 && separator + 3 < value.length()) {
            return value.substring(separator + 3).trim();
        }
        return null;
    }

    private static String normalizeKey(String value) {
        return value.toLowerCase(Locale.US).replaceAll("\\s+", " ").trim();
    }

    private static Object[][] buildBatch1Rows(@Nullable List<String> agencyNames) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        return new Object[][]{
                {"contact_seed_01", "Nguyễn Minh Anh", "minhanh@gmail.com", "0901234567",
                        pickBranch(agencyNames, 0), "praise",
                        "Trà sữa Ngô Gia hôm qua rất thơm, nhân viên phục vụ nhiệt tình!",
                        daysAgo(calendar, sdf, 1, 10, 30), 1, false, ""},
                {"contact_seed_02", "Trần Bảo Ngọc", "baongoc@email.com", "0912345678",
                        pickBranch(agencyNames, 1), "praise",
                        "Mình rất thích trà trái cây nhiệt đới, vị chua ngọt vừa phải.",
                        daysAgo(calendar, sdf, 2, 14, 15), 2, true,
                        "Cảm ơn bạn đã ủng hộ Ngô Gia. Hẹn gặp lại bạn sớm nhé!"},
                {"contact_seed_03", "Lê Hoàng Phúc", "hoangphuc@gmail.com", "0923456789",
                        pickBranch(agencyNames, 2), "suggest",
                        "Mong shop có thêm topping pudding và size 700ml cho team đi làm.",
                        daysAgo(calendar, sdf, 1, 16, 45), 1, false, ""},
                {"contact_seed_04", "Phạm Thu Hà", "thuha@email.com", "0934567890",
                        pickBranch(agencyNames, 3), "suggest",
                        "Nên mở thêm khung giờ giao hàng buổi tối sau 20h.",
                        daysAgo(calendar, sdf, 3, 9, 20), 2, true,
                        "Đã ghi nhận đề xuất và chuyển bộ phận vận hành xem xét."},
                {"contact_seed_05", "Hồng Hạnh", "honghanh@gmail.com", "0945678901",
                        pickBranch(agencyNames, 4), "complain",
                        "Đơn #HTNG-102 giao trễ hơn 40 phút, trà không còn đá.",
                        daysAgo(calendar, sdf, 0, 11, 5), 1, false, ""},
                {"contact_seed_06", "Võ Quốc Bình", "quocbinh@email.com", "0956789012",
                        pickBranch(agencyNames, 5), "complain",
                        "Nhân viên quên topping trân châu trong đơn mang đi.",
                        daysAgo(calendar, sdf, 2, 18, 50), 1, true, ""},
                {"contact_seed_07", "Đặng Kim Liên", "kimlien@gmail.com", "0967890123",
                        pickBranch(agencyNames, 6), "complain",
                        "App không cập nhật trạng thái đơn sau khi thanh toán MoMo.",
                        daysAgo(calendar, sdf, 4, 13, 40), 2, true,
                        "Lỗi đồng bộ đã được xử lý. Bạn vui lòng cập nhật app lên bản mới nhất."},
                {"contact_seed_08", "Bùi Gia Hân", "giahan@email.com", "0978901234",
                        pickBranch(agencyNames, 7), "other",
                        "Mình muốn hợp tác trưng bày sản phẩm tại sự kiện trường học.",
                        daysAgo(calendar, sdf, 1, 8, 15), 1, false, ""},
                {"contact_seed_09", "Ngô Thành Đạt", "thanhdat@gmail.com", "0989012345",
                        pickBranch(agencyNames, 8), "other",
                        "Xin thông tin tuyển part-time cuối tuần tại chi nhánh này.",
                        daysAgo(calendar, sdf, 5, 15, 0), 2, true,
                        "HR sẽ liên hệ qua email trong 2-3 ngày làm việc."},
                {"contact_seed_10", "Trịnh Lan Vy", "lanvy@email.com", "0990123456",
                        pickBranch(agencyNames, 0), "suggest",
                        "Có thể thêm tuỳ chọn ít đường 10% cho khách ăn kiêng không ạ?",
                        daysAgo(calendar, sdf, 0, 19, 25), 2, true,
                        "Đã bổ sung mức đường 10% trên menu từ tuần sau."},
        };
    }

    private static Object[][] buildBatch2Rows(@Nullable List<String> agencyNames) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        return new Object[][]{
                {"contact_seed_11", "Lý Thanh Tùng", "thanhtung@gmail.com", "0902111222",
                        pickBranch(agencyNames, 9), "praise",
                        "Không gian quán sạch sẽ, nhạc nhẹ rất dễ chịu khi làm việc.",
                        daysAgo(calendar, sdf, 0, 9, 10), 1, false, ""},
                {"contact_seed_12", "Mai Quỳnh Anh", "quynhanh@email.com", "0913222333",
                        pickBranch(agencyNames, 10), "suggest",
                        "Nên có combo 2 ly giảm 15% cho nhóm bạn đi học.",
                        daysAgo(calendar, sdf, 2, 12, 35), 2, true,
                        "Combo nhóm 2 ly đã được triển khai từ thứ Hai tuần này."},
                {"contact_seed_13", "Huỳnh Đức An", "ducan@gmail.com", "0924333444",
                        pickBranch(agencyNames, 11), "complain",
                        "Ly bị rò nước khi mang đi, túi giấy ướt sũng.",
                        daysAgo(calendar, sdf, 0, 14, 50), 1, false, ""},
                {"contact_seed_14", "Phan Ngọc Linh", "ngoclinh@email.com", "0935444555",
                        pickBranch(agencyNames, 12), "other",
                        "Cho mình xin menu PDF để in standee sự kiện công ty.",
                        daysAgo(calendar, sdf, 1, 17, 20), 1, false, ""},
                {"contact_seed_15", "Đỗ Minh Khôi", "minhkhoi@gmail.com", "0946555666",
                        pickBranch(agencyNames, 0), "praise",
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
