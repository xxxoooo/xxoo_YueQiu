package com.yueqiu.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.adapter.FavorBasicAdapter;
import com.yueqiu.bean.PublishedInfo;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yueqiu.bean.PublishedInfo.*;

/**
 * Created by wangyun on 14/12/30.
 */
public class MyFavorBasicFragment extends Fragment{
    private static final int GET_SUCCESS = 0;

    private View mView;
    private ListView mListView;
    private FavorBasicAdapter mAdapter;
    private int mType;
    private List<PublishedInfo.PublishedItemInfo> mList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_favor_basic_layout,null);

        Bundle mArgs = getArguments();
        mType = mArgs.getInt("type");


        mListView = (ListView) mView.findViewById(R.id.favor_basic_listView);

        new Thread(new Runnable() {
            @Override
            public void run() {
                requestResult();
            }
        }).start();
        return mView;
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case GET_SUCCESS:
                    Log.d("wy","success");
                    PublishedInfo publishedInfo = (PublishedInfo) msg.obj;
                    mList = publishedInfo.mList;
                    mAdapter = new FavorBasicAdapter(getActivity(),mList);
                    mListView.setAdapter(mAdapter);
                    break;
                default:
                    break;
            }
        }
    };

    private void requestResult(){
        Map<String,Integer> map = new HashMap<String, Integer>();
        map.put(PublicConstant.USER_ID, YueQiuApp.sUserInfo.getUser_id());
        map.put(HttpConstants.Published.TYPE,mType);
        map.put(HttpConstants.Published.STAR_NO,0);
        map.put(HttpConstants.Published.END_NO, 9);
        String result = HttpUtil.urlClient(HttpConstants.Published.URL,map, HttpConstants.RequestMethod.GET);
        try {
            JSONObject jsonResult = new JSONObject(result);

            if (jsonResult.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                PublishedInfo published = new PublishedInfo();
                published.setStart_no(jsonResult.getJSONObject("result").getInt("start_no"));
                published.setEnd_no(jsonResult.getJSONObject("result").getInt("end_no"));
                published.setSumCount(jsonResult.getJSONObject("result").getInt("count"));
                JSONArray list_data = jsonResult.getJSONObject("result").getJSONArray("list_data");

                for(int i=0;i<list_data.length();i++) {
                    PublishedInfo.PublishedItemInfo itemInfo = published.new PublishedItemInfo();
                    itemInfo.setTable_id(list_data.getJSONObject(i).getString("id"));
                    itemInfo.setImage_url(list_data.getJSONObject(i).getString("img_url"));
                    itemInfo.setTitle(list_data.getJSONObject(i).getString("title"));
                    itemInfo.setContent(list_data.getJSONObject(i).getString("content"));
                    itemInfo.setDateTime(list_data.getJSONObject(i).getString("create_time"));
                    published.mList.add(itemInfo);
                }
                mHandler.obtainMessage(GET_SUCCESS, published).sendToTarget();
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
