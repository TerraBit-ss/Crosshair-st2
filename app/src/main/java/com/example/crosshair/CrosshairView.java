package com.example.crosshair;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/** Рисует прицел в центре вью (плюс смещение из настроек). */
public class CrosshairView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
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
        float cx = getWidth() / 2f + c.offX;
        float cy = getHeight() / 2f + c.offY;
        if (c.outline > 0) {
            drawPass(canvas, cx, cy, true);
        }
        drawPass(canvas, cx, cy, false);
    }

    private void drawPass(Canvas canvas, float cx, float cy, boolean outlinePass) {
        int a = Math.round(c.alpha * 2.55f);
        int col = outlinePass ? (a << 24) : ((c.color & 0x00FFFFFF) | (a << 24));
        int e = outlinePass ? c.outline : 0;
        paint.setColor(col);

        int t = Math.max(1, c.thickness);
        int cxI = Math.round(cx);
        int cyI = Math.round(cy);
        int h0 = Math.round(cx - t / 2f); // левый край вертикальной линии
        int v0 = Math.round(cy - t / 2f); // верхний край горизонтальной линии

        boolean hasCross = c.style == 0 || c.style == 1 || c.style == 3;
        boolean hasCircle = c.style == 2 || c.style == 3;

        if (hasCross && c.length > 0) {
            int g = c.gap;
            int len = c.length;
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(false);
            // правая и левая
            rect(canvas, cxI + g, v0, cxI + g + len, v0 + t, e);
            rect(canvas, cxI - g - len, v0, cxI - g, v0 + t, e);
            // нижняя
            rect(canvas, h0, cyI + g, h0 + t, cyI + g + len, e);
            // верхняя (в стиле «T» её нет)
            if (c.style != 1) {
                rect(canvas, h0, cyI - g - len, h0 + t, cyI - g, e);
            }
        }

        paint.setAntiAlias(true);
        if (hasCircle) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(t + 2 * e);
            canvas.drawCircle(cx, cy, c.radius, paint);
        }

        if (c.dot > 0) {
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(cx, cy, c.dot / 2f + e, paint);
        }
    }

    private void rect(Canvas canvas, int l, int t, int r, int b, int e) {
        canvas.drawRect(l - e, t - e, r + e, b + e, paint);
    }
}
