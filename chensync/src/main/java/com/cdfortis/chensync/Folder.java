package com.cdfortis.chensync;

import java.io.Serializable;

/**
 * Created by Diuy on 2017/4/5.
 * Folder
 */

public class Folder implements Serializable {

    public String ip;
    public int port;
    public String folder;
    public String wifi;

    public Folder(String ip,int port,String folder,String wifi ){
        this.ip = ip;
        this.port = port;
        this.folder = folder;
        this.wifi = wifi;
    }

    public Folder(){

    }
}
