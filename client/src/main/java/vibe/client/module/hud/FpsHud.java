package vibe.client.module.hud;

import net.minecraft.client.Minecraft;
import vibe.client.module.TextHudModule;

public final class FpsHud extends TextHudModule {
    public FpsHud() {
        super("fps", "FPS", "Кадры в секунду", false, 4, 22);
    }

    @Override
    protected String text(Minecraft mc) {
        return mc.getFps() + " FPS";
    }
}
