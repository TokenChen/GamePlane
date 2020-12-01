package com.ispring.gameplane;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends Activity implements Button.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
}