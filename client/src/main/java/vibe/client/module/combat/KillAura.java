package vibe.client.module.combat;

import net.minecraft.client.Minecraft;
import vibe.client.module.Category;
import vibe.client.module.CheatModule;
import vibe.client.setting.BoolSetting;
import vibe.client.setting.ModeSetting;
import vibe.client.setting.NumberSetting;
import vibe.client.util.Actions;

/** Бьёт ближайшую цель рядом, когда перезарядка удара полная. */
public final class KillAura extends CheatModule {
    private final TargetSettings targets = new TargetSettings();
    private final NumberSetting range = add(new NumberSetting("Радиус", 3.0, 2.0, 4.5, 0.1));
    private final ModeSetting rotation = add(new ModeSetting("Поворот", "Авто", "Плавный", "Резкий", "Нет"));
    private final NumberSetting smooth = add(new NumberSetting("Плавность", 0.35, 0.05, 1.0, 0.05));
    private final BoolSetting crits = add(new BoolSetting("Криты прыжком", false));
    private final NumberSetting fov = add(new NumberSetting("Угол обзора", 180, 30, 180, 5));

    public KillAura() {
        super("kill_aura", "KillAura", "Бьёт ближайшую цель с полной перезарядкой", Category.COMBAT);
        add(targets.players);
        add(targets.monsters);
        add(targets.others);
    }

    @Override
    protected void cheatTick(Minecraft mc) {
        if (mc.gui.screen() != null) return;
        Targets.nearest(mc, range.get(), fov.floatValue(), targets).ifPresent(target -> {
            float left = 0;
            if (rotation.is("Авто")) {
                left = Targets.turnTowards(mc.player, target, Targets.autoSpeed(mc.player, target), 2);
            } else if (rotation.is("Плавный")) left = Targets.turnTowards(mc.player, target, smooth.floatValue());
            else if (rotation.is("Резкий")) left = Targets.turnTowards(mc.player, target, 1f);
            // С плавным поворотом бьём, только когда уже почти смотрим на цель.
            if (left >= 25f || !Actions.cooledDown(mc)) return;
            if (crits.on() && !mc.player.isInWater() && !mc.player.onClimbable()) {
                // Крит засчитывается при ударе в падении: подпрыгиваем и ждём начала спуска.
                if (mc.player.onGround()) {
                    mc.player.jumpFromGround();
                    return;
                }
                if (mc.player.fallDistance <= 0) return;
            }
            Actions.attack(mc, target);
        });
    }
}
