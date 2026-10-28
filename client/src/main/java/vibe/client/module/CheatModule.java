package vibe.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Чит-модуль: работает только там, где разрешает {@link ServerGuard}.
 * На остальных серверах выключается сам и не даёт себя включить.
 */
public abstract class CheatModule extends Module {
    private final Category category;

    protected CheatModule(String id, String name, String description, Category category) {
        super(id, name, description + " (только одиночка и HvH)", false);
        this.category = category;
    }

    @Override
    public Category category() {
        return category;
    }

    /** Можно ли сейчас работать чит-модулям. */
    public static boolean allowed(Minecraft mc) {
        return mc.player != null && ServerGuard.combatAllowed(mc);
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

    /** Включён и разрешён прямо сейчас: для проверок из миксинов. */
    public boolean active(Minecraft mc) {
        return isEnabled() && allowed(mc);
    }

    @Override
    public final void tick(Minecraft mc) {
        if (mc.player == null) return;
        if (!ServerGuard.combatAllowed(mc)) {
            super.setEnabled(false);
            return;
        }
        cheatTick(mc);
    }

    protected void cheatTick(Minecraft mc) {
    }
}
