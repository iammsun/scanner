package com.simon.lib.scanner;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author sunmeng
 * @date 16/6/2
 */
public class FinderView extends View {

    protected final Paint paint;

    private static final int MIN_FRAME_WIDTH = 240;
    private static final int MIN_FRAME_HEIGHT = 240;
    private static final int MAX_FRAME_WIDTH = 1200; // = 5/8 * 1920
    private static final int MAX_FRAME_HEIGHT = 675; // = 5/8 * 1080

    /**
     * 四个绿色边角对应的长度
     */
    private int mScreenRate;

    /**
     * 四个绿色边角对应的宽度
     */
    private static final int CORNER_WIDTH = 10;

    /**
     * 中间那条线每次刷新移动的距离
     */
    private static final int SPEEN_DISTANCE = 5;

    /**
     * 手机的屏幕密度
     */
    private static float mDensity;
    /**
     * 字体大小
     */
    private static final int TEXT_SIZE = 16;
    /**
     * 字体距离扫描框下面的距离
     */
    private static final int TEXT_PADDING_TOP = 40;

    /**
     * 中间滑动线的最顶端位置
     */
    private int slideTop;

    private String mHintText;
    private final Rect mLineRect = new Rect();

    private int mFinderWidth;
    private int mFinderHeight;
    private int mXOffset;
    private int mYOffset;
    private final int mMaskColor;

    public FinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources resources = getResources();
        mDensity = resources.getDisplayMetrics().density;
        Point screenSize = ScannerUtils.getScreenSize(context);
        mFinderWidth = findDesiredDimensionInRange(screenSize.x, MIN_FRAME_WIDTH, MAX_FRAME_WIDTH);
        mFinderHeight = findDesiredDimensionInRange(screenSize.y, MIN_FRAME_HEIGHT,
                MAX_FRAME_HEIGHT);
        retrieveAttrs(attrs);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScreenRate = (int) (15 * mDensity);
        mMaskColor = resources.getColor(R.color.finder_mask);
    }

    private int findDesiredDimensionInRange(int resolution, int hardMin, int hardMax) {
        int dim = 5 * resolution / 8;
        if (dim < hardMin) {
            return hardMin;
        }
        if (dim > hardMax) {
            return hardMax;
        }
        return dim;
    }

    private void retrieveAttrs(AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray styles = getContext().obtainStyledAttributes(attrs, R.styleable.finder_style);
        try {
            final int attrsCount = styles.getIndexCount();
            for (int i = 0; i < attrsCount; ++i) {
                final int idx = styles.getIndex(i);
                if (idx == R.styleable.finder_style_finder_width) {
                    mFinderWidth = styles.getDimensionPixelSize(R.styleable
                            .finder_style_finder_width, mFinderWidth);
                } else if (idx == R.styleable.finder_style_finder_height) {
                    mFinderHeight = styles.getDimensionPixelSize(R.styleable
                            .finder_style_finder_height, mFinderHeight);
                } else if (idx == R.styleable.finder_style_finder_x_offset) {
                    mXOffset = styles.getDimensionPixelSize(R.styleable
                            .finder_style_finder_x_offset, mXOffset);
                } else if (idx == R.styleable.finder_style_finder_y_offset) {
                    mYOffset = styles.getDimensionPixelSize(R.styleable
                            .finder_style_finder_y_offset, mYOffset);
                } else if (idx == R.styleable.finder_style_finder_help) {
                    int resource = styles.getResourceId(R.styleable.finder_style_finder_help, 0);
                    if (resource != 0) {
                        mHintText = getResources().getString(resource);
                    }
                }
            }
        } finally {
            styles.recycle();
        }
    }

    public void setFrameSize(int width, int height) {
        mFinderWidth = width;
        mFinderHeight = height;
        invalidate();
    }


    public void setFramePosition(int xoffset, int yoffset) {
        mXOffset = xoffset;
        mYOffset = yoffset;
        invalidate();
    }

    public Rect getPreviewRect() {
        int left = getLeft() + (getWidth() - mFinderWidth) / 2 + mXOffset;
        int top = getTop() + (getHeight() - mFinderHeight) / 2 + mYOffset;
        int right = left + mFinderWidth;
        int bottom = top + mFinderHeight;
        return new Rect(left, top, right, bottom);
    }

    @Override
    public void onDraw(Canvas canvas) {
        Rect preview = getPreviewRect();

        int left = preview.left;
        int top = preview.top;
        int right = preview.right;
        int bottom = preview.bottom;

        paint.setColor(mMaskColor);
        canvas.drawRect(getLeft(), getTop(), getRight(), top, paint);
        canvas.drawRect(getLeft(), bottom, getRight(), getBottom(), paint);
        canvas.drawRect(getLeft(), top, left, bottom, paint);
        canvas.drawRect(right, top, getRight(), bottom, paint);

        //画扫描框边上的角，总共8个部分
        paint.setColor(Color.GREEN);
        canvas.drawRect(left, top, left + mScreenRate,
                top + CORNER_WIDTH, paint);
        canvas.drawRect(left, top, left + CORNER_WIDTH, top
                + mScreenRate, paint);
        canvas.drawRect(right - mScreenRate, top, right,
                top + CORNER_WIDTH, paint);
        canvas.drawRect(right - CORNER_WIDTH, top, right, top
                + mScreenRate, paint);
        canvas.drawRect(left, bottom - CORNER_WIDTH, left
                + mScreenRate, bottom, paint);
        canvas.drawRect(left, bottom - mScreenRate,
                left + CORNER_WIDTH, bottom, paint);
        canvas.drawRect(right - mScreenRate, bottom - CORNER_WIDTH,
                right, bottom, paint);
        canvas.drawRect(right - CORNER_WIDTH, bottom - mScreenRate,
                right, bottom, paint);

        //初始化中间线滑动的最上边和最下边
        if (slideTop == 0) {
            slideTop = top;
        }
        //绘制中间的线，每次刷新界面，中间的线往下移动SPEEN_DISTANCE
        slideTop += SPEEN_DISTANCE;
        if (slideTop >= bottom - CORNER_WIDTH) {
            slideTop = top;
        }

        mLineRect.left = left;
        mLineRect.right = right;
        mLineRect.top = slideTop;
        mLineRect.bottom = slideTop + 18;
        canvas.drawBitmap(((BitmapDrawable) (getResources().getDrawable(R.drawable
                .scan_qrcode_line))).getBitmap(), null, mLineRect, paint);

        //画扫描框下面的字
        if (!TextUtils.isEmpty(mHintText)) {
            paint.setColor(Color.WHITE);
            paint.setTextSize(TEXT_SIZE * mDensity);
            paint.setAlpha(0xaa);
            paint.setTypeface(Typeface.create("System", Typeface.NORMAL));
            float textWidth = paint.measureText(mHintText);
            canvas.drawText(mHintText, (getWidth() - textWidth) / 2,
                    (bottom + (float) TEXT_PADDING_TOP * mDensity), paint);
        }

        postInvalidateDelayed(20L, left, top, right, bottom);
    }

    public void setHint(String hintText) {
        mHintText = hintText;
        invalidate();
    }
}
