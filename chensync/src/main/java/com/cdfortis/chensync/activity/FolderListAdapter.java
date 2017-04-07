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
import com.cdfortis.chensync.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Diuy on 2017/4/5.
 * FolderListAdapter
 */

public class FolderListAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<FolderInfo> folderInfos;
    private Map<String, FolderStatus> statusMap = new HashMap<>();
    private Context context;
    private View.OnClickListener onClickListener;

    public FolderListAdapter(Context context, List<FolderInfo> folderInfos,View.OnClickListener onClickListener) {
        this.inflater = LayoutInflater.from(context);
        this.folderInfos = folderInfos;
        this.context = context;
        this.onClickListener = onClickListener;
    }

    public void removeStatus(String id){
        if(statusMap.containsKey(id)){
            statusMap.remove(id);
        }
    }

    public void setStatus(String id,String message,String file,int percent) {
        FolderStatus status ;
        if (statusMap.containsKey(id)) {
            status = statusMap.get(id);
        }else{
            status = new FolderStatus();
            statusMap.put(id,status);
        }
        if(message == null)
            status.message = "";
        else
            status.message = message;

        if(file == null)
            status.file = "";
        else
            status.file = file;

        status.percent = percent;
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
            holder.textFile = (TextView) convertView.findViewById(R.id.textFile);
            holder.textProgress = (TextView) convertView.findViewById(R.id.textProgress);
            holder.btnSync = (Button) convertView.findViewById(R.id.btnSync);
            holder.viewStatus = (LinearLayout)convertView.findViewById(R.id.viewStatus);
            holder.btnSync.setOnClickListener(onClickListener);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        FolderInfo folderInfo = folderInfos.get(position);
        FolderStatus folderStatus = statusMap.get(folderInfo.id);

        holder.textFolder.setText(folderInfo.folder);
        holder.textWifi.setText(folderInfo.wifi);
        holder.btnSync.setTag(position);

        if(folderStatus!=null){
            holder.textFolder.setText(folderStatus.message);
            holder.textFolder.setText(folderStatus.file);
            if(!TextUtils.isEmpty(folderStatus.file))
                holder.textFolder.setText(""+folderStatus.percent+"%");
            holder.viewStatus.setVisibility(View.VISIBLE);
            holder.btnSync.setText("取消");
        }else{
            holder.viewStatus.setVisibility(View.GONE);
            holder.btnSync.setText("同步");
        }
        return convertView;
    }


    private class Holder {
        TextView textFolder;
        TextView textWifi;
        TextView textFile;
        TextView textProgress;
        Button btnSync;
        LinearLayout viewStatus;
    }

    private class FolderStatus {
        public String message;
        public String file;
        public int percent;
    }
}
