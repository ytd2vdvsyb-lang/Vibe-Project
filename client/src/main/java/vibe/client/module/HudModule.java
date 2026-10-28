package vibe.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Модуль, который рисует блок на экране. Позицию можно перетаскивать в меню. */
public abstract class HudModule extends Module {
    public static final int PADDING = 4;
    public static final int BACKGROUND = 0x90101018;
    public static final int TEXT = 0xFFECECF4;
    public static final int ACCENT = 0xFFA78BFA;

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

    public abstract int width(Minecraft mc);

    public abstract int height(Minecraft mc);

    /** Рисует модуль в точке x(), y(). */
    public abstract void render(GuiGraphicsExtractor g, Minecraft mc);

    public boolean contains(double mouseX, double mouseY, Minecraft mc) {
        return mouseX >= x && mouseY >= y && mouseX < x + width(mc) && mouseY < y + height(mc);
    }
}
