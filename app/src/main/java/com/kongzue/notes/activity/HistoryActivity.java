package com.kongzue.notes.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.hanks.lineheightedittext.LineHeightEditText;
import com.kongzue.baseframework.BaseActivity;
import com.kongzue.baseframework.BaseAdapter;
import com.kongzue.baseframework.interfaces.DarkNavigationBarTheme;
import com.kongzue.baseframework.interfaces.DarkStatusBarTheme;
import com.kongzue.baseframework.interfaces.Layout;
import com.kongzue.baseframework.interfaces.NavigationBarBackgroundColor;
import com.kongzue.baseframework.interfaces.SimpleAdapterSettings;
import com.kongzue.baseframework.interfaces.SimpleMapAdapterSettings;
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
import com.kongzue.kongzuedb.DB;
import com.kongzue.kongzuedb.DBData;
import com.kongzue.notes.BuildConfig;
import com.kongzue.notes.R;
import com.kongzue.notes.util.Base64Util;
import com.kongzue.notes.util.DBUtil;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kongzue.baseframework.BaseFrameworkSettings.DEBUGMODE;

@Layout(R.layout.activity_history)
@NavigationBarBackgroundColor(a = 0)
public class HistoryActivity extends BaseActivity {
    
    private ImageView btnMore;
    private SmartRefreshLayout refreshLayout;
    private LinearLayout boxRefresher;
    private ListView list;
    private LinearLayout boxFooter;
    private ImageView imgLoadMore;
    private TextView txtLoadMore;
    
    @Override
    public void initViews() {
        btnMore = findViewById(R.id.btn_more);
        refreshLayout = findViewById(R.id.refreshLayout);
        boxRefresher = findViewById(R.id.box_refresher);
        list = findViewById(R.id.list);
        boxFooter = findViewById(R.id.box_footer);
        imgLoadMore = findViewById(R.id.img_loadMore);
        txtLoadMore = findViewById(R.id.txt_loadMore);
    }
    
    private List<DBData> dbDataList;
    private List<Map<String, Object>> datas;
    private BaseAdapter baseAdapter;
    
    @Override
    public void initDatas(JumpParameter paramer) {
        refreshLayout.setEnableOverScrollBounce(true);
        refreshLayout.setEnableOverScrollDrag(true);
        refreshLayout.setEnableRefresh(false);
        refreshLayout.setEnableAutoLoadMore(false);
        
        loadDatas();
        baseAdapter = new BaseAdapter(me, datas, R.layout.item_ground, new SimpleMapAdapterSettings() {
            @Override
            public Object setViewHolder(View convertView) {
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.txtText = convertView.findViewById(R.id.txt_text);
                viewHolder.txtTime = convertView.findViewById(R.id.txt_time);
                return viewHolder;
            }
    
            @Override
            public void setData(Object viewHolder, Map data, int index) {
                ViewHolder viewHolders = (ViewHolder) viewHolder;
                viewHolders.txtText.setText((String) data.get("title"));
                viewHolders.txtTime.setText((String) data.get("content"));
            }
    
        });
        
        list.setAdapter(baseAdapter);
        
    }
    
    private void loadDatas() {
        dbDataList = new ArrayList<>();
        dbDataList = DBUtil.getInstance().getDatas();
        datas = new ArrayList<>();
        for (DBData dbData : dbDataList) {
            Map<String, Object> map = new HashMap<>();
            map.put("title", titleParse(dbData));
            map.put("content", contentParse(dbData));
            datas.add(map);
        }
    }
    
    private String contentParse(DBData dbData) {
        String content = Base64Util.decode(dbData.getString("content"));
        content = content.replace("　", "");
        content = content.replace("\n", " ");
        return content;
    }
    
    private String titleParse(DBData dbData) {
        String title = Base64Util.decode(dbData.getString("title"));
        if (isNull(title)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM月dd日");
            Date date = new Date(dbData.getLong("time"));
            title = simpleDateFormat.format(date);
        }
        
        title = title.replace("　", "");
        title = title.replace("\n", " ");
        return title;
    }
    
