package com.kongzue.notes.activity;

import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.hanks.lineheightedittext.LineHeightEditText;
import com.hanks.lineheightedittext.TextWatcher;
import com.kongzue.baseframework.BaseActivity;
import com.kongzue.baseframework.interfaces.DarkNavigationBarTheme;
import com.kongzue.baseframework.interfaces.DarkStatusBarTheme;
import com.kongzue.baseframework.interfaces.Layout;
import com.kongzue.baseframework.interfaces.LifeCircleListener;
import com.kongzue.baseframework.interfaces.NavigationBarBackgroundColor;
import com.kongzue.baseframework.util.JumpParameter;
import com.kongzue.baseframework.util.OnPermissionResponseListener;
import com.kongzue.baseframework.util.Preferences;
import com.kongzue.dialog.listener.DialogLifeCycleListener;
import com.kongzue.dialog.listener.OnMenuItemClickListener;
import com.kongzue.dialog.v2.BottomMenu;
import com.kongzue.dialog.v2.DialogSettings;
import com.kongzue.dialog.v2.MessageDialog;
import com.kongzue.dialog.v2.SelectDialog;
import com.kongzue.dialog.v2.TipDialog;
import com.kongzue.dialog.v2.WaitDialog;
import com.kongzue.kongzuedb.DBData;
import com.kongzue.notes.R;
import com.kongzue.notes.util.Base64Util;
import com.kongzue.notes.util.DBUtil;
import com.kongzue.notes.util.ViewWrapper;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import ren.qinc.edit.PerformEdit;

import static com.kongzue.notes.NotesApp.navigationHeight;
import static com.kongzue.notes.NotesApp.screenHeight;
import static com.kongzue.notes.NotesApp.screenWidth;

@Layout(R.layout.activity_main)
@DarkNavigationBarTheme(true)
@DarkStatusBarTheme(true)
@NavigationBarBackgroundColor(a = 0)
public class MainActivity extends BaseActivity {
    
    private static MainActivity mainActivity;
    private int boxRefresherBigHeight = -1;
    
    private RelativeLayout boxRoot;
    private RelativeLayout boxBody;
    private SmartRefreshLayout refreshLayout;
    private LinearLayout boxRefresher;
    private ImageView imgRefresher;
    private TextView txtRefresher;
    private LinearLayout boxEditor;
    private ScrollView scroller;
    private LineHeightEditText editTitle;
    private LineHeightEditText editNotes;
    private LinearLayout boxFooter;
    private ImageView imgLoadMore;
    private TextView txtLoadMore;
    private LinearLayout boxToolBar;
    private ImageView btnMore;
    private TextView btnDoubleSpace;
    private TextView btnUnDo;
    private TextView btnPaste;
    
    @Override
    public void initViews() {
        boxRoot = findViewById(R.id.box_root);
        boxBody = findViewById(R.id.box_body);
        refreshLayout = findViewById(R.id.refreshLayout);
        boxRefresher = findViewById(R.id.box_refresher);
        imgRefresher = findViewById(R.id.img_refresher);
        txtRefresher = findViewById(R.id.txt_refresher);
        boxEditor = findViewById(R.id.box_editor);
        scroller = findViewById(R.id.scroller);
        editTitle = findViewById(R.id.edit_title);
        editNotes = findViewById(R.id.edit_notes);
        boxFooter = findViewById(R.id.box_footer);
        imgLoadMore = findViewById(R.id.img_loadMore);
        txtLoadMore = findViewById(R.id.txt_loadMore);
        boxToolBar = findViewById(R.id.box_toolBar);
        btnMore = findViewById(R.id.btn_more);
        btnDoubleSpace = findViewById(R.id.btn_doubleSpace);
        btnUnDo = findViewById(R.id.btn_unDo);
        btnPaste = findViewById(R.id.btn_paste);
    }
    
    private PerformEdit performEdit;
    
