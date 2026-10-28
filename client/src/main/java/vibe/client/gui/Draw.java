package vibe.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Скруглённые плашки и работа с цветом поверх обычных fill. */
public final class Draw {
    private Draw() {
    }

    /** Прямоугольник со скруглёнными углами радиуса r. Строки не перекрываются, так что прозрачность ровная. */
    public static void round(GuiGraphicsExtractor g, int x, int y, int w, int h, int r, int color) {
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = (int) Math.round(r - Math.sqrt(r * r - dy * dy));
            g.fill(x + inset, y + i, x + w - inset, y + i + 1, color);
            g.fill(x + inset, y + h - 1 - i, x + w - inset, y + h - i, color);
        }
        g.fill(x, y + r, x + w, y + h - r, color);
    }

    /** Круг диаметра size. */
    public static void circle(GuiGraphicsExtractor g, int x, int y, int size, int color) {
        round(g, x, y, size, size, size / 2, color);
    }

    /** Переключатель-«таблетка»: progress 0 = выкл, 1 = вкл. */
    public static void toggle(GuiGraphicsExtractor g, int x, int y, float progress, int accent) {
        int w = 18, h = 10;
        round(g, x, y, w, h, 5, mix(0xFF3A3A46, accent, progress));
        int knob = x + 1 + Math.round((w - h) * progress);
        circle(g, knob, y + 1, h - 2, 0xFFFFFFFF);
    }

    /** Цвет с другой прозрачностью (0..1). */
    public static int alpha(int color, float alpha) {
        int a = Math.round(((color >>> 24) & 0xFF) * Math.max(0, Math.min(1, alpha)));
        return (a << 24) | (color & 0xFFFFFF);
    }

    /** Смешивает два цвета: t = 0 даёт a, t = 1 даёт b. */
    public static int mix(int a, int b, float t) {
        t = Math.max(0, Math.min(1, t));
        int r = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            int ca = (a >>> shift) & 0xFF, cb = (b >>> shift) & 0xFF;
            r |= Math.round(ca + (cb - ca) * t) << shift;
        }
        return r;
    }

    /** Плавно приближает value к target; скорость не зависит от FPS. */
    public static float approach(float value, float target, float dtMs, float timeMs) {
        return value + (target - value) * (float) (1 - Math.exp(-dtMs / timeMs));
    }
}
