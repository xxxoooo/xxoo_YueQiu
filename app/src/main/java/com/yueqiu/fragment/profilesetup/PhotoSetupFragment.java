package com.yueqiu.fragment.profilesetup;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.toolbox.NetworkImageView;
import com.yueqiu.R;

/**
 * Created by doushuqi on 15/1/4.
 */
public class PhotoSetupFragment extends Fragment {

    private Context mContext;
    private NetworkImageView mPhotoView;
    private ImageView mAddImg;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //TODO:布局暂时写死！！！！
        View view = inflater.inflate(R.layout.fragment_upload_photo, container, false);

        return view;
    }
}
