package com.example.crosshair;

import android.content.Context;
import android.content.SharedPreferences;

/** Все настройки прицела. Размеры в пикселях экрана. */
public class Config {
    public static final String[] SHAPES = {
            "Нет", "Круг", "Квадрат", "Ромб", "Треугольник", "Шеврон (V)"
    };
    public static final String[] DOT_SHAPES = {"Круглая", "Квадратная"};

    // Ключи (с префиксом v2, чтобы не путаться со старыми настройками)
    public static final String K_CROSS = "v2_cross";
    public static final String K_ARM_UP = "v2_arm_up";
    public static final String K_ARM_DOWN = "v2_arm_down";
    public static final String K_ARM_LEFT = "v2_arm_left";
    public static final String K_ARM_RIGHT = "v2_arm_right";
    public static final String K_LEN_H = "v2_len_h";
    public static final String K_LEN_V = "v2_len_v";
    public static final String K_THICK = "v2_thick";
    public static final String K_GAP = "v2_gap";

    public static final String K_SHAPE = "v2_shape";
    public static final String K_SHAPE_SIZE = "v2_shape_size";
    public static final String K_SHAPE_THICK = "v2_shape_thick";
    public static final String K_SHAPE_FILL = "v2_shape_fill";

    public static final String K_DOT = "v2_dot";
    public static final String K_DOT_SHAPE = "v2_dot_shape";

    public static final String K_COLOR = "v2_color";
    public static final String K_OUTLINE = "v2_outline";
    public static final String K_OUTLINE_COLOR = "v2_outline_color";
    public static final String K_ALPHA = "v2_alpha";
    public static final String K_ROTATE = "v2_rotate";
    public static final String K_OFFX = "v2_offx";
    public static final String K_OFFY = "v2_offy";

    public boolean cross, armUp, armDown, armLeft, armRight, shapeFill;
    public int lenH, lenV, thick, gap;
    public int shape, shapeSize, shapeThick;
    public int dot, dotShape;
    public int color, outline, outlineColor, alpha, rotate, offX, offY;

    public static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("crosshair", Context.MODE_PRIVATE);
    }

    public static Config load(Context ctx) {
        SharedPreferences p = prefs(ctx);
        Config c = new Config();
        c.cross = p.getBoolean(K_CROSS, true);
        c.armUp = p.getBoolean(K_ARM_UP, true);
        c.armDown = p.getBoolean(K_ARM_DOWN, true);
        c.armLeft = p.getBoolean(K_ARM_LEFT, true);
        c.armRight = p.getBoolean(K_ARM_RIGHT, true);
        c.lenH = p.getInt(K_LEN_H, 10);
        c.lenV = p.getInt(K_LEN_V, 10);
        c.thick = p.getInt(K_THICK, 2);
        c.gap = p.getInt(K_GAP, 4);

        c.shape = p.getInt(K_SHAPE, 0);
        c.shapeSize = p.getInt(K_SHAPE_SIZE, 16);
        c.shapeThick = p.getInt(K_SHAPE_THICK, 2);
        c.shapeFill = p.getBoolean(K_SHAPE_FILL, false);

        c.dot = p.getInt(K_DOT, 2);
        c.dotShape = p.getInt(K_DOT_SHAPE, 0);

        c.color = p.getInt(K_COLOR, 0xFF00FF00);
        c.outline = p.getInt(K_OUTLINE, 1);
        c.outlineColor = p.getInt(K_OUTLINE_COLOR, 0xFF000000);
        c.alpha = p.getInt(K_ALPHA, 100);
        c.rotate = p.getInt(K_ROTATE, 0);
        c.offX = p.getInt(K_OFFX, 0);
        c.offY = p.getInt(K_OFFY, 0);
        return c;
    }

    /** Готовые шаблоны: имя и функция, которая записывает настройки. */
    public static final String[] TEMPLATES = {
            "Выберите шаблон…",
            "Классический крест",
            "T-образный",
            "Только точка",
            "Круг с точкой",
            "Крест в круге",
            "X (диагональный)",
            "Шеврон",
            "Квадрат с точкой",
            "Длинный тонкий крест"
    };

    public static void applyTemplate(Context ctx, int idx) {
        SharedPreferences.Editor e = prefs(ctx).edit();
        // Общая основа
        e.putBoolean(K_CROSS, true).putBoolean(K_ARM_UP, true).putBoolean(K_ARM_DOWN, true)
                .putBoolean(K_ARM_LEFT, true).putBoolean(K_ARM_RIGHT, true)
                .putInt(K_LEN_H, 10).putInt(K_LEN_V, 10).putInt(K_THICK, 2).putInt(K_GAP, 4)
                .putInt(K_SHAPE, 0).putInt(K_SHAPE_SIZE, 16).putInt(K_SHAPE_THICK, 2)
                .putBoolean(K_SHAPE_FILL, false).putInt(K_DOT, 0).putInt(K_DOT_SHAPE, 0)
                .putInt(K_ROTATE, 0);
        switch (idx) {
            case 1: // классический
                break;
            case 2: // T
                e.putBoolean(K_ARM_UP, false);
                break;
            case 3: // точка
                e.putBoolean(K_CROSS, false).putInt(K_DOT, 6);
                break;
            case 4: // круг с точкой
                e.putBoolean(K_CROSS, false).putInt(K_SHAPE, 1).putInt(K_SHAPE_SIZE, 18)
                        .putInt(K_DOT, 4);
                break;
            case 5: // крест в круге
                e.putInt(K_SHAPE, 1).putInt(K_SHAPE_SIZE, 22).putInt(K_GAP, 6).putInt(K_DOT, 2);
                break;
            case 6: // X
                e.putInt(K_ROTATE, 45).putInt(K_GAP, 5);
                break;
            case 7: // шеврон
                e.putBoolean(K_CROSS, false).putInt(K_SHAPE, 5).putInt(K_SHAPE_SIZE, 14)
                        .putInt(K_SHAPE_THICK, 3);
                break;
            case 8: // квадрат с точкой
                e.putBoolean(K_CROSS, false).putInt(K_SHAPE, 2).putInt(K_SHAPE_SIZE, 14)
                        .putInt(K_DOT, 4).putInt(K_DOT_SHAPE, 1);
                break;
            case 9: // длинный тонкий
                e.putInt(K_LEN_H, 40).putInt(K_LEN_V, 40).putInt(K_THICK, 1).putInt(K_GAP, 8);
                break;
            default:
                return;
        }
        e.apply();
    }
}
