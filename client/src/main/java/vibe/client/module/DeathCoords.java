package vibe.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** После смерти пишет в чат, где ты умер. */
public final class DeathCoords extends Module {
    private boolean wasDead;

    public DeathCoords() {
        super("death_coords", "Координаты смерти", "Пишет в чат место смерти", true);
    }

    @Override
    public void tick(Minecraft mc) {
        if (mc.player == null) return;
        boolean dead = mc.player.isDeadOrDying();
        if (dead && !wasDead) {
            String text = String.format("§d[Vibe]§r Ты умер на §e%d %d %d§r (%s)", mc.player.getBlockX(),
                    mc.player.getBlockY(), mc.player.getBlockZ(), mc.player.level().dimension().identifier().getPath());
            mc.gui.hud.getChat().addClientSystemMessage(Component.literal(text));
        }
        wasDead = dead;
    }
}
