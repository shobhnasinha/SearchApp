package com.shobhna.searchapp.util;

import android.graphics.Bitmap;
import android.util.LruCache;

public class ImageCaches {

    public static LruCache<Integer, Bitmap> RESULTS_MEMORY_CACHE;

    static {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int resultsCacheSize = maxMemory / 4;
        RESULTS_MEMORY_CACHE = new LruCache<Integer, Bitmap>(resultsCacheSize) {
            @Override
            protected int sizeOf(Integer key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public static void addBitmapToMemoryCache(Integer key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null && bitmap != null && key != null) {
            RESULTS_MEMORY_CACHE.put(key, bitmap);
        }
    }

    public static Bitmap getBitmapFromMemCache(Integer key) {
        if (key == null) {
            return null;
        }
        return RESULTS_MEMORY_CACHE.get(key);
    }

    public static void clearLruCache() {
        RESULTS_MEMORY_CACHE.evictAll();
    }
}
