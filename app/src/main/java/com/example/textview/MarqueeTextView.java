package com.example.textview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MarqueeTextView extends androidx.appcompat.widget.AppCompatTextView {

    private TypedArray array;
    private Paint mPaint;
//    private Bitmap bitmap;
    private Disposable disposable;

    private int times;
    private int speed;
//    private Float centerBaseLine;
    private float textWidth;
    private int textHeight;
    private int textScrollTo;
    private int viewWidth;
    private int realWidth;
    private int realHeight;

    private boolean isScrolling = false;
    private boolean firstScroll = true;

    public String TAG = "MarqueeTextView";
    private Paint.FontMetrics fontMetrics;
    private boolean isStop = true;
    private int scrollTimes = 0;
    private LinearLayout.LayoutParams param;
    private boolean isInit = true;
    private Bitmap drawingCache;

    private Observable<Long> longObservable;
    private Observer<Long> observer;

    public MarqueeTextView(Context context) {
        super(context);
    }

    public MarqueeTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        array = context.getTheme()
                .obtainStyledAttributes(attrs,
                        R.styleable.MarqueeTextView,
                        0, 0);
        speed = array.getInt(R.styleable.MarqueeTextView_speed, 5);
        times = array.getInt(R.styleable.MarqueeTextView_times, 0);
        Init();
    }

    private void Init() {
        setMaxLines(1);
        mPaint = getPaint();
        textWidth = mPaint.measureText(getText().toString());
        fontMetrics = mPaint.getFontMetrics();
        textHeight = (int) (fontMetrics.descent - fontMetrics.ascent
                + fontMetrics.leading);
        InitObserver();
    }

    public MarqueeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width, height;
        width = measureLength(0, widthMeasureSpec);
        height = measureLength(1, heightMeasureSpec);
        if (isInit) {
            realWidth = width;
            realHeight = height;
            setMeasuredDimension((int) textWidth, height);
        } else {
            setMeasuredDimension(width, height);
        }

    }

    private int measureLength(int widthOrHeight, int measureSpec) {
        int Measured;
        int mode = MeasureSpec.getMode(measureSpec);
        switch (mode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                if (widthOrHeight == 0) {
                    Measured = (int) (textWidth + getPaddingBottom() + getPaddingTop()
                            + getPaddingRight() + getPaddingLeft());
                } else {
                    Measured = textHeight + getPaddingBottom() + getPaddingTop()
                            + getPaddingStart() + getPaddingEnd();
                }
                break;
            case MeasureSpec.EXACTLY:
            default:
                Measured = measureSpec + getPaddingBottom() + getPaddingTop()
                        + getPaddingRight() + getPaddingLeft();
                break;
        }
        return Measured;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
//        int centerY = getMeasuredHeight() / 2;
//        centerBaseLine = centerY + (fontMetrics.bottom - fontMetrics.ascent) / 2
//                - fontMetrics.descent;
//        bitmap = Bitmap.createBitmap((int) textWidth, h, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        canvas.drawText(getText().toString(),
//                -textScrollTo,
//                centerBaseLine, mPaint);
        param = new LinearLayout.LayoutParams(realWidth, realHeight);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        if (mPaint != null) {
            textWidth = mPaint.measureText(getText().toString());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInit) {
            super.onDraw(canvas);
            buildDrawingCache();
            drawingCache = getDrawingCache();
            setLayoutParams(param);
            isInit = false;
        }
        if (!firstScroll) {
            canvas.drawBitmap(drawingCache,
                    viewWidth - textScrollTo,
                    0, null);
        } else {
            canvas.drawBitmap(drawingCache, -textScrollTo, 0, null);
        }

        if (times != 0 && scrollTimes >= times) {
            stopScrolling();
            canvas.drawBitmap(drawingCache, -textScrollTo, 0, null);
        }

        if (!isScrolling && isStop) {
            isScrolling = true;
            scrolling();
        }
    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent event) {
//        int action = event.getAction();
//
//        switch (action){
//            case MotionEvent.ACTION_DOWN:
//                break;
//            case MotionEvent.ACTION_MOVE:
//                Log.d(TAG, "onTouchEvent: ");
//                textScrollTo+=event.getX();
//                invalidate();
//                break;
//        }
//        return super.dispatchTouchEvent(event);
//    }

    private void scrolling() {
        longObservable.subscribe(observer);
    }

    private void InitObserver() {
        longObservable = Observable.interval(speed, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread());

        observer = new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(Long aLong) {
                textScrollTo++;
                if (firstScroll) {
                    if (textScrollTo == textWidth) {
                        textScrollTo = 0;
                        scrollTimes++;
                        firstScroll = false;
                    }
                } else {
                    if (textScrollTo == (textWidth + viewWidth)) {
                        textScrollTo = 0;
                        scrollTimes++;
                    }
                }
                if (isStop) {
                    invalidate();
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
    }

    private int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    private int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

    public void stopScrolling() {
        if (isScrolling) {
            isStop = false;
            isScrolling = false;
            if (disposable != null) {
                disposable.dispose();
                System.gc();
            }
        }
    }

    public void startScrolling() {
        if (!isScrolling) {
            isStop = true;
            invalidate();
        }
    }

    public boolean isScrolling() {
        return isScrolling;
    }
}
