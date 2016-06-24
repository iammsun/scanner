package com.simon.lib.scanner;

import android.graphics.Point;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

class Zxing implements IDecoder {

    private final MultiFormatReader mMultiFormatReader = new MultiFormatReader();

    @Override
    public String decode(byte[] data, Point dataSize) {
        try {
            Result result = mMultiFormatReader.decode(new BinaryBitmap(new HybridBinarizer(new
                    PlanarYUVLuminanceSource(data, dataSize.x, dataSize.y, 0, 0, dataSize.x,
                    dataSize.y, false))));
            if (result != null) {
                return result.getText();
            }
        } catch (Exception e) {
        }
        return null;
    }
}
