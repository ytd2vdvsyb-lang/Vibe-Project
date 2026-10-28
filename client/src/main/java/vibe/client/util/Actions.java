package vibe.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.SwingAnimation;

/** Действия игрока, общие для модулей. */
public final class Actions {
    private Actions() {
    }

    public static boolean cooledDown(Minecraft mc) {
        return mc.player.getAttackStrengthScale(0.5f) >= 1f;
    }

    public static void attack(Minecraft mc, Entity target) {
        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
    }
}
