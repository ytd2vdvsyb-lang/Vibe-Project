package vibe.client.module.player;

import net.minecraft.client.Minecraft;
import vibe.client.mixin.MinecraftAccessor;
import vibe.client.module.Category;
import vibe.client.module.CheatModule;

/** Ставит блоки без задержки в 4 тика между кликами. */
public final class FastPlace extends CheatModule {
    public FastPlace() {
        super("fast_place", "FastPlace", "Блоки ставятся без задержки", Category.PLAYER);
    }

    @Override
    protected void cheatTick(Minecraft mc) {
        ((MinecraftAccessor) mc).vibe$setRightClickDelay(0);
    }
}
