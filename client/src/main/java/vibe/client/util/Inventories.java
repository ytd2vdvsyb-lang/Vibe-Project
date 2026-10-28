package vibe.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/** Поиск предметов и клики в инвентаре игрока. */
public final class Inventories {
    private Inventories() {
    }

    /** Индекс в инвентаре (0-8 хотбар, 9-35 рюкзак) первого подходящего предмета или -1. */
    public static int find(Minecraft mc, int from, int to, Predicate<ItemStack> filter) {
        for (int i = from; i < to; i++) {
            if (filter.test(mc.player.getInventory().getItem(i))) return i;
        }
        return -1;
    }

    public static int findHotbar(Minecraft mc, Predicate<ItemStack> filter) {
        return find(mc, 0, 9, filter);
    }

    /** Номер слота в меню инвентаря: хотбар там лежит в 36-44, остальное совпадает. */
    public static int menuSlot(int inventoryIndex) {
        return inventoryIndex < 9 ? 36 + inventoryIndex : inventoryIndex;
    }

    public static void click(Minecraft mc, int menuSlot, int button, ContainerInput input) {
        mc.gameMode.handleContainerInput(mc.player.inventoryMenu.containerId, menuSlot, button, input, mc.player);
    }
}
