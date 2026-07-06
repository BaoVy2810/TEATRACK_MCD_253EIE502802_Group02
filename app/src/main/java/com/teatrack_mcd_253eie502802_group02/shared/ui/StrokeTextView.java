package com.teatrack_mcd_253eie502802_group02.shared.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.appcompat.widget.AppCompatTextView;

import com.teatrack_mcd_253eie502802_group02.R;

/**
 * Text with transparent fill and stroke outline (web -webkit-text-stroke).
 */
public class StrokeTextView extends AppCompatTextView {

    private static final int DEFAULT_STROKE_COLOR = 0x40163557; // rgba(22,53,87,0.25)
    private static final float DEFAULT_STROKE_WIDTH_DP = 3f;

    private float strokeWidthPx;
    private int strokeColor = DEFAULT_STROKE_COLOR;

    public StrokeTextView(Context context) {
        this(context, null);
    }

    public StrokeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public StrokeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        float defaultStrokePx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_STROKE_WIDTH_DP,
                context.getResources().getDisplayMetrics()
        );
        strokeWidthPx = defaultStrokePx;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StrokeTextView);
            strokeWidthPx = a.getDimension(R.styleable.StrokeTextView_strokeWidthDp, defaultStrokePx);
            strokeColor = a.getColor(R.styleable.StrokeTextView_strokeColor, DEFAULT_STROKE_COLOR);
            a.recycle();
        }
    }

    public void setStrokeColor(int color) {
        strokeColor = color;
        invalidate();
    }

    public void setStrokeWidthDp(float dp) {
        strokeWidthPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = getPaint();
        Paint.Style previousStyle = paint.getStyle();
        float previousWidth = paint.getStrokeWidth();
        int previousColor = getCurrentTextColor();

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidthPx);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setFakeBoldText(true);
        setTextColor(strokeColor);
        super.onDraw(canvas);

        paint.setStyle(previousStyle);
        paint.setStrokeWidth(previousWidth);
        setTextColor(previousColor);
    }
}
