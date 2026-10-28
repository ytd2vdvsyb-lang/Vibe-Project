package vibe.client.module.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.Optional;

/** Общие помощники боевых модулей: поиск цели, углы, удар с полной перезарядкой. */
final class Targets {
    private Targets() {
    }

    static boolean valid(LocalPlayer self, Entity entity) {
        return entity instanceof LivingEntity living && living != self && living.isAlive()
                && !(living instanceof ArmorStand) && !living.isSpectator() && !living.isInvisible();
    }

    /** Ближайшая живая цель в радиусе, не дальше {@code maxAngle} градусов от прицела. */
    static Optional<LivingEntity> nearest(Minecraft mc, double range, float maxAngle) {
        LocalPlayer self = mc.player;
        return mc.level.getEntitiesOfClass(LivingEntity.class, self.getBoundingBox().inflate(range),
                        e -> valid(self, e) && self.distanceTo(e) <= range && angleTo(self, e) <= maxAngle)
                .stream()
                .min(Comparator.comparingDouble(self::distanceTo));
    }

    /** Поворот {yaw, pitch}, при котором игрок смотрит в центр цели. */
    static float[] rotationTo(LocalPlayer self, Entity target) {
        Vec3 eye = self.getEyePosition();
        Vec3 aim = target.getBoundingBox().getCenter();
        double dx = aim.x - eye.x, dy = aim.y - eye.y, dz = aim.z - eye.z;
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        return new float[]{yaw, Mth.clamp(pitch, -90f, 90f)};
    }

    static float angleTo(LocalPlayer self, Entity target) {
        float[] rot = rotationTo(self, target);
        float dYaw = Mth.wrapDegrees(rot[0] - self.getYRot());
        float dPitch = rot[1] - self.getXRot();
        return (float) Math.sqrt(dYaw * dYaw + dPitch * dPitch);
    }

    static boolean cooledDown(LocalPlayer self) {
        return self.getAttackStrengthScale(0.5f) >= 1f;
    }

    static void attack(Minecraft mc, Entity target) {
        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);
    }
}
