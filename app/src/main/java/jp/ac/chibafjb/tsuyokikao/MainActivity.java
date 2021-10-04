package jp.ac.chibafjb.tsuyokikao;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "[Main]===DEBUG:";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v(TAG, "onCreate()が呼ばれました。");


        Intent intent = new Intent(getApplication(), CameraActivity.class);
        startActivity(intent);
        Log.v(TAG, "CameraActivity呼びだし: " + intent.toString());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart()が呼ばれました。");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()が呼ばれました。");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()が呼ばれました。");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "onStop()が呼ばれました。");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.v(TAG, "onRestart()が呼ばれました。");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy()が呼ばれました。");
        System.exit(0);
    }

}