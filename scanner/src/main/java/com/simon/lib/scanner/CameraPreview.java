package com.simon.lib.scanner;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.zxing.client.android.camera.CameraConfigurationUtils;

/**
 * @author sunmeng
 * @date 16/6/17
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final long TIME_INTERVAL_SAMPLE = 1000;

    public interface PreviewStateListener {

        void onPreviewStateChanged(boolean preview);

        void onPreviewError(Throwable error);
    }

    private Camera mCamera;
    private boolean mPreviewing;
    private PreviewCallback mPreviewCallback;
    private int mOrientation;
    private PreviewStateListener mPreviewStateListener;
    private Camera.CameraInfo mCameraInfo;
    private boolean surfaceReady;

    private final Handler autoFocusHandler = new Handler();

    private final Runnable doAutoFocus = new Runnable() {
        public void run() {
            autoFocus();
        }
    };

    private final Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, TIME_INTERVAL_SAMPLE);
        }
    };

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setPreviewStateListener(PreviewStateListener previewStateListener) {
        mPreviewStateListener = previewStateListener;
    }

    private synchronized void changePreviewState(boolean previewing) {
        if (previewing == mPreviewing) {
            return;
        }
        mPreviewing = previewing;
        notifyPreviewStateChanged();
    }

    private synchronized void notifyPreviewStateChanged() {
        if (mPreviewStateListener == null) {
            return;
        }
        mPreviewStateListener.onPreviewStateChanged(mPreviewing);
    }

    private void notifyError(Throwable throwable) {
        if (mPreviewStateListener == null) {
            return;
        }
        mPreviewStateListener.onPreviewError(throwable);
    }

    private boolean openCamera() {
        int numCameras = Camera.getNumberOfCameras();
        if (numCameras == 0) {
            notifyError(new IllegalArgumentException("no camera found!"));
            return false;
        }
        int index = 0;
        Camera.CameraInfo cameraInfo = null;
        while (index < numCameras) {
            cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(index, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                break;
            }
            index++;
        }
        if (index >= numCameras) {
            notifyError(new IllegalArgumentException("no FACING_BACK camera found!"));
            return false;
        }
        try {
            mCamera = Camera.open(index);
            mCameraInfo = cameraInfo;
        } catch (Exception e) {
            try {
                mCamera.release();
                mCamera = null;
            } catch (Exception error) {
            }
            notifyError(e);
            return false;
        }
        setPreviewSize();
        return mCamera != null;
    }

    private void setPreviewSize() {
        if (mCamera == null) {
            return;
        }
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            Point screenSize = new Point(getWidth(), getHeight());
            Point bestSize = CameraConfigurationUtils.findBestPreviewSizeValue(parameters,
                    screenSize);
            boolean isScreenPortrait = screenSize.x < screenSize.y;
            boolean isPreviewSizePortrait = bestSize.x < bestSize.y;
            if (isScreenPortrait != isPreviewSizePortrait) {
                parameters.setPreviewSize(bestSize.y, bestSize.x);
            } else {
                parameters.setPreviewSize(bestSize.x, bestSize.y);
            }
            mCamera.setParameters(parameters);
        } catch (Exception e) {
        }
    }

    /**
     * 设置预览回调
     *
     * @param previewCallback
     */
    public void setOneShotPreviewCallback(PreviewCallback previewCallback) {
        mPreviewCallback = previewCallback;
        if (!isPreviewing()) {
            return;
        }
        mCamera.setOneShotPreviewCallback(previewCallback);
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        if (!surfaceReady) {
            getHolder().removeCallback(this);
        }
        changePreviewState(false);
        if (mCamera == null) {
            return;
        }
        try {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        } catch (Exception e) {
            notifyError(e);
        }
    }

    /**
     * 获取相机方向
     *
     * @return
     */
    public int getCameraOrientation() {
        return mOrientation;
    }

    /**
     * 开始预览
     *
     * @return
     */
    public void startPreview() {
        if (!surfaceReady) {
            getHolder().addCallback(this);
            return;
        }
        if (isPreviewing() || (mCamera == null && !openCamera())) {
            return;
        }
        try {
            mOrientation = (4 + mCameraInfo.orientation / 90 - ScannerUtils.getWindowRotation
                    (getContext())) % 4;
            mCamera.setDisplayOrientation(mOrientation * 90);
            mCamera.setPreviewDisplay(getHolder());
            mCamera.startPreview();
            mCamera.setOneShotPreviewCallback(mPreviewCallback);
            changePreviewState(true);
        } catch (Exception e) {
            try {
                mCamera.release();
                mCamera = null;
            } catch (Exception error) {
            }
            notifyError(e);
            return;
        }
        autoFocus();
    }

    private void autoFocus() {
        if (!isPreviewing()) {
            return;
        }
        try {
            mCamera.autoFocus(autoFocusCB);
        } catch (Exception e) {
            autoFocusHandler.postDelayed(doAutoFocus, TIME_INTERVAL_SAMPLE);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!surfaceReady) {
            surfaceReady = true;
            startPreview();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceReady = false;
    }


    public synchronized boolean isPreviewing() {
        return mPreviewing;
    }
}
