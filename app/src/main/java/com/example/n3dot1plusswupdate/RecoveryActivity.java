package com.example.n3dot1plusswupdate;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hmdglobal.app.n3dot1plusswupdate.R;

public class RecoveryActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "RecoveryActivity";

    private ImageView recoverImg;
    private TextView promptText, back_tv, next_tv;
    private LinearLayout navigation;
    private Context context;
    private int clickNums = 1;

    private String path;

    private View[] navigations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery);
        initView();
        Intent intent = getIntent();
        path = intent.getStringExtra("path");

        back_tv.setOnClickListener(this);
        next_tv.setOnClickListener(this);
        setView(clickNums);
    }

    private void initView() {
        recoverImg = findViewById(R.id.recover_img);
        promptText = findViewById(R.id.prompt_text);
        back_tv = findViewById(R.id.back_tv);
        next_tv = findViewById(R.id.next_tv);
        navigations = new View[]{findViewById(R.id.navigation1), findViewById(R.id.navigation2),
                findViewById(R.id.navigation3), findViewById(R.id.navigation4),
                findViewById(R.id.navigation5), findViewById(R.id.navigation6),
                findViewById(R.id.navigation7), findViewById(R.id.navigation8)};
        navigation = findViewById(R.id.navigation);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.next_tv:
                clickNums++;
                setView(clickNums);
                break;
            case R.id.back_tv:
                if (clickNums == 1) {
                    clickNums = 0;
                }else {
                    clickNums --;
                }
                setView(clickNums);
                break;
        }
    }

    private void setView(int clickNum) {
        Log.d(TAG, "点击的次数： "+ clickNum);
        for (int i = 0 ;i< navigations.length;i++) {
            if (i + 1 == clickNum) {
                navigations[i].setEnabled(true);
            } else {
                navigations[i].setEnabled(false);
            }
        }
        switch (clickNum) {
            case 0:
                finish();
            case 1:
                recoverImg.setImageResource(R.mipmap.device_img_01);
                promptText.setText(R.string.prompt_text1);
                break;
            case 2:
                recoverImg.setImageResource(R.mipmap.device_img_02);
                promptText.setText(R.string.prompt_text2);
                break;
            case 3:
                recoverImg.setImageResource(R.mipmap.device_img_03);
                promptText.setText(R.string.prompt_text3);
                break;
            case 4:
                recoverImg.setImageResource(R.mipmap.device_img_04);
                promptText.setText(R.string.prompt_text4);
                break;
            case 5:
                recoverImg.setImageResource(R.mipmap.device_img_05);
                promptText.setText(R.string.prompt_text5);
                break;
            case 6:
                recoverImg.setImageResource(R.mipmap.device_img_06);
                promptText.setText(R.string.prompt_text6);
                break;
            case 7:
                recoverImg.setImageResource(R.mipmap.device_img_07);
                promptText.setText(getResources().getString(R.string.prompt_text7));
                break;
            case 8:
                recoverImg.setImageResource(R.mipmap.device_img_08);
                promptText.setText(Html.fromHtml(getResources().getString(R.string.prompt_text8)));
            break;
            case 9:
                clickNums --;
                Intent intent = new Intent(RecoveryActivity.this, ImportantActivity.class);
                intent.putExtra("path", path);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "点击的次数 _ 返回： " + clickNums);
        setView(clickNums);
    }

}