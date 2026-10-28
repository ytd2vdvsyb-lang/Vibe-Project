package vibe.client.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vibe.client.VibeClient;
import vibe.client.gui.VibeMenuScreen;
import vibe.client.module.Module;
import vibe.client.module.render.Zoom;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    /** Код действия "нажата" (0 = отпущена, 2 = повтор). */
    private static final int PRESS = 1;

    /** Right Shift в игре открывает меню Vibe Client, остальные клавиши переключают модули. */
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void vibe$openMenu(long window, int action, KeyEvent event, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (action == PRESS && event.key() == InputConstants.KEY_RSHIFT
                && mc.gui.screen() == null && mc.player != null) {
            mc.gui.setScreen(new VibeMenuScreen());
            ci.cancel();
            return;
        }
        if (event.key() == Zoom.KEY_C) {
            VibeClient.get().modules().get(Zoom.class).setHeld(action != 0 && mc.gui.screen() == null);
        }
        // Клавиши модулей работают только в самой игре, не в чате и не в меню.
        if (action == PRESS && mc.gui.screen() == null && mc.player != null) {
            for (Module module : VibeClient.get().modules().all()) {
                if (module.key() == event.key()) module.toggle();
            }
        }
    }
}
