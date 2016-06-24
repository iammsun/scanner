package com.simon.lib.scanner;

import android.content.Intent;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class CaptureActivity extends AppCompatActivity implements DecodeHandler.OnResultListener {

    private Camera.PreviewCallback mPreviewCb = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();
            DecodeHandler.DecodeParams previewData = new DecodeHandler.DecodeParams(data, new
                    Point(size.width, size.height), mPreview.getCameraOrientation(), new Point
                    (mFinderView.getWidth(), mFinderView.getHeight()), mFinderView.getPreviewRect
                    (), ScannerUtils.getWindowRotation(CaptureActivity.this));
            mDecodeHandler.decode(previewData);
        }
    };

    protected BeepManager mBeepManager;

    protected CameraPreview mPreview;
    protected FinderView mFinderView;
    private HandlerThread mDecodeThread;
    private DecodeHandler mDecodeHandler;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDecodeThread = new HandlerThread("decode_thread");
        mDecodeThread.start();
        mDecodeHandler = new DecodeHandler(mDecodeThread.getLooper());
        mDecodeHandler.setOnResultListener(this);
        setContentView(getLayoutID());
        mBeepManager = new BeepManager(this);
        mPreview = (CameraPreview) findViewById(R.id.preview_view);
        mFinderView = (FinderView) findViewById(R.id.finder_view);
        if (mPreview == null) {
            throw new RuntimeException(
                    "Your content must have a CameraPreview whose id attribute is 'R.id" +
                            ".preview_view'");
        }
        if (mFinderView == null) {
            throw new RuntimeException(
                    "Your content must have a FinderView whose id attribute is 'R.id" +
                            ".finder_view'");
        }
        mFinderView.setVisibility(View.INVISIBLE);
        mPreview.setPreviewCallback(mPreviewCb);
        mPreview.setPreviewStateListener(new CameraPreview.PreviewStateListener() {
            @Override
            public void onPreviewStateChanged(boolean preview) {
                if (preview) {
                    mFinderView.setVisibility(View.VISIBLE);
                } else {
                    mFinderView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onError(Throwable error) {
                error.printStackTrace();
                finish();
            }
        });
    }

    protected int getLayoutID() {
        return R.layout.activity_capture;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stopPreview();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPreview.startPreview();
    }

    @Override
    public void onBackPressed() {
        if (mPreview.isPreviewing()) {
            super.onBackPressed();
            return;
        }
        mPreview.startPreview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDecodeThread.quit();
        mBeepManager.close();
    }

    @Override
    public void onResult(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBeepManager.beepAndVibrate();
                Intent data = new Intent();
                data.putExtra(Intent.EXTRA_TEXT, text);
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }
}
