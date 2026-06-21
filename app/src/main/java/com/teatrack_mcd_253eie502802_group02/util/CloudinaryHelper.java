package com.teatrack_mcd_253eie502802_group02.util;

import android.content.Context;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class CloudinaryHelper {
    public static final String CLOUD_NAME = "dixd1w59t";
    public static final String API_KEY = "455641774934559";
    public static final String API_SECRET = "eppjhIoFmxGSM2an5-FU8NQY2jk";
    public static final String UPLOAD_PRESET = "teatrack_preset";

    public static void init(Context context) {
        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", CLOUD_NAME);
            config.put("api_key", API_KEY);
            config.put("api_secret", API_SECRET);
            MediaManager.init(context, config);
        } catch (IllegalStateException e) {
            // MediaManager already initialized
        }
    }
}
