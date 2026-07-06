package com.teatrack_mcd_253eie502802_group02.util;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.FirebaseOrder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class AgencyNameHelper {

    private static final String DB_URL =
            "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";

    private static final Map<String, String> agencyNameCache = new HashMap<>();
    private static boolean agencyIndexLoaded = false;
    private static final Map<String, String> agencyNameByAddress = new HashMap<>();

    private AgencyNameHelper() {
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

    private static void resolveByAddress(TextView tv, Context context, String branchAddress) {
        if (branchAddress == null || branchAddress.isEmpty()) {
            tv.setTag(null);
            tv.setText(context.getString(R.string.str_branch_unknown));
            return;
        }

        String addressKey = normalizeAddressKey(branchAddress);
        tv.setTag("address:" + addressKey);

        if (agencyIndexLoaded) {
            String name = agencyNameByAddress.get(addressKey);
            tv.setText(name != null && !name.isEmpty() ? name : branchAddress);
            return;
        }

        tv.setText(branchAddress);
        FirebaseDatabase.getInstance(DB_URL)
                .getReference("agencies")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        agencyNameByAddress.clear();
                        agencyNameCache.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String id = child.getKey();
                            String name = child.child("name").getValue(String.class);
                            String address = child.child("address").getValue(String.class);
                            if (id != null && name != null) {
                                agencyNameCache.put(id, name.trim());
                            }
                            if (address != null && name != null) {
                                agencyNameByAddress.put(normalizeAddressKey(address), name.trim());
                            }
                        }
                        agencyIndexLoaded = true;
                        if (!("address:" + addressKey).equals(tv.getTag())) return;
                        String name = agencyNameByAddress.get(addressKey);
                        tv.setText(name != null && !name.isEmpty() ? name : branchAddress);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (!("address:" + addressKey).equals(tv.getTag())) return;
                        tv.setText(branchAddress);
                    }
                });
    }

    private static String fallbackBranchLabel(Context context, String branchAddress) {
        if (branchAddress != null && !branchAddress.isEmpty()) return branchAddress;
        return context.getString(R.string.str_branch_unknown);
    }

    private static String normalizeAddressKey(String address) {
        return address.toLowerCase(Locale.US)
                .replaceAll("\\s+", " ")
                .trim();
    }
}
