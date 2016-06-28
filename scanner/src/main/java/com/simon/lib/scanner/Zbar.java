package com.simon.lib.scanner;

import android.graphics.Point;
import android.graphics.Rect;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.SymbolSet;

/**
 * @author sunmeng
 * @date 16/6/17
 */
class Zbar implements IDecoder {

    static {
        System.loadLibrary("iconv");
    }

    private ImageScanner mImageScanner;

    public Zbar() {
        mImageScanner = new ImageScanner();
        mImageScanner.setConfig(0, Config.X_DENSITY, 3);
        mImageScanner.setConfig(0, Config.Y_DENSITY, 3);
    }

    @Override
    public String decode(byte[] data, Point dataSize) {
        Image barcode = new Image(dataSize.x, dataSize.y, "Y800");
        barcode.setData(data);

        int result = mImageScanner.scanImage(barcode);
        if (result != 0) {
            SymbolSet syms = mImageScanner.getResults();
            if (!syms.isEmpty()) {
                return syms.iterator().next().getData();
            }
        }
        return null;
    }
}
