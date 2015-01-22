package com.yueqiu.fragment.myprofilesetup;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.yueqiu.R;

/**
 * Created by doushuqi on 15/1/4.
 */
public class MyProfileRadioSetupFragment extends Fragment implements View.OnClickListener {
    private String[] mTexts;
    private int mCurrentIndex;
    private MyProfileSetupListener mListener;
    private View mView1, mView2, mView3, mThirdLine;
    private TextView mTextView1, mTextView2, mTextView3;
    private ImageView mImageView1, mImageView2, mImageView3;

    public MyProfileRadioSetupFragment(String[] text, int index) {
        mTexts = text;
        mCurrentIndex = index;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_radio_profile_setup, container, false);
        initView(view);
        initData();
        setListener();
        return view;
    }

    private void initView(View view) {
        mView1 = view.findViewById(R.id.the_first_content);
        mView2 = view.findViewById(R.id.the_second_content);
        mView3 = view.findViewById(R.id.the_third_content);
        mThirdLine = view.findViewById(R.id.the_third_line);
        mTextView1 = (TextView) view.findViewById(R.id.text_view1);
        mTextView2 = (TextView) view.findViewById(R.id.text_view2);
        mTextView3 = (TextView) view.findViewById(R.id.text_view3);
        mImageView1 = (ImageView) view.findViewById(R.id.the_first_iv);
        mImageView2 = (ImageView) view.findViewById(R.id.the_second_iv);
        mImageView3 = (ImageView) view.findViewById(R.id.the_third_iv);
        if (mCurrentIndex == 1)
            setVisible(mImageView1, mImageView2, mImageView3);
        else if (mCurrentIndex == 2)
            setVisible(mImageView2, mImageView1, mImageView3);
        else setVisible(mImageView3, mImageView1, mImageView2);
    }

    private void setVisible(ImageView v1, ImageView v2, ImageView v3) {
        v1.setVisibility(View.VISIBLE);
        v2.setVisibility(View.GONE);
        v3.setVisibility(View.GONE);
    }

    private void initData() {
        if (mTexts.length < 3) {
            mThirdLine.setVisibility(View.GONE);
            mView3.setVisibility(View.GONE);
            mTextView1.setText(mTexts[0]);
            mTextView2.setText(mTexts[1]);
        } else {
            mTextView1.setText(mTexts[0]);
            mTextView2.setText(mTexts[1]);
            mTextView3.setText(mTexts[2]);
        }
    }

    private void setListener() {
        mView1.setOnClickListener(this);
        mView2.setOnClickListener(this);
        mView3.setOnClickListener(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (MyProfileSetupListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement MyProfileSetupListener!");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.the_first_content:
                setVisible(mImageView1, mImageView2, mImageView3);
                mListener.setOnSetupListener("1");
                break;
            case R.id.the_second_content:
                setVisible(mImageView2, mImageView1, mImageView3);
                mListener.setOnSetupListener("2");
                break;
            case R.id.the_third_content:
                setVisible(mImageView3, mImageView1, mImageView2);
                mListener.setOnSetupListener("3");
                break;
            default:
                break;
        }
    }
}
