package vibe.client.mixin;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vibe.client.gui.VibeMenuScreen;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    /** Right Shift в игре открывает меню Vibe Client. */
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void vibe$openMenu(long window, int action, KeyEvent event, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (action == GLFW.GLFW_PRESS && event.key() == GLFW.GLFW_KEY_RIGHT_SHIFT
                && mc.gui.screen() == null && mc.player != null) {
            mc.gui.setScreen(new VibeMenuScreen());
            ci.cancel();
        }
    }
}
