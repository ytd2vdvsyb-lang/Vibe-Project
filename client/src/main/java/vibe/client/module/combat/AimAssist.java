package vibe.client.module.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import vibe.client.module.CombatModule;

/** Плавно доводит прицел до ближайшей цели перед тобой: удобно с тачпадом. */
public final class AimAssist extends CombatModule {
    private static final double RANGE = 4.5;
    private static final float FOV = 60f;
    private static final float SPEED = 0.25f;

    public AimAssist() {
        super("aim_assist", "Доводка прицела", "Плавно наводит на цель в пределах 60°");
    }

    @Override
    protected void combatTick(Minecraft mc) {
        if (mc.gui.screen() != null) return;
        Targets.nearest(mc, RANGE, FOV).ifPresent(target -> {
            float[] rot = Targets.rotationTo(mc.player, target);
            float yaw = mc.player.getYRot();
            float pitch = mc.player.getXRot();
            mc.player.setYRot(yaw + Mth.wrapDegrees(rot[0] - yaw) * SPEED);
            mc.player.setXRot(pitch + (rot[1] - pitch) * SPEED);
        });
    }
}
