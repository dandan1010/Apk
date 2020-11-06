package com.example.n3dot1plusswupdate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;

public class SampleAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try{
            DownloaderClientMarshaller.startDownloadServiceIfRequired(context, intent,
                    SampleDownloaderService.class);
        }catch(PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
