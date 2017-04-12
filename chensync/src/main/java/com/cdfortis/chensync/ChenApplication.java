package com.cdfortis.chensync;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Diuy on 2017/4/7.
 * ChenApplication
 */

public class ChenApplication extends Application {

    private final List<FolderInfo> folderInfos = new ArrayList<>();
    private Map<String, FolderStatus> folderStatuses = new HashMap<>();
    private Setting setting;

    @Override
    public void onCreate() {
        super.onCreate();
        loadFolders();
        loadSetting();
    }

    public List<FolderInfo> getFolderInfos() {
        return folderInfos;
    }

    public Map<String, FolderStatus> getFolderStatuses() {
        return folderStatuses;
    }

    public Setting getSetting() {
        return setting;
    }

    public void shutdown() {
        stopService(new Intent(this, SyncService.class));

    }

    private void loadSetting() {
        setting = new Setting();
        SharedPreferences preferences = this.getSharedPreferences(this.getPackageName(), 0);
        String str = preferences.getString("setting", "");
        if (!TextUtils.isEmpty(str)) {
            try {
                JSONObject object = new JSONObject(str);
                setting.setDevice(object.optString("device"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (TextUtils.isEmpty(setting.getDevice())) {
            setting.setDevice(Build.MODEL);
            Log.e("", Build.MODEL);
        }
    }

    public void saveSetting() {
        if (setting == null)
            return;
        JSONObject object = new JSONObject();
        try {
            object.putOpt("device", setting.getDevice());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SharedPreferences preferences = this.getSharedPreferences(this.getPackageName(), 0);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString("setting", object.toString());
        edit.apply();
    }

    private void loadFolders() {
        folderInfos.clear();
        SharedPreferences preferences = this.getSharedPreferences(this.getPackageName(), 0);
        String str = preferences.getString("folderInfos", "");
        if (TextUtils.isEmpty(str))
            return;
        try {
            JSONArray array = new JSONArray(str);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.optJSONObject(i);
                FolderInfo folderInfo = new FolderInfo();
                folderInfo.id = object.optString("id");
                folderInfo.ip = object.optString("ip");
                folderInfo.port = object.optInt("port", 0);
                folderInfo.folder = object.optString("folderInfo");
                folderInfo.wifi = object.optString("wifi");
                if (!TextUtils.isEmpty(folderInfo.id))
                    folderInfos.add(folderInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void saveFolders() {
        JSONArray array = new JSONArray();
        for (FolderInfo folderInfo : folderInfos) {
            JSONObject object = new JSONObject();
            try {
                object.putOpt("id", folderInfo.id);
                object.putOpt("ip", folderInfo.ip);
                object.putOpt("port", folderInfo.port);
                object.putOpt("folderInfo", folderInfo.folder);
                object.putOpt("wifi", folderInfo.wifi);
                array.put(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String str = array.toString();
        SharedPreferences preferences = this.getSharedPreferences(this.getPackageName(), 0);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString("folderInfos", str);
        edit.apply();
    }

}
