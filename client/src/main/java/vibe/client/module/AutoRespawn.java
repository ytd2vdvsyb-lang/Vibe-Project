package vibe.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;

/** Сразу возрождает после смерти, без экрана "Вы умерли". */
public final class AutoRespawn extends Module {
    public AutoRespawn() {
        super("auto_respawn", "Авто-возрождение", "Пропускает экран смерти", false);
    }

    @Override
    public void tick(Minecraft mc) {
        if (mc.player != null && mc.gui.screen() instanceof DeathScreen) {
            mc.player.respawn();
            mc.gui.setScreen(null);
        }
    }
}
