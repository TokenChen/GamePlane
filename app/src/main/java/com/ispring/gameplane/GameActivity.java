package com.ispring.gameplane;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.baidu.duer.botvoiceapi.BotSpeechRecog;
import com.baidu.duer.botvoiceapi.ISpeechTranscriberResultListener;
import com.baidu.duer.speech.soundspec.SpecResult;
import com.ispring.gameplane.game.GameView;


public class GameActivity extends Activity implements GameViewStateMonitor, ISpeechTranscriberResultListener {

    private static final int NONE_BULLET = 60;
    private static final int SINGLE_BULLET = 80;
    private static final int DOUBLE_BULLET = 95;
    private static final int BOMB_BULLET = 99;
    private GameView gameView;
    public static final String TAG = "GameActivity";
    private TextView speechResult;
    private long lastRecognizeTimestamp = 0;
    private boolean isRecognization = false;

    private int speechToBombSeconds = 3; //最大声音三秒才会触发爆炸
    private boolean isWaitingBomb = false;
    private final Handler mainHandler = new Handler();
    private boolean isResetting = false;
    private final Runnable resetWaitingBomb = new Runnable() {
        @Override
        public void run() {
            mainHandler.removeCallbacks(fireBomb);
            gameView.updateGameStatus(GameView.STATUS_GAME_STARTED);
            isWaitingBomb = false;
            isResetting = false;
        }
    };
    private final Runnable fireBomb = new Runnable() {
        @Override
        public void run() {
            gameView.fireBomb();
            isWaitingBomb = false;
            mainHandler.removeCallbacks(resetWaitingBomb);
            gameView.updateGameStatus(GameView.STATUS_GAME_STARTED);
            isResetting = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        speechResult = findViewById(R.id.speech_recognize_result);
        gameView = (GameView)findViewById(R.id.gameView);
        //0:combatAircraft
        //1:explosion
        //2:yellowBullet
        //3:blueBullet
        //4:smallEnemyPlane
        //5:middleEnemyPlane
        //6:bigEnemyPlane
        //7:bombAward
        //8:bulletAward
        //9:pause1
        //10:pause2
        //11:bomb
        int[] bitmapIds = {
                R.drawable.plane,
                R.drawable.explosion,
                R.drawable.yellow_bullet,
                R.drawable.blue_bullet,
                R.drawable.small,
                R.drawable.middle,
                R.drawable.big,
                R.drawable.bomb_award,
                R.drawable.bullet_award,
                R.drawable.pause1,
                R.drawable.pause2,
                R.drawable.bomb
        };
        gameView.start(bitmapIds);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(gameView != null){
            gameView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(gameView != null){
            gameView.destroy();
        }
        gameView = null;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        BotSpeechRecog.getInstance().init(getApplicationContext());
        boolean setLevelSuccess = BotSpeechRecog.getInstance().setLevel(BotSpeechRecog.Level.P1_VOICE_API);
        BotSpeechRecog.getInstance().setSpeechResultListener(this);
        boolean recordingSuccess = BotSpeechRecog.getInstance().startRecording();
        if (setLevelSuccess && recordingSuccess) {
            Log.i(TAG, "init bot speech recognize success");
        } else {
            Log.e(TAG, "init bot speech recognize fail");
        }
        BotSpeechRecog.getInstance().startRecognition();
        gameView.setGameViewStateMonitor(this);
    }

    @Override
    public void onGameStarted() {
        Log.i(TAG, "on game started and start recogniztion");
        if (!isRecognization) {
            isRecognization = true;
        }
    }

    @Override
    public void onGamePaused() {
        isResetting = false;
        isWaitingBomb = false;
        mainHandler.removeCallbacksAndMessages(null);
        Log.i(TAG, "on game paused and stop recogniztion");
    }

    @Override
    public void onGameOver() {
        isResetting = false;
        isWaitingBomb = false;
        mainHandler.removeCallbacksAndMessages(null);

    }

    @Override
    public void onHandleP1SpeechResult(final SpecResult specResult) {
        if (specResult != null && (gameView.getStatus() == GameView.STATUS_GAME_STARTED || gameView.getStatus() == GameView.STATUS_WARNING_BOMB)) {
            if (specResult.resultTimestamp - lastRecognizeTimestamp > 200) {
                lastRecognizeTimestamp = specResult.resultTimestamp;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        speechResult.setText("Energy:" + specResult.energy);
                        resetWaitingBomb(specResult.energy);
                        if (specResult.energy < NONE_BULLET) {
                            // 能量低于50，不发射子弹
                            speechResult.setTextColor(Color.GREEN);
                            gameView.getCombatAircraft().setFiring(false);
                        } else if (specResult.energy < DOUBLE_BULLET) {
                            gameView.getCombatAircraft().setSingle(true);
                            gameView.getCombatAircraft().setFiring(true);
                        } else if (specResult.energy < BOMB_BULLET) {
                            speechResult.setTextColor(Color.YELLOW);
                            gameView.getCombatAircraft().setSingle(false);
                            gameView.getCombatAircraft().setFiring(true);
                        } else {
                            triggerBomb();
                            speechResult.setTextColor(Color.RED);
                        }
                    }
                });
            }
        }
    }

    private void triggerBomb() {
        if (!isWaitingBomb) {
            mainHandler.postDelayed(fireBomb, 3000);
            gameView.updateGameStatus(GameView.STATUS_WARNING_BOMB);
            isWaitingBomb = true;
        }
        if (isResetting) {
            mainHandler.removeCallbacks(resetWaitingBomb);
            isResetting = false;
        }
    }

    private void resetWaitingBomb(double speechEnergy) {
        if (speechEnergy < BOMB_BULLET && isWaitingBomb && !isResetting) {
            // 如果当前正在等待爆炸，并且未取消中，并且能量小于炸弹能量，发送一个取消任务
            mainHandler.postDelayed(resetWaitingBomb, 1000);
            isResetting = true;
        }
    }

    @Override
    public void onHandleP2SpeechResult(String s) {

    }
}