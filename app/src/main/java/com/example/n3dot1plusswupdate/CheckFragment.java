package com.example.n3dot1plusswupdate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.hmdglobal.app.n3dot1plusswupdate.R;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

public class CheckFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "CheckFragment";
    public static final String OTA_PACKAGE_NAME1 = "00WW_3_15J";
    public static final String OTA_PACKAGE_NAME2 = "00WW_3_15H";
    private View view;
    public static final int FREE_SIZE = 600;
    private static String sdcard_path;

    private TextView errorText, new_version_desc, done_sd_text, done_space_text, done_power_text;
    private ImageView done_sd_img, done_space_img, done_power_img;
    private Button nextBtn;
    private LinearLayout check_relative;
    private ReceiveryBroadcastReciver reciver;
    private Context mContext;
    private Timer timer;
    private TimerTask timerTask;
    private int level = 0;
    private boolean checkSdFlag = false;
    private boolean checkPowerFlag = false;
    private boolean checkSpaceFlag = false;
    private Handler myHandle = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                checkConditions();
            }
        }
    };

    public CheckFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().findViewById(R.id.recovery_btn).setVisibility(View.GONE);
        view = inflater.inflate(R.layout.fragment_check, container, false);
        mContext = getContext();
        initView();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (reciver != null) {
            mContext.unregisterReceiver(reciver);
        }
    }

    private void initView() {
        errorText = view.findViewById(R.id.error_text);
        nextBtn = view.findViewById(R.id.recovery_btn);
        done_sd_text = view.findViewById(R.id.done_sd_text);
        done_space_text = view.findViewById(R.id.done_space_text);
        done_power_text = view.findViewById(R.id.done_power_text);
        done_sd_img = view.findViewById(R.id.done_sd_img);
        done_space_img = view.findViewById(R.id.done_space_img);
        done_power_img = view.findViewById(R.id.done_power_img);
        new_version_desc = view.findViewById(R.id.new_version_desc);
        check_relative = view.findViewById(R.id.check_relative);

        reciver = new ReceiveryBroadcastReciver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);//电池电量变化

        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);//sd卡卸载
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);//sd卡挂载

        mContext.registerReceiver(reciver, intentFilter);
    }

    private void checkConditions() {
        Log.d(TAG, "checkConditions");
        nextBtn.setAlpha(0.5f);
        done_sd_text.setAlpha(0.5f);
        done_space_text.setAlpha(0.5f);
        done_power_text.setAlpha(0.5f);

        if (isUpVersion()) {
            check_relative.setVisibility(View.VISIBLE);
            errorText.setText(getResources().getString(R.string.check_title));
            new_version_desc.setVisibility(View.GONE);
            nextBtn.setText(R.string.button_next);
            nextBtn.setEnabled(false);
            if (isStorageMounted(mContext)) {
                if (timer != null) {
                    timer.cancel();
                }
                checkSdFlag = true;
                done_sd_img.setImageResource(R.mipmap.done);
                done_sd_text.setEnabled(true);
                done_sd_text.setAlpha(1f);
            } else {
                checkSdFlag = false;
                done_sd_img.setImageResource(R.mipmap.untested);
                done_sd_text.setEnabled(false);
                if (timer != null) {
                    timer.cancel();
                }
                timer = new Timer();
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        myHandle.sendEmptyMessage(1);
                    }
                };
                timer.schedule(timerTask, 3 * 1000);
            }

            if (getSdBlockSize()) {
                checkSpaceFlag = true;
                done_space_img.setImageResource(R.mipmap.done);
                done_space_text.setEnabled(true);
                done_space_text.setAlpha(1f);
            } else {
                checkSpaceFlag = false;
                done_space_img.setImageResource(R.mipmap.untested);
                done_space_text.setEnabled(false);
            }
            if (level >= 30) {
                checkPowerFlag = true;
                done_power_text.setEnabled(true);
                done_power_text.setAlpha(1f);
                done_power_img.setImageResource(R.mipmap.done);
            } else {
                checkPowerFlag = false;
                done_power_text.setEnabled(false);
                done_power_img.setImageResource(R.mipmap.untested);
            }
            if (checkPowerFlag && checkSdFlag && checkSpaceFlag) {
                nextBtn.setEnabled(true);
                nextBtn.setAlpha(1f);
                nextBtn.setOnClickListener(this);
            } else {
                nextBtn.setEnabled(false);
            }
        } else {
            check_relative.setVisibility(View.GONE);
            errorText.setText(getResources().getString(R.string.new_version));
            new_version_desc.setVisibility(View.VISIBLE);
            nextBtn.setEnabled(true);
            nextBtn.setAlpha(1f);
            nextBtn.setText(R.string.exit);
            nextBtn.setOnClickListener(this);
        }
    }


    //判断外置sd卡是否存在
    public static boolean isStorageMounted(Context mContext) {
        boolean isMounted = false;
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Method getState = storageVolumeClazz.getMethod("getState");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                String state = (String) getState.invoke(storageVolumeElement);
                sdcard_path = (String) getPath.invoke(storageVolumeElement);
                Log.d(TAG, "sdcard_path : " + sdcard_path);
                if (removable && state.equals(Environment.MEDIA_MOUNTED)) {
                    isMounted = removable;
                    break;
                }

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return isMounted;
    }

    //获取sd卡的空间
    private boolean getSdBlockSize() {
        StatFs stat = (StatFs) new StatFs(sdcard_path);
        long blockSize;
        long availableBlocks;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
            availableBlocks = stat.getAvailableBlocksLong();
        } else {
            blockSize = stat.getBlockSize();
            availableBlocks = stat.getAvailableBlocks();
        }
        String size = Formatter.formatFileSize(mContext, availableBlocks * blockSize);
        Log.d(TAG, "SD card remaining space" + availableBlocks * blockSize + ",  size " + size);
        return availableBlocks * blockSize >= FREE_SIZE * 1024 * 1024;
    }

    //获取要升级的版本号
    private boolean isUpVersion() {
        if (OTA_PACKAGE_NAME1.equals(Build.VERSION.INCREMENTAL)) {
            return true;
        } else if (OTA_PACKAGE_NAME2.equals(Build.VERSION.INCREMENTAL)) {
            return true;
        } else {
            return true;
        }
    }

    @Override
    public void onClick(View view) {
        if (nextBtn.getText().equals(getResources().getString(R.string.exit))) {
            getActivity().finish();
        } else {
            Intent intent = new Intent(mContext, ImportantNoticeActivity.class);
            intent.putExtra("path", sdcard_path);
            startActivity(intent);
        }
    }

    private class ReceiveryBroadcastReciver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            //判断当前电量是否改变
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                //获取手机当前电量
                level = intent.getIntExtra("level", 0);
                checkConditions();
            } else if (intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                checkConditions();
            } else if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
                checkConditions();
            }
        }
    }
}