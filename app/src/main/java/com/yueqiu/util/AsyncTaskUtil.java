package com.yueqiu.util;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.yueqiu.constant.PublicConstant;
import org.json.JSONObject;
import java.util.Map;

public class AsyncTaskUtil<T> extends AsyncTask<Map<String,String>,Void,JSONObject>{

    private Map<String,T> mMap;

    public AsyncTaskUtil(Map<String,T> map){
        mMap = map;
    }

    @Override
    protected JSONObject doInBackground(Map<String,String>... params) {
        String result = HttpUtil.urlClient(params[0].get(PublicConstant.URL),mMap, params[0].get(PublicConstant.METHOD));
        JSONObject jsonResult = Utils.parseJson(result);
        Log.d("wy","json->" + jsonResult);
        return jsonResult;
    }
}
