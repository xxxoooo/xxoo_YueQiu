package com.yueqiu.fragment.myprofilesetup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.yueqiu.R;

/**
 * Created by doushuqi on 15/1/4.
 */
public class PlayAgeSetupFragment extends Fragment {

    private EditText mEditText;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_setup, container, false);
        mEditText = (EditText) view.findViewById(R.id.my_profile_setup_text);
        mEditText.setHint(R.string.play_age);
        return view;
    }
}
