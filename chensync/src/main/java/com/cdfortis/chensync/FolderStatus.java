package com.cdfortis.chensync;

/**
 * Created by Diuy on 2017/4/10.
 * FolderStatus
 */
public class FolderStatus {
    public String message = "Ready...";
    public int fileCount = 0;
    public int fileIndex = 0;
    public String file = "";
    public int percent = 0;
    public int finish = 1;//0同步中,1成功,-1失败
}
