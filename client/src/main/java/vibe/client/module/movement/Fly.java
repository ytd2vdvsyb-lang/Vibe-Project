package vibe.client.module.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import vibe.client.module.Category;
import vibe.client.module.CheatModule;
import vibe.client.setting.NumberSetting;

/** Полёт: WASD, пробел вверх, Shift вниз. В воздухе стоишь на месте. */
public final class Fly extends CheatModule {
    private final NumberSetting bps = add(new NumberSetting("Блоков/сек", 10.0, 2.0, 40.0, 1.0));
    private final NumberSetting vertical = add(new NumberSetting("Вверх/вниз", 6.0, 1.0, 20.0, 1.0));

    public Fly() {
        super("fly", "Fly", "Полёт с настраиваемой скоростью", Category.MOVEMENT);
    }

    @Override
    protected void cheatTick(Minecraft mc) {
        Vec3 dir = MoveInput.horizontal(mc);
        double h = bps.get() / 20.0;
        double v = vertical.get() / 20.0;
        double y = mc.options.keyJump.isDown() ? v : mc.options.keyShift.isDown() ? -v : 0;
        mc.player.setDeltaMovement(dir.x * h, y, dir.z * h);
        mc.player.fallDistance = 0;
    }
}
