package com.simon.lib.scanner;

import android.graphics.Point;
import android.graphics.Rect;

/**
 * @author sunmeng
 * @date 16/6/17
 */
public interface IDecoder {

    String decode(byte[] data, Point dataSize);
}
