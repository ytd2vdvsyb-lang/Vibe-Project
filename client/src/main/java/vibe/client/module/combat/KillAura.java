package vibe.client.module.combat;

import net.minecraft.client.Minecraft;
import vibe.client.module.CombatModule;

/** Бьёт ближайшую цель рядом, когда перезарядка удара полная. */
public final class KillAura extends CombatModule {
    private static final double RANGE = 3.0;

    public KillAura() {
        super("kill_aura", "Аура", "Бьёт ближайшую цель в радиусе 3 блоков");
    }

    @Override
    protected void combatTick(Minecraft mc) {
        if (mc.gui.screen() != null || !Targets.cooledDown(mc.player)) return;
        Targets.nearest(mc, RANGE, 180f).ifPresent(target -> {
            float[] rot = Targets.rotationTo(mc.player, target);
            mc.player.setYRot(rot[0]);
            mc.player.setXRot(rot[1]);
            Targets.attack(mc, target);
        });
    }
}
