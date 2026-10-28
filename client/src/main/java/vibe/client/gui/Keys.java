package vibe.client.gui;

/** Коды клавиш (как в InputConstants) и их короткие имена для меню. */
public final class Keys {
    public static final int ESCAPE = 256;
    public static final int BACKSPACE = 259;
    public static final int DELETE = 261;
    public static final int RIGHT_SHIFT = 344;

    private Keys() {
    }

    public static String name(int key) {
        if (key < 0) return "нет";
        if (key >= 65 && key <= 90) return String.valueOf((char) key);
        if (key >= 48 && key <= 57) return String.valueOf((char) key);
        if (key >= 290 && key <= 314) return "F" + (key - 289);
        if (key >= 320 && key <= 329) return "Num" + (key - 320);
        return switch (key) {
            case 32 -> "Пробел";
            case 258 -> "Tab";
            case 280 -> "Caps";
            case 340 -> "LShift";
            case 341 -> "LCtrl";
            case 342 -> "LAlt";
            case 345 -> "RCtrl";
            case 346 -> "RAlt";
            case 262 -> "→";
            case 263 -> "←";
            case 264 -> "↓";
            case 265 -> "↑";
            case 96 -> "`";
            case 91 -> "[";
            case 93 -> "]";
            case 59 -> ";";
            case 39 -> "'";
            case 44 -> ",";
            case 46 -> ".";
            case 47 -> "/";
            case 45 -> "-";
            case 61 -> "=";
            default -> "#" + key;
        };
    }
}
