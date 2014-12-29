package com.yueqiu.fragment.search.subfragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yueqiu.R;

/**
 * Created by scguo on 14/12/18.
 *
 * 这是针对球友当中的所有的子Fragment的父类
 */
@SuppressLint("ValidFragment")
public class SubBaseFragment extends Fragment
{
    private static final String TAG = "SubBaseFragment";

    private OnFragmentInteractionListener mCallbackListener;

    @SuppressLint("ValidFragment")
    public SubBaseFragment()
    {
    }

    private static final String KEY_FRAGMENT_PARAMS = "keyFragmentParams";

    public static SubBaseFragment newInstance(String params)
    {
        SubBaseFragment fragment = new SubBaseFragment();
        Bundle args = new Bundle();
        args.putString(KEY_FRAGMENT_PARAMS, params);
        fragment.setArguments(args);
        return fragment;
    }

    private String mParams;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // get the arguments that set while make an instance of SubBaseFragment
        if (getArguments() != null)
        {
            mParams = getArguments().getString(KEY_FRAGMENT_PARAMS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.mate_sub_fragment_base_layout, null);
        TextView textView = (TextView) view.findViewById(R.id.mate_subfragment_test_content);
        textView.setText(mParams);

        return view;
    }

    // define the callback that hook back into the fragment
    public static interface OnFragmentInteractionListener
    {
        public void onFragmentInteraction();
    }
}
