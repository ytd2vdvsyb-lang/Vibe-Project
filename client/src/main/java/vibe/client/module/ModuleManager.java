package vibe.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import vibe.client.module.hud.ClockHud;
import vibe.client.module.hud.CoordinatesHud;
import vibe.client.module.hud.CpsHud;
import vibe.client.module.hud.FpsHud;
import vibe.client.module.hud.KeystrokesHud;

import java.util.List;

public final class ModuleManager {
    private final CpsHud cps = new CpsHud();
    private final List<Module> modules = List.of(
            new FpsHud(),
            new CoordinatesHud(),
            cps,
            new KeystrokesHud(),
            new ClockHud(),
            new ToggleSprint());

    public List<Module> all() {
        return modules;
    }

    public void tick(Minecraft mc) {
        for (Module module : modules) {
            if (module.isEnabled()) module.tick(mc);
        }
    }

    /** Вызывается из HudMixin каждый кадр. */
    public void renderHud(GuiGraphicsExtractor g, Minecraft mc) {
        if (mc.options.hideGui || mc.player == null) return;
        cps.sample(mc);
        for (Module module : modules) {
            if (module instanceof HudModule hud && hud.isEnabled()) hud.render(g, mc);
        }
    }
}
