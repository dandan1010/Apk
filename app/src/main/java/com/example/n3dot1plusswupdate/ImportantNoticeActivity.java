package com.example.n3dot1plusswupdate;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.hmdglobal.app.n3dot1plusswupdate.AssestFile;
import com.hmdglobal.app.n3dot1plusswupdate.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class ImportantNoticeActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "ImportantActivity";

    private TextView important_desc;
    private Button recovery_nextBtn;
    private String path;
    private AssestFile assestFile;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.recovery_nextBtn:
                Intent intent = new Intent(this, RecoveryActivity.class);
                intent.putExtra("path", path);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);
        Intent intent = getIntent();
        path = intent.getStringExtra("path");
        initView();
        initListener();
    }

    private void initView() {
        recovery_nextBtn = findViewById(R.id.recovery_nextBtn);
        important_desc = findViewById(R.id.important_desc);
        assestFile = new AssestFile(this);
        important_desc.setText(Html.fromHtml(getResources().getString(R.string.import_desc_next)));
    }

    private void initListener() {
        recovery_nextBtn.setOnClickListener(this);
        assestFile.handleHtmlClickAndStyle(this, important_desc);
    }


}