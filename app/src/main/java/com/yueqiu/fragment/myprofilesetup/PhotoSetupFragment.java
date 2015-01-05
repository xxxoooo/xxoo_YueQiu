package com.yueqiu.fragment.myprofilesetup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yueqiu.R;

/**
 * Created by doushuqi on 15/1/4.
 */
public class PhotoSetupFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //TODO:布局暂时写死！！！！
        View view = inflater.inflate(R.layout.fragment_upload_photo, container, false);

        return view;
    }
}
