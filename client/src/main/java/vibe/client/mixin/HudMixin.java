package vibe.client.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vibe.client.VibeClient;
import vibe.client.gui.VibeMenuScreen;

@Mixin(Hud.class)
public abstract class HudMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void vibe$renderHud(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        // В меню клиента HUD рисует само меню, чтобы его можно было двигать.
        if (mc.gui.screen() instanceof VibeMenuScreen) return;
        VibeClient.get().modules().renderHud(graphics, mc);
    }
}
