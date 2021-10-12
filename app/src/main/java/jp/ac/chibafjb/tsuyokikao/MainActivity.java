package jp.ac.chibafjb.tsuyokikao;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_DATA = "jp.ac.chibafjb.tsuyokikao.BITMAP";
    public static final int RESULT_CODE = 241;
    private static final String TAG = "[Main]===DEBUG:";
    private static final int REQUEST_CAMERA_PERMISSIONS = 1;
    private static final String[] CAMERA_PERMISSIONS = {Manifest.permission.CAMERA};
    private Bitmap mBitmap = null;
    private ImageView mImageView = null;

    static {
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV installed successfully");
        } else {
            Log.d(TAG, "OpenCV is not installed");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, CAMERA_PERMISSIONS, REQUEST_CAMERA_PERMISSIONS);
        }

        mImageView = (ImageView)findViewById(R.id.image_kao);
        Button btnGetKao = (Button)findViewById(R.id.button_get_kao);
        btnGetKao.setOnClickListener(view -> {
            Intent intent = new Intent(getApplication(), CameraActivity.class);
            startActivityForResult(intent, RESULT_CODE);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d(TAG, "requestCode="+requestCode+", resultCode="+resultCode);
        Log.d(TAG, "intent="+intent.toString());

        if (resultCode == RESULT_OK && requestCode == RESULT_CODE && intent != null) {
            Bundle bundle = intent.getExtras();
            mBitmap = (Bitmap)bundle.get(MainActivity.EXTRA_DATA);
            Log.d(TAG, "mBitmap=" + mBitmap.toString());
            mImageView.setImageBitmap(mBitmap);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onstart-------------------");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume-------------------");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause-------------------");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop-------------------");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart-------------------");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy-------------------");
        finish();
    }

}