package vibe.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/** Бег без зажатия Ctrl, как опция "Переключение бега" в настройках, но включается одной кнопкой. */
public final class ToggleSprint extends Module {
    public ToggleSprint() {
        super("toggle_sprint", "Sprint", "Бежать, пока идёшь вперёд", false);
    }

    @Override
    public Category category() {
        return Category.MOVEMENT;
    }

    @Override
    public void tick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.gui.screen() != null) return;
        if (mc.options.keyUp.isDown() && !player.isShiftKeyDown() && !player.isUsingItem()
                && player.getFoodData().getFoodLevel() > 6) {
            player.setSprinting(true);
        }
    }
}
