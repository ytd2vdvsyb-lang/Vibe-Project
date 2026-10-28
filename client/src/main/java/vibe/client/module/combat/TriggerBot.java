package vibe.client.module.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.EntityHitResult;
import vibe.client.module.CombatModule;

/** Бьёт цель под прицелом, как только перезарядка удара полная. */
public final class TriggerBot extends CombatModule {
    public TriggerBot() {
        super("trigger_bot", "Триггербот", "Удар по цели под прицелом");
    }

    @Override
    protected void combatTick(Minecraft mc) {
        if (mc.gui.screen() != null || !Targets.cooledDown(mc.player)) return;
        if (mc.hitResult instanceof EntityHitResult hit && Targets.valid(mc.player, hit.getEntity())) {
            Targets.attack(mc, hit.getEntity());
        }
    }
}
