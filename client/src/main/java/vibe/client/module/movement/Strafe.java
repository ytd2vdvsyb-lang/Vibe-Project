package vibe.client.module.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import vibe.client.module.Category;
import vibe.client.module.CheatModule;
import vibe.client.setting.BoolSetting;

/** Поворот в воздухе без потери скорости: движение сразу идёт туда, куда жмёшь WASD. */
public final class Strafe extends CheatModule {
    private final BoolSetting onlyAir = add(new BoolSetting("Только в воздухе", true));
    private final BoolSetting stop = add(new BoolSetting("Стоп без клавиш", false));

    public Strafe() {
        super("strafe", "Strafe", "Резкие повороты в прыжке, скорость сохраняется", Category.MOVEMENT);
    }

    @Override
    protected void cheatTick(Minecraft mc) {
        if (mc.player.isInWater() || mc.player.getAbilities().flying) return;
        if (onlyAir.on() && mc.player.onGround()) return;
        Vec3 motion = mc.player.getDeltaMovement();
        Vec3 dir = MoveInput.horizontal(mc);
        if (dir == Vec3.ZERO) {
            if (stop.on()) mc.player.setDeltaMovement(0, motion.y, 0);
            return;
        }
        double speed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        mc.player.setDeltaMovement(dir.x * speed, motion.y, dir.z * speed);
    }
}
