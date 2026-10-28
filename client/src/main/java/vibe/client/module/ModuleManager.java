package vibe.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import vibe.client.module.combat.AimAssist;
import vibe.client.module.combat.AutoTotem;
import vibe.client.module.combat.KillAura;
import vibe.client.module.combat.TriggerBot;
import vibe.client.module.hud.ArmorHud;
import vibe.client.module.combat.BowAimbot;
import vibe.client.module.combat.Velocity;
import vibe.client.module.player.AutoArmor;
import vibe.client.module.player.AutoEat;
import vibe.client.module.player.AutoSoup;
import vibe.client.module.player.FastPlace;
import vibe.client.module.render.FullBright;
import vibe.client.module.movement.Fly;
import vibe.client.module.movement.NoFall;
import vibe.client.module.render.Esp;
import vibe.client.module.movement.Speed;
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
            new DeathCoords(),
            new HoldAttack(),
            new AimAssist(),
            new TriggerBot(),
            new KillAura(),
            new AutoTotem(),
            new Speed(),
            new Fly(),
            new NoFall(),
            new Velocity(),
            new Esp(),
            new BowAimbot(),
            new AutoArmor(),
            new AutoEat(),
            new AutoSoup(),
            new FastPlace(),
            new FullBright());

    /** Модуль по классу: для миксинов, которым нужен конкретный модуль. */
    public <T extends Module> T get(Class<T> type) {
        for (Module module : modules) {
            if (type.isInstance(module)) return type.cast(module);
        }
        throw new IllegalArgumentException(type.getName());
    }

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
