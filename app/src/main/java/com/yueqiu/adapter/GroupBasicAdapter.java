package com.yueqiu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.rockerhieu.emojicon.EmojiconTextView;
import com.yueqiu.R;
import com.yueqiu.bean.GroupNoteInfo;
import com.yueqiu.util.VolleySingleton;
import com.yueqiu.view.CustomNetWorkImageView;

import java.util.List;

/**
 * Created by wangyun on 14/12/30.
 */
public class GroupBasicAdapter extends BaseAdapter{
    private List<GroupNoteInfo> mList;
    private Context mContext;
    private LayoutInflater mInflater;
    private ImageLoader mImgLoader;

    public GroupBasicAdapter(Context context,List<GroupNoteInfo> list){
        this.mContext = context;
        this.mList = list;
        this.mInflater = LayoutInflater.from(mContext);
        this.mImgLoader = VolleySingleton.getInstance().getImgLoader();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.item_billiard_group_layout,null);
            holder = new ViewHolder();
            holder.image = (CustomNetWorkImageView) convertView.findViewById(R.id.group_item_image);
            holder.title = (TextView) convertView.findViewById(R.id.billiard_group_title);
            holder.content = (EmojiconTextView) convertView.findViewById(R.id.group_content);
            holder.browseCount = (TextView) convertView.findViewById(R.id.billiard_group_browse_count_text);
            holder.commentCount = (TextView) convertView.findViewById(R.id.group_comment_count_text);
            holder.issueTime = (TextView) convertView.findViewById(R.id.group_issue_time);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        holder.image.setDefaultImageResId(R.drawable.default_head);
        holder.image.setErrorImageResId(R.drawable.default_head);
        holder.image.setImageUrl("http://"+ mList.get(position).getImg_url(),mImgLoader);
        holder.title.setText(mList.get(position).getTitle());
        holder.content.setText(mList.get(position).getContent());
        holder.browseCount.setText(""+mList.get(position).getBrowseCount());
        holder.commentCount.setText(""+mList.get(position).getCommentCount());
        holder.issueTime.setText(mList.get(position).getIssueTime());
        return convertView;
    }

    class ViewHolder{
        CustomNetWorkImageView image;
        TextView  title;
        EmojiconTextView content;
        TextView  browseCount;
        TextView  commentCount;
        TextView  issueTime;
    }
}
