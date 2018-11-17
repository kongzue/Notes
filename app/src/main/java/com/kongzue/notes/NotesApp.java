package com.kongzue.notes;

import android.app.Application;

import com.kongzue.baseframework.BaseActivity;
import com.kongzue.dialog.v2.DialogSettings;
import com.kongzue.notes.util.DBUtil;

import static com.kongzue.dialog.v2.DialogSettings.THEME_DARK;
import static com.kongzue.dialog.v2.DialogSettings.TYPE_IOS;
import static com.kongzue.dialog.v2.DialogSettings.TYPE_KONGZUE;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2018/8/16 21:03
 */
public class NotesApp extends Application {
    
    public static boolean DEBUGMODE = true;
    private static NotesApp me;
    
    public static int screenWidth;
    public static int screenHeight;
    public static int navigationHeight;
    
    @Override
    public void onCreate() {
        super.onCreate();
        me = this;
        
        BaseActivity.DEBUGMODE = DEBUGMODE;
        DialogSettings.DEBUGMODE = DEBUGMODE;
        
        DialogSettings.type = TYPE_KONGZUE;
        
        DBUtil.getInstance().init(me);
        DBUtil.getInstance().createTable();
    }
    
    public static NotesApp getInstance() {
        return me;
    }
    
    @Override
    public void onTerminate() {
        DBUtil.getInstance().close();
        super.onTerminate();
    }
}
