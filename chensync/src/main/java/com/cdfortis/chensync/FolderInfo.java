package com.cdfortis.chensync;

import java.io.Serializable;

/**
 * Created by Diuy on 2017/4/5.
 * FolderInfo
 */

public class FolderInfo implements Serializable {

    public String id;
    public String ip;
    public int port;
    public String folder;
    public String wifi;

    public FolderInfo(String id, String ip, int port, String folder, String wifi ){
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.folder = folder;
        this.wifi = wifi;
    }

    public FolderInfo(FolderInfo other){
        copyFrom(other);
    }

    public void copyFrom(FolderInfo other){
        this.id = other.id;
        this.ip = other.ip;
        this.port = other.port;
        this.folder = other.folder;
        this.wifi = other.wifi;
    }

    public FolderInfo(){

    }
}
