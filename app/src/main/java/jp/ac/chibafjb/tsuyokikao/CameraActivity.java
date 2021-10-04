package jp.ac.chibafjb.tsuyokikao;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.concurrent.Executor;

public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "[ Camera ]";
    private static final int REQUEST_CAMERA_PERMISSIONS = 1;
    private static final String[] CAMERA_PERMISSIONS = {Manifest.permission.CAMERA};

    private CameraDevice mCameraDevice = null;
    private CameraCaptureSession mCaptureSession = null;
    private TextureView mTextureView = null;
    private Surface mSurface = null;
    private CaptureRequest.Builder mPreviewRequestBuilder = null;

    private void openCamera() {
        Log.d(TAG, "openCamera()------------------------------");
        CameraManager manager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);

        try {
            // カメラIDを取得
            String selectedCameraId = CameraUtil.getCameraId(manager, CameraCharacteristics.LENS_FACING_BACK);
            Log.v(TAG, "selectedCameraId = " + selectedCameraId);

            // 画面に撮影の許可を確認するダイアログが表示される
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(
                        this,
                        CAMERA_PERMISSIONS,
                        REQUEST_CAMERA_PERMISSIONS
                );
            }

            manager.openCamera(selectedCameraId, mStateCallback, null);

        } catch (CameraAccessException e) {
            Log.e(TAG, "========== Cameraアクセスエラー");
            Log.e(TAG, e.toString());
        }
    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Log.v(TAG, "OpenCameraCallback==========");
            mCameraDevice = cameraDevice;
            ArrayList<Surface> surfaceList = new ArrayList();
            mSurface = new Surface(mTextureView.getSurfaceTexture());
            surfaceList.add(mSurface);
            try {
                mPreviewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                mPreviewRequestBuilder.addTarget(mSurface);
                cameraDevice.createCaptureSession(surfaceList, new CameraCaptureSessionCallback(), null);
            } catch (CameraAccessException e) {
                Log.e(TAG, "OpenCameraCallbackでエラー");
                Log.e(TAG, e.toString());
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            mCameraDevice = null;
        }
    };

    private class CameraCaptureSessionCallback extends CameraCaptureSession.StateCallback {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
            mCaptureSession = cameraCaptureSession;

            try {
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                        CameraMetadata.CONTROL_AF_TRIGGER_START);
                cameraCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), new CaptureCallback(), null);
            } catch (CameraAccessException e) {
                Log.e(TAG, "CameraCaptureSessionCallbackでエラー");
                Log.e(TAG, e.toString());
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

        }
    }

    private class CaptureCallback extends CameraCaptureSession.CaptureCallback {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()--------------------");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mTextureView = (TextureView) findViewById(R.id.preview);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                openCamera();
            }
            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                return true;
            }
            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) { }
            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) { }
        });

        findViewById(R.id.btnCapture).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { finish(); }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // カメラセッションの終了
        if (mCaptureSession != null) {
            try {
                mCaptureSession.stopRepeating();
            } catch (CameraAccessException e) {
                Log.e(TAG, "========== キャプチャセッション終了時のエラー");
                Log.e(TAG, e.toString());
            }
            mCaptureSession.close();
        }
        // カメラデバイスの切断
        if (mCameraDevice != null) {
            mCameraDevice.close();
        }
    }
}
