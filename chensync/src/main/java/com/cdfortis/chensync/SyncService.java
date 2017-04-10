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
import java.util.Map;

public class SyncService extends Service implements ChenConstant {


    public static final String TAG = "SyncService";

    private List<SyncAsyncTask> tasks = new ArrayList<>();

    public SyncService() {
    }

    private ChenApplication getChenApplication() {
        return (ChenApplication) getApplication();
    }

    private Map<String, FolderStatus> getFolderStatuses() {
        return getChenApplication().getFolderStatuses();
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

    private void sendStatus(String folderId) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_FOLDER_ID, folderId);
        intent.setAction(ACTION_STATUS);
        sendBroadcast(intent);
    }

    class SyncAsyncTask extends AsyncTask<Object, Void, Void> implements FileClient.ProgressCallback {
        private String device;
        private FolderInfo folderInfo;
        private FolderStatus folderStatus;

        SyncAsyncTask(String device, FolderInfo folderInfo) {
            this.device = device;
            this.folderInfo = folderInfo;
            this.folderStatus = getFolderStatuses().get(folderInfo.id);
            if (null == this.folderStatus) {
                throw new IllegalStateException("no folderStatus id:" + folderInfo.id);
            }
        }

        FolderInfo getFolderInfo() {
            return folderInfo;
        }

        private void setMessage(String message) {
            if (!isCancelled()) {
                folderStatus.message = message;
                sendStatus(folderInfo.id);
            }
        }

        private void setFileProgress(String message, String file, int fileCount, int fileIndex) {
            if (!isCancelled()) {
                folderStatus.message = message;
                folderStatus.fileCount = fileCount;
                folderStatus.file = file;
                folderStatus.fileIndex = fileIndex;
                sendStatus(folderInfo.id);
            }
        }

        private void setPercent(int percent) {
            if (!isCancelled()) {
                folderStatus.percent = percent;
                sendStatus(folderInfo.id);
            }
        }

        private void setFinish(String message, boolean success) {
            if (!isCancelled()) {
                folderStatus.message = message;
                if (success)
                    folderStatus.finish = 1;
                else
                    folderStatus.finish = -1;

                sendStatus(folderInfo.id);
            }
        }

        @Override
        protected Void doInBackground(Object... params) {
            setMessage("Search file ...");
            List<FileInfo> fileInfoList = FileManager.getFileInfos(folderInfo.folder);
            if (fileInfoList.isEmpty()) {
                Log.e(TAG, folderInfo.folder + ": is empty");
                setFinish("Success!", true);
                return null;
            }
            FileClient fileClient = new FileClient(folderInfo.ip, folderInfo.port, device, this);

            try {
                setMessage("Check file...");
                List<String> files = fileClient.checkFile(folderInfo.folder, fileInfoList);
                Log.e(TAG, "check file:" + folderInfo.folder + ",new files:" + files.size());
                if(files.size()==0){
                    setFinish("No file to upload,Success!", true);
                    return null;
                }
                int index = 0;
                for (String file : files) {
                    FileInfo fileInfo = getFileInfo(fileInfoList, file);
                    index++;
                    if (fileInfo != null) {
                        String fileName = new File(fileInfo.path).getName();
                        setFileProgress("Upload...", fileName, files.size(), index);
                        Log.e(TAG, "upload file start:" + new File(folderInfo.folder, fileInfo.path).getAbsolutePath() + ",size:" + fileInfo.fileSize);
                        fileClient.uploadFile(folderInfo.folder, fileInfo);
                        Log.e(TAG, "upload file success:" + new File(folderInfo.folder, fileInfo.path).getAbsolutePath() + ",size:" + fileInfo.fileSize);
                    }
                }
                setFinish("Success!", true);
            } catch (Exception e) {
                setFinish(e.getMessage(), false);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onProgress(int percent) {
            setPercent(percent);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (tasks.contains(this))
                tasks.remove(this);
        }


    }


}
