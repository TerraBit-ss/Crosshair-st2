package com.example.crosshair;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/** Рисует прицел в центре вью (плюс смещение и поворот из настроек). */
public class CrosshairView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private Config c;

    public CrosshairView(Context ctx) {
        super(ctx);
        reload();
    }

    public void reload() {
        c = Config.load(getContext());
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float cx = Math.round(getWidth() / 2f) + c.offX;
        float cy = Math.round(getHeight() / 2f) + c.offY;

        canvas.save();
        if (c.rotate != 0) {
            canvas.rotate(c.rotate, cx, cy);
        }
        int a = Math.round(c.alpha * 2.55f);
        if (c.outline > 0) {
            int oc = (c.outlineColor & 0x00FFFFFF) | (a << 24);
            drawAll(canvas, cx, cy, oc, c.outline);
        }
        int mc = (c.color & 0x00FFFFFF) | (a << 24);
        drawAll(canvas, cx, cy, mc, 0);
        canvas.restore();
    }

    /** e = насколько расширить каждый элемент (для обводки). */
    private void drawAll(Canvas canvas, float cx, float cy, int color, float e) {
        paint.setColor(color);
        paint.setStrokeJoin(Paint.Join.MITER);
        paint.setStrokeCap(Paint.Cap.BUTT);

        // Крест
        if (c.cross) {
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(c.rotate % 90 != 0);
            float t = Math.max(1, c.thick);
            float h = t / 2f;
            float g = c.gap;
            if (c.armRight && c.lenH > 0)
                rect(canvas, cx + g, cy - h, cx + g + c.lenH, cy + h, e);
            if (c.armLeft && c.lenH > 0)
                rect(canvas, cx - g - c.lenH, cy - h, cx - g, cy + h, e);
            if (c.armDown && c.lenV > 0)
                rect(canvas, cx - h, cy + g, cx + h, cy + g + c.lenV, e);
            if (c.armUp && c.lenV > 0)
                rect(canvas, cx - h, cy - g - c.lenV, cx + h, cy - g, e);
        }

        // Фигура
        if (c.shape > 0) {
            paint.setAntiAlias(true);
            float s = c.shapeSize;
            boolean closed = c.shape != 5;
            paint.setStyle(c.shapeFill && closed ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(1, c.shapeThick) + 2 * e);
            path.reset();
            switch (c.shape) {
                case 1: // круг
                    canvas.drawCircle(cx, cy, s, paint);
                    break;
                case 2: // квадрат
                    canvas.drawRect(cx - s, cy - s, cx + s, cy + s, paint);
                    break;
                case 3: // ромб
                    path.moveTo(cx, cy - s);
                    path.lineTo(cx + s, cy);
                    path.lineTo(cx, cy + s);
                    path.lineTo(cx - s, cy);
                    path.close();
                    canvas.drawPath(path, paint);
                    break;
                case 4: // треугольник вершиной вверх
                    path.moveTo(cx, cy - s);
                    path.lineTo(cx + s * 0.866f, cy + s * 0.5f);
                    path.lineTo(cx - s * 0.866f, cy + s * 0.5f);
                    path.close();
                    canvas.drawPath(path, paint);
                    break;
                case 5: // шеврон: вершина в центре, крылья вниз
                    if (e > 0) paint.setStrokeCap(Paint.Cap.SQUARE);
                    path.moveTo(cx - s, cy + s * 0.7f);
                    path.lineTo(cx, cy);
                    path.lineTo(cx + s, cy + s * 0.7f);
                    canvas.drawPath(path, paint);
                    break;
            }
        }

        // Точка
        if (c.dot > 0) {
            paint.setStyle(Paint.Style.FILL);
            float r = c.dot / 2f + e;
            if (c.dotShape == 1) {
                paint.setAntiAlias(false);
                canvas.drawRect(cx - r, cy - r, cx + r, cy + r, paint);
            } else {
                paint.setAntiAlias(true);
                canvas.drawCircle(cx, cy, r, paint);
            }
        }
    }

    private void rect(Canvas canvas, float l, float t, float r, float b, float e) {
        canvas.drawRect(l - e, t - e, r + e, b + e, paint);
    }
}
