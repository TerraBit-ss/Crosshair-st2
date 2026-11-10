package com.example.crosshair;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
    private EditText hex;

    private static final int[] PRESETS = {
            0xFFFFFFFF, 0xFFFF2D2D, 0xFF00FF00, 0xFF00E5FF, 0xFFFFEB3B, 0xFFFF00FF, 0xFFFF9800
    };

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        sp = Config.prefs(this);

        ScrollView sv = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(24), dp(16), dp(40));
        sv.addView(root);
        setContentView(sv);

        TextView title = new TextView(this);
        title.setText("Прицел по центру экрана");
        title.setTextSize(22);
        root.addView(title);

        // Предпросмотр
        FrameLayout box = new FrameLayout(this);
        box.setBackgroundColor(0xFF2B2B2B);
        preview = new CrosshairView(this);
        box.addView(preview, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        LinearLayout.LayoutParams boxLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(180));
        boxLp.topMargin = dp(12);
        boxLp.bottomMargin = dp(12);
        root.addView(box, boxLp);

        // Включить / выключить
        toggle = new Button(this);
        toggle.setOnClickListener(v -> onToggle());
        root.addView(toggle);

        // Стиль
        root.addView(label("Стиль"));
        Spinner style = new Spinner(this);
        style.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, Config.STYLES));
        style.setSelection(sp.getInt(Config.K_STYLE, Config.D_STYLE));
        style.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                sp.edit().putInt(Config.K_STYLE, pos).apply();
                preview.reload();
            }

            @Override
            public void onNothingSelected(AdapterView<?> p) {
            }
        });
        root.addView(style);

        // Ползунки
        addSeek(root, "Длина линии", Config.K_LENGTH, 0, 100, Config.D_LENGTH, " px");
        addSeek(root, "Толщина", Config.K_THICK, 1, 20, Config.D_THICK, " px");
        addSeek(root, "Зазор от центра", Config.K_GAP, 0, 60, Config.D_GAP, " px");
        addSeek(root, "Радиус круга", Config.K_RADIUS, 3, 100, Config.D_RADIUS, " px");
        addSeek(root, "Точка в центре", Config.K_DOT, 0, 30, Config.D_DOT, " px");
        addSeek(root, "Обводка (чёрная)", Config.K_OUTLINE, 0, 6, Config.D_OUTLINE, " px");
        addSeek(root, "Непрозрачность", Config.K_ALPHA, 10, 100, Config.D_ALPHA, " %");
        addSeek(root, "Смещение по X", Config.K_OFFX, -300, 300, Config.D_OFFX, " px");
        addSeek(root, "Смещение по Y", Config.K_OFFY, -300, 300, Config.D_OFFY, " px");

        // Цвет
        root.addView(label("Цвет"));
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (int col : PRESETS) {
            Button cb = new Button(this);
            cb.setBackgroundColor(col);
            cb.setOnClickListener(v -> setColor(col));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(44), dp(44));
            lp.rightMargin = dp(8);
            row.addView(cb, lp);
        }
        root.addView(row);

        hex = new EditText(this);
        hex.setHint("HEX, например 00FF00");
        hex.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        hex.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        hex.setText(String.format("%06X", sp.getInt(Config.K_COLOR, Config.D_COLOR) & 0xFFFFFF));
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
                        sp.edit().putInt(Config.K_COLOR, col).apply();
                        preview.reload();
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        });
        root.addView(hex);

        // Сброс
        Button reset = new Button(this);
        reset.setText("Сбросить настройки");
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
                + "Если из-за панелей экрана он стоит не точно по центру игры, "
                + "поправьте смещением по X и Y.");
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

    private TextView label(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setPadding(0, dp(16), 0, 0);
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

    private void setColor(int col) {
        sp.edit().putInt(Config.K_COLOR, col).apply();
        hex.setText(String.format("%06X", col & 0xFFFFFF));
        preview.reload();
    }

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
