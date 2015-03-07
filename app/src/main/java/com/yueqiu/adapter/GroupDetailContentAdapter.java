package com.yueqiu.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.yueqiu.R;
import com.yueqiu.bean.GroupDetailCommentItem;
import com.yueqiu.bean.GroupDetailContentItem;
import com.yueqiu.bean.IGroupDetailItem;
import com.yueqiu.util.VolleySingleton;

import java.util.List;

/**
 * Created by wangyun on 15/3/5.
 */
public class GroupDetailContentAdapter extends BaseAdapter{

    private List<IGroupDetailItem> mList;
    private Context mContext;
    private LayoutInflater mInflater;
    private ImageLoader mImgLoader;

    public GroupDetailContentAdapter(List<IGroupDetailItem> list, Context context) {
        this.mList = list;
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        this.mImgLoader = VolleySingleton.getInstance().getImgLoader();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        IGroupDetailItem item = (IGroupDetailItem) getItem(position);
        int type = item.getType();
        return type;
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
        ViewContentHolder contentHolder;
        ViewCommentHolder commentHolder;
        IGroupDetailItem item = (IGroupDetailItem) getItem(position);
        switch(item.getType()){
            case IGroupDetailItem.CONTENT_TYPE:
                if(convertView == null){
                    convertView = mInflater.inflate(R.layout.item_group_detail_content,null);
                    contentHolder = new ViewContentHolder();
                    contentHolder.title = (TextView) convertView.findViewById(R.id.group_detail_title);
                    contentHolder.content = (TextView) convertView.findViewById(R.id.group_detail_content);
                    contentHolder.image = (NetworkImageView) convertView.findViewById(R.id.group_detail_extra_img);
                    convertView.setTag(contentHolder);
                }else{
                    contentHolder = (ViewContentHolder) convertView.getTag();
                }
                GroupDetailContentItem contentItem = (GroupDetailContentItem) item;
                contentHolder.title.setText(contentItem.getTitle());
                contentHolder.content.setText(contentItem.getContent());
                contentHolder.image.setImageUrl("http://" + contentItem.getImg_url(),mImgLoader);
                break;
            case IGroupDetailItem.COMMENT_TYPE:
                if(convertView == null){
                    convertView = mInflater.inflate(R.layout.item_group_detail_comment,null);
                    commentHolder = new ViewCommentHolder();
                    commentHolder.image = (NetworkImageView) convertView.findViewById(R.id.group_detail_comment_img);
                    commentHolder.name = (TextView) convertView.findViewById(R.id.group_detail_comment_owner);
                    commentHolder.time = (TextView) convertView.findViewById(R.id.group_detail_comment_time);
                    commentHolder.comment = (TextView) convertView.findViewById(R.id.group_detail_comment_str);
                    convertView.setTag(commentHolder);
                }else{
                    commentHolder = (ViewCommentHolder) convertView.getTag();
                }
                GroupDetailCommentItem commentItem = (GroupDetailCommentItem) item;
                commentHolder.image.setDefaultImageResId(R.drawable.default_head);
                commentHolder.image.setErrorImageResId(R.drawable.default_head);
                commentHolder.image.setImageUrl("http://" + commentItem.getImg_url(),mImgLoader);
                commentHolder.name.setText(commentItem.getUsername());
                commentHolder.time.setText(commentItem.getCreate_time());
                commentHolder.comment.setText(commentItem.getContent());
                break;
        }
        return convertView;
    }

    class ViewContentHolder{
        TextView title;
        TextView content;
        NetworkImageView image;
    }

    class ViewCommentHolder{
        NetworkImageView image;
        TextView name;
        TextView time;
        TextView comment;
    }
}
