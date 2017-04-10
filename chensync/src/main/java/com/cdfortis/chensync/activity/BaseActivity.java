package com.cdfortis.chensync.activity;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cdfortis.chensync.ChenApplication;
import com.cdfortis.chensync.ChenConstant;
import com.cdfortis.chensync.FolderInfo;
import com.cdfortis.chensync.FolderStatus;
import com.cdfortis.chensync.Setting;

import java.util.List;
import java.util.Map;

/**
 * Created by Diuy on 2017/4/7.
 * BaseActivity
 */

public class BaseActivity extends Activity implements ChenConstant {

    protected ChenApplication getChenApplication() {
        return (ChenApplication) getApplication();
    }

    protected List<FolderInfo> getFolderInfos() {
        return getChenApplication().getFolderInfos();
    }

    protected Setting getSetting() {
        return getChenApplication().getSetting();
    }

    protected Map<String, FolderStatus> getFolderStatuses() {
        return getChenApplication().getFolderStatuses();
    }

    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected void showView(int id) {
        findViewById(id).setVisibility(View.VISIBLE);
    }

    protected void hideView(int id) {
        findViewById(id).setVisibility(View.GONE);
    }

    protected void showToast(String message, boolean longTime) {
        if (longTime) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    protected void clearItemText(int id) {
        TextView text = (TextView) findViewById(id);
        text.setText("");
    }

    protected String getItemText(int id) {
        TextView text = (TextView) findViewById(id);
        return text.getText().toString();
    }

    protected void setItemText(int id, String str) {
        if (str == null) {
            str = "";
        }
        TextView text = (TextView) findViewById(id);
        text.setText(str);
    }
}
