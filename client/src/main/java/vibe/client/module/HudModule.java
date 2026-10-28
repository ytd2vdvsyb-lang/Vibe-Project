package vibe.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import vibe.client.gui.Draw;
import vibe.client.gui.Theme;

/** Модуль, который рисует блок на экране. Позицию можно перетаскивать в меню. */
public abstract class HudModule extends Module {
    public static final int PADDING = 4;
    public static final int BACKGROUND = Theme.HUD;
    public static final int TEXT = Theme.TEXT;
    public static final int RADIUS = 4;

    private int x;
    private int y;

    protected HudModule(String id, String name, String description, boolean enabledByDefault, int x, int y) {
        super(id, name, description, enabledByDefault);
        this.x = x;
        this.y = y;
    }

    @Override
    public Category category() {
        return Category.HUD;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = Math.max(0, x);
        this.y = Math.max(0, y);
    }

    /** Текущий цвет акцента из модуля «Тема». */
    protected static int accent() {
        return Theme.accent();
    }

    /** Скруглённая подложка блока. */
    protected static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        Draw.round(g, x, y, w, h, RADIUS, BACKGROUND);
    }

    public abstract int width(Minecraft mc);

    public abstract int height(Minecraft mc);

    /** Рисует модуль в точке x(), y(). */
    public abstract void render(GuiGraphicsExtractor g, Minecraft mc);

    public boolean contains(double mouseX, double mouseY, Minecraft mc) {
        return mouseX >= x && mouseY >= y && mouseX < x + width(mc) && mouseY < y + height(mc);
    }
}
