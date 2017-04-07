package com.cdfortis.chensync;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.cdfortis.chensync.core.FileClient;
import com.cdfortis.chensync.core.FileInfo;
import com.cdfortis.chensync.core.FileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SyncService extends Service implements ChenConstant {


    public static final String TAG = "SyncService";

    private List<SyncAsyncTask> tasks = new ArrayList<>();

    public SyncService() {
    }

    private ChenApplication getChenApplication() {
        return (ChenApplication) getApplication();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void startSync(Context context, FolderInfo folderInfo) {
        if (TextUtils.isEmpty(folderInfo.ip) || folderInfo.port <= 0 || folderInfo.port > 65535 || TextUtils.isEmpty(folderInfo.folder))
            throw new IllegalArgumentException("argument not valid");

        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_START_SYNC);
        intent.putExtra(EXTRA_FOLDER_INFO, folderInfo);
        context.startService(intent);
    }

    public static void stopSync(Context context, String folderId) {
        if (TextUtils.isEmpty(folderId))
            throw new IllegalArgumentException("argument not valid");

        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_STOP_SYNC);
        intent.putExtra(EXTRA_FOLDER_ID, folderId);
        context.startService(intent);
    }

    public static void registerActionStatus(Context context, BroadcastReceiver receiver) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_STATUS);
        context.registerReceiver(receiver, filter);
    }

    private FileInfo getFileInfo(List<FileInfo> fileInfoList, String path) {
        for (FileInfo fileInfo : fileInfoList) {
            if (fileInfo.path != null && fileInfo.path.equals(path))
                return fileInfo;
        }

        return null;
    }

    private SyncAsyncTask getTask(String folderId) {
        for (SyncAsyncTask task : tasks) {
            if (TextUtils.equals(task.getFolderInfo().id, folderId)) {
                return task;
            }
        }
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }

        final String action = intent.getAction();
        if (ACTION_START_SYNC.equals(action)) {
            final FolderInfo folderInfo = (FolderInfo) intent.getSerializableExtra(EXTRA_FOLDER_INFO);

            if (getTask(folderInfo.id) != null) {
                Log.e(TAG, "is running :" + folderInfo.folder);
                return super.onStartCommand(intent, flags, startId);
            }
            SyncAsyncTask task = new SyncAsyncTask(getChenApplication().getSetting().getDevice(), folderInfo);
            tasks.add(task);
            task.execute();
        } else if (ACTION_STOP_SYNC.equals(action)) {
            final String folderId = intent.getStringExtra(EXTRA_FOLDER_ID);
            if (TextUtils.isEmpty(folderId)) {
                for (SyncAsyncTask task : tasks) {
                    task.cancel(true);
                }
                tasks.clear();
            } else {
                SyncAsyncTask task = getTask(folderId);
                if (task != null) {
                    task.cancel(true);
                    tasks.remove(task);
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        for (SyncAsyncTask task : tasks) {
            task.cancel(true);
        }
        tasks.clear();
        super.onDestroy();
    }

    private void sendStatus(String folderId, String file, int progress, String message) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_FOLDER_ID, folderId);
        if (!TextUtils.isEmpty(file))
            intent.putExtra(EXTRA_FILE, file);
        intent.putExtra(EXTRA_PROGRESS, progress);
        if (!TextUtils.isEmpty(message))
            intent.putExtra(EXTRA_MESSAGE, message);

        intent.setAction(ACTION_STATUS);
        sendBroadcast(intent);
    }

    class SyncAsyncTask extends AsyncTask<Object, Void, Void> implements FileClient.ProgressCallback {
        private String device;
        private FolderInfo folderInfo;
        private String fileName = "";

        SyncAsyncTask(String device, FolderInfo folderInfo) {
            this.device = device;
            this.folderInfo = folderInfo;
        }

        public FolderInfo getFolderInfo() {
            return folderInfo;
        }

        private void sendStatus(String message) {
            if (!isCancelled())
                SyncService.this.sendStatus(folderInfo.id, "", 0, message);
        }

        private void sendStatus(String file, int progress) {
            if (!isCancelled()) SyncService.this.sendStatus(folderInfo.id, file, progress, "");
        }

        private void sendStatus(String file, String message) {
            if (!isCancelled()) SyncService.this.sendStatus(folderInfo.id, file, 0, message);
        }

        @Override
        protected Void doInBackground(Object... params) {
            sendStatus("Search file ...");
            List<FileInfo> fileInfoList = FileManager.getFileInfos(folderInfo.folder);
            if (fileInfoList.isEmpty()) {
                Log.e(TAG, folderInfo.folder + ": is empty");
                sendStatus(MESSAGE_SUCCESS);
                return null;
            }
            FileClient fileClient = new FileClient(folderInfo.ip, folderInfo.port, device, this);

            try {
                sendStatus("Check file...");
                List<String> files = fileClient.checkFile(folderInfo.folder, fileInfoList);
                Log.e(TAG, "check file:" + folderInfo.folder + ",new files:" + files.size());

                for (String file : files) {
                    FileInfo fileInfo = getFileInfo(fileInfoList, file);
                    if (fileInfo != null) {
                        fileName = new File(fileInfo.path).getName();
                        sendStatus(fileName, "Upload...");
                        Log.e(TAG, "upload file start:" + new File(folderInfo.folder, fileInfo.path).getAbsolutePath() + ",size:" + fileInfo.fileSize);
                        fileClient.uploadFile(folderInfo.folder, fileInfo);
                        Log.e(TAG, "upload file success:" + new File(folderInfo.folder, fileInfo.path).getAbsolutePath() + ",size:" + fileInfo.fileSize);
                    }
                }
            } catch (Exception e) {
                sendStatus(e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onProgress(int percent) {
            sendStatus(fileName, percent);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (tasks.contains(this))
                tasks.remove(this);
        }


    }


}
