package com.simon.lib.scanner;

import android.graphics.Point;
import android.graphics.Rect;

public interface IDecoder {

    String decode(byte[] data, Point dataSize);
}
