package com.yueqiu.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImgUtil {
	private static final String TAG = "LoadImageUtil";
    private static final int ww=480;
    private static final int hh=800;
	private static ImgUtil instance;
	private static HashMap<String, SoftReference<Bitmap>> imgCaches;
	private static ExecutorService executorThreadPool = Executors
			.newFixedThreadPool(1);
	static {
		instance = new ImgUtil();
		imgCaches = new HashMap<String, SoftReference<Bitmap>>();
	}

	public static ImgUtil getInstance() {
		if (instance != null) {
			return instance;
		}
		return null;
	}

	public void loadBitmap(final String path,
			final OnLoadBitmapListener listener) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Bitmap bitmap = (Bitmap) msg.obj;
				listener.loadImage(bitmap, path);
			}
		};
		new Thread() {

			@Override
			public void run() {
				executorThreadPool.execute(new Runnable() {
					@Override
					public void run() {
						Bitmap bitmap = loadBitmapFromCache(path);
						if (bitmap != null) {
							Message msg = handler.obtainMessage();
							msg.obj = bitmap;
							handler.sendMessage(msg);
						}

					}
				});
			}

		}.start();
	}

	private Bitmap loadBitmapFromCache(String path) {
		if (imgCaches == null) {
			imgCaches = new HashMap<String, SoftReference<Bitmap>>();
		}
		Bitmap bitmap = null;
		if (imgCaches.containsKey(path)) {
			bitmap = imgCaches.get(path).get();
		}
		if (bitmap == null) {
			bitmap = loadBitmapFromLocal(path);
		}
		return bitmap;
	}

	private Bitmap loadBitmapFromLocal(String path) {
		if (path == null) {
			return null;
		}
		Options options = new Options();
		options.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		float height = 800f;
		float width = 480f;
		float scale = 1;
		if (options.outWidth > width && options.outWidth > options.outHeight) {
			scale = options.outWidth / width;
		} else if (options.outHeight > height
				&& options.outHeight > options.outWidth) {
			scale = options.outHeight / height;
		} else {
			scale = 1;
		}
		options.inSampleSize = (int) scale;
		options.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeFile(path, options);
		bitmap = decodeBitmap(bitmap);
		if (!imgCaches.containsKey(path)) {
			//imgCaches.put(path, new SoftReference<Bitmap>(bitmap));
			addCache(path, bitmap);
		}
		return bitmap;
	}

	private Bitmap decodeBitmap(Bitmap bitmap) {
		int scale = 100;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, scale, bos);
		while ((bos.toByteArray().length / 1024) > 30) {
			bos.reset();
			bitmap.compress(Bitmap.CompressFormat.JPEG, scale, bos);
			scale -= 10;
		}
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		bitmap = BitmapFactory.decodeStream(bis);
		return bitmap;
	}
	
	public void addCache(String path,Bitmap bitmap){
		imgCaches.put(path, new SoftReference<Bitmap>(bitmap));
	}
	
	public void reomoveCache(String path){
		imgCaches.remove(path);
	}

	public interface OnLoadBitmapListener {
		void loadImage(Bitmap bitmap, String path);
	}

    public static int saveBitmap(String path,Bitmap bitmap){
        int result=-1;
        try {
            FileOutputStream fos=new FileOutputStream(new File(path));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            result=1;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

    }
    public static Bitmap getdecodeBitmap(String filePath){
        if(filePath==null){
            return null;
        }
        Options options=new Options();
        options.inJustDecodeBounds=true;
        Bitmap bitmap= BitmapFactory.decodeFile(filePath, options);

        int width=options.outWidth;
        int height=options.outHeight;
        float scale=1f;
        if(width>ww&&width>height){
            scale=width/ww;
        }else if(height>hh&&height>width){
            scale=height/hh;
        }else{
            scale=1;
        }

        options.inSampleSize=(int) scale;
        options.inJustDecodeBounds=false;
        bitmap= BitmapFactory.decodeFile(filePath, options);
        return bitmap;
    }

    public static int saveBitmap(String path,byte[] buffer){
        int result=-1;
        try {
            FileOutputStream out=new FileOutputStream(new File(path));
            out.write(buffer);
            out.flush();
            out.close();
            result=1;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

    }
    /**
     * 图片变圆角
     * @param bitmap
     * @param pixels
     * @return
     */
    public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }
}
