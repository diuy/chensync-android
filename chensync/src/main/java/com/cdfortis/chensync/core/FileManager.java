package com.cdfortis.chensync.core;

import android.util.Log;

import com.cdfortis.chensync.core.FileInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Diuy on 2017/3/29.
 * FileManager
 */

public class FileManager {
    public static List<FileInfo> getFileInfos(String folder){
        File folderFile = new File(folder);
        List<FileInfo> fileInfos = new ArrayList<>();
        if(!folderFile.isDirectory()){
            return fileInfos;
        }
        List<File> files = getFileInfos(folderFile);
        int folderPathSize = folderFile.getAbsolutePath().length();
        for (File file :files) {
            FileInfo fileInfo = new FileInfo();
            fileInfo.path = file.getAbsolutePath().substring(folderPathSize+1);
            fileInfo.modifyTime = file.lastModified()/1000;
            fileInfo.fileSize = file.length();
            fileInfos.add(fileInfo);
        }
        return fileInfos;
    }

    private static List<File> getFileInfos(File folder){
        List<File> files = new ArrayList<>();

        File[] fs = folder.listFiles();
        for (File f:fs){
            if(f.getName().startsWith(".")){
                Log.e("","");
                continue;
            }

            if(f.isDirectory()){
                files.addAll(getFileInfos(f));
            }else if(f.isFile()){
                files.add(f);
            }
        }
        return files;
    }
}
