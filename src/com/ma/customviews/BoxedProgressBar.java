package com.ma.customviews;

import com.ma.dc.Common;
import com.ma.dc.util.LogHelper;
import com.ma.dc.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class BoxedProgressBar extends View {

    private int mNrOfBoxes;
    private int mProgressInPercent;
    private int mStartColor;
    private int mIntermediateColor;
    private int mCriticalColor;

    private int mBoxWidth;
    private Paint[] mFillPaints;
    private Paint mBoxPaint;
    private Rect[] mRects;
    
    private boolean initialized = false;

    public BoxedProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);       

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.BoxedProgressBar, 0, 0);

        setWillNotDraw(false);

        try {
            mNrOfBoxes = a.getInteger(R.styleable.BoxedProgressBar_nrOfBoxes, 6);
            mProgressInPercent = a.getInteger(R.styleable.BoxedProgressBar_setProgressInPercent, 0);
            mStartColor = a.getColor(R.styleable.BoxedProgressBar_startColor, Color.GREEN);
            mIntermediateColor = a.getColor(R.styleable.BoxedProgressBar_intermediateColor, Color.GREEN);
            mCriticalColor = a.getColor(R.styleable.BoxedProgressBar_criticalColor, Color.GREEN);
            
            mFillPaints = new Paint[mNrOfBoxes];
            mRects = new Rect[mNrOfBoxes];
        } finally {
            a.recycle();
        }
    }

    private void init(int width, int heigth) {
        final int startX = getPaddingLeft();
        final int startY = getPaddingTop();
        
        final int properWidth = width - (startX + getPaddingRight());
        final int properHeigth = heigth - (startY + getPaddingBottom());
        
        final int endY = startY + properHeigth;
        
        mBoxWidth = properWidth / mNrOfBoxes;

        final int startIntermidiateColor = mNrOfBoxes / 3;
        final int startCriticalColor = startIntermidiateColor * 2;

        for (int i = 0; i < mNrOfBoxes; i++) {
            Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG); 

            fillPaint.setStyle(Paint.Style.FILL);
            if (i < startIntermidiateColor) {
                fillPaint.setColor(mStartColor);
            } else if (i < startCriticalColor) {
                fillPaint.setColor(mIntermediateColor);
            } else {
                fillPaint.setColor(mCriticalColor);
            }

            int boxStartX = startX + (i * mBoxWidth);     

            mRects[i] = new Rect(boxStartX, startY, boxStartX + mBoxWidth, endY);

            mFillPaints[i] = fillPaint;
            
        }
        
        mBoxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setColor(Color.BLACK);
        
        initialized = true;
    }
    
    
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onSizeChanged w:" + w);
        
        if(w == 0 || h == 0){
            return;
        }
        
        init(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if(!initialized) {
            return;
        }

        int endPos = (mProgressInPercent * mNrOfBoxes) / 100;
        
        for (int i = 0; i < endPos; i++) {
            canvas.drawRect(mRects[i], mFillPaints[i]);
        }
        
        for (int i = 0; i < mNrOfBoxes; i++) {
            canvas.drawRect(mRects[i], mBoxPaint);
        }
    }

    public void setProgressInPercent(final int progressInPercent) {
        if (mProgressInPercent != progressInPercent) {
            mProgressInPercent = progressInPercent;
            invalidate();
            requestLayout();
        }
    }

}
