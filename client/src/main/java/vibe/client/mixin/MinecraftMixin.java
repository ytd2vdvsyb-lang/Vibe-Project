package vibe.client.mixin;

import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.entity.Entity;
import vibe.client.VibeClient;
import vibe.client.module.render.Esp;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "createTitle", at = @At("HEAD"), cancellable = true)
    private void vibe$windowTitle(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(VibeClient.NAME + " " + VibeClient.version()
                + " | Minecraft " + SharedConstants.getCurrentVersion().name());
    }

    /** ESP: подсветка контуром сквозь стены. */
    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true, require = 0)
    private void vibe$esp(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        Esp esp = VibeClient.get().modules().get(Esp.class);
        if (esp.highlights((Minecraft) (Object) this, entity)) cir.setReturnValue(true);
    }
}
