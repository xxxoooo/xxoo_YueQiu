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
    private ProgressBar mProgressBar;
    private TextView mTextView;

    public AsyncTaskUtil(Map<String,T> map,ProgressBar progressBar,TextView textView){
        this.mMap = map;
        this.mProgressBar = progressBar;
        this.mTextView = textView;
    }

    @Override
    protected JSONObject doInBackground(Map<String,String>... params) {
        String result = HttpUtil.urlClient(params[0].get(PublicConstant.URL),mMap, params[0].get(PublicConstant.METHOD));
        JSONObject jsonResult = Utils.parseJson(result);
        return jsonResult;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);

        mProgressBar.setVisibility(View.GONE);
        mTextView.setVisibility(View.GONE);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressBar.setVisibility(View.VISIBLE);
        mTextView.setVisibility(View.VISIBLE);
    }
}
