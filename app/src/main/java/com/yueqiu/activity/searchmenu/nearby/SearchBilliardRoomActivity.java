package com.yueqiu.activity.searchmenu.nearby;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.yueqiu.R;

/**
 * @author scguo
 *
 * 这是用于展示球厅的具体Activity
 * 当我们点击球厅子Fragment(BilliardsSearchRoomFragment)当中的ListView的任何的一个item，就会
 * 跳转到当前的这个Fragment当中
 *
 *
 */
public class SearchBilliardRoomActivity extends Activity
{
    private ImageView mRoomPhoto;
    private TextView mRoomName;
    private float mRoomRatingLevel;
    private TextView mRoomRatingNum;
    private RatingBar mRoomRatingBar;
    private TextView mRoomPrice, mRoomTag, mRoomAddress, mRoomPhone;
    private TextView mRoomDetailedInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_billiard_room);

        mRoomPhoto = (ImageView) findViewById(R.id.img_search_room_detailed_photo);
        mRoomName = (TextView) findViewById(R.id.tv_search_room_detailed_name);
        mRoomRatingBar = (RatingBar) findViewById(R.id.ratingbar_search_room_detailed_ratingbar);
        mRoomRatingNum = (TextView) findViewById(R.id.tv_search_room_level_num);

        // the tag textView collection here
        mRoomPrice = (TextView) findViewById(R.id.tv_search_room_per_people_price);
        mRoomTag = (TextView) findViewById(R.id.tv_search_room_tag);
        mRoomAddress = (TextView) findViewById(R.id.tv_search_room_address);
        mRoomPhone = (TextView) findViewById(R.id.tv_search_room_phone);

        mRoomDetailedInfo = (TextView) findViewById(R.id.tv_search_room_detailed_info);

        // then, we need the data that transferred from the previous listView item to inflate
        // the detailed content of these TextView and ImageViews

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_search_billiard_room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
