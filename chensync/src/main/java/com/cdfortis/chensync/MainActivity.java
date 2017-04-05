package com.cdfortis.chensync;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private List<Folder> folders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SyncService.registerActionStatus(this, receiver);
        load();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == EditActivity.CODE_EDIT && resultCode == RESULT_OK){
            Folder folder =(Folder) data.getSerializableExtra(EditActivity.EXTRA_FOLDER);
            if(folder!=null){
                folders.add(folder);
                save();
                //TODO 刷新
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void load() {
        folders.clear();
        SharedPreferences preferences = this.getSharedPreferences("folders", 0);
        String str =  preferences.getString("folders","");
        if(TextUtils.isEmpty(str))
            return;
        try {
            JSONArray array = new JSONArray(str);
            for (int i = 0 ; i< array.length() ; i ++){
                JSONObject object = array.optJSONObject(i);
                Folder folder = new Folder();
                folder.ip = object.optString("ip");
                folder.port = object.optInt("port",0);
                folder.folder = object.optString("folder");
                folder.wifi = object.optString("wifi");
                folders.add(folder);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void save() {
        JSONArray array = new JSONArray();
        for (Folder folder : folders) {
            JSONObject object = new JSONObject();
            try {
                object.putOpt("ip", folder.ip);
                object.putOpt("port", folder.port);
                object.putOpt("folder", folder.folder);
                object.putOpt("wifi", folder.wifi);
                array.put(folder);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String str = array.toString();
        SharedPreferences preferences = this.getSharedPreferences("folders",0);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString("folders", str);
        edit.apply();
    }
}
