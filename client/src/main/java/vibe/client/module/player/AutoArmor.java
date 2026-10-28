package vibe.client.module.player;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import vibe.client.module.Category;
import vibe.client.module.CheatModule;
import vibe.client.util.Inventories;

import java.util.List;

/** Надевает лучшую броню из инвентаря в пустые и слабые слоты. */
public final class AutoArmor extends CheatModule {
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    /** Номера слотов брони в меню инвентаря: шлем 5, нагрудник 6, поножи 7, ботинки 8. */
    private static final int[] MENU_SLOTS = {5, 6, 7, 8};
    private static final List<String> TIERS = List.of("leather", "golden", "chainmail", "copper", "iron", "turtle", "diamond", "netherite");
    private int cooldown;

    public AutoArmor() {
        super("auto_armor", "AutoArmor", "Сам надевает лучшую броню", Category.PLAYER);
    }

    @Override
    protected void cheatTick(Minecraft mc) {
        if (mc.gui.screen() != null || --cooldown > 0) return;
        for (int s = 0; s < SLOTS.length; s++) {
            EquipmentSlot slot = SLOTS[s];
            int current = score(mc.player.getItemBySlot(slot), slot);
            int best = -1, bestScore = current;
            for (int i = 0; i < 36; i++) {
                int score = score(mc.player.getInventory().getItem(i), slot);
                if (score > bestScore) {
                    best = i;
                    bestScore = score;
                }
            }
            if (best < 0) continue;
            int from = Inventories.menuSlot(best);
            if (current >= 0) {
                // Меняем местами: берём новую, кладём в слот брони, старую возвращаем на место новой.
                Inventories.click(mc, from, 0, ContainerInput.PICKUP);
                Inventories.click(mc, MENU_SLOTS[s], 0, ContainerInput.PICKUP);
                Inventories.click(mc, from, 0, ContainerInput.PICKUP);
            } else {
                Inventories.click(mc, from, 0, ContainerInput.QUICK_MOVE);
            }
            cooldown = 4;
            return;
        }
    }

    /** Насколько хороша вещь для слота: -1 если не подходит. Учитываем материал и чары. */
    private static int score(ItemStack stack, EquipmentSlot slot) {
        if (stack.isEmpty()) return -1;
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.slot() != slot) return -1;
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        int tier = 0;
        for (int t = 0; t < TIERS.size(); t++) {
            if (id.startsWith(TIERS.get(t))) tier = t + 1;
        }
        return tier * 10 + (stack.isEnchanted() ? 5 : 0);
    }
}
