package vibe.launcher.ui;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;

/** Цвета и шрифты лаунчера. */
final class Theme {
    static final Color BACKGROUND = new Color(0x0F0F17);
    static final Color SURFACE = new Color(0x181824);
    static final Color FIELD = new Color(0x22222F);
    static final Color BORDER = new Color(0x2E2E3E);
    static final Color TEXT = new Color(0xECECF4);
    static final Color MUTED = new Color(0x8C8CA3);
    static final Color ACCENT = new Color(0x8B5CF6);
    static final Color ACCENT_2 = new Color(0xEC4899);
    static final Color ERROR = new Color(0xF87171);

    private static final String FAMILY = pickFamily();

    private Theme() {
    }

    static Font font(int style, float size) {
        return new Font(FAMILY, style, Math.round(size));
    }

    static Border fieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12));
    }

    static <T extends JComponent> T style(T component) {
        component.setFont(font(Font.PLAIN, 14));
        component.setForeground(TEXT);
        component.setBackground(FIELD);
        return component;
    }

    private static String pickFamily() {
        List<String> available = Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        for (String family : new String[]{"Segoe UI", "SF Pro Text", "Inter", "Noto Sans", "DejaVu Sans"}) {
            if (available.contains(family)) return family;
        }
        return Font.SANS_SERIF;
    }
}
