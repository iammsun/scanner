package com.simon.lib.scanner;

import android.graphics.Point;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

public class DefaultDecoder {


    private static final int FEATURE_BASE = 0x0001;

    public static final int FEATURE_ZXING = FEATURE_BASE;
    public static final int FEATURE_ZBAR = FEATURE_ZXING << 1;

    private final List<IDecoder> mFeatures = new ArrayList<>();

    public DefaultDecoder(int features) {
        if ((features & FEATURE_ZXING) == FEATURE_ZXING) {
            mFeatures.add(new Zxing());
        }
        if ((features & FEATURE_ZBAR) == FEATURE_ZBAR) {
            mFeatures.add(new Zbar());
        }
        if (mFeatures.isEmpty()) {
            throw new IllegalStateException("no decoder found!");
        }
    }

    /**
     * 扫码接口
     *
     * @param data            原始数据
     * @param dataSize        原始数据尺寸
     * @param dataRotation    原始数据方向
     * @param previewSize     扫码窗口大小
     * @param previewRect     预览窗口尺寸和位置
     * @param previewRotation 预览窗口方向
     * @return
     */
    public String decode(byte[] data, Point dataSize, int dataRotation, Point previewSize, Rect
            previewRect, int previewRotation) {

        if (dataRotation % 2 != previewRotation % 2) {
            data = ScannerUtils.rotate(data, dataSize);
            dataSize = new Point(dataSize.y, dataSize.x);
        }

        previewRect.top = previewRect.top * dataSize.y / previewSize.y;
        previewRect.bottom = previewRect.bottom * dataSize.y / previewSize.y;
        previewRect.left = previewRect.left * dataSize.x / previewSize.x;
        previewRect.right = previewRect.right * dataSize.x / previewSize.x;

        data = ScannerUtils.crop(data, dataSize, previewRect);
        dataSize = new Point(previewRect.width(), previewRect.height());
        for (IDecoder decoder : mFeatures) {
            String result = decoder.decode(data, dataSize);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
