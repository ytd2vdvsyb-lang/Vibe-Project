package vibe.client.module.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.phys.Vec3;
import vibe.client.module.Category;
import vibe.client.module.CheatModule;
import vibe.client.setting.NumberSetting;

/** Пока натягиваешь лук, наводит с учётом падения стрелы и бега цели. */
public final class BowAimbot extends CheatModule {
    private final TargetSettings targets = new TargetSettings();
    private final NumberSetting range = add(new NumberSetting("Радиус", 40, 10, 80, 5));

    public BowAimbot() {
        super("bow_aimbot", "BowAimbot", "Наводка лука с упреждением", Category.COMBAT);
        add(targets.players);
        add(targets.monsters);
        add(targets.others);
    }

    @Override
    protected void cheatTick(Minecraft mc) {
        if (!mc.player.isUsingItem() || !(mc.player.getUseItem().getItem() instanceof BowItem)) return;
        Targets.nearest(mc, range.get(), 90f, targets).ifPresent(target -> {
            // Сила натяжения как у ванильного лука: полная через 20 тиков.
            float pull = Math.min(1f, mc.player.getTicksUsingItem() / 20f);
            pull = (pull * pull + pull * 2) / 3;
            double velocity = Math.max(0.1, pull * 3.0);

            Vec3 eye = mc.player.getEyePosition();
            Vec3 aim = target.getBoundingBox().getCenter();
            double distance = eye.distanceTo(aim);
            double flightTicks = distance / velocity;
            aim = aim.add(target.getDeltaMovement().multiply(flightTicks, 0, flightTicks));

            double dx = aim.x - eye.x, dy = aim.y - eye.y, dz = aim.z - eye.z;
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            // Баллистика с гравитацией стрелы 0.05 блока/тик².
            double g = 0.05, v2 = velocity * velocity;
            double root = v2 * v2 - g * (g * horizontal * horizontal + 2 * dy * v2);
            if (root < 0) return;
            float pitch = (float) -Math.toDegrees(Math.atan((v2 - Math.sqrt(root)) / (g * horizontal)));
            float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
            mc.player.setYRot(yaw);
            mc.player.setXRot(pitch);
        });
    }
}
