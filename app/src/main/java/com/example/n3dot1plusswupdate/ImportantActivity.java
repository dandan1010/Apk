package com.example.n3dot1plusswupdate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hmdglobal.app.n3dot1plusswupdate.AssestFile;
import com.hmdglobal.app.n3dot1plusswupdate.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class ImportantActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private static final String TAG = "ImportantActivity";
    private static File RECOVERY_DIR = new File("/cache/recovery");
    private static File COMMAND_FILE = new File(RECOVERY_DIR, "command");

    public static final String OTA_PACKAGE_NAME1 = "00WW_3_15J";
    public static final String OTA_PACKAGE_NAME2 = "00WW_3_15H";
    private TextView important_desc;
    private CheckBox agree_box;
    private Button recovery_nextBtn;
    private ProgressDialog progressDialog;
    public static Handler myHandle;
    private AssestFile assestFile;
    private String path;
    private PowerManager powerManager;

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (agree_box.isChecked()) {
            recovery_nextBtn.setEnabled(true);
            recovery_nextBtn.setAlpha(1f);
        } else {
            recovery_nextBtn.setEnabled(false);
            recovery_nextBtn.setAlpha(0.5f);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.recovery_nextBtn:
                progressDialog.show();
                if (OTA_PACKAGE_NAME1.equals(Build.VERSION.INCREMENTAL)) {
                    assestFile.deepFile(OTA_PACKAGE_NAME1 + ".zip", path);
                } else if (OTA_PACKAGE_NAME2.equals(Build.VERSION.INCREMENTAL)) {
                    assestFile.deepFile(OTA_PACKAGE_NAME2 + ".zip", path);
                }
                break;
            default:
                break;
        }

    }

    class MyHandler extends Handler {
        private final WeakReference<ImportantActivity> mAct;

        private MyHandler(ImportantActivity mainActivity) {
            mAct = new WeakReference<ImportantActivity>(mainActivity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.arg1 == 100) {
                progressDialog.dismiss();
                try {
                    recoveryMode(ImportantActivity.this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        powerManager.reboot("recovery-update");
                        return null;
                    }
                }.execute();
            } else {
                progressDialog.setProgress(msg.arg1);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_important);
        Intent intent = getIntent();
        path = intent.getStringExtra("path");
        Log.d(TAG, "sdcard path : " + path);
        initView();
        initListener();
    }

    private void initView() {
        agree_box = findViewById(R.id.agree_box);
        recovery_nextBtn = findViewById(R.id.recovery_nextBtn);
        important_desc = findViewById(R.id.important_desc);
        important_desc.setText(Html.fromHtml(getResources().getString(R.string.import_desc_update)));
        assestFile = new AssestFile(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMax(100);
        //这里设置为不可以通过按取消按钮关闭进度条
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgressDrawable(getResources().getDrawable(R.drawable.progressbar));
        //这里设置的是是否显示进度,设为false才是显示的哦！
        progressDialog.setIndeterminate(false);
        powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        myHandle = new MyHandler(ImportantActivity.this);

    }

    private void initListener() {
        agree_box.setOnCheckedChangeListener(this);
        recovery_nextBtn.setOnClickListener(this);
        assestFile.handleHtmlClickAndStyle(this, important_desc);
    }

    private void recoveryMode(Context context) throws IOException {
        String arg = "--update_package=/sdcard/00WW_3_15J.zip";
        RECOVERY_DIR.mkdirs();

        FileWriter command = new FileWriter(COMMAND_FILE);
        try {
            command.write(arg); // 往/cache/recovery/command中写入recoveryELF的执行参数。
            command.write("\n");
        } finally {
            command.close();
        }
        throw new IOException("Reboot failed (no permissions?)");
    }
}