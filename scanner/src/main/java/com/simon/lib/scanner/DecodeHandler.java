package com.simon.lib.scanner;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

public class DecodeHandler extends Handler {

    public static final class DecodeParams {

        private byte[] data;
        private Point dataSize;
        private int dataRotation;
        private Point previewSize;
        private Rect previewRect;
        private int previewRotation;

        public DecodeParams(byte[] data, Point dataSize, int dataRotation, Point previewSize, Rect
                previewRect, int previewRotation) {
            this.data = data;
            this.dataSize = dataSize;
            this.dataRotation = dataRotation;
            this.previewSize = previewSize;
            this.previewRect = previewRect;
            this.previewRotation = previewRotation;
        }
    }

    public interface OnResultListener {

        void onResult(String text);

    }

    private static final int EVENT_DECODE = 1;

    private final DefaultDecoder mDecoder = new DefaultDecoder(DefaultDecoder.FEATURE_ZBAR |
            DefaultDecoder.FEATURE_ZXING);
    private OnResultListener mOnResultListener;
    private long last;

    public DecodeHandler(Looper looper) {
        super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case EVENT_DECODE:
                if (msg.obj instanceof DecodeParams) {
                    handleData((DecodeParams) msg.obj);
                }
                break;
        }
    }

    public void decode(DecodeParams params) {
        obtainMessage(EVENT_DECODE, params).sendToTarget();
    }

    private void handleData(DecodeParams previewData) {
        if (mOnResultListener == null || isSkip()) {
            return;
        }
        String result = mDecoder.decode(previewData.data, previewData.dataSize, previewData
                .dataRotation, previewData.previewSize, previewData.previewRect, previewData
                .previewRotation);
        updateLast();
        if (result != null) {
            mOnResultListener.onResult(result);
            removeCallbacksAndMessages(null);
        }
    }

    public void setOnResultListener(OnResultListener onResultListener) {
        mOnResultListener = onResultListener;
    }

    private boolean isSkip() {
        return SystemClock.elapsedRealtime() - last < CameraPreview.TIME_INTERVAL_SAMPLE;
    }

    private void updateLast() {
        last = SystemClock.elapsedRealtime();
    }
}