    @Override
    public void setEvents() {
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                refreshLayout.finishLoadMore(500);
                if (MainActivity.getMainActivity() != null) {
                    finish();
                } else {
                    jump(MainActivity.class, new JumpParameter().put("newNote", true));
                    finish();
                    jumpAnim(R.anim.slide_in_bottom, R.anim.slide_out_top);
                }
            }
        });
        
        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(me, btnMore);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.history, popup.getMenu());
                popup.setOnMenuItemClickListener(onMenuItemClickListener);
                popup.show();
            }
        });
        
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                log("p:" + position);
                if (MainActivity.getMainActivity() != null) {
                    MainActivity.getMainActivity().setData(dbDataList.get(position));
                    onBackPressed();
                } else {
                    jump(MainActivity.class, new JumpParameter().put("data", dbDataList.get(position)));
                    jumpAnim(R.anim.slide_in_bottom, R.anim.slide_out_top);
                }
            }
        });
        
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                String[] areas = new String[]{"导出...", "发送...", "删除"};
                BottomMenu.show(me, areas, new OnMenuItemClickListener() {
                    @Override
                    public void onClick(String text, int index) {
                        switch (index) {
                            case 0:
                                if (!checkPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"})) {
                                    SelectDialog.show(me, getString(R.string.need_permission_title), getString(R.string.import_need_permission), getString(R.string.start_get_permission), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            requestPermission(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, new OnPermissionResponseListener() {
                                                @Override
                                                public void onSuccess(String[] permissions) {
                                                    exportFile(dbDataList.get(position), true);
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
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (isActive) {
                                                exportFile(dbDataList.get(position), true);
                                            }
                                        }
                                    }, 500);
                                }
            
                                break;
                            case 1:
                                shareText(dbDataList.get(position));
                                break;
                            case 2:
                                DBUtil.getInstance().getDb().delete(dbDataList.get(position));
                                loadDatas();
                                baseAdapter.refreshMapDataChanged(datas);
                                if (MainActivity.getMainActivity() != null)
                                    MainActivity.getMainActivity().finish();
                                break;
                            default:
                                break;
                        }
                    }
                },true,"取消").setTitle(titleParse(dbDataList.get(position)));
                return true;
            }
        });
    }
    
    private void shareText(DBData dbData) {
        String text = Base64Util.decode(dbData.getString("content"));
        
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, titleParse(dbData));
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, "分享到"));
    }
    
    private int sameFileIndex;
    
    private void exportFile(DBData dbData, final boolean isShownWaitTip) {
        if (isShownWaitTip) WaitDialog.show(me, "请稍候...");
        JSONArray backUpData = new JSONArray();
        
        try {
            FileOutputStream fos = null;
            try {
                File file = new File(getInnerSDCardPath(), titleParse(dbData)
                        + ".txt");
                if (file.exists()) {
                    if (isShownWaitTip) {
                        file.delete();
                    } else {
                        sameFileIndex = 0;
                        while (file.exists()) {
                            sameFileIndex++;
                            file = new File(getInnerSDCardPath(), titleParse(dbData) + "_" + sameFileIndex
                                    + ".txt");
                        }
                    }
                }
                fos = new FileOutputStream(file);
                
                String text = Base64Util.decode(dbData.getString("content"));
                
                byte[] buffer = text.getBytes();
                fos.write(buffer);
                fos.close();
                
                log(file.getPath());
                
                if (isShownWaitTip) {
                    runOnMainDelayed(new Runnable() {
                        @Override
                        public void run() {
                            WaitDialog.dismiss();
                            TipDialog.show(me, getString(R.string.export_to_local_finish), TipDialog.TYPE_FINISH);
                            
                            Toast.makeText(me, "已存储在“内存”目录下的“记”文件夹中", Toast.LENGTH_LONG).show();
                        }
                    }, 500);
                }
                
            } catch (Exception e) {
                if (isShownWaitTip) {
                    WaitDialog.dismiss();
                    TipDialog.show(me, getString(R.string.export_to_local_error), TipDialog.TYPE_ERROR);
                    if (DEBUGMODE) e.printStackTrace();
                }
            } finally {
                if (fos != null) fos.close();
            }
            
        } catch (Exception e) {
            if (isShownWaitTip) {
                WaitDialog.dismiss();
                TipDialog.show(me, getString(R.string.export_to_local_error), TipDialog.TYPE_ERROR);
                if (DEBUGMODE) e.printStackTrace();
            }
        }
    }
    
    private void exportAll() {
        WaitDialog.show(me, "请稍候...");
        
        sameFileIndex = 0;
        for (DBData dbData : dbDataList) {
            exportFile(dbData, false);
        }
        runOnMainDelayed(new Runnable() {
            @Override
            public void run() {
                WaitDialog.dismiss();
                TipDialog.show(me, getString(R.string.export_to_local_finish), TipDialog.TYPE_FINISH);
                
                Toast.makeText(me, "已存储在“内存”目录下的“记”文件夹中", Toast.LENGTH_LONG).show();
            }
        }, 1000);
    }
    
    public String getInnerSDCardPath() {
        String path = Environment.getExternalStorageDirectory().getPath() + "/记";
        File file = new File(path);
        if (!file.exists())
            file.mkdir();
        return path;
    }
    
    private PopupMenu.OnMenuItemClickListener onMenuItemClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.menu_importText:
                    LayoutInflater inflater = (LayoutInflater) me.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view = inflater.inflate(R.layout.dialog_import_data, null);
                    MessageDialog.show(me, getString(R.string.how_to_import), null, getString(R.string.iknow), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
            
                        }
                    }).setCustomView(view);
                    break;
                case R.id.menu_exportAll:
                    if (!checkPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"})) {
                        DialogSettings.style = DialogSettings.STYLE_MATERIAL;
                        SelectDialog.show(me, getString(R.string.need_permission_title), getString(R.string.import_need_permission), getString(R.string.start_get_permission), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermission(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, new OnPermissionResponseListener() {
                                    @Override
                                    public void onSuccess(String[] permissions) {
                                        exportAll();
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
                        DialogSettings.style = DialogSettings.STYLE_KONGZUE;
                    } else {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (isActive) {
                                    exportAll();
                                }
                            }
                        }, 500);
                    }
                    break;
                case R.id.menu_about:
                    jump(AboutActivity.class);
                    jumpAnim(R.anim.fade, R.anim.hold);
                    break;
            }
            return false;
        }
    };
    
    @Override
    public void finish() {
        super.finish();
        jumpAnim(R.anim.slide_in_bottom, R.anim.slide_out_top);
    }
    
    class ViewHolder {
        TextView txtText;
        TextView txtTime;
    }
}
