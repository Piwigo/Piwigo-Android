package org.piwigo.internal.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.squareup.picasso.Cache;

import org.piwigo.PiwigoApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class PiwigoImageCache implements Cache {

    private String cacheDir;

    public PiwigoImageCache(PiwigoApplication piwigoApplication) {
        this.cacheDir = piwigoApplication.getCacheDir().getAbsolutePath();
    }

    private String getPathFromKey(String key)
    {
        return (key.substring(key.indexOf("/"), key.indexOf("\n")));
    }

    @Override
    public Bitmap get(String key) {
        File file = new File(cacheDir + "/bmp/" + getPathFromKey(key));

        Log.d("PiwigoImageCache", "Getting image from cache: " + file.getAbsolutePath());

        if (file.exists()) {
            try {
                return BitmapFactory.decodeStream(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                //@TODO Here we silently do nothing to avoid flooding logs..
            }
        }
        return null;
    }

    @Override
    public void set(String key, Bitmap bitmap) {
        File file = new File(cacheDir + "/bmp/" + getPathFromKey(key));
        file.getParentFile().mkdirs();

        Log.d("PiwigoImageCache", "Writing image to cache: " + file.getAbsolutePath());

        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            //@TODO Here we silently do nothing to avoid flooding logs..
        }
    }

    @Override
    public int size() {
        File dir = new File(cacheDir + "/bmp/");
        int len = 0;

        for (File file : dir.listFiles())
            len += file.length();
        return (len);
    }

    @Override
    public int maxSize() {
        File file = new File(cacheDir + "/bmp/");

        return ((int)file.getFreeSpace());
    }

    @Override
    public void clear() {
        File dir = new File(cacheDir + "/bmp/");

        for (File file : dir.listFiles())
            if (file.exists()) file.delete();
    }

    @Override
    public void clearKeyUri(String keyPrefix) {
        File dir = new File(cacheDir + "/bmp/");

        for (File file : dir.listFiles())
            if (file.getName().startsWith(keyPrefix)) file.delete();
    }
}