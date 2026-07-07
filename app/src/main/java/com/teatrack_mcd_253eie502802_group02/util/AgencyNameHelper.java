package com.teatrack_mcd_253eie502802_group02.util;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.FirebaseOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AgencyNameHelper {

    private static final String DB_URL =
            "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";

    private static final Map<String, String> agencyNameCache = new HashMap<>();
    private static final Map<String, String> agencyNameByAddress = new HashMap<>();
    private static final Map<String, String> agencyNameByLabel = new HashMap<>();
    private static final List<AgencyIndexEntry> agencyIndex = new ArrayList<>();

    private static boolean agencyIndexLoaded = false;
    private static boolean agencyIndexLoading = false;
    private static final List<Runnable> pendingIndexCallbacks = new ArrayList<>();

    private AgencyNameHelper() {
    }

    public static void bindBranchDisplay(
            @NonNull TextView tv,
            @Nullable String branchRef,
            @NonNull Context context) {
        if (branchRef == null || branchRef.trim().isEmpty()) {
            tv.setTag(null);
            tv.setText(context.getString(R.string.str_branch_unknown));
            return;
        }

        String branch = branchRef.trim();
        String tag = "contact-branch:" + normalizeKey(branch);
        tv.setTag(tag);

        if (agencyIndexLoaded) {
            applyContactBranch(tv, tag, branch, context);
            return;
        }

        tv.setText(branch);
        ensureAgencyIndex(() -> applyContactBranch(tv, tag, branch, context));
    }

    public static void loadName(@NonNull TextView tv, @NonNull FirebaseOrder order, @NonNull Context context) {
        String agencyId = order.getAgencyId();
        String branch = order.getBranchAddress();
        if (branch == null || branch.isEmpty()) {
            branch = order.getCustomerAddress();
        }
        final String fallbackAddress = branch;

        if (agencyId != null && !agencyId.isEmpty()) {
            tv.setTag("agency:" + agencyId);
            String cached = agencyNameCache.get(agencyId);
            if (cached != null) {
                tv.setText(cached.isEmpty() ? fallbackBranchLabel(context, fallbackAddress) : cached);
                return;
            }
            FirebaseDatabase.getInstance(DB_URL)
                    .getReference("agencies")
                    .child(agencyId)
                    .child("name")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String name = snapshot.getValue(String.class);
                            String resolved = name != null ? name.trim() : "";
                            agencyNameCache.put(agencyId, resolved);
                            if (!("agency:" + agencyId).equals(tv.getTag())) return;
                            tv.setText(resolved.isEmpty()
                                    ? fallbackBranchLabel(context, fallbackAddress)
                                    : resolved);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            if (!("agency:" + agencyId).equals(tv.getTag())) return;
                            tv.setText(fallbackBranchLabel(context, fallbackAddress));
                        }
                    });
            return;
        }

        resolveByAddress(tv, context, fallbackAddress);
    }

    private static void applyContactBranch(
            @NonNull TextView tv,
            @NonNull String tag,
            @NonNull String branch,
            @NonNull Context context) {
        if (!tag.equals(tv.getTag())) {
            return;
        }
        String resolved = resolveContactBranch(branch);
        tv.setText(resolved != null && !resolved.isEmpty()
                ? resolved
                : fallbackBranchLabel(context, branch));
    }

    @Nullable
    private static String resolveContactBranch(@NonNull String branchRef) {
        String branch = branchRef.trim();
        String key = normalizeKey(branch);

        String byId = agencyNameCache.get(branch);
        if (byId != null && !byId.isEmpty()) {
            return byId;
        }

        String byLabel = agencyNameByLabel.get(key);
        if (byLabel != null && !byLabel.isEmpty()) {
            return byLabel;
        }

        String byAddress = agencyNameByAddress.get(key);
        if (byAddress != null && !byAddress.isEmpty()) {
            return byAddress;
        }

        String districtSuffix = extractDistrictSuffix(branch);
        String districtKey = districtSuffix != null ? normalizeKey(districtSuffix) : null;

        String bestName = null;
        int bestScore = 0;
        for (AgencyIndexEntry entry : agencyIndex) {
            if (branch.equalsIgnoreCase(entry.name)) {
                return entry.name;
            }
            if (key.equals(entry.normalizedName)) {
                return entry.name;
            }
            if (districtKey != null
                    && (entry.normalizedName.contains(districtKey)
                    || entry.normalizedAddress.contains(districtKey))) {
                return entry.name;
            }
            if (key.contains(entry.normalizedName) || entry.normalizedName.contains(key)) {
                int score = entry.normalizedName.length();
                if (score > bestScore) {
                    bestScore = score;
                    bestName = entry.name;
                }
            }
        }
        return bestName;
    }

    private static void resolveByAddress(TextView tv, Context context, String branchAddress) {
        if (branchAddress == null || branchAddress.isEmpty()) {
            tv.setTag(null);
            tv.setText(context.getString(R.string.str_branch_unknown));
            return;
        }

        String addressKey = normalizeKey(branchAddress);
        tv.setTag("address:" + addressKey);

        if (agencyIndexLoaded) {
            String name = agencyNameByAddress.get(addressKey);
            tv.setText(name != null && !name.isEmpty() ? name : branchAddress);
            return;
        }

        tv.setText(branchAddress);
        ensureAgencyIndex(() -> {
            if (!("address:" + addressKey).equals(tv.getTag())) return;
            String name = agencyNameByAddress.get(addressKey);
            tv.setText(name != null && !name.isEmpty() ? name : branchAddress);
        });
    }

    private static void ensureAgencyIndex(@NonNull Runnable onReady) {
        if (agencyIndexLoaded) {
            onReady.run();
            return;
        }
        pendingIndexCallbacks.add(onReady);
        if (agencyIndexLoading) {
            return;
        }
        agencyIndexLoading = true;
        FirebaseDatabase.getInstance(DB_URL)
                .getReference("agencies")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        buildAgencyIndex(snapshot);
                        agencyIndexLoading = false;
                        List<Runnable> callbacks = new ArrayList<>(pendingIndexCallbacks);
                        pendingIndexCallbacks.clear();
                        for (Runnable callback : callbacks) {
                            callback.run();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        agencyIndexLoading = false;
                        pendingIndexCallbacks.clear();
                    }
                });
    }

    private static void buildAgencyIndex(@NonNull DataSnapshot snapshot) {
        agencyNameCache.clear();
        agencyNameByAddress.clear();
        agencyNameByLabel.clear();
        agencyIndex.clear();

        for (DataSnapshot child : snapshot.getChildren()) {
            String id = child.getKey();
            String name = trimToNull(child.child("name").getValue(String.class));
            String address = trimToNull(child.child("address").getValue(String.class));
            if (id == null || name == null) {
                continue;
            }

            agencyNameCache.put(id, name);
            agencyNameByLabel.put(normalizeKey(name), name);

            String normalizedAddress = address != null ? normalizeKey(address) : "";
            agencyIndex.add(new AgencyIndexEntry(id, name, address, normalizedAddress));

            if (address != null) {
                agencyNameByAddress.put(normalizedAddress, name);
            }
        }
        agencyIndexLoaded = true;
    }

    @Nullable
    private static String extractDistrictSuffix(@NonNull String value) {
        int separator = value.lastIndexOf(" - ");
        if (separator >= 0 && separator + 3 < value.length()) {
            return value.substring(separator + 3).trim();
        }
        return null;
    }

    @Nullable
    private static String trimToNull(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String fallbackBranchLabel(Context context, String branchAddress) {
        if (branchAddress != null && !branchAddress.isEmpty()) return branchAddress;
        return context.getString(R.string.str_branch_unknown);
    }

    private static String normalizeKey(String value) {
        return value.toLowerCase(Locale.US)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static final class AgencyIndexEntry {
        final String id;
        final String name;
        final String normalizedName;

        @SuppressWarnings("unused")
        final String address;
        final String normalizedAddress;

        AgencyIndexEntry(String id, String name, @Nullable String address, String normalizedAddress) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.normalizedName = normalizeKey(name);
            this.normalizedAddress = normalizedAddress;
        }
    }
}
