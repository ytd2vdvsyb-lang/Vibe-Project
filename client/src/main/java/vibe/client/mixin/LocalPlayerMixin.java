package vibe.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import vibe.client.VibeClient;
import vibe.client.module.movement.NoFall;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    /** NoFall: при падении сообщаем серверу, что стоим на земле. */
    @Redirect(method = "sendPosition", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;onGround()Z"), require = 0)
    private boolean vibe$noFall(LocalPlayer player) {
        NoFall noFall = VibeClient.get().modules().get(NoFall.class);
        return player.onGround() || noFall.active(Minecraft.getInstance()) && player.fallDistance > 2.0;
    }
}
