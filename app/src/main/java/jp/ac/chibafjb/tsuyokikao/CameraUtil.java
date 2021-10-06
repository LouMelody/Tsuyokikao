package jp.ac.chibafjb.tsuyokikao;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;

class CameraUtil {
    private static final String TAG = "[CameraUtil] ";

    public static String getCameraId(CameraManager manager, int facing) throws CameraAccessException {
        for (String cameraId : manager.getCameraIdList()) {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            if (characteristics.get(CameraCharacteristics.LENS_FACING) == facing) {
                return cameraId;
            }
        }
        return null;
    }
}
