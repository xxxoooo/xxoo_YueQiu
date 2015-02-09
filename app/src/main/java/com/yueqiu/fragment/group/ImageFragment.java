package com.yueqiu.fragment.group;


import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.yueqiu.R;
import com.yueqiu.util.ImgUtil;

import java.util.ArrayList;
import java.util.List;

//TODO:图片太小时，有问题
public class ImageFragment extends DialogFragment {
  
    private static final String EXTRA_IMAGE_PATH = "image_path";
    private static final String EXTRA_IMAGE_URI = "image_uri";
    private String mImagePath;
    private Uri mImgUri;
    private ImageView mImageView;
    private List<ImageView> mList = new ArrayList<ImageView>();

    // TODO: Rename and change types and number of parameters
    public static ImageFragment newInstance(String imagePath,String imgUri) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_IMAGE_PATH, imagePath);
        args.putString(EXTRA_IMAGE_URI,imgUri);
        fragment.setArguments(args);
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE,0);
        return fragment;
    }

    public ImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mImagePath = getArguments().getString(EXTRA_IMAGE_PATH);
            mImgUri = getArguments().getString(EXTRA_IMAGE_URI) == null ? null : Uri.parse(getArguments().getString(EXTRA_IMAGE_URI));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_image_fragment,null);
        mImageView = (ImageView) view.findViewById(R.id.dialog_image);
        BitmapDrawable image = null;
        if(null != mImagePath) {
            image = ImgUtil.getLargeScaledBitmap(getActivity(), mImagePath);
        }
        if(null != mImgUri){
            image = ImgUtil.getLargeScaleBitmapFromUri(getActivity(),mImgUri);
        }
        mImageView.setImageDrawable(image);

        mList.add(mImageView);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //ImgUtil.clearImageView(mList);
    }
}
