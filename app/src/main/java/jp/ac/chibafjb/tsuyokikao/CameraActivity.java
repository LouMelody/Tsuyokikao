package jp.ac.chibafjb.tsuyokikao;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "[ Camera ]";
    private final int WIDTH = 200;
    private final int HEIGHT = 320;
    private CameraDevice mCameraDevice = null;
    private CameraCaptureSession mCaptureSession = null;
    private ImageReader mImageReader = null;
    private CaptureRequest.Builder mPreviewRequestBuilder = null;
    private CaptureRequest.Builder mCaptureRequestBuilder = null;

    private TextureView mTextureView = null;
    private Surface mPreviewSurface = null;
    private ImageReader.OnImageAvailableListener mTakePictureAvailableListener = null;

    private Handler mHandler = null;
    private Bitmap resultBitmap = null;

    private void openCamera() {
        // CameraManager生成
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);

        mImageReader = ImageReader.newInstance(WIDTH, HEIGHT, ImageFormat.JPEG, 3);
        Log.d(TAG, "mImageReaderSurface = " + mImageReader.getSurface());
        mImageReader.setOnImageAvailableListener(mTakePictureAvailableListener, null);

        try {
            // カメラIDを取得
            String selectedCameraId = CameraUtil.getCameraId(manager, CameraCharacteristics.LENS_FACING_BACK);
            Log.d(TAG, "selectedCameraId = " + selectedCameraId);

            // パーミッションチェック
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // 許可が与えられていなければトーストでメッセージを表示
                Toast.makeText(this, "使用権限がないためカメラが使えません", Toast.LENGTH_LONG).show();
                finish();
            }
            // カメラオープン
            manager.openCamera(selectedCameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "========== openCamera() ----- Cameraアクセスエラー");
            Log.e(TAG, e.toString());
        }
    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Log.d(TAG, "mStateCallback#onOpened() --------------------");
            // カメラデバイスを記録
            mCameraDevice = cameraDevice;

            // TextureViewからサーフェスを取得
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            texture.setDefaultBufferSize(WIDTH, HEIGHT);
            Surface surface = new Surface(texture);

            try {
                // プレビュー用のCaptureRequest.Builderを作成
                mPreviewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                mPreviewRequestBuilder.addTarget(surface);
                // 必要なパラメータの設定
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                // 撮影用
//                mCaptureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
//                mCaptureBuilder.addTarget(mPreviewSurface);
//                mCaptureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
//                mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 90);
//                mCaptureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);

                cameraDevice.createCaptureSession(Arrays.asList(surface), mCaptureSessionCallback, null);

            } catch (CameraAccessException e) {
                Log.e(TAG, "mStateCallback#onOpened()でエラー");
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

    private final CameraCaptureSession.StateCallback mCaptureSessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
            Log.d(TAG, "CameraCaptureSessionCallback#onConfigured()");
            // カメラがクローズされていたら何もしない
            if (mCameraDevice == null) return;

            // キャプチャーセッションの記録
            mCaptureSession = cameraCaptureSession;

            try {
                // プレビューの開始
                cameraCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mPreviewCallback, null);
            } catch (CameraAccessException e) {
                Log.e(TAG, "CameraCaptureSessionCallbackでエラー");
                Log.e(TAG, e.toString());
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
        }
    };

    private final CameraCaptureSession.CaptureCallback mPreviewCallback = new CameraCaptureSession.CaptureCallback() {
    };

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
            public void onClick(View v) {
                Bitmap bitmap = null;
                try {
                    // プレビューの停止
                    mCaptureSession.stopRepeating();
                    if (mTextureView.isAvailable()) {
                        Log.d(TAG, "撮影処理ーーーーーーーーーーーーーーーーーーーーーーーーーーーーーーー");
//                        mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
//                        mCaptureRequestBuilder.addTarget(mImageReader.getSurface());
//                        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
//                        mCaptureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, 0);
//                        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
//                        mCaptureSession.capture(mCaptureRequestBuilder.build(), mCaptureCallback, null);
                        bitmap = mTextureView.getBitmap();
                        Log.d(TAG, "bitmap = " + bitmap.toString());
                        Log.d(TAG, "w=" + bitmap.getWidth() + ", h=" + bitmap.getHeight());
                        resultBitmap = Bitmap.createScaledBitmap(bitmap, WIDTH, HEIGHT, true);
                        Log.d(TAG, "w=" + resultBitmap.getWidth() + ", h=" + bitmap.getHeight());
                    }
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }

                if (bitmap != null) {
                    Intent intent = new Intent();
                    intent.putExtra(MainActivity.EXTRA_DATA, resultBitmap);
                    Log.d(TAG, "intent = " + intent.toString());
                    setResult(RESULT_OK, intent);
                }
                finish();
            }
        });
    }
    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
    };


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
