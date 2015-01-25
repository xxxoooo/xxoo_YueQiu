package com.yueqiu.fragment.profilesetup;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.yueqiu.R;

/**
 * Created by doushuqi on 15/1/4.
 */
public class MyProfileTextSetupFragment extends Fragment {
    private String mText;
    public MyProfileTextSetupFragment(String text) {
        mText = text;
    }
    private EditText mEditText;
    private MyProfileSetupListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_setup, container, false);
        mEditText = (EditText) view.findViewById(R.id.my_profile_setup_text);
        mEditText.setHint(mText);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mListener.setOnSetupListener(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (MyProfileSetupListener) activity;
        }catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement MyProfileSetupListener!");
        }
    }
}
