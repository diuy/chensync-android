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

public class EditActivity extends Activity {

    public final static int CODE_EDIT = 1;
    public final static String EXTRA_FOLDER = "folder";

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

        Folder folder = (Folder) getIntent().getSerializableExtra(EXTRA_FOLDER);
        if (folder != null) {
            setItemText(R.id.textIp, folder.ip);
            setItemText(R.id.textPort, String.valueOf(folder.port));
            setItemText(R.id.textFolder, folder.folder);
            setItemText(R.id.textWifi, folder.wifi);
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
            showToast("folder is empty");
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

        Intent intent = new Intent();
        intent.putExtra(EXTRA_FOLDER, new Folder(ip, p, folder, wifi));
        setResult(RESULT_OK);
    }
}
