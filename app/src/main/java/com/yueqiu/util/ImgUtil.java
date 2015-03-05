package com.yueqiu.util;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Display;
import android.widget.ImageView;

import com.yueqiu.view.IssueImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImgUtil {
	private static ImgUtil instance;
	static {
		instance = new ImgUtil();
	}

	public static ImgUtil getInstance() {
		if (instance != null) {
			return instance;
		}
		return null;
	}
    public static BitmapDrawable getThumbnailScaledBitmap(Activity activity,String path,int destWidth,int destHeight){

        BitmapFactory.Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        int inSampleSize = 1;
        if(srcHeight > destHeight || srcWidth > destWidth){
//            if(srcWidth >srcHeight){
//                inSampleSize = Math.round(srcHeight / destHeight);
//            }else{
//                inSampleSize = Math.round(srcWidth / destWidth);
//            }
            final float halfSrcWidth = srcWidth / 2;
            final float halfSrcHeight = srcHeight / 2;

            while((halfSrcWidth / inSampleSize) > destWidth && (halfSrcHeight / inSampleSize) > destHeight){
                inSampleSize *= 2;
            }

        }

        options = new Options();
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(path,options);
        Bitmap roundBitmap = toRoundCorner(bitmap,8);
        return new BitmapDrawable(activity.getResources(),roundBitmap);
    }

    public static BitmapDrawable getLargeScaledBitmap(Activity activity,String path){
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int destWidth = point.x * 2 / 3;
        int destHeight = point.y * 2/ 3;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        int inSampleSize = 1;
        if(srcHeight > destHeight || srcWidth > destWidth){
            if(srcWidth >srcHeight){
                inSampleSize = Math.round(srcHeight / destHeight);
            }else{
                inSampleSize = Math.round(srcWidth / destWidth);
            }
        }

        options = new Options();
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(path,options);
        Bitmap roundBitmap = toRoundCorner(bitmap,8);
        return new BitmapDrawable(activity.getResources(),roundBitmap);
    }

    public static BitmapDrawable getThumbnailScaleBitmapByUri(Activity activity,Uri uri,int destWidth,int destHeight){
        BitmapFactory.Options options = new Options();
        options.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(uri),null,options);
            float srcWidth = options.outWidth;
            float srcHeight = options.outHeight;

            int inSampleSize = 1;
            if(srcHeight > destHeight || srcWidth > destWidth){
                final float halfSrcWidth = srcWidth / 2;
                final float halfSrcHeight = srcHeight / 2;

                while((halfSrcWidth / inSampleSize) > destWidth && (halfSrcHeight / inSampleSize) > destHeight){
                    inSampleSize *= 2;
                }

            }

            options = new Options();
            options.inSampleSize = inSampleSize;
            options.inJustDecodeBounds = false;

            Bitmap bitmap = BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(uri),null,options);
            Bitmap roundBitmap = toRoundCorner(bitmap,8);
            return new BitmapDrawable(activity.getResources(),roundBitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BitmapDrawable getLargeScaleBitmapFromUri(Activity activity,Uri uri){
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int destWidth = point.x *2 / 3;
        int destHeight = point.y * 2 / 3;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(uri),null,options);
            float srcWidth = options.outWidth;
            float srcHeight = options.outHeight;

            int inSampleSize = 1;
            if(srcHeight > destHeight || srcWidth > destWidth){
                final float halfSrcWidth = srcWidth / 2;
                final float halfSrcHeight = srcHeight / 2;

                while((halfSrcWidth / inSampleSize) > destWidth && (halfSrcHeight / inSampleSize) > destHeight){
                    inSampleSize *= 2;
                }

            }

            options = new Options();
            options.inSampleSize = inSampleSize;
            options.inJustDecodeBounds = false;

            Bitmap bitmap = BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(uri),null,options);
            Bitmap roundBitmap = toRoundCorner(bitmap,8);
            return new BitmapDrawable(activity.getResources(),roundBitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static  Bitmap getOriginBitmapByPath(Activity activity,String path){
        if(TextUtils.isEmpty(path)){
            return null;
        }
        File file = new File(path);
        if(!file.exists()){
            return null;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(path);
        return bitmap;
    }

    public static  Bitmap getOriginBitmapByUri(Activity activity,Uri uri){
        if(uri == null){
            return null;
        }
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(uri));
            return bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void clearImageView(List<IssueImageView> list){
        for(IssueImageView imageView : list) {
            if (!(imageView.getDrawable() instanceof BitmapDrawable)) {
                return;
            }
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            drawable.getBitmap().recycle();
            imageView.setImageDrawable(null);
            imageView.setBitmapBean(null);
        }
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

    /**
     * 嵌入图片
     * @param resources
     * @param source
     * @param embedImgId
     * @return
     */
    public static Bitmap embedBitmap(Resources resources,Bitmap source,int embedImgId){
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
//            options.inMutable = true;
//        }
        //options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        //Bitmap source = BitmapFactory.decodeResource(resources,sourceId,options);
        Bitmap embedded;
        if(source.isMutable()){
            embedded = source;
        }else{
            embedded = source.copy(Bitmap.Config.ARGB_8888,true);
            // TODO: 以下的是之前的实现，移除之后可以结合Volley当中的ImageLoader 使用，但是不移除的
            // TODO: 话，暂时还不确定是否会影响内存资源的回收问题
            // TODO: 关于source.recycle()内部的具体操作还不确定。需要参考StackOverflow上面的分析理解
//            source.recycle();
        }

        embedded.setHasAlpha(true);

        final int srcWidth = embedded.getWidth();
        final int srcHeight = embedded.getHeight();

        Canvas canvas = new Canvas(embedded);
        Bitmap mask = BitmapFactory.decodeResource(resources,embedImgId);
        final int maskWidth = mask.getWidth();
        final int maskHeight = mask.getHeight();

        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        canvas.drawBitmap(mask,srcWidth-maskWidth,srcHeight-maskHeight,paint);

        mask.recycle();

        return embedded;
    }
}
