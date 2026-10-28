package vibe.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vibe.client.VibeClient;
import vibe.client.module.render.FullBright;

@Mixin(OptionInstance.class)
public abstract class OptionInstanceMixin {
    /** FullBright: яркость выше 100%, которую обычный ползунок не даёт выставить. */
    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void vibe$fullBright(CallbackInfoReturnable<Object> cir) {
        Minecraft mc = Minecraft.getInstance();
        VibeClient client = VibeClient.get();
        if (mc == null || mc.options == null || client == null || client.modules() == null) return;
        if ((Object) this == mc.options.gamma() && client.modules().get(FullBright.class).isEnabled()) {
            cir.setReturnValue(16.0);
        }
    }
}
