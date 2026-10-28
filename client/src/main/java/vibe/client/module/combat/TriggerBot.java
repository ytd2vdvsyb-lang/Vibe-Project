package vibe.client.module.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import vibe.client.module.Category;
import vibe.client.module.CheatModule;
import vibe.client.util.Actions;

/** Бьёт цель под прицелом, как только перезарядка удара полная. */
public final class TriggerBot extends CheatModule {
    private final TargetSettings targets = new TargetSettings();

    public TriggerBot() {
        super("trigger_bot", "TriggerBot", "Удар по цели под прицелом", Category.COMBAT);
        add(targets.players);
        add(targets.monsters);
        add(targets.others);
    }

    @Override
    protected void cheatTick(Minecraft mc) {
        if (mc.gui.screen() != null || !Actions.cooledDown(mc)) return;
        if (mc.hitResult instanceof EntityHitResult hit && Targets.valid(mc.player, hit.getEntity())
                && targets.test((LivingEntity) hit.getEntity())) {
            Actions.attack(mc, hit.getEntity());
        }
    }
}
