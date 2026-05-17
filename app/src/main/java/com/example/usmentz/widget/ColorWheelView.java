package com.example.usmentz.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class ColorWheelView extends View {

    private Paint wheelPaint;
    private Paint selectorPaint;
    private Paint centerPaint;

    private int selectedColor = Color.RED;
    private float centerX, centerY, radius;
    private float selectorRadius = 16f;

    private boolean isDragging = false;

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    private OnColorSelectedListener listener;

    public ColorWheelView(Context context) {
        super(context);
        init();
    }

    public ColorWheelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorWheelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        wheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wheelPaint.setStyle(Paint.Style.STROKE);
        wheelPaint.setStrokeWidth(60f * getResources().getDisplayMetrics().density);

        selectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectorPaint.setStyle(Paint.Style.FILL);
        selectorPaint.setColor(Color.WHITE);
        selectorPaint.setShadowLayer(8f, 0f, 2f, 0x44000000);

        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setStyle(Paint.Style.FILL);

        setLayerType(LAYER_TYPE_SOFTWARE, selectorPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = h / 2f;
        radius = Math.min(w, h) / 2f - wheelPaint.getStrokeWidth() / 2f - 4f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the hue wheel ring
        float strokeWidth = wheelPaint.getStrokeWidth();
        RectF rect = new RectF(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
        );

        // Sweep through all hues
        int segments = 360;
        for (int i = 0; i < segments; i++) {
            wheelPaint.setColor(Color.HSVToColor(new float[]{i, 1.0f, 1.0f}));
            canvas.drawArc(rect, i - 90f - 0.5f, 1.0f, false, wheelPaint);
        }

        // Draw selector
        float[] hsv = new float[3];
        Color.colorToHSV(selectedColor, hsv);
        float angle = (hsv[0] - 90) * (float) Math.PI / 180f;
        float selX = centerX + (float) Math.cos(angle) * radius;
        float selY = centerY + (float) Math.sin(angle) * radius;

        selectorPaint.setColor(selectedColor);
        canvas.drawCircle(selX, selY, selectorRadius, selectorPaint);
        canvas.drawCircle(selX, selY, selectorRadius - 3f, centerPaint);

        // Draw center preview
        centerPaint.setColor(selectedColor);
        canvas.drawCircle(centerX, centerY, strokeWidth / 2f - 8f, centerPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isInWheel(x, y)) {
                    isDragging = true;
                    pickColor(x, y);
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    pickColor(x, y);
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isDragging) {
                    isDragging = false;
                    pickColor(x, y);
                    return true;
                }
                break;
        }

        return super.onTouchEvent(event);
    }

    private boolean isInWheel(float x, float y) {
        float dx = x - centerX;
        float dy = y - centerY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        float strokeHalf = wheelPaint.getStrokeWidth() / 2f;
        return dist >= radius - strokeHalf - 10f && dist <= radius + strokeHalf + 10f;
    }

    private void pickColor(float x, float y) {
        float dx = x - centerX;
        float dy = y - centerY;

        // Calculate angle in degrees (0-360), offset so red starts at top
        double angle = Math.atan2(dy, dx) * 180.0 / Math.PI;
        float hue = (float) ((angle + 90 + 360) % 360);

        selectedColor = Color.HSVToColor(new float[]{hue, 1.0f, 1.0f});
        invalidate();

        if (listener != null) {
            listener.onColorSelected(selectedColor);
        }
    }

    public void setColor(int color) {
        selectedColor = color;
        invalidate();
    }

    public int getColor() {
        return selectedColor;
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = Math.min(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec)
        );
        setMeasuredDimension(size, size);
    }
}