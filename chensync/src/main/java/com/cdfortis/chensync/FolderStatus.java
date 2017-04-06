package com.cdfortis.chensync;

/**
 * Created by Diuy on 2017/4/6.
 * FolderStatus
 */

public class FolderStatus extends FolderInfo {
    public String message;
    public String file;
    public int progress;

    public FolderStatus(FolderInfo folderInfo) {
        super(folderInfo);
    }

    @Override
    public void copyFrom(FolderInfo other) {
        super.copyFrom(other);
        this.message = "";
        this.file = "";
        this.progress = 0;
    }
}
