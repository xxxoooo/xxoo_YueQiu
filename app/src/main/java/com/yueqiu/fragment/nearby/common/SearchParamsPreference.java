package com.yueqiu.fragment.nearby.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by scguo on 15/1/21.
 *
 * 用于辅助完成用户需要的多重筛选操作
 *
 */
public class SearchParamsPreference implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private static final String TAG = "SearchParamsPreference";

    // 球友当中的参数
    private static final String KEY_MATE_RANGE_PARAMS = "keyMateRange";
    private static final String KEY_MATE_GENDER_PARAMS = "keyMateGender";

    // 约球当中的筛选参数
    private static final String KEY_DATING_RANGE_PARAMS = "keyDatingRange";
    private static final String KEY_DATING_PUBLISHED_DATE_PARAMS = "keyDatingPublishedDate";

    // 助教当中的筛选参数
    private static final String KEY_ASCOUCH_RANGE_PARANS = "keyASCouchRange";
    private static final String KEY_ASCOUCH_PRICE_PARAMS = "keyASCouchPrice";
    private static final String KEY_ASCOUCH_CLAZZ_PARAMS = "keyASCouchClazz";
    private static final String KEY_ASCOUCH_LEVEL_PARAMS = "keyASCouchLevel";

    // 教练当中的筛选参数
    private static final String KEY_COUCH_LEVEL_PARAMS = "keyCouchLevel";
    private static final String KEY_COUCH_CLAZZ_PARAMS = "keyCouchClazz";

    // **对于球厅当中的筛选参数，我们需要根据大众点评提供的接口单独进行筛选** //

    private SharedPreferences mSharedPreference = null;

    private static SearchParamsPreference sPreference = null;

    private SearchParamsPreference(){}

    public static SearchParamsPreference getInstance()
    {
        return sPreference == null ? sPreference = new SearchParamsPreference() : sPreference;
    }

    public SearchParamsPreference ensurePreference(Context context)
    {
        if ( null == mSharedPreference )
            mSharedPreference = PreferenceManager.getDefaultSharedPreferences(context);

        return this;
    }

    public String getMateRange(Context context)
    {
        ensurePreference(context);
        return mSharedPreference.getString(KEY_MATE_RANGE_PARAMS, "");
    }

    public String getMateGender(Context context)
    {
        ensurePreference(context);
        return mSharedPreference.getString(KEY_MATE_GENDER_PARAMS, "");
    }

    public void setMateRange(Context context, String range)
    {
        ensurePreference(context);
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putString(KEY_MATE_RANGE_PARAMS, range);
        editor.commit();
    }

    public void setMateGender(Context context, String gender)
    {
        ensurePreference(context);
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putString(KEY_MATE_GENDER_PARAMS, gender);
        editor.commit();
    }

    public String getDatingRange(Context context)
    {
        ensurePreference(context);
        return mSharedPreference.getString(KEY_DATING_RANGE_PARAMS, "");
    }

    public String getDatingPublishedDate(Context context)
    {
        ensurePreference(context);
        return mSharedPreference.getString(KEY_DATING_PUBLISHED_DATE_PARAMS, "");
    }

    public void setDatingRange(Context context, String range)
    {
        ensurePreference(context);
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putString(KEY_DATING_RANGE_PARAMS, range);
        editor.commit();
    }

    public void setDatingPublishedDate(Context context, String publishedDate)
    {
        ensurePreference(context);
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putString(KEY_DATING_PUBLISHED_DATE_PARAMS, publishedDate);
        editor.commit();
    }

    public String getAScouchRange(Context context)
    {
        ensurePreference(context);
        return mSharedPreference.getString(KEY_ASCOUCH_RANGE_PARANS, "");
    }

    public String getASCouchPrice(Context context)
    {
        ensurePreference(context);
        return mSharedPreference.getString(KEY_ASCOUCH_PRICE_PARAMS, "");
    }

    public String getASCouchClazz(Context context)
    {
        ensurePreference(context);
        return mSharedPreference.getString(KEY_ASCOUCH_CLAZZ_PARAMS, "");
    }

    public String getASCouchLevel(Context context)
    {
        ensurePreference(context);
        return mSharedPreference.getString(KEY_ASCOUCH_LEVEL_PARAMS, "");
    }

    public void setAScouchRange(Context context, String range)
    {
        ensurePreference(context);
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putString(KEY_ASCOUCH_RANGE_PARANS, range);
        editor.commit();
    }

    public void setAScouchLevel(Context context, String level)
    {
        ensurePreference(context);
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putString(KEY_ASCOUCH_LEVEL_PARAMS, level);
        editor.commit();
    }
    public void setAScouchPrice(Context context, String price)
    {
        ensurePreference(context);
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putString(KEY_ASCOUCH_PRICE_PARAMS, price);
        editor.commit();
    }
    public void setAScouchClazz(Context context, String clazz)
    {
        ensurePreference(context);
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putString(KEY_ASCOUCH_CLAZZ_PARAMS, clazz);
        editor.commit();
    }

    public String getCouchLevel(Context context)
    {
        ensurePreference(context);
        return mSharedPreference.getString(KEY_COUCH_LEVEL_PARAMS, "");
    }

    public String getCouchClazz(Context context)
    {
        ensurePreference(context);
        return mSharedPreference.getString(KEY_COUCH_CLAZZ_PARAMS, "");
    }

    public void setCouchLevel(Context context, String level)
    {
        ensurePreference(context);
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putString(KEY_COUCH_LEVEL_PARAMS, level);
        editor.commit();
    }
    public void setCouchClazz(Context context, String clazz)
    {
        ensurePreference(context);
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putString(KEY_COUCH_CLAZZ_PARAMS, clazz);
        editor.commit();
    }

    /**
     * Called when a shared preference is changed, added, or removed. This
     * may be called even if a preference is set to its existing value.
     * <p/>
     * <p>This callback will be run on your main thread.
     *
     * @param sharedPreferences The {@link android.content.SharedPreferences} that received
     *                          the change.
     * @param key               The key of the preference that was changed, added, or
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {

    }
}




























































