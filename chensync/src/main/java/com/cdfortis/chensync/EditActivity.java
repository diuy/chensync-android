package com.cdfortis.chensync;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

public class EditActivity extends Activity {

    public final static int CODE_EDIT = 1;
    public final static String EXTRA_FOLDER = "folderInfo";
    private FolderInfo folderInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        EditText text = (EditText) findViewById(R.id.textWifi);
        text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == KeyEvent.ACTION_DOWN || actionId == EditorInfo.IME_ACTION_DONE) {
                    onSave(v);
                }
                return true;
            }
        });

        folderInfo = (FolderInfo) getIntent().getSerializableExtra(EXTRA_FOLDER);
        if (folderInfo != null) {
            setItemText(R.id.textIp, folderInfo.ip);
            setItemText(R.id.textPort, String.valueOf(folderInfo.port));
            setItemText(R.id.textFolder, folderInfo.folder);
            setItemText(R.id.textWifi, folderInfo.wifi);
        } else {
            clearItemText(R.id.textIp);
            clearItemText(R.id.textPort);
            clearItemText(R.id.textFolder);
            clearItemText(R.id.textWifi);
        }
    }

    private void clearItemText(int id) {
        EditText text = (EditText) findViewById(id);
        text.setText("");
    }

    private String getItemText(int id) {
        EditText text = (EditText) findViewById(id);
        return text.getText().toString();
    }

    private void setItemText(int id, String str) {
        EditText text = (EditText) findViewById(id);
        text.setText(str);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void onSave(View view) {
        String ip = getItemText(R.id.textIp);
        String port = getItemText(R.id.textPort);
        String folder = getItemText(R.id.textFolder);
        String wifi = getItemText(R.id.textWifi);
        if (TextUtils.isEmpty(ip)) {
            showToast("ip is empty");
            return;
        }

        if (TextUtils.isEmpty(port)) {
            showToast("port is empty");
            return;
        }

        if (TextUtils.isEmpty(folder)) {
            showToast("folderInfo is empty");
            return;
        }

        if (TextUtils.isEmpty(wifi)) {
            showToast("wifi is empty");
            return;
        }
        int p = 0;
        try {
            p = Integer.parseInt(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (p <= 0 || p > 65535)
            showToast("输入正确的端口号");

        String id =null ;
        if(this.folderInfo != null)
            id = this.folderInfo.id;

        Intent intent = new Intent();
        intent.putExtra(EXTRA_FOLDER, new FolderInfo(id,ip, p, folder, wifi));
        setResult(RESULT_OK,intent);
    }
}
