package com.yueqiu.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.bean.ContactsList;
import com.yueqiu.bean.RecentChat;
import com.yueqiu.util.FileUtil;
import com.yueqiu.view.contacts.IphoneTreeView;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;

public class ExpAdapter extends BaseExpandableListAdapter implements
        IphoneTreeView.IphoneTreeHeaderAdapter {

    private static final String TAG = "ExpAdapter";
    private Context mContext;
    private HashMap<String, List<RecentChat>> maps;
    private IphoneTreeView mIphoneTreeView;
    private View mSearchView;
    private HashMap<String, SoftReference<Bitmap>> hashMaps = new HashMap<String, SoftReference<Bitmap>>();
    private String dir = FileUtil.getRecentChatPath();

    public void setData(HashMap<Integer, List<ContactsList.Contacts>> data) {
        mData = data;
    }

    private HashMap<Integer, List<ContactsList.Contacts>> mData;

    // 伪数据
    private HashMap<Integer, Integer> groupStatusMap;
    private String[] groups = {"球友", "助教", "教练"};
    private String[][] children = {
            {"宋慧乔", "章泽天", "宋茜", "韩孝珠", "景甜", "刘亦菲", "康逸琨", "邓紫棋"},
            {"宋慧乔", "章泽天", "宋茜", "韩孝珠", "景甜", "刘亦菲"},
            {"宋慧乔", "章泽天", "宋茜", "韩孝珠", "景甜", "刘亦菲", "康逸琨", "邓紫棋"}};
    private String[][] childPath = {
            {dir + "songhuiqiao.jpg", dir + "zhangzetian.jpg",
                    dir + "songqian.jpg", dir + "hangxiaozhu.jpg",
                    dir + "jingtian.jpg", dir + "liuyifei.jpg",
                    dir + "kangyikun.jpg", dir + "dengziqi.jpg"},
            {dir + "songhuiqiao.jpg", dir + "zhangzetian.jpg",
                    dir + "songqian.jpg", dir + "hangxiaozhu.jpg",
                    dir + "jingtian.jpg", dir + "liuyifei.jpg",
                    dir + "kangyikun.jpg", dir + "dengziqi.jpg"},
            {dir + "songhuiqiao.jpg", dir + "zhangzetian.jpg",
                    dir + "songqian.jpg", dir + "hangxiaozhu.jpg",
                    dir + "jingtian.jpg", dir + "liuyifei.jpg",
                    dir + "kangyikun.jpg", dir + "dengziqi.jpg"}};

    public ExpAdapter(Context context, IphoneTreeView mIphoneTreeView) {
        this.mContext = context;
        this.mIphoneTreeView = mIphoneTreeView;
        groupStatusMap = new HashMap<Integer, Integer>();
//        dir = FileUtil.getRecentChatPath();
//        mSearchView = searchView;
    }

//    public ExpAdapter(Context context, HashMap<String, List<RecentChat>> maps, IphoneTreeView mIphoneTreeView) {
//        this.mContext = context;
//        this.maps = maps;
//        this.mIphoneTreeView = mIphoneTreeView;
//        groupStatusMap = new HashMap<Integer, Integer>();
////        dir = FileUtil.getRecentChatPath();
//    }

    public ExpAdapter(Context context, HashMap<Integer, List<ContactsList.Contacts>> data, IphoneTreeView mIphoneTreeView) {
        this.mContext = context;
        mData = data;
        this.mIphoneTreeView = mIphoneTreeView;
        groupStatusMap = new HashMap<Integer, Integer>();
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
        GroupHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.item_fragment_constact_child, null);
            holder = new GroupHolder();
            holder.nameView = (TextView) convertView
                    .findViewById(R.id.contact_list_item_name);
            holder.feelView = (TextView) convertView
                    .findViewById(R.id.cpntact_list_item_state);
            holder.iconView = (ImageView) convertView.findViewById(R.id.icon);
            holder.date = (TextView) convertView.findViewById(R.id.contact_list_item_time);
            convertView.setTag(holder);
        } else {
            holder = (GroupHolder) convertView.getTag();
        }

        String path = childPath[groupPosition][childPosition];
        /**
         * 高大上的好友头像加载机制，demo中为减轻资源的装载省略利用路径的方式加载图片！！
         */
//		if (hashMaps.containsKey(path)) {
//			holder.iconView.setImageBitmap(hashMaps.get(path).get());
//			// 另一个地方缓存释放资源
//			ImgUtil.getInstance().reomoveCache(path);
//		} else {
//			holder.iconView.setTag(path);
//			ImgUtil.getInstance().loadBitmap(path, new ImgUtil.OnLoadBitmapListener() {
//				@Override
//				public void loadImage(Bitmap bitmap, String path) {
//					ImageView iv = (ImageView) mIphoneTreeView
//							.findViewWithTag(path);
//					if (bitmap != null && iv != null) {
//						bitmap = SystemMethod.toRoundCorner(bitmap, 15);
//						iv.setImageBitmap(bitmap);
//
//						if (!hashMaps.containsKey(path)) {
//							hashMaps.put(path,
//									new SoftReference<Bitmap>(bitmap));
//						}
//					}
//				}
//			});
//
//		}
        holder.nameView.setText(((ContactsList.Contacts) getChild(groupPosition, childPosition)).getUsername());
        holder.feelView.setText(((ContactsList.Contacts) getChild(groupPosition, childPosition)).getContent());
        holder.date.setText(((ContactsList.Contacts) getChild(groupPosition, childPosition)).getCreate_time());
        return convertView;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        ChildHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.item_fragment_constact_group, null);
            holder = new ChildHolder();
            holder.nameView = (TextView) convertView
                    .findViewById(R.id.group_name);
            holder.onLineView = (TextView) convertView
                    .findViewById(R.id.online_count);
            holder.iconView = (ImageView) convertView
                    .findViewById(R.id.group_indicator);
            convertView.setTag(holder);
        } else {
            holder = (ChildHolder) convertView.getTag();
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

    class GroupHolder {
        TextView nameView;
        TextView feelView;
        ImageView iconView;
        TextView date;
    }

    class ChildHolder {
        TextView nameView;
        TextView onLineView;
        ImageView iconView;
    }

}
