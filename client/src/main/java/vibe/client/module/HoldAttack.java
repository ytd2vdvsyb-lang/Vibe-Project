package vibe.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Для тачпада: пока зажата кнопка атаки, удар повторяется по цели под прицелом,
 * но не чаще, чем позволяет обычная перезарядка. Целиться нужно самому.
 */
public final class HoldAttack extends Module {
    public HoldAttack() {
        super("hold_attack", "Удар зажатием", "Зажми атаку: удары с обычной перезарядкой", false);
    }

    @Override
    public void tick(Minecraft mc) {
        if (mc.player == null || mc.gui.screen() != null || !mc.options.keyAttack.isDown()) return;
        if (mc.player.getAttackStrengthScale(0.5f) < 1f) return;
        if (mc.hitResult instanceof EntityHitResult hit && hit.getEntity().isAlive()) {
            mc.gameMode.attack(mc.player, hit.getEntity());
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }
}
