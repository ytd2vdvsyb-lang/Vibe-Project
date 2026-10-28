package vibe.client.module.combat;

import net.minecraft.client.Minecraft;
import vibe.client.module.Category;
import vibe.client.module.CheatModule;
import vibe.client.setting.BoolSetting;
import vibe.client.setting.NumberSetting;

/** Плавно доводит прицел до ближайшей цели перед тобой: удобно с тачпадом. */
public final class AimAssist extends CheatModule {
    private final TargetSettings targets = new TargetSettings();
    private final NumberSetting range = add(new NumberSetting("Радиус", 4.5, 2.0, 8.0, 0.5));
    private final NumberSetting fov = add(new NumberSetting("Угол обзора", 60, 10, 180, 5));
    private final NumberSetting speed = add(new NumberSetting("Скорость", 0.25, 0.05, 1.0, 0.05));
    private final BoolSetting auto = add(new BoolSetting("Авто-скорость", true));
    private final BoolSetting onlyAttacking = add(new BoolSetting("Только при атаке", false));

    public AimAssist() {
        super("aim_assist", "AimAssist", "Плавно наводит прицел на цель", Category.COMBAT);
        add(targets.players);
        add(targets.monsters);
        add(targets.others);
    }

    @Override
    protected void cheatTick(Minecraft mc) {
        if (mc.gui.screen() != null) return;
        if (onlyAttacking.on() && !mc.options.keyAttack.isDown()) return;
        Targets.nearest(mc, range.get(), fov.floatValue(), targets)
                .ifPresent(target -> {
                    if (auto.on()) Targets.turnTowards(mc.player, target, Targets.autoSpeed(mc.player, target) * speed.floatValue() * 2, 2);
                    else Targets.turnTowards(mc.player, target, speed.floatValue());
                });
    }
}
