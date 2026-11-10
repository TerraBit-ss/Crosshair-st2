package com.example.crosshair;

import android.content.Context;
import android.content.SharedPreferences;

/** Настройки прицела. Все размеры в пикселях экрана. */
public class Config {
    public static final String[] STYLES = {
            "Крест",
            "Крест без верхней линии (T)",
            "Круг",
            "Круг + крест",
            "Только точка"
    };

    public static final String K_STYLE = "style";
    public static final String K_LENGTH = "length";
    public static final String K_THICK = "thickness";
    public static final String K_GAP = "gap";
    public static final String K_DOT = "dot";
    public static final String K_OUTLINE = "outline";
    public static final String K_ALPHA = "alpha";
    public static final String K_OFFX = "offx";
    public static final String K_OFFY = "offy";
    public static final String K_RADIUS = "radius";
    public static final String K_COLOR = "color";

    public static final int D_STYLE = 0;
    public static final int D_LENGTH = 10;
    public static final int D_THICK = 2;
    public static final int D_GAP = 4;
    public static final int D_DOT = 2;
    public static final int D_OUTLINE = 1;
    public static final int D_ALPHA = 100;
    public static final int D_OFFX = 0;
    public static final int D_OFFY = 0;
    public static final int D_RADIUS = 14;
    public static final int D_COLOR = 0xFF00FF00;

    public int style, length, thickness, gap, dot, outline, alpha, offX, offY, radius, color;

    public static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("crosshair", Context.MODE_PRIVATE);
    }

    public static Config load(Context c) {
        SharedPreferences p = prefs(c);
        Config k = new Config();
        k.style = p.getInt(K_STYLE, D_STYLE);
        k.length = p.getInt(K_LENGTH, D_LENGTH);
        k.thickness = p.getInt(K_THICK, D_THICK);
        k.gap = p.getInt(K_GAP, D_GAP);
        k.dot = p.getInt(K_DOT, D_DOT);
        k.outline = p.getInt(K_OUTLINE, D_OUTLINE);
        k.alpha = p.getInt(K_ALPHA, D_ALPHA);
        k.offX = p.getInt(K_OFFX, D_OFFX);
        k.offY = p.getInt(K_OFFY, D_OFFY);
        k.radius = p.getInt(K_RADIUS, D_RADIUS);
        k.color = p.getInt(K_COLOR, D_COLOR);
        return k;
    }
}
