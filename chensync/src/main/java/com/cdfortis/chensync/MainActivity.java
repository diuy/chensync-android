package com.cdfortis.chensync;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends Activity {

    private List<FolderInfo> folderInfos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SyncService.registerActionStatus(this, receiver);
        load();
        //TODO 刷新
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String folder = intent.getStringExtra(SyncService.EXTRA_FOLDER);
            String file = intent.getStringExtra(SyncService.EXTRA_FILE);
            int progress = intent.getIntExtra(SyncService.EXTRA_PROGRESS, 0);
            String message = intent.getStringExtra(SyncService.EXTRA_MESSAGE);
            onStatus(folder, file, progress, message);
        }
    };

    private void onStatus(String folder, String file, int progress, String message) {

    }

    public void onSyncAll(View view) {

    }

    public void onAddFolder(View view) {
        startActivityForResult(new Intent(this, EditActivity.class), EditActivity.CODE_EDIT);
    }

    private FolderInfo getFolderInfo(String id){
        for (FolderInfo folderInfo: folderInfos){
            if(TextUtils.equals(id,folderInfo.id))
                return folderInfo;
        }
        return null;
    }

    private boolean hasSameFolder(FolderInfo folderInfo){
        for(FolderInfo f : folderInfos){
            if(TextUtils.equals(new File(folderInfo.folder).getAbsolutePath(),new File(f.folder).getAbsolutePath()) &&
                    TextUtils.equals(f.ip, folderInfo.ip) &&
                    f.port == folderInfo.port)
                return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == EditActivity.CODE_EDIT && resultCode == RESULT_OK){
            FolderInfo folderInfo =(FolderInfo) data.getSerializableExtra(EditActivity.EXTRA_FOLDER);
            if(folderInfo == null)
                return;

            if(hasSameFolder(folderInfo)){
                Toast.makeText(this,"相同地址包括相同的文件夹",Toast.LENGTH_LONG).show();
                return;
            }

            if(TextUtils.isEmpty(folderInfo.id)){
                folderInfo.id = UUID.randomUUID().toString();
                folderInfos.add(folderInfo);
                save();
                //TODO 刷新
            }else{
                FolderInfo fi = getFolderInfo(folderInfo.id);
                if(fi != null){
                    fi.copyFrom(folderInfo);
                    save();
                    //TODO 刷新
                }
            }
        }
    }

    private void load() {
        folderInfos.clear();
        SharedPreferences preferences = this.getSharedPreferences("folderInfos", 0);
        String str =  preferences.getString("folderInfos","");
        if(TextUtils.isEmpty(str))
            return;
        try {
            JSONArray array = new JSONArray(str);
            for (int i = 0 ; i< array.length() ; i ++){
                JSONObject object = array.optJSONObject(i);
                FolderInfo folderInfo = new FolderInfo();
                folderInfo.ip = object.optString("ip");
                folderInfo.port = object.optInt("port",0);
                folderInfo.folder = object.optString("folderInfo");
                folderInfo.wifi = object.optString("wifi");
                folderInfos.add(folderInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void save() {
        JSONArray array = new JSONArray();
        for (FolderInfo folderInfo : folderInfos) {
            JSONObject object = new JSONObject();
            try {
                object.putOpt("ip", folderInfo.ip);
                object.putOpt("port", folderInfo.port);
                object.putOpt("folderInfo", folderInfo.folder);
                object.putOpt("wifi", folderInfo.wifi);
                array.put(folderInfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String str = array.toString();
        SharedPreferences preferences = this.getSharedPreferences("folderInfos",0);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString("folderInfos", str);
        edit.apply();
    }
}
