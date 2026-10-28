package vibe.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vibe.client.VibeClient;
import vibe.client.module.combat.Velocity;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    /** Velocity: отдача от ударов уменьшается на заданный процент. */
    @Inject(method = "handleSetEntityMotion", at = @At("HEAD"), cancellable = true, require = 0)
    private void vibe$velocity(ClientboundSetEntityMotionPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        // Пакет сначала приходит в сетевой поток и перекладывается в игровой: работаем только во втором.
        if (!mc.isSameThread() || mc.player == null || packet.id() != mc.player.getId()) return;
        Velocity velocity = VibeClient.get().modules().get(Velocity.class);
        if (!velocity.active(mc)) return;
        mc.player.setDeltaMovement(velocity.scale(packet.movement()));
        ci.cancel();
    }
}
