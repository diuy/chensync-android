package com.cdfortis.chensync;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class SyncService extends Service {
    private static final String ACTION_SYNC = "com.cdfortis.chensync.action.SYNC";

    private static final String EXTRA_FOLDER = "com.cdfortis.chensync.extra.FOLDER";
    public static final String TAG ="SyncService" ;

    public SyncService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void startActionSync(Context context, String folder) {
        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_SYNC);
        intent.putExtra(EXTRA_FOLDER, folder);
        context.startService(intent);
    }

    private FileInfo getFileInfo(List<FileInfo> fileInfoList ,String path){
        for (FileInfo fileInfo:fileInfoList){
            if(fileInfo.path != null && fileInfo.path.equals(path))
                return fileInfo;
        }

        return null;
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SYNC.equals(action)) {
                final String folder = intent.getStringExtra(EXTRA_FOLDER);
                new AsyncTask<Object,Void,Void>(){

                    @Override
                    protected Void doInBackground(Object[] params) {
                        List<FileInfo> fileInfoList = FileManager.getFileInfos(folder);
                        if(fileInfoList.isEmpty()){
                            Log.e(TAG,folder+": is empty");
                            return null;
                        }
                        FileClient fileClient = new FileClient("172.20.2.229",9999,"MI5");

                        try {
                            List<String> files =fileClient.checkFile(folder,fileInfoList);
                            Log.e(TAG,"check file:"+folder+",new files:"+files.size());

                            for (String file:files){
                                FileInfo fileInfo = getFileInfo(fileInfoList,file);
                                if(fileInfo!=null){
                                    Log.e(TAG,"upload file start:"+new File(folder,fileInfo.path).getAbsolutePath()+",size:"+fileInfo.fileSize);
                                    fileClient.uploadFile(folder,fileInfo);
                                    Log.e(TAG,"upload file success:"+new File(folder,fileInfo.path).getAbsolutePath()+",size:"+fileInfo.fileSize);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.execute();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



}
