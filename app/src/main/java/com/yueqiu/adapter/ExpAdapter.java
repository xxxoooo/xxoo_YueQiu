package com.yueqiu.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.yueqiu.R;
import com.yueqiu.bean.ContactsList;
import com.yueqiu.bean.RecentChat;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.util.FileUtil;
import com.yueqiu.util.VolleySingleton;
import com.yueqiu.view.CustomNetWorkImageView;
import com.yueqiu.view.contacts.IphoneTreeView;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;

public class ExpAdapter extends BaseExpandableListAdapter implements
        IphoneTreeView.IphoneTreeHeaderAdapter {

    private static final String TAG = "ExpAdapter";
    private Context mContext;
    private IphoneTreeView mIphoneTreeView;

    public void setData(HashMap<Integer, List<ContactsList.Contacts>> data) {
        mData = data;
    }
    private HashMap<Integer, List<ContactsList.Contacts>> mData;
    private ImageLoader mImgLoader;

    // 伪数据
    private HashMap<Integer, Integer> groupStatusMap;
    private String[] groups = {"球友", "助教", "教练"};

    public ExpAdapter(Context context, IphoneTreeView mIphoneTreeView) {
        this.mContext = context;
        this.mIphoneTreeView = mIphoneTreeView;
        this.groupStatusMap = new HashMap<Integer, Integer>();
        this.mImgLoader = VolleySingleton.getInstance().getImgLoader();
    }

    public ExpAdapter(Context context, HashMap<Integer, List<ContactsList.Contacts>> data, IphoneTreeView mIphoneTreeView) {
        this.mContext = context;
        this.mData = data;
        this.mIphoneTreeView = mIphoneTreeView;
        this.groupStatusMap = new HashMap<Integer, Integer>();
        this.mImgLoader = VolleySingleton.getInstance().getImgLoader();
    }

    public Object getChild(int groupPosition, int childPosition) {
        Object o = null;
        if (null != mData && null != mData.get(groupPosition))
            o = mData.get(groupPosition).get(childPosition);
        return o;
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public int getChildrenCount(int groupPosition) {
        int count = 0;
        if (null != mData && null != mData.get(groupPosition))
            count = mData.get(groupPosition).size();
        return count;
    }

    public Object getGroup(int groupPosition) {
        return groups[groupPosition];
    }

    public int getGroupCount() {
        return groups.length;
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        ChildHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.item_fragment_constact_child, null);
            holder = new ChildHolder();
            holder.nameView = (TextView) convertView.findViewById(R.id.contact_list_item_name);
            holder.feelView = (TextView) convertView.findViewById(R.id.cpntact_list_item_state);
            holder.iconView = (CustomNetWorkImageView) convertView.findViewById(R.id.icon);
            holder.date = (TextView) convertView.findViewById(R.id.contact_list_item_time);
            convertView.setTag(holder);
        } else {
            holder = (ChildHolder) convertView.getTag();
        }
        holder.iconView.setDefaultImageResId(R.drawable.default_head);
        holder.iconView.setErrorImageResId(R.drawable.default_head);
        holder.iconView.setImageUrl("http://" + ((ContactsList.Contacts) getChild(groupPosition, childPosition)).getImg_url(),mImgLoader);
        holder.nameView.setText(((ContactsList.Contacts) getChild(groupPosition, childPosition)).getUsername());
        holder.feelView.setText(((ContactsList.Contacts) getChild(groupPosition, childPosition)).getContent());
        holder.date.setText(((ContactsList.Contacts) getChild(groupPosition, childPosition)).getCreate_time());
        return convertView;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        GroupHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.item_fragment_constact_group, null);
            holder = new GroupHolder();
            holder.nameView = (TextView) convertView
                    .findViewById(R.id.group_name);
            holder.onLineView = (TextView) convertView
                    .findViewById(R.id.online_count);
            holder.iconView = (ImageView) convertView
                    .findViewById(R.id.group_indicator);
            convertView.setTag(holder);
        } else {
            holder = (GroupHolder) convertView.getTag();
        }
        holder.nameView.setText(groups[groupPosition]);
        holder.nameView.setTextColor(mContext.getResources().getColor(R.color.md__defaultBackground));
        holder.onLineView.setText(getChildrenCount(groupPosition) + "/"
                + getChildrenCount(groupPosition));
        if (isExpanded) {
            holder.iconView.setImageResource(R.drawable.arrow_down);
        } else {
            holder.iconView.setImageResource(R.drawable.arrow_right);
        }
        return convertView;
    }

    @Override
    public int getTreeHeaderState(int groupPosition, int childPosition) {
        final int childCount = getChildrenCount(groupPosition);
        if (childPosition == childCount - 1) {
            //mSearchView.setVisibility(View.GONE);
            return PINNED_HEADER_PUSHED_UP;
        } else if (childPosition == -1
                && !mIphoneTreeView.isGroupExpanded(groupPosition)) {
            //mSearchView.setVisibility(View.VISIBLE);
            return PINNED_HEADER_GONE;
        } else {
            //mSearchView.setVisibility(View.GONE);
            return PINNED_HEADER_VISIBLE;
        }
    }

    @Override
    public void configureTreeHeader(View header, int groupPosition,
                                    int childPosition, int alpha) {
        ((TextView) header.findViewById(R.id.group_name))
                .setText(groups[groupPosition]);
        ((TextView) header.findViewById(R.id.online_count))
                .setText(getChildrenCount(groupPosition) + "/"
                        + getChildrenCount(groupPosition));
    }

    @Override
    public void onHeadViewClick(int groupPosition, int status) {
        groupStatusMap.put(groupPosition, status);
    }

    @Override
    public int getHeadViewClickStatus(int groupPosition) {
        if (groupStatusMap.containsKey(groupPosition)) {
            return groupStatusMap.get(groupPosition);
        } else {
            return 0;
        }
    }

    class ChildHolder {
        TextView nameView;
        TextView feelView;
        CustomNetWorkImageView iconView;
        TextView date;
    }

    class GroupHolder {
        TextView nameView;
        TextView onLineView;
        ImageView iconView;
    }

}
