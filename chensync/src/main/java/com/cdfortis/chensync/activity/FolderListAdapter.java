package com.cdfortis.chensync.activity;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cdfortis.chensync.FolderInfo;
import com.cdfortis.chensync.FolderStatus;
import com.cdfortis.chensync.R;

import java.util.List;
import java.util.Map;

/**
 * Created by Diuy on 2017/4/5.
 * FolderListAdapter
 */

public class FolderListAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<FolderInfo> folderInfos;
    private Map<String, FolderStatus> folderStatus;
    private Context context;
    private View.OnClickListener onClickListener;

    public FolderListAdapter(Context context, List<FolderInfo> folderInfos, Map<String, FolderStatus> folderStatus, View.OnClickListener onClickListener) {
        this.inflater = LayoutInflater.from(context);
        this.folderInfos = folderInfos;
        this.folderStatus = folderStatus;
        this.context = context;
        this.onClickListener = onClickListener;
    }

    @Override
    public int getCount() {
        return folderInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return folderInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.folder_list_item, null);
            holder = new Holder();
            holder.textFolder = (TextView) convertView.findViewById(R.id.textFolder);
            holder.textWifi = (TextView) convertView.findViewById(R.id.textWifi);
            holder.textMessage = (TextView) convertView.findViewById(R.id.textMessage);
            holder.textFile = (TextView) convertView.findViewById(R.id.textFile);
            holder.textProgress = (TextView) convertView.findViewById(R.id.textProgress);
            holder.btnSync = (Button) convertView.findViewById(R.id.btnSync);
            holder.viewStatus = (LinearLayout) convertView.findViewById(R.id.viewStatus);
            holder.btnSync.setOnClickListener(onClickListener);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        FolderInfo folderInfo = folderInfos.get(position);
        FolderStatus folderStatus = this.folderStatus.get(folderInfo.id);

        holder.textFolder.setText(folderInfo.folder);
        holder.textWifi.setText(folderInfo.wifi);
        holder.textMessage.setText("");
        holder.textFile.setText("");
        holder.textProgress.setText("");
        holder.btnSync.setTag(position);

        if (folderStatus != null) {
            holder.textMessage.setText(folderStatus.message);
            holder.textFile.setText(folderStatus.file);
            if (!TextUtils.isEmpty(folderStatus.file))
                holder.textProgress.setText("" + folderStatus.percent + "%");
            holder.viewStatus.setVisibility(View.VISIBLE);
        }else{
            holder.viewStatus.setVisibility(View.GONE);
        }

        if (folderStatus == null || folderStatus.finish != 0) {
            holder.btnSync.setText("同步");
        } else {
            holder.btnSync.setText("取消");
        }

        if(folderStatus != null && folderStatus.finish == -1){
            holder.textMessage.setTextColor(0xFFF00000);
        }else{
            holder.textMessage.setTextColor(0xFF787878);
        }

        return convertView;
    }


    private class Holder {
        TextView textFolder;
        TextView textWifi;
        TextView textMessage;
        TextView textFile;
        TextView textProgress;
        Button btnSync;
        LinearLayout viewStatus;
    }

}
