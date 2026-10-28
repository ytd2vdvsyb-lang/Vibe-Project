package vibe.client.module.player;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import vibe.client.module.Category;
import vibe.client.module.CheatModule;
import vibe.client.setting.NumberSetting;
import vibe.client.util.Inventories;

/** Суп-PvP: при низком здоровье сам выпивает суп из хотбара. */
public final class AutoSoup extends CheatModule {
    private final NumberSetting health = add(new NumberSetting("Здоровье ≤", 12, 2, 19, 1));
    private int cooldown;

    public AutoSoup() {
        super("auto_soup", "AutoSoup", "Сам ест суп при малом здоровье", Category.PLAYER);
    }

    @Override
    protected void cheatTick(Minecraft mc) {
        if (--cooldown > 0 || mc.gui.screen() != null || mc.player.getHealth() > health.intValue()) return;
        int soup = Inventories.findHotbar(mc, stack -> stack.is(Items.MUSHROOM_STEW));
        if (soup < 0) return;
        var inventory = mc.player.getInventory();
        int previous = inventory.getSelectedSlot();
        inventory.setSelectedSlot(soup);
        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        inventory.setSelectedSlot(previous);
        cooldown = 3;
    }
}
