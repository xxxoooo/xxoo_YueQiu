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
                } else {
                    sb.append("'");
                    sb.append(list.get(i).getImg_url()).append("'");
                }
                sb.append(",'").append(list.get(i).getTitle()).append("','").
                        append(list.get(i).getContent()).append("','").append(list.get(i).getCreate_time()).
                        append("');");
                db.execSQL(sb.toString());
            }
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            db.endTransaction();
        }
        return false;
    }

    @Override
    public boolean insertActivities(Activities activities) {
        SQLiteDatabase db = dbUtils.getWritableDatabase();
        ContentValues values = getContentValues(activities);
        long ret = db.insert(DatabaseConstant.ActivitiesTable.TABLENAME, null, values);
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
                Object o = method.invoke(activities);
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
        return ret == -1 ? false : true;
    }

    @Override
    public ArrayList<Activities> getActivities(int start, int end) {
        SQLiteDatabase db = dbUtils.getReadableDatabase();
        String sql = "select * from " + DatabaseConstant.ActivitiesTable.TABLENAME +
                " order by " + DatabaseConstant.ActivitiesTable._ID + " DESC " + " limit "
                + start + "," + 10;
        Cursor cursor = db.rawQuery(sql, null);
        int count = cursor.getColumnCount();
        Log.d("wy","count->" + count);
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
        cursor.close();

        return count == 0 ? null : list;
    }

    @Override
    public Activities getActivities(int id) {
        SQLiteDatabase db = dbUtils.getReadableDatabase();
        String sql = "SELECT * FROM " + DatabaseConstant.ActivitiesTable.TABLENAME + " WHERE " +
                DatabaseConstant.ActivitiesTable._ID + "=?";
        try {
            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(id)});
            return getActivitesByCursor(cursor);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return null;
    }


    private Activities getActivitesByCursor(Cursor cursor) {
        try {
            Activities activities = Activities.class.newInstance();
            Field[] fields = Activities.class.getDeclaredFields();
            int length = fields.length;
            if(cursor.moveToNext())
            {
                for (int i = 0; i < length; i++) {
                    fields[i].setAccessible(true);
                    if(!fields[i].getName().equals("look_num"))
                    {
                        fields[i].set(activities, cursor.getString(
                                    cursor.getColumnIndex(fields[i].getName())) );
                    }
                    else
                    {
                        fields[i].set(activities, cursor.getInt(
                                    cursor.getColumnIndex(fields[i].getName())) );
                    }
                }
            }
            return activities;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }finally {
            cursor.close();
        }

        return null;
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
        //db.close();
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
        return ret == -1 ? false : true;
    }
}
