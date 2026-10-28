package vibe.client.module.render;

import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import vibe.client.VibeClient;
import vibe.client.module.Category;
import vibe.client.module.CheatModule;
import vibe.client.setting.BoolSetting;
import vibe.client.setting.NumberSetting;

/** Линии от прицела к игрокам и мобам: сразу видно, откуда идут. */
public final class Tracers extends CheatModule {
    private final BoolSetting players = add(new BoolSetting("Игроки", true));
    private final BoolSetting monsters = add(new BoolSetting("Монстры", false));
    private final NumberSetting range = add(new NumberSetting("Дальность", 64, 16, 256, 16));
    private boolean failed;

    public Tracers() {
        super("tracers", "Tracers", "Линии к игрокам и мобам", Category.RENDER);
    }

    /** Вызывается из события отрисовки мира, когда собираются гизмо кадра. */
    public void draw(Minecraft mc) {
        if (failed || !active(mc) || mc.level == null) return;
        try {
            Vec3 eye = mc.player.getEyePosition();
            Vec3 start = eye.add(mc.player.getLookAngle().scale(0.5));
            for (LivingEntity entity : mc.level.getEntitiesOfClass(LivingEntity.class,
                    mc.player.getBoundingBox().inflate(range.get()), e -> e != mc.player && e.isAlive())) {
                int color;
                if (entity instanceof Player && players.on()) color = 0xFFA78BFA;
                else if (entity instanceof Enemy && monsters.on()) color = 0xFFF87171;
                else continue;
                Gizmos.line(start, entity.getBoundingBox().getCenter(), color, 1.5f).setAlwaysOnTop();
            }
        } catch (RuntimeException e) {
            // Если отрисовка в этой версии устроена иначе, не роняем игру, а выключаем линии.
            failed = true;
            VibeClient.LOG.warn("Tracers отключены: {}", e.toString());
        }
    }
}
