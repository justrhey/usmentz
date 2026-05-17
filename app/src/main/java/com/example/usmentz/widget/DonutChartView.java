package com.example.usmentz.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class DonutChartView extends View {

    private Paint trackPaint;
    private Paint arcPaint;
    private Paint textPaint;
    private Paint labelPaint;

    private float progress = 0.67f; // 0.0 to 1.0
    private int trackColor = 0xFFF3EDFF;
    private int arcColor = 0xFF9B5CFF;
    private int overColor = 0xFFFF5252;
    private float strokeWidth = 12f;
    private float size = 80f;

    private String centerText = "67%";
    private String labelText = "of budget";

    public DonutChartView(Context context) {
        super(context);
        init();
    }

    public DonutChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DonutChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setColor(trackColor);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);

        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setColor(arcColor);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF1A1A1A);
        textPaint.setTextAlign(Paint.Align.CENTER);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(0xFF9E9E9E);
        labelPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(w, h);
        setMeasuredDimension(size, size);
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0, Math.min(1, progress));
        // Update center text
        int percent = (int) (this.progress * 100);
        this.centerText = percent + "%";
        // Color: over budget = red, else purple
        arcPaint.setColor(this.progress > 1.0f ? overColor : arcColor);
        invalidate();
    }

    public void setLabel(String label) {
        this.labelText = label;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        float stroke = strokeWidth * getResources().getDisplayMetrics().density;
        float radius = (Math.min(getWidth(), getHeight()) - stroke) / 2f - 4f;

        trackPaint.setStrokeWidth(stroke);
        arcPaint.setStrokeWidth(stroke);

        RectF rect = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);

        // Draw track
        canvas.drawArc(rect, 135, 270, false, trackPaint);

        // Draw arc (progress from 135 degrees, sweep = progress * 270)
        float sweep = Math.min(progress, 1f) * 270f;
        canvas.drawArc(rect, 135, sweep, false, arcPaint);

        // Draw center text
        textPaint.setTextSize(18 * getResources().getDisplayMetrics().scaledDensity);
        canvas.drawText(centerText, cx, cy + 4f * getResources().getDisplayMetrics().scaledDensity, textPaint);

        // Draw label below
        labelPaint.setTextSize(9 * getResources().getDisplayMetrics().scaledDensity);
        canvas.drawText(labelText, cx, cy + 20f * getResources().getDisplayMetrics().scaledDensity, labelPaint);
    }
}