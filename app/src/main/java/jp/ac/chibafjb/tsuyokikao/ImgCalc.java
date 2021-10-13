package jp.ac.chibafjb.tsuyokikao;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


class ImgCalc {
    private static final String TAG = "[ ImgCalc ]";

    public static Bitmap tsuyoCalc(Activity act, Bitmap img) {
        Log.d(TAG, "tsuyoCalc() --------------------");
        Bitmap ret = null;

        try {
            InputStream inStream = act.getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
            File cascadeDir = act.getDir("cascade", Context.MODE_PRIVATE);
            File cascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt.xml");
            FileOutputStream outStream = new FileOutputStream(cascadeFile);
            byte[] buf = new byte[2048];
            int rdBytes;
            while ((rdBytes = inStream.read(buf)) != -1) {
                outStream.write(buf, 0, rdBytes);
            }
            outStream.close();
            inStream.close();

            CascadeClassifier faceDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());

            if (faceDetector.empty()) {
                faceDetector = null;
            } else {
                cascadeDir.delete();
                cascadeFile.delete();
            }

            Mat matImg = new Mat();
            Utils.bitmapToMat(img, matImg);
            MatOfRect faceRects = new MatOfRect();

            faceDetector.detectMultiScale(matImg, faceRects);
            Log.d(TAG, "認識された顔の数:" + faceRects.toArray().length);
            if (faceRects.toArray().length > 0) {
                for (Rect face: faceRects.toArray()) {
                    Log.d(TAG, "顔の縦幅: " + face.height);
                    Log.d(TAG, "顔の横幅: " + face.width);
                    Log.d(TAG, "顔の位置(Y座標): " + face.y);
                    Log.d(TAG, "顔の位置(X座標): " + face.x);
                }
                Rect face = faceRects.toArray()[0];
                Rect roi = new Rect(face.x, face.y, face.width + 32, face.height + 32);
                Mat trim = new Mat(matImg, roi);

//                Imgproc.resize(matImg, matImg, new Size(face.width,face.height),0, 0, Imgproc.INTER_LINEAR);
                Bitmap dsc = Bitmap.createBitmap(roi.width, roi.height, Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(trim, dsc);
                ret = dsc;
            } else {
                ret = img;
            }

            return ret;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return img;
    }
}
