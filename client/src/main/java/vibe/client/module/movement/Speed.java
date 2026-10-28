package vibe.client.module.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import vibe.client.module.Category;
import vibe.client.module.CheatModule;
import vibe.client.setting.ModeSetting;
import vibe.client.setting.NumberSetting;

/** Быстрее бег по земле. Скорость задаётся в блоках в секунду. */
public final class Speed extends CheatModule {
    private final ModeSetting preset = add(new ModeSetting("Пресет", "Легит", "Средний", "Макс", "Свой"));
    private final NumberSetting bps = add(new NumberSetting("Блоков/сек", 7.0, 5.0, 20.0, 0.5));

    public Speed() {
        super("speed", "Speed", "Быстрый бег по земле", Category.MOVEMENT);
    }

    @Override
    protected void cheatTick(Minecraft mc) {
        if (!mc.player.onGround() || mc.player.isShiftKeyDown() || mc.player.isInWater()) return;
        Vec3 dir = MoveInput.horizontal(mc);
        if (dir == Vec3.ZERO) return;
        double perTick = blocksPerSecond() / 20.0;
        Vec3 motion = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(dir.x * perTick, motion.y, dir.z * perTick);
    }

    /** Скорость из пресета или со своего ползунка. */
    private double blocksPerSecond() {
        return switch (preset.get()) {
            case "Легит" -> 6.5;
            case "Средний" -> 8.5;
            case "Макс" -> 12;
            default -> bps.get();
        };
    }
}
