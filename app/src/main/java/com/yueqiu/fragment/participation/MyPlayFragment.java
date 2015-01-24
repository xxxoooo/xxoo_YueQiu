package com.yueqiu.fragment.participation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.yueqiu.R;

/**
 * Created by doushuqi on 14/12/20.
 *
 */
public class MyPlayFragment extends Fragment {
    private static final String TAG = "MyPlayFragment";
    private ListView mListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_play, container, false);
        mListView = (ListView) view.findViewById(R.id.my_activities_list_view);
        mListView.setAdapter(new MyAdapter());
        return view;
    }

    class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return 10;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_my_play, null);
                viewHolder = new ViewHolder();
                viewHolder.mImageView = (ImageView) convertView.findViewById(R.id.my_activities_lv_item_iv_head);
                viewHolder.mNickName = (TextView) convertView.findViewById(R.id.my_activities_lv_item_tv_nick_name);
                viewHolder.mMessage = (TextView) convertView.findViewById(R.id.my_activities_lv_item_tv_message);
                viewHolder.mTime = (TextView) convertView.findViewById(R.id.my_activities_lv_item_tv_time);
                //绑定viewholder对象
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            //TODO:设置布局中控件的内容,need data!
            Log.e(TAG, String.valueOf(convertView));
//        viewHolder.mAccount.setText();
//        viewHolder.mLastMessage.setText();


            return convertView;
        }

        final class ViewHolder {
            public ImageView mImageView;
            public TextView mNickName;
            public TextView mMessage;
            public TextView mTime;
        }
    }
}