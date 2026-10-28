package vibe.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/** Действия игрока, общие для модулей. */
public final class Actions {
    private Actions() {
    }

    public static boolean cooledDown(Minecraft mc) {
        return mc.player.getAttackStrengthScale(0.5f) >= 1f;
    }

    public static void attack(Minecraft mc, Entity target) {
        mc.gameMode.attack(mc.player, target);
        // TODO: взмах рукой; в 26.3 swing принимает ещё SwingAnimation, ждём сигнатуру из probe-api.
    }
}
