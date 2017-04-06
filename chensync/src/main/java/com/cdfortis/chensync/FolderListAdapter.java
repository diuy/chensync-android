package com.cdfortis.chensync;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by Diuy on 2017/4/5.
 * FolderListAdapter
 */

public class FolderListAdapter  extends BaseAdapter {
    private LayoutInflater inflater;
    private List<FolderStatus> statuses;
    private Context context ;

    public FolderListAdapter(Context context, List<FolderStatus> statuses){
        this.inflater = LayoutInflater.from(context);
        this.statuses = statuses;
        this.context = context;
    }
    @Override
    public int getCount() {
        return statuses.size();
    }

    @Override
    public Object getItem(int position) {
        return statuses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.folder_list_item, null);
        }
//        setImage(convertView,R.id.imgPlate,records.get(position).image);
//        setText(convertView,R.id.textDate,records.get(position).time+"-"+records.get(position).speed);
//        setText(convertView,R.id.textBestPlate,records.get(position).bestPlate);
//        setText(convertView,R.id.textPlate,records.get(position).plates);

        return convertView;
    }
}
