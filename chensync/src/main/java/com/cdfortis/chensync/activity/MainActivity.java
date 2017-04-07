package com.cdfortis.chensync.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.cdfortis.chensync.ChenApplication;
import com.cdfortis.chensync.ChenConstant;
import com.cdfortis.chensync.FolderInfo;
import com.cdfortis.chensync.R;
import com.cdfortis.chensync.SyncService;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class MainActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

    private List<FolderInfo> folderInfos;
    private ListView listFolder;
    private FolderListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listFolder = (ListView) findViewById(R.id.listFolder);
        folderInfos = getFolderInfos();
        adapter = new FolderListAdapter(this, folderInfos, this);
        listFolder.setAdapter(adapter);
        listFolder.setOnItemLongClickListener(this);
        listFolder.setOnItemClickListener(this);
        refreshListView();
        SyncService.registerActionStatus(this, receiver);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void refreshListView() {
        adapter.notifyDataSetChanged();
        if (folderInfos.size() > 0)
            showView(R.id.btnAllSync);
        else
            hideView(R.id.btnAllSync);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String folderID = intent.getStringExtra(SyncService.EXTRA_FOLDER_ID);
            String file = intent.getStringExtra(SyncService.EXTRA_FILE);
            int progress = intent.getIntExtra(SyncService.EXTRA_PROGRESS, 0);
            String message = intent.getStringExtra(SyncService.EXTRA_MESSAGE);
            onStatus(folderID, message, file, progress);
        }
    };

    private void onStatus(String folderID, String message, String file, int progress) {
        adapter.setStatus(folderID, message, file, progress);
        refreshListView();
    }

    public void onSyncAll(View view) {
        for (FolderInfo folderInfo : folderInfos) {
            SyncService.startSync(this, folderInfo);
            adapter.setStatus(folderInfo.id, "start", "", 0);
        }

        refreshListView();
    }

    public void onAddFolder(View view) {
        startActivityForResult(new Intent(this, EditActivity.class), ChenConstant.CODE_EDIT);
    }

    private void startSync(FolderInfo folderInfo) {
        SyncService.startSync(this, folderInfo);
        adapter.setStatus(folderInfo.id, "start", "", 0);
        refreshListView();

    }

    private void stopSync(String folderId) {
        SyncService.stopSync(this, folderId);
        adapter.removeStatus(folderId);
        refreshListView();
    }

    private FolderInfo getFolderInfo(String id) {
        for (FolderInfo folderInfo : folderInfos) {
            if (TextUtils.equals(id, folderInfo.id))
                return folderInfo;
        }
        return null;
    }

    private boolean hasSameFolder(FolderInfo folderInfo) {
        for (FolderInfo f : folderInfos) {
            if (TextUtils.equals(new File(folderInfo.folder).getAbsolutePath(), new File(f.folder).getAbsolutePath()) &&
                    TextUtils.equals(f.ip, folderInfo.ip) &&
                    f.port == folderInfo.port)
                return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ChenConstant.CODE_EDIT && resultCode == RESULT_OK) {
            FolderInfo folderInfo = (FolderInfo) data.getSerializableExtra(EditActivity.EXTRA_FOLDER_INFO);
            if (folderInfo == null)
                return;

            if (hasSameFolder(folderInfo)) {
                Toast.makeText(this, "相同地址包括相同的文件夹", Toast.LENGTH_LONG).show();
                return;
            }

            if (TextUtils.isEmpty(folderInfo.id)) {
                folderInfo.id = UUID.randomUUID().toString();
                folderInfos.add(folderInfo);
                getChenApplication().saveFolders();
                refreshListView();
            } else {
                FolderInfo fi = getFolderInfo(folderInfo.id);
                if (fi != null) {
                    fi.copyFrom(folderInfo);
                    getChenApplication().saveFolders();
                    stopSync(fi.id);
                }
            }
        } else if (requestCode == ChenConstant.CODE_SETTING && resultCode == RESULT_OK) {
            stopSync(null);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSync) {
            int position = (int) v.getTag();
            Button button = (Button) v;
            if (TextUtils.equals(button.getText(), "同步")) {
                startSync(getFolderInfos().get(position));
            } else {
                stopSync(getFolderInfos().get(position).id);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.actionSetting) {
            startActivityForResult(new Intent(this, SettingActivity.class), ChenConstant.CODE_SETTING);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        if (position < folderInfos.size()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("是否删除该目录");
            builder.setTitle("确认删除");
            builder.setCancelable(true);
            builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FolderInfo folderInfo = folderInfos.get(position);
                    folderInfos.remove(position);
                    adapter.removeStatus(folderInfo.id);
                    stopSync(folderInfo.id);
                    refreshListView();
                }
            });
            builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position < folderInfos.size()) {
            Intent intent = new Intent(this, EditActivity.class);
            intent.putExtra(EditActivity.EXTRA_FOLDER_INFO, folderInfos.get(position));
            startActivityForResult(new Intent(this, EditActivity.class), EditActivity.CODE_EDIT);
        }
    }
}
