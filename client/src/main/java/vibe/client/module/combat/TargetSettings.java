package vibe.client.module.combat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import vibe.client.setting.BoolSetting;

import java.util.function.Predicate;

/** Кого можно выбирать целью: общие переключатели для боевых модулей. */
final class TargetSettings implements Predicate<LivingEntity> {
    final BoolSetting players = new BoolSetting("Игроки", true);
    final BoolSetting monsters = new BoolSetting("Монстры", true);
    final BoolSetting others = new BoolSetting("Животные", false);

    @Override
    public boolean test(LivingEntity entity) {
        if (entity instanceof Player) return players.on();
        if (entity instanceof Enemy) return monsters.on();
        return others.on();
    }
}