    @Override
    public void initDatas(JumpParameter paramer) {
        mainActivity = this;
        
        navigationHeight = getNavbarHeight();
        screenWidth = getDisplayWidth();
        screenHeight = getRootHeight();
        
        boxToolBar.setVisibility(View.GONE);
        //editNotes.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/SourceHanSerifCN-Light.otf"));
        
        performEdit = new PerformEdit(editNotes);
        performEdit.clearHistory();
        
        if (paramer != null) {
            boolean newNote = paramer.getBoolean("newNote");
            if (newNote) {
                dbData = null;
                editTitle.setText("");
                editNotes.setText("");
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM月dd日");
                Date date = new Date(System.currentTimeMillis());
                editTitle.setHint(simpleDateFormat.format(date));
            } else {
                String importFilePath = paramer.getString("import");
                if (!isNull(importFilePath)) {
                    importFile(importFilePath);
                } else {
                    DBData data = (DBData) paramer.get("data");
                    if (data != null) {
                        setData(data);
                    } else {
                        int id = Preferences.getInstance().getInt(me, "cache", "id");
                        if (id != 0) {
                            log(">>>id=" + id);
                            try {
                                dbData = DBUtil.getInstance().getDb().find(new DBData("notes").set("_id", id)).get(0);
                                setData(dbData);
                            } catch (Exception e) {
                                Preferences.getInstance().set(me, "cache", "id", 0);
                            }
                            
                        }
                    }
                }
            }
        } else {
            int id = Preferences.getInstance().getInt(me, "cache", "id");
            if (id != 0) {
                log(">>>id=" + id);
                try {
                    dbData = DBUtil.getInstance().getDb().find(new DBData("notes").set("_id", id)).get(0);
                    setData(dbData);
                } catch (Exception e) {
                    Preferences.getInstance().set(me, "cache", "id", 0);
                }
            } else {
                dbData = null;
                editTitle.setText("");
                editNotes.setText("");
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM月dd日");
                Date date = new Date(System.currentTimeMillis());
                editTitle.setHint(simpleDateFormat.format(date));
            }
        }
        
        refreshLayout.setEnableOverScrollBounce(true);
        refreshLayout.setEnableOverScrollDrag(true);
        refreshLayout.setEnableAutoLoadMore(false);
        
        boxRefresher.setPadding(0, getStatusBarHeight(), 0, 0);
        
        boxBody.post(new Runnable() {
            @Override
            public void run() {
                editNotes.setPadding(
                        dip2px(20),
                        dip2px(20),
                        dip2px(10),
                        dip2px(65) + navigationHeight
                );
            }
        });
    }
    
