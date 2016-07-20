package com.simon.lib.scanner;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.HandlerThread;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author sunmeng
 * @date 16/07/07
 */
public class CaptureFragment extends Fragment {

    private static final String TAG = "CaptureFragment";

    private static final String EXTRA_OFFSETS = "offsets";

    private Camera.PreviewCallback mPreviewCb = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            DecodeHandler.DecodeParams previewData = null;
            try {
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = parameters.getPreviewSize();
                previewData = new DecodeHandler.DecodeParams(data, new
                        Point(size.width, size.height), mPreview.getCameraOrientation(), new Point
                        (mFinderView.getWidth(), mFinderView.getHeight()), mFinderView
                        .getPreviewRect(), ScannerUtils.getWindowRotation(getContext()));
            } catch (Exception e) {
                Log.d(TAG, "failed to get frame data!", e);
            }
            mDecodeHandler.decode(previewData);
        }
    };

    private BeepManager mBeepManager;
    private CameraPreview mPreview;
    private FinderView mFinderView;
    private HandlerThread mDecodeThread;
    private DecodeHandler mDecodeHandler;
    private Point mOffsets;
    private CameraPreview.PreviewStateListener mPreviewStateListener;
    private DecodeHandler.OnResultListener mResultListener;

    public static CaptureFragment newInstance() {
        CaptureFragment fragment = new CaptureFragment();
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_OFFSETS, mOffsets);
    }

    public void setFramePosition(int xoffset, int yoffset) {
        mOffsets = new Point(xoffset, yoffset);
        if (mFinderView != null) {
            mFinderView.setFramePosition(xoffset, yoffset);
        }
    }

    public void setPreviewStateListener(CameraPreview.PreviewStateListener previewStateListener) {
        mPreviewStateListener = previewStateListener;
    }

    public void setOnResultListener(DecodeHandler.OnResultListener resultListener) {
        mResultListener = resultListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mOffsets = savedInstanceState.getParcelable(EXTRA_OFFSETS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_capture, container, false);
        mFinderView = (FinderView) view.findViewById(R.id.finder_view);
        mPreview = (CameraPreview) view.findViewById(R.id.preview_view);
        if (mOffsets != null) {
            mFinderView.setFramePosition(mOffsets.x, mOffsets.y);
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPreview.stopPreview();
                        mBeepManager.beepAndVibrate();
                        if (mResultListener != null) {
                            mResultListener.onResult(text);
                        }
                    }
                });
            }
        });
        mBeepManager = new BeepManager(getActivity());
        mPreview.setOneShotPreviewCallback(mPreviewCb);
        mPreview.setPreviewStateListener(new CameraPreview.PreviewStateListener() {
            @Override
            public void onPreviewStateChanged(boolean preview) {
                if (preview) {
                    mFinderView.setVisibility(View.VISIBLE);
                } else {
                    mFinderView.setVisibility(View.INVISIBLE);
                }
                if (mPreviewStateListener != null) {
                    mPreviewStateListener.onPreviewStateChanged(preview);
                }
            }

            @Override
            public void onPreviewError(Throwable error) {
                if (mPreviewStateListener != null) {
                    mPreviewStateListener.onPreviewError(error);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDecodeThread.quit();
        mBeepManager.close();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPreview.stopPreview();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPreview.startPreview();
    }

    public void startPreview() {
        if (mPreview != null) {
            mPreview.startPreview();
        }
    }
}
