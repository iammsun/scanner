package com.simon.lib.scanner;

import android.content.Intent;
import android.graphics.Point;
import android.hardware.Camera;
import android.net.Uri;
import android.os.HandlerThread;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * @author sunmeng
 * @date 16/6/17
 */
public class CaptureActivity extends AppCompatActivity implements CameraPreview
                                                                          .PreviewStateListener {
    private static final String TAG = "CaptureActivity";

    private Camera.PreviewCallback mPreviewCb = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            DecodeHandler.DecodeParams previewData = null;
            try {
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = parameters.getPreviewSize();
                previewData = new DecodeHandler.DecodeParams(data, new
                        Point(size.width, size.height), mPreview.getCameraOrientation(), new Point
                        (mFinderView.getWidth(), mFinderView.getHeight()), mFinderView
                        .getPreviewRect(), ScannerUtils.getWindowRotation(CaptureActivity.this));
            } catch (Exception e) {
                Log.d(TAG, "failed to get frame data!", e);
            }
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
        mDecodeHandler.setOnResultListener(new DecodeHandler.OnResultListener() {
            @Override
            public void onResult(final String text) {
                if (text == null) {
                    mPreview.setOneShotPreviewCallback(mPreviewCb);
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handleResult(text);
                    }
                });
            }
        });
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
        mPreview.setOneShotPreviewCallback(mPreviewCb);
        mPreview.setPreviewStateListener(this);
    }

    protected int getLayoutID() {
        return R.layout.activity_capture;
    }

    @Override
    protected void onPause() {
        mPreview.stopPreview();
        super.onPause();
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

    protected void handleResult(String text) {
        mBeepManager.beepAndVibrate();
        Intent data = new Intent();
        data.putExtra(Intent.EXTRA_TEXT, text);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onPreviewStateChanged(boolean preview) {
        if (preview) {
            mFinderView.setVisibility(View.VISIBLE);
        } else {
            mFinderView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onPreviewError(Throwable error) {
        Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
        finish();
    }
}
