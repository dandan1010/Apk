package com.hmdglobal.app.n3dot1plusswupdate;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Message;
import android.os.storage.StorageManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.n3dot1plusswupdate.DocumentsUtils;
import com.example.n3dot1plusswupdate.ImportantActivity;
import com.example.n3dot1plusswupdate.RecoveryActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AssestFile {

    private static final String TAG = "AssestFile";
    private InputStream is;
        private FileOutputStream fos;
//    OutputStream fos;
    private int length = 0;
    private Context mContext;
    private long fileLength = 0;

    public AssestFile(Context mContext) {
        this.mContext = mContext;
    }

    public void deepFile(final String path, final String sdcard_path) {

        new Thread() {
            @Override
            public void run() {

                try {
                    is = mContext.getAssets().open(path);
                    fileLength = is.available();
                    Log.d(TAG, "write file legth :" + fileLength);
                    File file = new File(sdcard_path);
                    fos = new FileOutputStream(new File(file, "/update.zip"));
//                    fos = DocumentsUtils.getOutputStream(mContext, new File(file, "/update.zip"));//获取输出流
                    byte[] buffer = new byte[1024];
                    int count = 0;
                    while ((length = is.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                        count += length;
                        Message message = ImportantActivity.myHandle.obtainMessage();
                        message.arg1 = (int) ((float) count / (float) fileLength * 100);
                        ImportantActivity.myHandle.sendMessage(message);
                    }
                    fos.flush();
                    Log.d(TAG, "write file success");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.d(TAG, "error : " + e.getMessage());
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.start();

    }

    public void handleHtmlClickAndStyle(Context context, TextView textview) {
        textview.setMovementMethod(LinkMovementMethod.getInstance());//需要处理点击得加这句
        CharSequence text = textview.getText();
        if (text instanceof Spannable) {
            Spannable sp = (Spannable) text;
            URLSpan[] oldUrlSpans = sp.getSpans(0, text.length(), URLSpan.class);
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
            for (URLSpan oldUrlSpan : oldUrlSpans) {
                //span 不能重复设置，需要先删除
                spannableStringBuilder.removeSpan(oldUrlSpan);
                CustomURLSpan customURLSpan = new CustomURLSpan(context, oldUrlSpan.getURL());
                spannableStringBuilder.setSpan(customURLSpan, sp.getSpanStart(oldUrlSpan),
                        sp.getSpanEnd(oldUrlSpan), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
        }
    }

    //自定义CustomURLSpan，用来替换默认的URLSpan
    private class CustomURLSpan extends ClickableSpan {
        private Context mContext;
        private String mUrl;

        CustomURLSpan(Context context, String url) {
            mUrl = url;
            mContext = context;
        }

        @Override
        public void onClick(View view) {
            //此处处理点击事件  mUrl 为<a>标签的href属性
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);

        }
    }


}
