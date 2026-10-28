package vibe.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** HUD-модуль из одной строки текста на полупрозрачной подложке. */
public abstract class TextHudModule extends HudModule {
    protected TextHudModule(String id, String name, String description, boolean enabledByDefault, int x, int y) {
        super(id, name, description, enabledByDefault, x, y);
    }

    protected abstract String text(Minecraft mc);

    @Override
    public int width(Minecraft mc) {
        return mc.font.width(text(mc)) + PADDING * 2;
    }

    @Override
    public int height(Minecraft mc) {
        return mc.font.lineHeight + PADDING * 2;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Minecraft mc) {
        String text = text(mc);
        int w = mc.font.width(text) + PADDING * 2;
        panel(g, x(), y(), w, height(mc));
        g.fill(x() + 1, y() + 3, x() + 2, y() + height(mc) - 3, accent());
        g.text(mc.font, Component.literal(text), x() + PADDING, y() + PADDING, TEXT, true);
    }
}
