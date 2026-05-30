package com.root.common.shared;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class BitmapUtil {
    public Bitmap temp;






    /**
     * 将bitmap对象保存成图片到sd卡中
     */
    public void saveBitmapToSDCard(Bitmap bitmap, String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);//设置PNG的话，透明区域不会变成黑色

            fileOutputStream.close();
            System.out.println("----------save success-------------------");
        } catch (Exception v0) {
            v0.printStackTrace();
        }
    }

    /**
     * 从sd卡中获取图片的bitmap对象
     */
    public Bitmap getBitmapFromSDCard(String path) {
        Bitmap bitmap = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(path);
            if (fileInputStream != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2; //当图片资源太大的适合，会出现内存溢出。图片宽高都为原来的二分之一，即图片为原来的四分一
                bitmap = BitmapFactory.decodeStream(((InputStream) fileInputStream), null, options);
            }
        } catch (Exception e) {
            return null;
        }

        return bitmap;
    }
}