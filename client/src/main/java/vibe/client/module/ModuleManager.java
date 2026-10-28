package vibe.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import vibe.client.module.hud.ArmorHud;
import vibe.client.module.hud.ClockHud;
import vibe.client.module.hud.CoordinatesHud;
import vibe.client.module.hud.CpsHud;
import vibe.client.module.hud.EffectsHud;
import vibe.client.module.hud.FpsHud;
import vibe.client.module.hud.KeystrokesHud;
import vibe.client.module.hud.ModuleListHud;
import vibe.client.module.hud.WatermarkHud;

import java.util.List;

public final class ModuleManager {
    private final CpsHud cps = new CpsHud();
    private final List<Module> modules = List.of(
            new WatermarkHud(),
            new FpsHud(),
            new CoordinatesHud(),
            cps,
            new KeystrokesHud(),
            new ClockHud(),
            new ArmorHud(),
            new EffectsHud(),
            new ModuleListHud(),
            new ToggleSprint(),
            new AutoRespawn(),
            new DeathCoords());

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
        if (mc.gui.hud.isHidden() || mc.player == null) return;
        cps.sample(mc);
        for (Module module : modules) {
            if (module instanceof HudModule hud && hud.isEnabled()) hud.render(g, mc);
        }
    }
}
