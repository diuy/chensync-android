package com.cdfortis.chensync.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.cdfortis.chensync.FolderInfo;
import com.cdfortis.chensync.R;

public class EditActivity extends BaseActivity {

    private FolderInfo folderInfo;
    private EditText textWifi;
    private EditText textFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        // EditText textIp = (EditText) findViewById(R.id.textIp);
        //textIp.requestFocus();
        textWifi = (EditText) findViewById(R.id.textWifi);
        textFolder = (EditText) findViewById(R.id.textFolder);
        textFolder.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    startActivityForResult(new Intent(EditActivity.this, DirectoryActivity.class), CODE_DIRECTORY);
                }
            }
        });
        textFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(EditActivity.this, DirectoryActivity.class), CODE_DIRECTORY);
            }
        });
        textWifi.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == KeyEvent.ACTION_DOWN || actionId == EditorInfo.IME_ACTION_DONE) {
                    onSave(v);
                }
                return true;
            }
        });

        folderInfo = (FolderInfo) getIntent().getSerializableExtra(EXTRA_FOLDER_INFO);
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
            setTitle("添加目录");
            setItemText(R.id.textPort, "8888");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_DIRECTORY && resultCode == RESULT_OK) {
            String path = data.getStringExtra(EXTRA_PATH);
            if (!TextUtils.isEmpty(path)) {
                textFolder.setText(path);
            }
        }
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

        String id = null;
        if (this.folderInfo != null)
            id = this.folderInfo.id;

        Intent intent = new Intent();
        intent.putExtra(EXTRA_FOLDER_INFO, new FolderInfo(id, ip, p, folder, wifi));
        setResult(RESULT_OK, intent);
        finish();
    }
}
