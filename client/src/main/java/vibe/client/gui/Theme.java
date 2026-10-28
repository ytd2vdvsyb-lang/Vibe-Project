package vibe.client.gui;

import java.awt.Color;
import java.util.function.Supplier;

/** Цвета клиента. Акцент выбирается в модуле «Тема». */
public final class Theme {
    public static final int WINDOW = 0xF0101016;
    public static final int SIDEBAR = 0xF00B0B10;
    public static final int CARD = 0xFF17171F;
    public static final int CARD_HOVER = 0xFF1E1E28;
    public static final int SETTINGS = 0xFF121219;
    public static final int TEXT = 0xFFECECF4;
    public static final int MUTED = 0xFF8A8A9A;
    public static final int LOCKED = 0xFFF87171;
    /** Подложка HUD-плашек. */
    public static final int HUD = 0xB0101016;

    public static final String[] ACCENTS = {"Красный", "Фиолетовый", "Голубой", "Зелёный", "Розовый", "Оранжевый", "Радуга"};
    private static final int[] COLORS = {0xFFFF3B4E, 0xFFA78BFA, 0xFF38BDF8, 0xFF4ADE80, 0xFFF472B6, 0xFFFB923C};

    private static Supplier<String> accent = () -> ACCENTS[0];

    private Theme() {
    }

    /** Откуда брать название цвета: модуль «Тема» подставляет свою настройку. */
    public static void use(Supplier<String> source) {
        accent = source;
    }

    public static int accent() {
        return accent(0);
    }

    /** Акцент со сдвигом: для радуги соседние строки получают соседние оттенки. */
    public static int accent(int offset) {
        String name = accent.get();
        for (int i = 0; i < COLORS.length; i++) {
            if (ACCENTS[i].equals(name)) return COLORS[i];
        }
        float hue = ((System.currentTimeMillis() % 4000) / 4000f + offset * 0.03f) % 1f;
        return 0xFF000000 | (Color.HSBtoRGB(hue, 0.6f, 1f) & 0xFFFFFF);
    }
}
