package com.kongzue.notes.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kongzue.baseframework.BaseActivity;
import com.kongzue.baseframework.interfaces.Layout;
import com.kongzue.baseframework.interfaces.NavigationBarBackgroundColor;
import com.kongzue.baseframework.util.JumpParameter;
import com.kongzue.notes.BuildConfig;
import com.kongzue.notes.R;

@Layout(R.layout.activity_about)
@NavigationBarBackgroundColor(a = 0)
public class AboutActivity extends BaseActivity {
    
    private ImageView btnBack;
    private TextView ver;
    private TextView btnKongzue;
    
    @Override
    public void initViews() {
        btnBack = findViewById(R.id.btn_back);
        ver = findViewById(R.id.ver);
        btnKongzue = findViewById(R.id.btn_kongzue);
    }
    
    @Override
    public void initDatas(JumpParameter paramer) {
        ver.setText("v"+BuildConfig.VERSION_NAME);
    }
    
    @Override
    public void setEvents() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    
        btnKongzue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("http://www.kongzue.com/");//此处填链接
                intent.setData(content_url);
                startActivity(intent);
            }
        });
    }
    
    @Override
    public void finish() {
        super.finish();
        jumpAnim(R.anim.hold, R.anim.back);
    }
}
