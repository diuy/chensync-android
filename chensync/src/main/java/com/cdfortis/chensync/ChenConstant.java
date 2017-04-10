package com.cdfortis.chensync;

/**
 * Created by Diuy on 2017/4/7.
 * ChenConstant
 */

public interface ChenConstant {
    String ACTION_START_SYNC = "com.cdfortis.chensync.action.START_SYNC";
    String ACTION_STOP_SYNC = "com.cdfortis.chensync.action.STOP_SYNC";
    String ACTION_STATUS = "com.cdfortis.chensync.action.STATUS";

    String EXTRA_FOLDER_INFO = "com.cdfortis.chensync.extra.FOLDER_INFO";
    String EXTRA_FOLDER_ID = "com.cdfortis.chensync.extra.FOLDER_ID";
    String EXTRA_PATH = "com.cdfortis.chensync.extra.PATH";

    String EXTRA_FILE = "com.cdfortis.chensync.extra.FILE";
    String EXTRA_PROGRESS = "com.cdfortis.chensync.extra.PROGRESS";
    String EXTRA_MESSAGE = "com.cdfortis.chensync.extra.MESSAGE";

    String MESSAGE_SUCCESS = "success";

    //activity 请求代码
    int CODE_EDIT = 1;
    int CODE_SETTING = 2;
    int CODE_DIRECTORY = 3;
}
