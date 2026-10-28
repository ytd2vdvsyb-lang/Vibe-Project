package vibe.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.EntityHitResult;
import vibe.client.util.Actions;

/**
 * Для тачпада: пока зажата кнопка атаки, удар повторяется по цели под прицелом,
 * но не чаще, чем позволяет обычная перезарядка. Целиться нужно самому.
 */
public final class HoldAttack extends Module {
    public HoldAttack() {
        super("hold_attack", "Удар зажатием", "Зажми атаку: удары с обычной перезарядкой", false);
    }

    @Override
    public Category category() {
        return Category.COMBAT;
    }

    @Override
    public void tick(Minecraft mc) {
        if (mc.player == null || mc.gui.screen() != null || !mc.options.keyAttack.isDown()) return;
        if (!Actions.cooledDown(mc)) return;
        if (mc.hitResult instanceof EntityHitResult hit && hit.getEntity().isAlive()) {
            Actions.attack(mc, hit.getEntity());
        }
    }
}
