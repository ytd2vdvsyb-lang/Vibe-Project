package vibe.client.module.hud;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import vibe.client.module.HudModule;

/** Показывает нажатые WASD, пробел и кнопки мыши. */
public final class KeystrokesHud extends HudModule {
    private static final int KEY = 22;
    private static final int GAP = 2;

    public KeystrokesHud() {
        super("keystrokes", "Клавиши", "Нажатые WASD и кнопки мыши", false, 4, 80);
    }

    @Override
    public int width(Minecraft mc) {
        return KEY * 3 + GAP * 2;
    }

    @Override
    public int height(Minecraft mc) {
        return KEY * 3 + GAP * 3 + 12;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Minecraft mc) {
        int x = x();
        int y = y();
        key(g, mc, mc.options.keyUp, "W", x + KEY + GAP, y, KEY, KEY);
        key(g, mc, mc.options.keyLeft, "A", x, y + KEY + GAP, KEY, KEY);
        key(g, mc, mc.options.keyDown, "S", x + KEY + GAP, y + KEY + GAP, KEY, KEY);
        key(g, mc, mc.options.keyRight, "D", x + (KEY + GAP) * 2, y + KEY + GAP, KEY, KEY);
        int half = (width(mc) - GAP) / 2;
        key(g, mc, mc.options.keyAttack, "ЛКМ", x, y + (KEY + GAP) * 2, half, KEY);
        key(g, mc, mc.options.keyUse, "ПКМ", x + half + GAP, y + (KEY + GAP) * 2, half, KEY);
        key(g, mc, mc.options.keyJump, "———", x, y + (KEY + GAP) * 3, width(mc), 12);
    }

    private static void key(GuiGraphicsExtractor g, Minecraft mc, KeyMapping mapping, String label, int x, int y, int w, int h) {
        boolean down = mapping.isDown();
        vibe.client.gui.Draw.round(g, x, y, w, h, 3, down ? vibe.client.gui.Draw.alpha(accent(), 0.85f) : BACKGROUND);
        int textX = x + (w - mc.font.width(label)) / 2;
        int textY = y + (h - mc.font.lineHeight) / 2 + 1;
        g.text(mc.font, Component.literal(label), textX, textY, TEXT, false);
    }
}
