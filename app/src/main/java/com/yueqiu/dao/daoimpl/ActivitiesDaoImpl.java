package com.yueqiu.dao.daoimpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.yueqiu.bean.Activities;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.dao.ActivitiesDao;
import com.yueqiu.db.DBUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yinfeng on 15/1/10.
 */
public class ActivitiesDaoImpl implements ActivitiesDao {
    private Context mContext;
    private DBUtils dbUtils;

    public ActivitiesDaoImpl(Context context) {
        this.mContext = context;
        dbUtils = DBUtils.getInstance(mContext);
    }


    @Override
    public boolean insertActiviesList(ArrayList<Activities> list) {
        SQLiteDatabase db = dbUtils.getWritableDatabase();
//        int size = list.size();
//        long ret = -1;
//        for (int i = 0; i < size; i++) {
//            ContentValues values = new ContentValues();
//            values.put(DatabaseConstant.ActivitiesTable._ID, list.get(i).getId());
//            values.put(DatabaseConstant.ActivitiesTable.IMG_URL, list.get(i).getImg_url());
//            values.put(DatabaseConstant.ActivitiesTable.TITLE, list.get(i).getTitle());
//            values.put(DatabaseConstant.ActivitiesTable.CONTENT, list.get(i).getContent());
//            values.put(DatabaseConstant.ActivitiesTable.CREATE_TIME, list.get(i).getCreate_time());
//            ret = db.insert(DatabaseConstant.ActivitiesTable.TABLENAME, null, values);
//        }
//        return ret == -1 ? false : true;
        try {
            db.beginTransaction();
            int size = list.size();
            for (int i = 0; i < size; i++) {
                StringBuffer sb = new StringBuffer();
                sb.append("insert or ignore into ").append(DatabaseConstant.ActivitiesTable.TABLENAME)
                        .append(" ( ").append(DatabaseConstant.ActivitiesTable._ID).append(",").
                        append(DatabaseConstant.ActivitiesTable.IMG_URL).append(",")
                        .append(DatabaseConstant.ActivitiesTable.TITLE).append(",")
                        .append(DatabaseConstant.ActivitiesTable.CONTENT).append(",")
                        .append(DatabaseConstant.ActivitiesTable.CREATE_TIME)
                        .append(" ) ")
                        .append(" values (").append(list.get(i).getId()).append(",");
                if (list.get(i).getImg_url().equals("") || list.get(i).getImg_url() == null) {
                    sb.append("null");
                }
                else
                {
                    sb.append("'");
                    sb.append(list.get(i).getImg_url()).append("'");
                }
                sb.append(",'").append(list.get(i).getTitle()).append("','").
                        append(list.get(i).getContent()).append("','").append(list.get(i).getCreate_time()).
                        append("');");
                db.execSQL(sb.toString());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }
        db.close();
        return false;
    }

    @Override
    public boolean insertActivities(Activities activities) {
        SQLiteDatabase db = dbUtils.getWritableDatabase();
        ContentValues values = getContentValues(activities);
        long ret = db.insert(DatabaseConstant.ActivitiesTable.TABLENAME, null, values);
//        try {
//            StringBuffer sb = new StringBuffer();
//            sb.append("insert or ignore into ").append(DatabaseConstant.ActivitiesTable.TABLENAME)
//                    .append(" values (").append(activities.getId()).append(",").
//                    append(activities.getUsername()).append(",").append(activities.getSex()).append(",").
//                    append(activities.getImg_url()).append(",").append(activities.getLook_num()).append(",").
//                    append(activities.getType()).append(",").append(activities.getTitle()).append(",").
//                    append(activities.getAddress()).append(",").append(activities.getBegin_time()).append(",").
//                    append(activities.getEnd_time()).append(",").append(activities.getModel()).append(",").
//                    append(activities.getContent()).append(",").append(activities.getCreate_time()).
//                    append(");");
//            Log.i("Demo", sb.toString());
//            db.execSQL(sb.toString());
//            return true;
//        } catch (Exception e) {
//
//        } finally {
//            db.close();
//        }
        return ret == -1 ? false : true;
    }

    private ContentValues getContentValues(Activities activities) {
        ContentValues values = new ContentValues();
        Field fields[] = activities.getClass().getDeclaredFields();
        int length = fields.length;
        for (int i = 0; i < length; i++) {
            fields[i].setAccessible(true);
            String fieldName = fields[i].getName();
            try {
                Method method = activities.getClass().getMethod("get" +
                        fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1), null);
                Object o = method.invoke(activities.getClass());
                if (o != null) {
                    if (o instanceof String) {
                        values.put(fieldName, (String) o);
                    } else if (o instanceof Integer) {
                        values.put(fieldName, (Integer) o);
                    }
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return values;
    }


    @Override
    public boolean updateActivities(Activities activities) {
        SQLiteDatabase db = dbUtils.getWritableDatabase();
        ContentValues values = getContentValues(activities);
        long ret = db.update(DatabaseConstant.ActivitiesTable.TABLENAME,
                values, DatabaseConstant.ActivitiesTable._ID + "=?",
                new String[]{String.valueOf(activities.getId())});
        db.close();
        return ret == -1 ? false : true;
    }

    @Override
    public ArrayList<Activities> getActivities(int start, int end) {
        SQLiteDatabase db = dbUtils.getReadableDatabase();
        String sql = "select * from " + DatabaseConstant.ActivitiesTable.TABLENAME +
                " order by " + DatabaseConstant.ActivitiesTable._ID + " DESC " + " limit "
                + start + "," + end;
        Cursor cursor = db.rawQuery(sql, null);
        int count = cursor.getColumnCount();
        ArrayList<Activities> list = new ArrayList<Activities>();
        if (count != 0) {
            while (cursor.moveToNext()) {
                Activities activities = new Activities();
                activities.setId(cursor.getString(cursor.getColumnIndex(DatabaseConstant.ActivitiesTable._ID)));
                activities.setContent(cursor.getString(cursor.getColumnIndex(DatabaseConstant.ActivitiesTable.CONTENT)));
                activities.setCreate_time(cursor.getString(cursor.getColumnIndex(DatabaseConstant.ActivitiesTable.CREATE_TIME)));
                activities.setTitle(cursor.getString(cursor.getColumnIndex(DatabaseConstant.ActivitiesTable.TITLE)));
                activities.setImg_url(cursor.getString(cursor.getColumnIndex(DatabaseConstant.ActivitiesTable.IMG_URL)));
                list.add(activities);
            }
        }
        db.close();
        return count == 0 ? null : list;
    }

    @Override
    public String getRefreshTime() {
        SQLiteDatabase db = dbUtils.getReadableDatabase();
        String ret = null;
        String sql = "SELECT " + DatabaseConstant.RefreshTime.REFRESH_TIME +
                " FROM " + DatabaseConstant.RefreshTime.REFRESH_TIME_TABLE + " WHERE " +
                DatabaseConstant.RefreshTime.TABLE_NAME + " =?;";
        Cursor cur = db.rawQuery(sql, new String[]{DatabaseConstant.ActivitiesTable.TABLENAME});
        while (cur.moveToNext()) {
            ret = cur.getString(cur.getColumnIndex(DatabaseConstant.RefreshTime.REFRESH_TIME));
        }
        db.close();
        return ret == null ? null : ret;
    }

    @Override
    public boolean UpdateRefreshTime(String time) {
        SQLiteDatabase db = dbUtils.getWritableDatabase();
        long ret = -1;
        ContentValues values = new ContentValues();
        values.put(DatabaseConstant.RefreshTime.REFRESH_TIME, time);
        ret = db.update(DatabaseConstant.RefreshTime.REFRESH_TIME_TABLE,
                values, DatabaseConstant.RefreshTime.TABLE_NAME,
                new String[]{DatabaseConstant.ActivitiesTable.TABLENAME});
        db.close();
        return ret == -1 ? false : true;
    }

    @Override
    public boolean addRefreshTime(String time) {
        SQLiteDatabase db = dbUtils.getWritableDatabase();
        long ret = -1;
        ContentValues values = new ContentValues();
        values.put(DatabaseConstant.RefreshTime.TABLE_NAME,
                DatabaseConstant.ActivitiesTable.TABLENAME);
        values.put(DatabaseConstant.RefreshTime.REFRESH_TIME_TABLE,
                time);
        ret = db.insert(DatabaseConstant.RefreshTime.REFRESH_TIME_TABLE, null, values);
        db.close();
        return ret == -1 ? false : true;
    }
}
