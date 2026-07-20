package com.lathaeps.lathabulk;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

/** Pinch, double-tap and drag image preview without an external UI dependency. */
final class ZoomImageView extends ImageView {
    private final Matrix drawMatrix = new Matrix();
    private final ScaleGestureDetector scaleDetector;
    private final GestureDetector gestureDetector;
    private float baseScale = 1f;
    private float currentScale = 1f;
    private float lastX;
    private float lastY;
    private boolean dragging;

    ZoomImageView(Context context) {
        super(context);
        setScaleType(ScaleType.MATRIX);
        setBackgroundColor(0xffeeeeee);
        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override public boolean onScale(ScaleGestureDetector detector) {
                float wanted = currentScale * detector.getScaleFactor();
                float limited = Math.max(baseScale, Math.min(baseScale * 5f, wanted));
                float factor = limited / currentScale;
                drawMatrix.postScale(factor, factor, detector.getFocusX(), detector.getFocusY());
                currentScale = limited;
                constrain();
                setImageMatrix(drawMatrix);
                return true;
            }
        });
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDown(MotionEvent e) { return true; }
            @Override public boolean onDoubleTap(MotionEvent e) {
                if (currentScale > baseScale * 1.1f) resetToFit();
                else {
                    float factor = 2f;
                    drawMatrix.postScale(factor, factor, e.getX(), e.getY());
                    currentScale *= factor;
                    constrain();
                    setImageMatrix(drawMatrix);
                }
                return true;
            }
        });
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        resetToFit();
    }

    @Override public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        post(this::resetToFit);
    }

    private void resetToFit() {
        Drawable drawable = getDrawable();
        if (drawable == null || getWidth() <= 0 || getHeight() <= 0) return;
        float dw = Math.max(1, drawable.getIntrinsicWidth());
        float dh = Math.max(1, drawable.getIntrinsicHeight());
        baseScale = Math.min(getWidth() / dw, getHeight() / dh);
        currentScale = baseScale;
        float left = (getWidth() - dw * baseScale) / 2f;
        float top = (getHeight() - dh * baseScale) / 2f;
        drawMatrix.reset();
        drawMatrix.postScale(baseScale, baseScale);
        drawMatrix.postTranslate(left, top);
        setImageMatrix(drawMatrix);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        scaleDetector.onTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX(); lastY = event.getY(); dragging = true;
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                if (dragging && !scaleDetector.isInProgress() && currentScale > baseScale * 1.01f) {
                    float dx = event.getX() - lastX, dy = event.getY() - lastY;
                    drawMatrix.postTranslate(dx, dy);
                    constrain();
                    setImageMatrix(drawMatrix);
                }
                lastX = event.getX(); lastY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragging = false;
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return true;
    }

    private void constrain() {
        Drawable drawable = getDrawable();
        if (drawable == null) return;
        float[] values = new float[9];
        drawMatrix.getValues(values);
        float left = values[Matrix.MTRANS_X], top = values[Matrix.MTRANS_Y];
        float width = drawable.getIntrinsicWidth() * currentScale;
        float height = drawable.getIntrinsicHeight() * currentScale;
        float dx = width <= getWidth() ? (getWidth() - width) / 2f - left
                : Math.min(0, Math.max(getWidth() - width, left)) - left;
        float dy = height <= getHeight() ? (getHeight() - height) / 2f - top
                : Math.min(0, Math.max(getHeight() - height, top)) - top;
        drawMatrix.postTranslate(dx, dy);
    }
}
