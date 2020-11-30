package com.ispring.gameplane;

/**
 * 游戏界面状态监听
 */
public interface GameViewStateMonitor {
    /**
     * 游戏已经开始
     */
    void onGameStarted();

    /**
     * 游戏暂停
     */
    void onGamePaused();

    /**
     * 游戏结束
     */
    void onGameOver();
}
