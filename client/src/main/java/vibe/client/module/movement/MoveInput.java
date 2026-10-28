package vibe.client.module.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

/** Направление движения по нажатым WASD с учётом поворота головы. */
final class MoveInput {
    private MoveInput() {
    }

    /** Единичный вектор по горизонтали или нулевой, если клавиши не нажаты. */
    static Vec3 horizontal(Minecraft mc) {
        double forward = (mc.options.keyUp.isDown() ? 1 : 0) - (mc.options.keyDown.isDown() ? 1 : 0);
        double strafe = (mc.options.keyLeft.isDown() ? 1 : 0) - (mc.options.keyRight.isDown() ? 1 : 0);
        if (forward == 0 && strafe == 0) return Vec3.ZERO;
        double yaw = Math.toRadians(mc.player.getYRot());
        double sin = Math.sin(yaw), cos = Math.cos(yaw);
        double x = strafe * cos - forward * sin;
        double z = forward * cos + strafe * sin;
        double length = Math.sqrt(x * x + z * z);
        return new Vec3(x / length, 0, z / length);
    }
}
