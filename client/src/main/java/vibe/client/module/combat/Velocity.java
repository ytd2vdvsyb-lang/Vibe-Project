package vibe.client.module.combat;

import net.minecraft.world.phys.Vec3;
import vibe.client.module.Category;
import vibe.client.module.CheatModule;
import net.minecraft.client.Minecraft;
import vibe.client.setting.BoolSetting;
import vibe.client.setting.NumberSetting;

/** Меньше отдачи от ударов. 100% — как в обычной игре, 0% — совсем без отдачи. */
public final class Velocity extends CheatModule {
    private final NumberSetting horizontal = add(new NumberSetting("По горизонтали %", 60, 0, 100, 5));
    private final NumberSetting vertical = add(new NumberSetting("По вертикали %", 100, 0, 100, 5));
    private final BoolSetting auto = add(new BoolSetting("Авто", true));

    public Velocity() {
        super("velocity", "Velocity", "Уменьшает отдачу от ударов", Category.COMBAT);
    }

    public Vec3 scale(Vec3 motion) {
        double h = horizontal.get() / 100.0, v = vertical.get() / 100.0;
        Minecraft mc = Minecraft.getInstance();
        // «Авто»: в воздухе и в воде режем отдачу слабее, иначе движение выглядит дёрганым.
        if (auto.on() && mc.player != null && (!mc.player.onGround() || mc.player.isInWater())) h = Math.min(1, h + 0.3);
        return new Vec3(motion.x * h, motion.y * v, motion.z * h);
    }
}
