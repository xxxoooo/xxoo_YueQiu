package com.yueqiu.util;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.yueqiu.YueQiuApp;

/**
 * Created by scguo on 15/1/16.
 *
 */
public class VolleySingleton
{
    // 关于Volley的使用我们最好定义成全局的Singleton(这是Google官方的推荐),这样就可以把所有的RequesteQueue只保存一份，可以节省资源
    // 而且我们也可以将所有涉及到图片的缓存都有一个统一的存放的地方
    private static VolleySingleton sInstance = null;
    private RequestQueue mRequestQueue;
    private ImageLoader mImgLoader;

    private VolleySingleton()
    {
        mRequestQueue = Volley.newRequestQueue(YueQiuApp.getAppContext());
        mImgLoader = new ImageLoader(this.mRequestQueue, new ImageLoader.ImageCache()
        {
            private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(20);
            @Override
            public Bitmap getBitmap(String url)
            {
                return mCache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap)
            {
                mCache.put(url, bitmap);
            }
        });
    }

    public static VolleySingleton getInstance()
    {
        return sInstance == null ? (sInstance = new VolleySingleton()) : sInstance;
    }

    public RequestQueue getRequestQueue()
    {
        return mRequestQueue;
    }

    public ImageLoader getImgLoader()
    {
        return mImgLoader;
    }
}
