package vibe.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Боевой модуль: работает только там, где разрешает {@link ServerGuard}.
 * На остальных серверах выключается сам и не даёт себя включить.
 */
public abstract class CombatModule extends Module {
    protected CombatModule(String id, String name, String description) {
        super(id, name, description + " (только одиночка и HvH)", false);
    }

    @Override
    public void setEnabled(boolean enabled) {
        Minecraft mc = Minecraft.getInstance();
        if (enabled && mc.player != null && !ServerGuard.combatAllowed(mc)) {
            mc.gui.hud.getChat().addClientSystemMessage(
                    Component.literal("§d[Vibe]§r " + name() + " работает только в одиночной игре и на HvH-серверах"));
            return;
        }
        super.setEnabled(enabled);
    }

    @Override
    public final void tick(Minecraft mc) {
        if (mc.player == null) return;
        if (!ServerGuard.combatAllowed(mc)) {
            super.setEnabled(false);
            return;
        }
        combatTick(mc);
    }

    protected abstract void combatTick(Minecraft mc);
}
