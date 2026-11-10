package com.example.crosshair;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private SharedPreferences sp;
    private CrosshairView preview;
    private Button toggle;

    private static final int[] PRESETS = {
            0xFFFFFFFF, 0xFF000000, 0xFFFF2D2D, 0xFF00FF00, 0xFF00E5FF,
            0xFFFFEB3B, 0xFFFF00FF, 0xFFFF9800
    };

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        sp = Config.prefs(this);

        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);
        outer.setPadding(dp(16), dp(16), dp(16), 0);

        TextView title = new TextView(this);
        title.setText("Прицел по центру экрана");
        title.setTextSize(22);
        outer.addView(title);

        // Предпросмотр закреплён сверху, настройки прокручиваются под ним
        FrameLayout box = new FrameLayout(this);
        box.setBackgroundColor(0xFF6B7B5E);
        preview = new CrosshairView(this);
        box.addView(preview, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        LinearLayout.LayoutParams boxLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(160));
        boxLp.topMargin = dp(8);
        boxLp.bottomMargin = dp(8);
        outer.addView(box, boxLp);

        toggle = new Button(this);
        toggle.setOnClickListener(v -> onToggle());
        outer.addView(toggle);

        ScrollView sv = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, 0, 0, dp(40));
        sv.addView(root);
        outer.addView(sv, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        setContentView(outer);

        // Шаблоны
        section(root, "Шаблоны");
        Spinner tpl = new Spinner(this);
        tpl.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, Config.TEMPLATES));
        tpl.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                if (pos > 0) {
                    Config.applyTemplate(MainActivity.this, pos);
                    recreate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> p) {
            }
        });
        root.addView(tpl);

        // Крест
        section(root, "Крест");
        addCheck(root, "Показывать крест", Config.K_CROSS, true);
        LinearLayout arms = new LinearLayout(this);
        arms.setOrientation(LinearLayout.HORIZONTAL);
        addCheck(arms, "Верх", Config.K_ARM_UP, true);
        addCheck(arms, "Низ", Config.K_ARM_DOWN, true);
        addCheck(arms, "Лево", Config.K_ARM_LEFT, true);
        addCheck(arms, "Право", Config.K_ARM_RIGHT, true);
        root.addView(arms);
        addSeek(root, "Длина по горизонтали", Config.K_LEN_H, 0, 150, 10, " px");
        addSeek(root, "Длина по вертикали", Config.K_LEN_V, 0, 150, 10, " px");
        addSeek(root, "Толщина линий", Config.K_THICK, 1, 20, 2, " px");
        addSeek(root, "Зазор от центра", Config.K_GAP, 0, 80, 4, " px");

        // Фигура
        section(root, "Фигура");
        addSpinner(root, Config.SHAPES, Config.K_SHAPE, 0);
        addSeek(root, "Размер фигуры", Config.K_SHAPE_SIZE, 2, 150, 16, " px");
        addSeek(root, "Толщина контура", Config.K_SHAPE_THICK, 1, 20, 2, " px");
        addCheck(root, "Залить фигуру", Config.K_SHAPE_FILL, false);

        // Точка
        section(root, "Точка в центре");
        addSeek(root, "Размер точки", Config.K_DOT, 0, 40, 2, " px");
        addSpinner(root, Config.DOT_SHAPES, Config.K_DOT_SHAPE, 0);

        // Цвет
        section(root, "Цвет прицела");
        addColor(root, Config.K_COLOR, 0xFF00FF00);
        addSeek(root, "Непрозрачность", Config.K_ALPHA, 10, 100, 100, " %");

        // Обводка
        section(root, "Обводка");
        addSeek(root, "Толщина обводки", Config.K_OUTLINE, 0, 8, 1, " px");
        addColor(root, Config.K_OUTLINE_COLOR, 0xFF000000);

        // Положение
        section(root, "Положение");
        addSeek(root, "Поворот", Config.K_ROTATE, 0, 90, 0, "°");
        addSeek(root, "Смещение по X", Config.K_OFFX, -300, 300, 0, " px");
        addSeek(root, "Смещение по Y", Config.K_OFFY, -300, 300, 0, " px");

        Button reset = new Button(this);
        reset.setText("Сбросить всё");
        reset.setOnClickListener(v -> {
            sp.edit().clear().apply();
            recreate();
        });
        LinearLayout.LayoutParams rl = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rl.topMargin = dp(24);
        root.addView(reset, rl);

        TextView hint = new TextView(this);
        hint.setText("Прицел рисуется поверх игры и не мешает нажатиям. "
                + "Крест и фигуру можно включать вместе. Поворот 45° превращает крест в X. "
                + "Если прицел стоит не точно по центру игры, поправьте смещением.");
        hint.setTextSize(13);
        hint.setPadding(0, dp(16), 0, 0);
        root.addView(hint);

        refreshButton(OverlayService.running);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshButton(OverlayService.running);
    }

    // ---------- конструкторы элементов ----------

    private void section(LinearLayout root, String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(17);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(0, dp(24), 0, dp(4));
        root.addView(t);
    }

    private TextView label(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setPadding(0, dp(10), 0, 0);
        return t;
    }

    private void addSeek(LinearLayout root, String title, String key, int min, int max,
                         int def, String unit) {
        TextView label = label("");
        SeekBar sb = new SeekBar(this);
        sb.setMin(min);
        sb.setMax(max);
        int v = sp.getInt(key, def);
        sb.setProgress(v);
        label.setText(title + ": " + v + unit);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar s, int p, boolean fromUser) {
                label.setText(title + ": " + p + unit);
                sp.edit().putInt(key, p).apply();
                preview.reload();
            }

            @Override
            public void onStartTrackingTouch(SeekBar s) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar s) {
            }
        });
        root.addView(label);
        root.addView(sb);
    }

    private void addCheck(LinearLayout root, String title, String key, boolean def) {
        CheckBox cb = new CheckBox(this);
        cb.setText(title);
        cb.setChecked(sp.getBoolean(key, def));
        cb.setOnCheckedChangeListener((v, checked) -> {
            sp.edit().putBoolean(key, checked).apply();
            preview.reload();
        });
        root.addView(cb);
    }

    private void addSpinner(LinearLayout root, String[] items, String key, int def) {
        Spinner s = new Spinner(this);
        s.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, items));
        s.setSelection(sp.getInt(key, def));
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                sp.edit().putInt(key, pos).apply();
                preview.reload();
            }

            @Override
            public void onNothingSelected(AdapterView<?> p) {
            }
        });
        root.addView(s);
    }

    private void addColor(LinearLayout root, String key, int def) {
        EditText hex = new EditText(this);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (int col : PRESETS) {
            Button cb = new Button(this);
            cb.setBackgroundColor(col);
            cb.setOnClickListener(v -> hex.setText(String.format("%06X", col & 0xFFFFFF)));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(40), 1f);
            lp.rightMargin = dp(6);
            row.addView(cb, lp);
        }
        root.addView(row);

        hex.setHint("HEX, например 00FF00");
        hex.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        hex.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        hex.setText(String.format("%06X", sp.getInt(key, def) & 0xFFFFFF));
        hex.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int a, int c, int d) {
            }

            @Override
            public void onTextChanged(CharSequence s, int a, int c, int d) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 6) {
                    try {
                        int col = Color.parseColor("#" + s);
                        sp.edit().putInt(key, col).apply();
                        preview.reload();
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        });
        root.addView(hex);
    }

    // ---------- включение оверлея ----------

    private void onToggle() {
        if (OverlayService.running) {
            stopService(new Intent(this, OverlayService.class));
            refreshButton(false);
            return;
        }
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Разрешите показ поверх других приложений и вернитесь назад",
                    Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())));
            return;
        }
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
        startForegroundService(new Intent(this, OverlayService.class));
        refreshButton(true);
    }

    private void refreshButton(boolean on) {
        toggle.setText(on ? "Выключить прицел" : "Включить прицел");
    }
}