    private void importFile(final String importFilePath) {
        if (!checkPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"})) {
            SelectDialog.show(me, getString(R.string.need_permission_title), getString(R.string.import_need_permission), getString(R.string.start_get_permission), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    requestPermission(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, new OnPermissionResponseListener() {
                        @Override
                        public void onSuccess(String[] permissions) {
                            WaitDialog.show(me, "请稍候...");
                            doImportData(importFilePath);
                        }
                        
                        @Override
                        public void onFail() {
                        
                        }
                    });
                }
            }, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                
                }
            });
        } else {
            WaitDialog.show(me, "请稍候...");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isActive) {
                        doImportData(importFilePath);
                    }
                }
            }, 500);
        }
    }
    
    private void doImportData(String importFile) {
        try {
            importFile = java.net.URLDecoder.decode(importFile, "UTF-8");
        } catch (Exception e) {
            if (DEBUGMODE) e.printStackTrace();
        }
        
        importFile = FuckContent(importFile);
        
        importFile = importFile.replace("file:///", "");
        if (!importFile.startsWith("/")) importFile = "/" + importFile;
        
        try {
            File file = new File(importFile);
            int length = (int) file.length();
            byte[] buff = new byte[length];
            FileInputStream fin = new FileInputStream(file);
            fin.read(buff);
            fin.close();
            String result = new String(buff, "UTF-8");
            
            WaitDialog.dismiss();
            editTitle.setText(file.getName().replace(".txt", ""));
            editNotes.setText(result);
            
            TipDialog.show(me, getString(R.string.import_succeed), TipDialog.TYPE_FINISH);
            
        } catch (Exception e) {
            WaitDialog.dismiss();
            if (DEBUGMODE) e.printStackTrace();
            TipDialog.show(me, getString(R.string.import_error), TipDialog.TYPE_ERROR);
        }
    }
    
    private String FuckContent(String importFile) {
        String result = importFile;
        try {
            String[] us = importFile.split(":");
            result = Environment.getExternalStorageDirectory().getPath() + "/" + us[us.length - 1];
        } catch (Exception e) {
        
        }
        return result;
    }
    
    @Override
    public void setEvents() {
        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showIME(false, editNotes);
                String[] menu = {"全部复制", "生成长图片"};
                BottomMenu.show(me, menu, new OnMenuItemClickListener() {
                    @Override
                    public void onClick(String text, int index) {
                        switch (index) {
                            case 0:
                                String title = editTitle.getText().toString();
                                String contant = editNotes.getText().toString();
                                if (isNull(title)) {
                                    if (isNull(contant)) {
                                        TipDialog.show(me, "没有可复制的内容", TipDialog.SHOW_TIME_SHORT, TipDialog.TYPE_WARNING);
                                        return;
                                    } else {
                                        copy(contant);
                                    }
                                } else {
                                    copy(title + "\n\n" + contant);
                                }
                                TipDialog.show(me, "已复制", TipDialog.SHOW_TIME_SHORT, TipDialog.TYPE_FINISH);
                                break;
                            case 1:
                                if (!checkPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"})) {
                                    DialogSettings.type = DialogSettings.TYPE_MATERIAL;
                                    SelectDialog.show(me, getString(R.string.need_permission_title), getString(R.string.import_need_permission), getString(R.string.start_get_permission), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            requestPermission(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, new OnPermissionResponseListener() {
                                                @Override
                                                public void onSuccess(String[] permissions) {
                                                    exportBitmap();
                                                }
                                                
                                                @Override
                                                public void onFail() {
                                                
                                                }
                                            });
                                        }
                                    }, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        
                                        }
                                    });
                                    DialogSettings.type = DialogSettings.TYPE_KONGZUE;
                                } else {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (isActive) {
                                                exportBitmap();
                                            }
                                        }
                                    }, 500);
                                }
                                break;
                        }
                    }
                }, true, "取消");
            }
        });
        
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                refreshlayout.finishRefresh(500);
                jump(HistoryActivity.class);
                jumpAnim(R.anim.slide_in_top, R.anim.slide_out_bottom);
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                refreshlayout.finishLoadMore(500);
                jump(MainActivity.class, new JumpParameter().put("newNote", true));
                finish();
                jumpAnim(R.anim.slide_in_bottom, R.anim.slide_out_top);
            }
        });
        
        refreshLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int s = 0;
                if (refreshLayout.getHeight() >= boxRefresherBigHeight) {
                    boxRefresherBigHeight = refreshLayout.getHeight();
                    boxToolBar.setVisibility(View.GONE);
                    refreshLayout.setEnableRefresh(true);
                    refreshLayout.setEnableLoadMore(true);
                    s = 0;
                } else {
                    boxToolBar.setVisibility(View.VISIBLE);
                    refreshLayout.setEnableRefresh(false);
                    refreshLayout.setEnableLoadMore(false);
                    //scroller.smoothScrollBy(0,dip2px(me,30));
                    s = dip2px(44);
                }
                
                boxEditor.setPadding(
                        0,
                        0,
                        0,
                        s
                );
            }
        });
        
        btnDoubleSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editNotes.getText().insert(editNotes.getSelectionStart(), "　　");
            }
        });
        
        btnUnDo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performEdit.undo();
            }
        });
        
        btnPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = "";
                try {
                    ClipboardManager clip = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    text = clip.getPrimaryClip().getItemAt(0).getText().toString();
                } catch (Exception e) {
                }
                editNotes.getText().insert(editNotes.getSelectionStart(), text);
            }
        });
        
        editNotes.addTextWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                whenTextChange();
            }
        });
    
        editNotes.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (editNotes.getText().toString().contains("　　")){
                    runOnMainDelayed(new Runnable() {
                        @Override
                        public void run() {
                            editNotes.getText().insert(editNotes.getSelectionStart(),"　　");
                        }
                    },50);
                }
                return false;
            }
        });
        
        editTitle.addTextWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                whenTextChange();
            }
        });
    }
    
    private void exportBitmap() {
        Bitmap bitmap = convertViewToBitmap();
        final File saveFile = saveBitmap(bitmap);
        TipDialog.show(me, getString(R.string.export_to_local_finish), TipDialog.TYPE_FINISH);
        //Toast.makeText(me, "已存储在“内存”目录下的“记”文件夹中", Toast.LENGTH_LONG).show();
    
        runOnMainDelayed(new Runnable() {
            @Override
            public void run() {
                WaitDialog.dismiss();
                TipDialog.show(me, getString(R.string.export_to_local_finish), TipDialog.TYPE_FINISH).setDialogLifeCycleListener(new DialogLifeCycleListener() {
                    @Override
                    public void onCreate(Dialog alertDialog) {
                    
                    }
                
                    @Override
                    public void onShow(Dialog alertDialog) {
                    
                    }
                
                    @Override
                    public void onDismiss() {
                        try {
                            Intent share = new Intent(Intent.ACTION_SEND);
                            share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(saveFile));
                            share.setType("image/jpeg");//此处可发送多种文件
                            share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                Uri contentUri = FileProvider.getUriForFile(me, "com.kongzue.notes.fileProvider", saveFile);
                                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                share.setDataAndType(contentUri,"image/jpeg");
                            } else {
                                share.setDataAndType(Uri.fromFile(saveFile), "image/jpeg");
                            }
                            startActivity(Intent.createChooser(share, "分享截图"));
                        } catch (Exception e) {
                            WaitDialog.dismiss();
                            TipDialog.show(me, getString(R.string.export_to_local_error), TipDialog.TYPE_ERROR);
                            if (DEBUGMODE) e.printStackTrace();
                        }
                    }
                });
            }
        }, 500);
    }
    
    private File saveBitmap(Bitmap bm) {
        try {
            String title = textParse(editNotes.getText().toString());
            if (isNull(title)) title = "img_" + ((int) ((Math.random() * 9 + 1) * 100000)) + "";
            String path = Environment.getExternalStorageDirectory().getPath() + "/记";
            File f = new File(path, title + ".jpg");
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            return f;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private String textParse(String text) {
        text = text.substring(0,10);
        text = text.replace("　", "");
        text = text.replace("\n", " ");
        return text;
    }
    
    private Timer timer = new Timer();
    
    private void whenTextChange() {
        if (timer != null) {
            timer.cancel();
            timer = new Timer();
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isNull(editTitle.getText().toString().trim()) && isNull(editNotes.getText().toString().trim())) {
                    return;
                }
                log("存储开始>>>");
                if (dbData == null) {
                    //创建操作
                    dbData = new DBData("notes");
                    dbData.set("title", Base64Util.encode(editTitle.getText().toString()));
                    dbData.set("content", Base64Util.encode(editNotes.getText().toString()));
                    dbData.set("time", System.currentTimeMillis());
                    dbData.set("isSync", false);
                    DBUtil.getInstance().getDb().add(dbData, false);
                    dbData = DBUtil.getInstance().getDb().find(dbData).get(0);
                    log("_id:" + dbData.getInt("_id"));
                } else {
                    if (dbData.getInt("_id") != 0) {
                        dbData.set("title", Base64Util.encode(editTitle.getText().toString()));
                        dbData.set("content", Base64Util.encode(editNotes.getText().toString()));
                        dbData.set("time", System.currentTimeMillis());
                        dbData.set("isSync", false);
                        DBUtil.getInstance().getDb().update(dbData);
                    }
                }
            }
        }, 500); // 延时1秒
    }
    
    @Override
    public void onBackPressed() {
        mainActivity = null;
        super.onBackPressed();
    }
    
    @Override
    public void finish() {
        mainActivity = null;
        super.finish();
    }
    
    public static MainActivity getMainActivity() {
        return mainActivity;
    }
    
    private DBData dbData;
    
    public void setData(DBData data) {
        log("setData");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM月dd日");
        Date date = new Date(data.getLong("time"));
        editTitle.setHint(simpleDateFormat.format(date));
        editTitle.setText(Base64Util.decode(data.getString("title")));
        editNotes.setText(Base64Util.decode(data.getString("content")));
        dbData = data;
        performEdit.clearHistory();
    }
    
    @Override
    public void onPause() {
        if (dbData != null)
            Preferences.getInstance().set(me, "cache", "id", dbData.getInt("_id"));
        super.onPause();
    }
    
    public Bitmap convertViewToBitmap() {
//        view.setDrawingCacheEnabled(true);
//        view.buildDrawingCache();
//        Bitmap bitmap = view.getDrawingCache();
//        return bitmap;
    
        Bitmap bkgImage = BitmapFactory.decodeResource(getResources(), R.mipmap.img_write_bkg);
        int h = 0;
        Bitmap bitmap = null;
        for (int i = 0; i < scroller.getChildCount(); i++) {
            h += scroller.getChildAt(i).getHeight();
        }
        bitmap = Bitmap.createBitmap(scroller.getWidth(), h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        //绘制背景
        int count = (h + bkgImage.getHeight() - 1) / bkgImage.getHeight();
        for(int idx = 0; idx < count; ++ idx){
            canvas.drawBitmap(bkgImage, 0, idx * bkgImage.getHeight(), null);
        }
    
        //绘制内容
        scroller.draw(canvas);
        return bitmap;
    }
}
