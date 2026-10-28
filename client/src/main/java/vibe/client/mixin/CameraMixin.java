package vibe.client.mixin;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vibe.client.VibeClient;
import vibe.client.module.render.Zoom;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true, require = 0)
    private void vibe$zoom(float partialTick, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(VibeClient.get().modules().get(Zoom.class).apply(cir.getReturnValueF()));
    }
}
