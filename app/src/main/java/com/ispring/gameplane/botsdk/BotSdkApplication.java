package com.ispring.gameplane.botsdk;

import android.app.Application;

import com.baidu.duer.botsdk.BotSdk;

public class BotSdkApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ContextUtil.setContext(this);
        // 初始化BotSDK
        BotSdk.getInstance().init(this);
        // 打开BotSDK的Log开关，开发阶段建议打开Log开关，便于排查问题
        BotSdk.enableLog(true);
        BotSdk.getInstance().register(BotMessageListener.getInstance(), BotConstants.BOTID,
                "zhentao1", BotSDKUtils.sign("zhentao1"),
                "zhentao2", BotSDKUtils.sign("zhentao2"));
    }
}
