package vibe.client.module.render;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import vibe.client.module.Category;
import vibe.client.module.CheatModule;
import vibe.client.setting.BoolSetting;

/** Подсвечивает игроков и мобов контуром, видно сквозь стены. */
public final class Esp extends CheatModule {
    private final BoolSetting players = add(new BoolSetting("Игроки", true));
    private final BoolSetting monsters = add(new BoolSetting("Монстры", false));
    private final BoolSetting others = add(new BoolSetting("Животные", false));

    public Esp() {
        super("esp", "ESP", "Контур игроков и мобов сквозь стены", Category.RENDER);
    }

    public boolean highlights(Minecraft mc, Entity entity) {
        if (!active(mc) || entity == mc.player || !(entity instanceof LivingEntity)) return false;
        if (entity instanceof Player) return players.on();
        if (entity instanceof Enemy) return monsters.on();
        return others.on();
    }
}
