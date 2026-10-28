package vibe.client.module.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Items;
import vibe.client.module.CombatModule;

/** Держит тотем бессмертия во второй руке. */
public final class AutoTotem extends CombatModule {
    private static final int OFFHAND_BUTTON = 40;

    public AutoTotem() {
        super("auto_totem", "Авто-тотем", "Кладёт тотем во вторую руку");
    }

    @Override
    protected void combatTick(Minecraft mc) {
        if (mc.gui.screen() != null || mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) return;
        Inventory inventory = mc.player.getInventory();
        for (int i = 0; i < 36; i++) {
            if (!inventory.getItem(i).is(Items.TOTEM_OF_UNDYING)) continue;
            // В меню инвентаря хотбар (0-8) лежит в слотах 36-44, остальное совпадает.
            int slot = i < 9 ? 36 + i : i;
            mc.gameMode.handleContainerInput(mc.player.inventoryMenu.containerId, slot, OFFHAND_BUTTON,
                    ContainerInput.SWAP, mc.player);
            return;
        }
    }
}
