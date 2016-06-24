package com.simon.lib.scanner;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.WindowManager;

public class ScannerUtils {

    /**
     * 截取预览区域数据
     *
     * @param data        原始数据
     * @param dataSize    原始像素大小
     * @param previewRect 预览区域
     * @return 截取后数据
     */
    public static byte[] crop(byte[] data, Point dataSize, Rect previewRect) {
        int width = previewRect.width();
        int height = previewRect.height();
        int dataWidth = dataSize.x;
        int dataHeight = dataSize.y;

        if (width == dataWidth && height == dataHeight) {
            return data;
        }

        int area = width * height;
        byte[] matrix = new byte[area];
        int inputOffset = previewRect.top * dataWidth + previewRect.left;

        if (width == dataWidth) {
            System.arraycopy(data, inputOffset, matrix, 0, area);
            return matrix;
        }

        byte[] yuv = data;
        for (int y = 0; y < height; y++) {
            int outputOffset = y * width;
            System.arraycopy(yuv, inputOffset, matrix, outputOffset, width);
            inputOffset += dataWidth;
        }
        return matrix;
    }

    /**
     * 对数据旋转90度
     *
     * @param data     原始数据
     * @param dataSize 原始像素大小
     * @return 旋转后数据
     */
    public static byte[] rotate(byte[] data, Point dataSize) {
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < dataSize.y; y++) {
            for (int x = 0; x < dataSize.x; x++)
                rotatedData[x * dataSize.y + dataSize.y - y - 1] = data[x + y * dataSize.x];
        }
        return rotatedData;
    }

    /**
     * 获取屏幕分辨率
     *
     * @param context
     * @return
     */
    public static Point getScreenSize(Context context) {
        Point point = new Point();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getSize(point);
        return point;
    }

    /**
     * 获取屏幕方向
     *
     * @param context
     * @return
     */
    public static int getWindowRotation(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getRotation();
    }
}
