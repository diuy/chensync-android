package com.cdfortis.chensync;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SyncService extends Service {
    private static final String ACTION_SYNC = "com.cdfortis.chensync.action.SYNC";
    private static final String ACTION_STATUS = "com.cdfortis.chensync.action.STATUS";

    public static final String EXTRA_FOLDER = "com.cdfortis.chensync.extra.FOLDER";
    public static final String EXTRA_SERVER_IP = "com.cdfortis.chensync.extra.SERVER_IP";
    public static final String EXTRA_SERVER_PORT = "com.cdfortis.chensync.extra.SERVER_PORT";
    public static final String EXTRA_DEVICE = "com.cdfortis.chensync.extra.DEVICE";

    public static final String EXTRA_FILE = "com.cdfortis.chensync.extra.FILE";
    public static final String EXTRA_PROGRESS = "com.cdfortis.chensync.extra.PROGRESS";
    public static final String EXTRA_MESSAGE = "com.cdfortis.chensync.extra.MESSAGE";

    public static final String MESSAGE_SUCCESS = "success";

    public static final String TAG = "SyncService";

    private List<SyncAsyncTask> tasks = new ArrayList<>();

    public SyncService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void startActionSync(Context context, String ip, int port, String device, String folder) {
        if (TextUtils.isEmpty(ip) || port <= 0 || port > 65535 || TextUtils.isEmpty(device) || TextUtils.isEmpty(folder))
            throw new IllegalArgumentException("argument not valid");

        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_SYNC);
        intent.putExtra(EXTRA_SERVER_IP, ip);
        intent.putExtra(EXTRA_SERVER_PORT, port);
        intent.putExtra(EXTRA_DEVICE, device);
        intent.putExtra(EXTRA_FOLDER, folder);
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


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }

        final String action = intent.getAction();
        if (ACTION_SYNC.equals(action)) {
            final String folder = intent.getStringExtra(EXTRA_FOLDER);
            final String ip = intent.getStringExtra(EXTRA_SERVER_IP);
            final int port = intent.getIntExtra(EXTRA_SERVER_PORT, 8888);
            final String device = intent.getStringExtra(EXTRA_DEVICE);
            for (SyncAsyncTask task : tasks) {
                if (TextUtils.equals(task.getFolder(), folder)) {
                    Log.e(TAG, "is running :" + folder);
                    break;
                }
            }
            SyncAsyncTask task = new SyncAsyncTask(ip, port, device, folder);
            task.execute();
            tasks.add(task);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        for (SyncAsyncTask task : tasks) {
            task.cancel(true);
        }
        super.onDestroy();
    }

    private void sendStatus(String folder, String file, int progress, String message) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_FOLDER, folder);
        intent.putExtra(EXTRA_FILE, file);
        intent.putExtra(EXTRA_PROGRESS, progress);
        intent.putExtra(EXTRA_MESSAGE, message);
        intent.setAction(ACTION_STATUS);
        sendBroadcast(intent);
    }

    class SyncAsyncTask extends AsyncTask<Object, Void, Void> implements FileClient.ProgressCallback {
        private String ip;
        private int port;
        private String device;
        private String folder;
        private String fileName = "";

        public SyncAsyncTask(String ip, int port, String device, String folder) {
            this.device = device;
            this.folder = folder;
            this.ip = ip;
            this.port = port;
        }

        public String getFolder() {
            return folder;
        }

        private void sendStatus(String message) {
            if (!isCancelled())
                SyncService.this.sendStatus(folder, "", 0, message);
        }

        private void sendStatus(String file, int progress) {
            if (!isCancelled()) SyncService.this.sendStatus(folder, file, progress, "");
        }

        private void sendStatus(String file, String message) {
            if (!isCancelled()) SyncService.this.sendStatus(folder, file, 0, message);
        }

        @Override
        protected Void doInBackground(Object... params) {
            sendStatus("Search file ...");
            List<FileInfo> fileInfoList = FileManager.getFileInfos(folder);
            if (fileInfoList.isEmpty()) {
                Log.e(TAG, folder + ": is empty");
                sendStatus(MESSAGE_SUCCESS);
                return null;
            }
            FileClient fileClient = new FileClient(ip, port, device, this);

            try {
                sendStatus("Check file...");
                List<String> files = fileClient.checkFile(folder, fileInfoList);
                Log.e(TAG, "check file:" + folder + ",new files:" + files.size());

                for (String file : files) {
                    FileInfo fileInfo = getFileInfo(fileInfoList, file);
                    if (fileInfo != null) {
                        fileName = new File(fileInfo.path).getName();
                        sendStatus(fileName, "Upload...");
                        Log.e(TAG, "upload file start:" + new File(folder, fileInfo.path).getAbsolutePath() + ",size:" + fileInfo.fileSize);
                        fileClient.uploadFile(folder, fileInfo);
                        Log.e(TAG, "upload file success:" + new File(folder, fileInfo.path).getAbsolutePath() + ",size:" + fileInfo.fileSize);
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
            tasks.remove(this);
        }
    }


}
