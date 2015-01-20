package com.yueqiu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.yueqiu.R;
import com.yueqiu.bean.SearchMateSubFragmentUserBean;
import com.yueqiu.fragment.nearby.common.SubFragmentsCommonUtils;
import com.yueqiu.util.VolleySingleton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by scguo on 14/12/19.
 *
 * 用于实现SearchActivity当中的球友Fragment的子Fragment当中的ListView的Adapter实现
 * 这个是用于展示每一个位于SearchActivity当中所有的Fragment当中的ListView的Adapter
 * 我们可以将这个Adapter构造的复杂一点，让他可以接受各种各样的数据，即尽量动态化。
 *
 * 现在时V1阶段，即仅供SearchSubMateFragment使用
 *
 */
public class SearchMateSubFragmentListAdapter extends BaseAdapter
{
    private static final String TAG = "SearchMateSubFragmentListAdapter";

    private LayoutInflater mInflater;
    // contains all of the user list
    private List<SearchMateSubFragmentUserBean> mUserList;
    private ImageLoader mImgLoader;
    public SearchMateSubFragmentListAdapter(Context context, ArrayList<SearchMateSubFragmentUserBean> userList)
    {
        mImgLoader = VolleySingleton.getInstance().getImgLoader();

        this.mInflater = LayoutInflater.from(context);
        this.mUserList = userList;
    }

    @Override
    public int getCount()
    {
        return mUserList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mUserList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    /**
     * 我们采用R.layout.search_mate_fragment_listitem_layout作为这个ListView每一个Item的布局文件
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        final ViewHolder viewHolder;
        if (convertView == null)
        {
            convertView = mInflater.inflate(R.layout.search_mate_fragment_listitem_layout, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.mUserPhoto = (NetworkImageView) convertView.findViewById(R.id.img_mate_subfragment_listitem_photo);
            viewHolder.mUserNickName = (TextView) convertView.findViewById(R.id.tv_mate_subfragment_listitem_nickname);
            viewHolder.mUserGender = (TextView) convertView.findViewById(R.id.tv_mate_subfragment_listitem_gender);
            viewHolder.mUserDistanceMeter = (TextView) convertView.findViewById(R.id.tv_mate_subfragment_listitem_distance_meter);
            viewHolder.mUserDistrict = (TextView) convertView.findViewById(R.id.tv_mate_subfragment_listitem_district);
            convertView.setTag(viewHolder);
        } else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        SearchMateSubFragmentUserBean userInsta = mUserList.get(position);

        // then, inflate the content of the listView item
        // the following user photo are all test url
        // TODO: 现在的我们的JSon数据返回的关于用户头像的图片仍然都是一些空值，我们需要在正式数据完成的时候，继续深入的检测以下
        // TODO: 我们在Layout文件当中已经设置关于UserPhoto的默认图片，我们在这里重新加载以下
        viewHolder.mUserPhoto.setDefaultImageResId(R.drawable.default_head);
        viewHolder.mUserPhoto.setImageUrl(userInsta.getUserPhotoUrl(), mImgLoader);
        viewHolder.mUserGender.setText(userInsta.getUserGender());
        viewHolder.mUserGender.setCompoundDrawablesWithIntrinsicBounds(0, 0, SubFragmentsCommonUtils.parseGenderDrawable(userInsta.getUserGender()), 0);
        viewHolder.mUserGender.setCompoundDrawablePadding(6);
        viewHolder.mUserNickName.setText(userInsta.getUserNickName());
        viewHolder.mUserDistanceMeter.setText(userInsta.getUserDistance());
        viewHolder.mUserDistrict.setText(userInsta.getUserDistrict());

        return convertView;
    }


    private static class ViewHolder
    {
        public NetworkImageView mUserPhoto;
        public TextView mUserNickName, mUserGender, mUserDistanceMeter, mUserDistrict;
    }
}
























