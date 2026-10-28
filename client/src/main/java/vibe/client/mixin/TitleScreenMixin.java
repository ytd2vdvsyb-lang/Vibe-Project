package vibe.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vibe.client.VibeClient;
import vibe.client.module.HudModule;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {
    private static boolean vibe$logged;

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void vibe$branding(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!vibe$logged) {
            vibe$logged = true;
            // По этой строке автотест в CI понимает, что игра дошла до главного меню.
            VibeClient.LOG.info("Главное меню открыто");
        }
        Minecraft mc = Minecraft.getInstance();
        String text = VibeClient.NAME + " " + VibeClient.version();
        graphics.text(mc.font, Component.literal(text), 4, 4, HudModule.ACCENT, true);
    }
}
