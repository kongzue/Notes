package com.kongzue.notes.util;

import android.content.Context;
import android.util.Log;

import com.kongzue.baseframework.util.Preferences;
import com.kongzue.kongzuedb.DB;
import com.kongzue.kongzuedb.DBData;
import com.kongzue.notes.BuildConfig;
import com.kongzue.notes.R;

import java.util.List;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2018/8/26 21:26
 */
public class DBUtil {
    
    private static DB db;
    private static DBUtil dbUtil;
    private Context context;
    
    private DBUtil() {
    }
    
    public static DBUtil getInstance() {
        if (dbUtil == null) {
            synchronized (DBUtil.class) {
                dbUtil = new DBUtil();
            }
        }
        return dbUtil;
    }
    
    public void init(Context context) {
        this.context = context;
        db = new DB(context, "notes");
    }
    
    
    public void close() {
        db.closeDB();
        db = null;
        context = null;
        dbUtil = null;
    }
    
    public void createTable() {
        boolean isUsed = Preferences.getInstance().getBoolean(context, "cache", "isUsed");
        if (!isUsed) {
            DBData dbData = new DBData("notes");
            dbData.set("title", Base64Util.encode(context.getString(R.string.new_tip_title)));
            dbData.set("content", Base64Util.encode(
                    "你好！\n　　这是 @Kongzue 为你带来的纯净、易用、绿色的文字写作工具。\n　　随时随地打开，继续你的创作~\n\n　　- 点击文本即可编辑，底部工具栏可以方便地进行缩进、撤销和粘贴操作\n　　- 收起输入法，向下滑动进入历史笔记，向上滑动快速创建新笔记\n\n　　在这里你可以体验到最纯粹的写作方式，没有多余任何界面或功能的打扰\n　　Enjoy it and have Fun !"
            ));
            dbData.set("time", System.currentTimeMillis());
            dbData.set("isSync", false);
            db.add(dbData, false);
            dbData.set("title", Base64Util.encode(BuildConfig.VERSION_NAME + context.getString(R.string.update_title)));
            dbData.set("content", Base64Util.encode(
                    "　　「记」现已全新发布，和「给未来写封信」一样的风格，一张信纸，一片记忆，带给你更好的体验。\n　　Less is more，相信这一份简洁能带给你不一样的创作体验！\n　　1.5版本更换了信纸样式，支持了更好的分辨率并优化了界面动画效果，以带来更加完美的用户体验。"
            ));
            dbData.set("time", System.currentTimeMillis());
            dbData.set("isSync", false);
            db.add(dbData, false);
            Preferences.getInstance().set(context, "cache", "isUsed", true);
            Preferences.getInstance().set(context, "cache", "id", 1);
        }
    }
    
    public DB getDb() {
        return db;
    }
    
    public List<DBData> getDatas() {
        return db.findAll("notes");
    }
}
