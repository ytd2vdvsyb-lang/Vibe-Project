package vibe.client.module.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

/** Общие помощники боевых модулей: поиск цели и углы. */
final class Targets {
    private Targets() {
    }

    static boolean valid(LocalPlayer self, Entity entity) {
        return entity instanceof LivingEntity living && living != self && living.isAlive()
                && !(living instanceof ArmorStand) && !living.isSpectator() && !living.isInvisible();
    }

    /** Ближайшая подходящая цель в радиусе, не дальше {@code maxAngle} градусов от прицела. */
    static Optional<LivingEntity> nearest(Minecraft mc, double range, float maxAngle, Predicate<LivingEntity> filter) {
        LocalPlayer self = mc.player;
        return mc.level.getEntitiesOfClass(LivingEntity.class, self.getBoundingBox().inflate(range),
                        e -> valid(self, e) && filter.test(e) && self.distanceTo(e) <= range
                                && angleTo(self, e) <= maxAngle)
                .stream()
                .min(Comparator.comparingDouble(self::distanceTo));
    }

    /** Поворот {yaw, pitch}, при котором игрок смотрит в центр цели. */
    static float[] rotationTo(LocalPlayer self, Entity target) {
        return rotationTo(self, target, 0);
    }

    /** То же, но целимся туда, где цель будет через {@code ticks} тиков, если продолжит бежать так же. */
    static float[] rotationTo(LocalPlayer self, Entity target, double ticks) {
        Vec3 eye = self.getEyePosition();
        Vec3 aim = target.getBoundingBox().getCenter().add(target.getDeltaMovement().multiply(ticks, 0, ticks));
        double dx = aim.x - eye.x, dy = aim.y - eye.y, dz = aim.z - eye.z;
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        return new float[]{yaw, Mth.clamp(pitch, -90f, 90f)};
    }

    /**
     * Скорость поворота для режима «Авто»: при большом угле крутим быстрее, вблизи прицела — мягко,
     * а за быстро бегущей целью — ещё чуть быстрее, чтобы не отставать.
     */
    static float autoSpeed(LocalPlayer self, Entity target) {
        float angle = angleTo(self, target);
        double targetSpeed = target.getDeltaMovement().horizontalDistance();
        float speed = 0.2f + angle / 120f + (float) Math.min(0.2, targetSpeed);
        return Math.max(0.2f, Math.min(0.9f, speed));
    }

    static float angleTo(LocalPlayer self, Entity target) {
        float[] rot = rotationTo(self, target);
        float dYaw = Mth.wrapDegrees(rot[0] - self.getYRot());
        float dPitch = rot[1] - self.getXRot();
        return (float) Math.sqrt(dYaw * dYaw + dPitch * dPitch);
    }

    /** Поворачивает голову к цели на долю {@code speed} (1 = сразу). Возвращает угол, что остался. */
    static float turnTowards(LocalPlayer self, Entity target, float speed) {
        return turnTowards(self, target, speed, 0);
    }

    static float turnTowards(LocalPlayer self, Entity target, float speed, double predictTicks) {
        float[] rot = rotationTo(self, target, predictTicks);
        float yaw = self.getYRot();
        float pitch = self.getXRot();
        self.setYRot(yaw + Mth.wrapDegrees(rot[0] - yaw) * speed);
        self.setXRot(pitch + (rot[1] - pitch) * speed);
        return angleTo(self, target);
    }
}
