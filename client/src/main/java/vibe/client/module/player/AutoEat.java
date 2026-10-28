package vibe.client.module.player;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import vibe.client.module.Category;
import vibe.client.module.CheatModule;
import vibe.client.setting.NumberSetting;
import vibe.client.util.Inventories;

/** Ест еду из хотбара, когда голод ниже порога, и возвращает прежний слот. */
public final class AutoEat extends CheatModule {
    private final NumberSetting hunger = add(new NumberSetting("Есть при голоде ≤", 14, 1, 19, 1));
    private int previousSlot = -1;

    public AutoEat() {
        super("auto_eat", "AutoEat", "Сам ест, когда проголодался", Category.PLAYER);
    }

    @Override
    protected void cheatTick(Minecraft mc) {
        var inventory = mc.player.getInventory();
        if (previousSlot >= 0) {
            if (mc.player.isUsingItem() && mc.player.getMainHandItem().has(DataComponents.FOOD)) return;
            mc.options.keyUse.setDown(false);
            inventory.setSelectedSlot(previousSlot);
            previousSlot = -1;
            return;
        }
        if (mc.gui.screen() != null || mc.player.getFoodData().getFoodLevel() > hunger.intValue()) return;
        int food = Inventories.findHotbar(mc, stack -> stack.has(DataComponents.FOOD));
        if (food < 0) return;
        previousSlot = inventory.getSelectedSlot();
        inventory.setSelectedSlot(food);
        mc.options.keyUse.setDown(true);
    }

    @Override
    protected void onDisable() {
        if (previousSlot >= 0) Minecraft.getInstance().options.keyUse.setDown(false);
        previousSlot = -1;
    }
}
