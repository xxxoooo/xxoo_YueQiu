package com.yueqiu.fragment.chatbar;

import android.app.ActionBar;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.bean.SearchPeopleInfo;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by doushuqi on 14/12/17.
 * 聊吧添加好友Fragment
 */
public class AddPersonFragment extends Fragment implements LocationListener {
    private static final String TAG = "AddPersonFragment";
    private static final int GET_SUCCESS = 0;
    private ActionBar mActionBar;
    private LinearLayout mLinearLayout;
    private View mProgressBar;
    private List<SearchPeopleInfo.SearchPeopleItemInfo> mList;
    private ListView mListView;
    private LocationManager mLocationManager;
    private double mLatitude, mLongitude;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        setMenuVisibility(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbar_add_persion, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mActionBar = getActivity().getActionBar();
        }
        mListView = (ListView) view.findViewById(R.id.search_result_container);
        mProgressBar = view.findViewById(R.id.chatbar_search_progressbar_container);
        mLinearLayout = (LinearLayout) view.findViewById(R.id.search_friend_nearby);
        mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //通过坐标查找好友
                mProgressBar.setVisibility(View.VISIBLE);
                getLocationInfo();
            }
        });

        return view;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_SUCCESS:
                    mProgressBar.setVisibility(View.GONE);
                    SearchPeopleInfo searchPeopleInfo = (SearchPeopleInfo) msg.obj;
                    mList = searchPeopleInfo.mList;
                    MyAdapter adapter = new MyAdapter(getActivity(), mList);
                    mListView.setAdapter(adapter);
                    break;
                default:
                    break;
            }
        }
    };

    private void searchFriendsByLocation(double latitude, double longitude) {
        Map<String, Double> map = new HashMap<String, Double>();
        map.put(HttpConstants.SearchPeopleByNearby.LAT, latitude);
        map.put(HttpConstants.SearchPeopleByNearby.LNG, longitude);
        String result = HttpUtil.urlClient(HttpConstants.SearchPeopleByNearby.URL, map, HttpConstants.RequestMethod.GET);
        try {
            JSONObject jsonResult = new JSONObject(result);

            if (jsonResult.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                SearchPeopleInfo searchPeople = new SearchPeopleInfo();
                searchPeople.setCount(jsonResult.getJSONObject("result").getInt("count"));
                JSONArray list_data = jsonResult.getJSONObject("result").getJSONArray("list_data");
                for (int i = 0; i < list_data.length(); i++) {
                    SearchPeopleInfo.SearchPeopleItemInfo itemInfo = searchPeople.new SearchPeopleItemInfo();
                    itemInfo.setUser_id(list_data.getJSONObject(i).getInt("user_id"));
                    itemInfo.setGroup_id(list_data.getJSONObject(i).getInt("group_id"));
                    itemInfo.setAccount(list_data.getJSONObject(i).getInt("account"));
                    itemInfo.setImg_url(list_data.getJSONObject(i).getString("img_url"));
                    itemInfo.setContent(list_data.getJSONObject(i).getString("content"));
                    itemInfo.setDatetime(list_data.getJSONObject(i).getString("datetime"));
                    searchPeople.mList.add(itemInfo);
                }
                mHandler.obtainMessage(GET_SUCCESS, searchPeople).sendToTarget();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getLocationInfo() {
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        /*
         * TODO:这里逻辑有待确认
         */
        if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            return;
        }else if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        }else {
            Toast.makeText(getActivity(), "请检查GPS和网络是否可用！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
        Log.d(TAG, "位置信息：latitude = " + mLatitude + " longitude = " + mLongitude);
        new Thread(new Runnable() {
            @Override
            public void run() {
                searchFriendsByLocation(mLatitude, mLongitude);
            }
        }).start();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void searchFriendsByKeyWords(String keyWords) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(HttpConstants.SearchPeopleByKeyword.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
        map.put(HttpConstants.SearchPeopleByKeyword.KEYWORDS, keyWords);
        String result = HttpUtil.urlClient(HttpConstants.SearchPeopleByNearby.URL, map, HttpConstants.RequestMethod.GET);
        try {
            JSONObject jsonResult = new JSONObject(result);

            if (jsonResult.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                SearchPeopleInfo searchPeople = new SearchPeopleInfo();
                searchPeople.setCount(jsonResult.getJSONObject("result").getInt("count"));
                JSONArray list_data = jsonResult.getJSONObject("result").getJSONArray("list_data");
                for (int i = 0; i < list_data.length(); i++) {
                    SearchPeopleInfo.SearchPeopleItemInfo itemInfo = searchPeople.new SearchPeopleItemInfo();
                    itemInfo.setUser_id(list_data.getJSONObject(i).getInt("user_id"));
                    itemInfo.setGroup_id(list_data.getJSONObject(i).getInt("group_id"));
                    itemInfo.setAccount(list_data.getJSONObject(i).getInt("account"));
                    itemInfo.setImg_url(list_data.getJSONObject(i).getString("img_url"));
                    itemInfo.setContent(list_data.getJSONObject(i).getString("content"));
                    itemInfo.setDatetime(list_data.getJSONObject(i).getString("datetime"));
                    searchPeople.mList.add(itemInfo);
                }
                mHandler.obtainMessage(GET_SUCCESS, searchPeople).sendToTarget();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    class MyAdapter extends BaseAdapter {
        private Context mContext;
        private List<SearchPeopleInfo.SearchPeopleItemInfo> mList;
        private LayoutInflater mInflater;

        MyAdapter(Context context, List<SearchPeopleInfo.SearchPeopleItemInfo> list) {
            this.mContext = context;
            this.mList = list;
            mInflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            return mList.size();
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
                convertView = mInflater.inflate(R.layout.my_activities_listview_item, null);
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
            Log.e(TAG, String.valueOf(convertView));
//            viewHolder.mImageView.setImageDrawable();//设置头像
            viewHolder.mNickName.setText(mList.get(position).getAccount());
            viewHolder.mMessage.setText(mList.get(position).getContent());
            viewHolder.mTime.setText(mList.get(position).getDatetime());
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
