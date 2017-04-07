package com.cdfortis.chensync.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.cdfortis.chensync.ChenApplication;
import com.cdfortis.chensync.R;
import com.cdfortis.chensync.Setting;

import java.io.File;

public class SettingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Setting setting = ((ChenApplication)getApplication()).getSetting();
        setItemText(R.id.textDevice,setting.getDevice());
    }

    private boolean checkFileName(String name){
        try {
            File f = new File (this.getCacheDir(),name);
            return f.mkdir();
        }catch (Exception e){
            return  false;
        }
    }

    public void onSave(View view) {
        Setting setting = ((ChenApplication)getApplication()).getSetting();
        String device = getItemText(R.id.textDevice);
        if(TextUtils.isEmpty(device)){
            showToast("设备号为空");
            return;
        }

        if(device.length()<3 || device.length()>15){
            showToast("设备号长度3-15位");
            return;
        }

        if(!checkFileName(device)){
            showToast("设备名称格式错误");
            return;
        }
        setting.setDevice(device);
        ((ChenApplication)getApplication()).saveSetting();
        setResult(RESULT_OK);
        finish();
    }
}
