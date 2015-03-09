package com.yueqiu.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.YueQiuSplashScreen;
import com.yueqiu.util.AspectRatioImgView;

/**
 * Created by scguo on 15/3/7.
 */
public class SplashScreenPageFragment extends Fragment
{
    // 我们在这里采用Bitmap而不是Drawable是因为bitmap是已经实现了Parcelable接口，我们可以直接
    // 将bitmap放到Bundle当中进行传输
    private Bitmap mBitmap;
    private Bitmap mDefaultBitmap;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Bundle data = getArguments();
        mBitmap = data.getParcelable(YueQiuSplashScreen.KEY_CUREENT_SCREEN_BITMAP);
        // 如果我们加载图片有误的话，我们就默认加载第一种图片吧
        mDefaultBitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.splash_launch_page_1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.splash_screen_fragment_layout, container, false);
        AspectRatioImgView imgView = (AspectRatioImgView) view.findViewById(R.id.splash_screen_imgview);
        imgView.setImageBitmap(mBitmap != null ? mBitmap : mDefaultBitmap);
        return view;
    }
}
