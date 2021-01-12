package com.example.n3dot1plusswupdate;

import androidx.annotation.NonNull;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.vending.expansion.downloader.DownloadProgressInfo;
import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;
import com.google.android.vending.expansion.downloader.DownloaderServiceMarshaller;
import com.google.android.vending.expansion.downloader.Helpers;
import com.google.android.vending.expansion.downloader.IDownloaderClient;
import com.google.android.vending.expansion.downloader.IDownloaderService;
import com.google.android.vending.expansion.downloader.IStub;
import com.hmdglobal.app.n3dot1plusswupdate.AssestFile;
import com.hmdglobal.app.n3dot1plusswupdate.BuildConfig;
import com.hmdglobal.app.n3dot1plusswupdate.MainActivity;
import com.hmdglobal.app.n3dot1plusswupdate.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;

public class ImportantActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, IDownloaderClient {

    private static final String TAG = "ImportantActivity";
    private static File RECOVERY_DIR = new File("/cache/recovery");
    private static File COMMAND_FILE = new File(RECOVERY_DIR, "command");

    public static final String OTA_PACKAGE_NAME1 = "00WW_3_15J";
    public static final String MARKET_OBB1 = "main." + BuildConfig.VERSION_CODE + "."+BuildConfig.APPLICATION_ID+".obb";

    public static final String OTA_PACKAGE_NAME2 = "00WW_3_15H";
    public static final String MARKET_OBB2 = "patch." + BuildConfig.VERSION_CODE + "."+ BuildConfig.APPLICATION_ID +".obb";
    private TextView important_desc;
    private CheckBox agree_box;
    private Button recovery_nextBtn;
    private ProgressDialog progressDialog;
    public static Handler myHandle;
    private AssestFile assestFile;
    private String path;
    private PowerManager powerManager;
    private IStub mDownloaderClientStub;
    private IDownloaderService mRemoteService;
    private Context mContext;

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
                // Check if expansion files are available before going any further
                Log.d(TAG, "market have file : " + expansionFilesDelivered());
                if (!expansionFilesDelivered()) {
                    // Build an Intent to start this activity from the Notification
                    Intent notifierIntent = new Intent(this, ImportantActivity.class);
                    notifierIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                            notifierIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    // Start the download service (if required)
                    int startResult = 0;
                    try {
                        startResult = DownloaderClientMarshaller.startDownloadServiceIfRequired(this,
                                pendingIntent, SampleDownloaderService.class);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    // If download has started, initialize this activity to show download progress
                    if (startResult != DownloaderClientMarshaller.NO_DOWNLOAD_REQUIRED) {
                        Log.d(TAG, "download DownloaderClientMarshaller.NO_DOWNLOAD_REQUIRED : " + startResult);
                        // This is where you do set up to display the download progress (next step)
                        mDownloaderClientStub = DownloaderClientMarshaller.CreateStub(this,
                                SampleDownloaderService.class);
                        // Inflate layout that shows download progress
                    }
                } else {
                    Log.d(TAG, " expansionFilesDelivered is false, deepfile to sdcard : " + Build.VERSION.INCREMENTAL);
                    if (OTA_PACKAGE_NAME1.equals(Build.VERSION.INCREMENTAL)) {
                        assestFile.deepFile(MARKET_OBB1, path);
                    } else if (OTA_PACKAGE_NAME2.equals(Build.VERSION.INCREMENTAL)) {
                        assestFile.deepFile(MARKET_OBB2, path);
                    }
                }
                progressDialog.show();
                break;
            default:
                break;
        }

    }

    @Override
    public void onServiceConnected(Messenger m) {
        mRemoteService = DownloaderServiceMarshaller.CreateProxy(m);
        mRemoteService.onClientUpdated(mDownloaderClientStub.getMessenger());
    }

    @Override
    public void onDownloadStateChanged(int newState) {
        if (newState == IDownloaderClient.STATE_COMPLETED) {
            Log.d(TAG, " onDownloadStateChanged download newState");
            if (OTA_PACKAGE_NAME1.equals(Build.VERSION.INCREMENTAL)) {
                assestFile.deepFile(MARKET_OBB1, path);
            } else if (OTA_PACKAGE_NAME2.equals(Build.VERSION.INCREMENTAL)) {
                assestFile.deepFile(MARKET_OBB2, path);
            }
        }
    }

    @Override
    public void onDownloadProgress(DownloadProgressInfo progress) {
        Log.d(TAG, "onDownloadProgress : " + progress.mOverallProgress);
        progressDialog.setProgress((int) progress.mOverallProgress);
    }

    private boolean expansionFilesDelivered() {
        for (XAPKFile xf : xAPKS) {
            String fileName = Helpers.getExpansionAPKFileName(this, xf.mIsMain, xf.mFileVersion);
            Log.d(TAG, "expansionFilesDelivered file name : " + fileName + " , mIsMain : " + xf.mIsMain + ", version : " + xf.mFileVersion + ", fileSize : " + xf.mFileSize);
            if (!Helpers.doesFileExist(this, fileName, xf.mFileSize, false)) {
                Log.d(TAG, "expansionFilesDelivered return false");
                return false;
            }
        }
        return true;
    }

    private static final XAPKFile[] xAPKS = {
            new XAPKFile(
                    true, // true signifies a main file
                    BuildConfig.VERSION_CODE, // the version of the APK that the file was uploaded
                    // against
                    248686578l // the length of the file in bytes 3.15J
            ),
            new XAPKFile(
                    false, // false signifies a patch file
                    BuildConfig.VERSION_CODE, // the version of the APK that the patch file was uploaded
                    // against
                    250377643l // the length of the patch file in bytes 3.15H
            )
    };

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
        mContext = this;
        Intent intent = getIntent();
        path = intent.getStringExtra("path");
        Log.d(TAG, "sdcard path : " + path);
        if (null != mDownloaderClientStub) {
            mDownloaderClientStub.connect(this);
        }
        initView();
        initListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mDownloaderClientStub) {
            mDownloaderClientStub.disconnect(this);
        }
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
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
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