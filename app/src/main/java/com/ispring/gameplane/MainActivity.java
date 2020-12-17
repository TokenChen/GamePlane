package com.ispring.gameplane;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.duer.botsdk.BotIntent;
import com.baidu.duer.botsdk.BotSdk;
import com.baidu.duer.botsdk.UiContextPayload;
import com.ispring.gameplane.botsdk.BotConstants;
import com.ispring.gameplane.botsdk.BotMessageListener;
import com.ispring.gameplane.botsdk.IBotIntentCallback;

import java.util.Arrays;
import java.util.HashMap;


public class MainActivity extends Activity implements Button.OnClickListener, IBotIntentCallback {

    public static final String TAG = "MainActivity";
    private Button btnStart = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnStart = findViewById(R.id.btnGame);
        BotMessageListener.getInstance().addCallback(this);
        initBroadcast();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if(viewId == R.id.btnGame){
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                int result = checkSelfPermission(Manifest.permission.RECORD_AUDIO);
                if (PackageManager.PERMISSION_GRANTED == result) {
                    startGame();
                } else {
                    String[] permissions = new String[] {Manifest.permission.RECORD_AUDIO};
                    requestPermissions(permissions, 123);
                }
            } else {
                startGame();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123 && permissions != null) {
            for (int i = 0; i < permissions.length; i++) {
                if (Manifest.permission.RECORD_AUDIO.equals(permissions[i])
                        && PackageManager.PERMISSION_GRANTED == grantResults[i]) {
                    startGame();
                    return;
                }
            }
        }
        Toast.makeText(this, "request permission fail", Toast.LENGTH_LONG).show();
    }

    public void startGame(){
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

    private void initBroadcast() {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BotConstants.ACTION_REGISTER_SUCCESS);
        intentFilter.addAction(BotConstants.MAIN_ACTIVITY_LAUNCH_SUCCESS);
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    switch (intent.getAction()) {
                        case BotConstants.ACTION_REGISTER_SUCCESS:
                            Toast.makeText(MainActivity.this, "注册成功!", Toast.LENGTH_LONG).show();
                            initUiControl();
                            break;
                        default:
                            Log.i(TAG, "unknown action:" + intent.getAction());
                            break;
                    }
                }
            }
        }, intentFilter);
    }

    private void initUiControl() {
        UiContextPayload payload = new UiContextPayload();
        /**
         * 增加未识别话术兜底透传机制，用户说到了未识别的预定义话术，可以把用户query原文透传到App内，App可以根据用户query结果做逻辑处理
         * <b>建议自己有兜底语义理解需求的客户设置此开关为false，自身没有语义理解服务的客户请不要调用此代码！</b>
         */
        payload.setEnableGeneralUtterances(false);
        String[] loginWords = {"开始游戏", "开始"};
        // 可以定义支持的话术, link固定写法
        payload.addHyperUtterance(BotConstants.CLICK_START_GAME, Arrays.asList(loginWords), "link", null);
        BotSdk.getInstance().updateUiContext(payload);
    }

    @Override
    public void handleIntent(BotIntent intent, String customData) {

    }

    @Override
    public void onClickLink(String url, HashMap<String, String> paramMap) {
        if (BotConstants.CLICK_START_GAME.equals(url)) {
            btnStart.performClick();
        }
    }

    @Override
    public void onHandleScreenNavigatorEvent(int event) {

    }

    @Override
    protected void onResume() {
        initUiControl();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BotSdk.getInstance().updateUiContext(null);
    }

    @Override
    protected void onDestroy() {
        BotMessageListener.getInstance().removeCallback(this);
        super.onDestroy();
    }
}