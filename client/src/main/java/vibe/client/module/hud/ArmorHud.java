package vibe.client.module.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import vibe.client.module.HudModule;

/** Надетая броня и предмет в руке с прочностью. */
public final class ArmorHud extends HudModule {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND};
    private static final int ROW = 18;

    public ArmorHud() {
        super("armor", "Броня", "Броня и прочность", false, 4, 160);
    }

    @Override
    public int width(Minecraft mc) {
        return 16 + 4 + mc.font.width("100%") + PADDING * 2;
    }

    @Override
    public int height(Minecraft mc) {
        return SLOTS.length * ROW + PADDING * 2 - 2;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Minecraft mc) {
        if (mc.player == null) return;
        panel(g, x(), y(), width(mc), height(mc));
        int rowY = y() + PADDING;
        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = mc.player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                g.item(stack, x() + PADDING, rowY);
                g.itemDecorations(mc.font, stack, x() + PADDING, rowY);
                if (stack.isDamageableItem()) {
                    int percent = Math.round(100f * (stack.getMaxDamage() - stack.getDamageValue()) / stack.getMaxDamage());
                    int color = percent > 50 ? 0xFF86EFAC : percent > 20 ? 0xFFFDE047 : 0xFFF87171;
                    g.text(mc.font, Component.literal(percent + "%"), x() + PADDING + 20, rowY + 4, color, true);
                }
            }
            rowY += ROW;
        }
    }
}
