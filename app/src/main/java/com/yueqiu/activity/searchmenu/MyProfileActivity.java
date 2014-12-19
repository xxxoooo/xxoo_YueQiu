package com.yueqiu.activity.searchmenu;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.view.CornerListView;

/**
 * Created by doushuqi on 14/12/19.
 */
public class MyProfileActivity extends Activity {

    private CornerListView mCornerListView, mCornerListView2;
    private static final String ITEM_CATEGORY1[] = {"头像：", "账户：", "性别：", "", "昵称：", "区域：", "水平：", "球种：", "玩法：", "消费方式：", "约球时间："};
    private static final String ITEM_CATEGORY2[] = {"昵称：", "区域：", "水平：", "球种：", "玩法：", "消费方式：", "约球时间："};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_myprofile);
        mCornerListView = (CornerListView) findViewById(R.id.local_listView);
        MyAdapter adapter = new MyAdapter();
        mCornerListView.setAdapter(adapter);

        mCornerListView2 = (CornerListView) findViewById(R.id.local_listView2);
        MyAdapter2 adapter2 = new MyAdapter2();
        mCornerListView2.setAdapter(adapter2);
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 3;
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
            convertView = LayoutInflater.from(MyProfileActivity.this).inflate(R.layout.item_my_profile, null);
            TextView textView = (TextView) convertView.findViewById(R.id.myprofile_item_tv);
            textView.setText(ITEM_CATEGORY1[position]);
            TextView textView1 = (TextView) convertView.findViewById(R.id.myprofile_item_content_tv);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.myprofile_item_content_im);
            switch (position){
                case 0:
                    textView1.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    imageView.setVisibility(View.GONE);
                    textView1.setVisibility(View.VISIBLE);
                    textView1.setText("tqy123");
                    break;
                case 2:
                    imageView.setVisibility(View.GONE);
                    textView1.setVisibility(View.VISIBLE);
                    textView1.setText("女");
                    break;
            }

            return convertView;
        }
    }

    class MyAdapter2 extends BaseAdapter {

        @Override
        public int getCount() {
            return 7;
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
            convertView = LayoutInflater.from(MyProfileActivity.this).inflate(R.layout.item_my_profile, null);
            TextView textView = (TextView) convertView.findViewById(R.id.myprofile_item_tv);
            textView.setText(ITEM_CATEGORY2[position]);
            TextView textView1 = (TextView) convertView.findViewById(R.id.myprofile_item_content_tv);
            textView1.setVisibility(View.VISIBLE);
            //TODO:nedd database!!!
            textView1.setText("未设定");
            return convertView;
        }
    }


}
