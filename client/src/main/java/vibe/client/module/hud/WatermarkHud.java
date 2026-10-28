package vibe.client.module.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import vibe.client.VibeClient;
import vibe.client.module.HudModule;

/** Название клиента, ник и FPS в одной плашке. */
public final class WatermarkHud extends HudModule {
    public WatermarkHud() {
        super("watermark", "Ватермарк", "Vibe Client, ник и FPS", true, 4, 4);
    }

    private static String rest(Minecraft mc) {
        return "  " + mc.getUser().getName() + "  " + mc.getFps() + " fps";
    }

    @Override
    public int width(Minecraft mc) {
        return mc.font.width(VibeClient.NAME) + mc.font.width(rest(mc)) + PADDING * 2 + 3;
    }

    @Override
    public int height(Minecraft mc) {
        return mc.font.lineHeight + PADDING * 2;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Minecraft mc) {
        int w = width(mc);
        g.fill(x(), y(), x() + w, y() + height(mc), BACKGROUND);
        g.fill(x(), y(), x() + 2, y() + height(mc), ACCENT);
        int textX = x() + PADDING + 3;
        g.text(mc.font, Component.literal(VibeClient.NAME), textX, y() + PADDING, ACCENT, true);
        g.text(mc.font, Component.literal(rest(mc)), textX + mc.font.width(VibeClient.NAME), y() + PADDING, TEXT, true);
    }
}
