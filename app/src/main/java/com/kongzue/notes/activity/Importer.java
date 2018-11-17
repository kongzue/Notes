package com.kongzue.notes.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.kongzue.baseframework.util.AppManager;
import com.kongzue.baseframework.util.JumpParameter;
import com.kongzue.baseframework.util.ParameterCache;
import com.kongzue.dialog.v2.DialogSettings;
import com.kongzue.dialog.v2.Notification;
import com.kongzue.notes.R;

import java.util.Timer;
import java.util.TimerTask;

import static com.kongzue.notes.NotesApp.DEBUGMODE;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2018/9/12 02:36
 */
public class Importer extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (intent.ACTION_VIEW.equals(action)) {
                    final String file = intent.getDataString();
                    log("Importer>>>" + "ACTION_VIEW: " + file);
                    if (!isNull(file)) {
                        AppManager.getInstance().killAllActivity();
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                jump(MainActivity.class, new JumpParameter().put("import", file));
                                jumpAnim(R.anim.slide_in_bottom, R.anim.slide_out_top);
                            }
                        },500);
                        
                    } else {
                        doErrorTip();
                    }
                }
                if (intent.ACTION_SEND.equals(action)) {
                    Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    if (uri != null) {
                        String file = uri.getPath();
                        log("Importer>>>" + "ACTION_SEND: " + file);
                        if (!isNull(file)) {
                            if (MainActivity.getMainActivity()!=null)MainActivity.getMainActivity().finish();
                            jump(MainActivity.class, new JumpParameter().put("import", file));
                            jumpAnim(R.anim.slide_in_bottom, R.anim.slide_out_top);
                        } else {
                            doErrorTip();
                        }
                    } else {
                        doErrorTip();
                    }
                }
            } else {
                doErrorTip();
            }
        } else {
            doErrorTip();
        }
        finish();
    }
    
    //可以传任何类型参数的跳转方式
    public boolean jump(Class<?> cls, JumpParameter jumpParameter) {
        try {
            if (jumpParameter != null)
                ParameterCache.getInstance().set(cls.getName(), jumpParameter);
            startActivity(new Intent(Importer.this, cls));
        } catch (Exception e) {
            if (DEBUGMODE) e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public void jumpAnim(int enterAnim, int exitAnim) {
        int version = Integer.valueOf(android.os.Build.VERSION.SDK);
        if (version > 5) {
            overridePendingTransition(enterAnim, exitAnim);
        }
    }
    
    private void log(Object s) {
        if (s == null) return;
        if (!DEBUGMODE) return;
        Log.i(">>>", s.toString());
    }
    
    private void doErrorTip() {
        DialogSettings.type = DialogSettings.TYPE_KONGZUE;
        Notification.show(this, 0, getString(R.string.cannot_import), Notification.TYPE_ERROR);
    }
    
    private boolean isNull(String s) {
        if (s == null || s.trim().isEmpty() || s.equals("null")) {
            return true;
        }
        return false;
    }
}
