package vibe.client.mixin;

import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vibe.client.VibeClient;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "createTitle", at = @At("HEAD"), cancellable = true)
    private void vibe$windowTitle(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(VibeClient.NAME + " " + VibeClient.version()
                + " | Minecraft " + SharedConstants.getCurrentVersion().name());
    }
}
