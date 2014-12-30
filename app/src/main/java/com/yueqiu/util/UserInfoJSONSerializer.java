package com.yueqiu.util;

import android.content.Context;
import com.yueqiu.bean.UserInfo;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Created by doushuqi on 14/12/26.
 */
public class UserInfoJSONSerializer {

    private Context mContext;
    private String mFileName;

    public UserInfoJSONSerializer(Context context, String fileName) {
        mContext = context;
        mFileName = fileName;
    }

    public void saveUserInfo(ArrayList<UserInfo> userInfos) throws IOException, JSONException {
        JSONArray array = new JSONArray();
        for (UserInfo userInfo : userInfos) {
            array.put(userInfo.toJSON());
        }

        Writer writer = null;
        try {
            OutputStream out = mContext.openFileOutput(mFileName, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(out);
            writer.write(array.toString());
        } finally {
            if (writer != null)
                writer.close();
        }
    }
}
