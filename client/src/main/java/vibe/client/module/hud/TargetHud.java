package vibe.client.module.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import vibe.client.module.HudModule;

import java.util.Comparator;
import java.util.Locale;

/** Карточка противника: ник, здоровье полоской, броня и дистанция. */
public final class TargetHud extends HudModule {
    private static final int WIDTH = 130;
    private static final int HEIGHT = 34;
    private static final double RANGE = 8;

    public TargetHud() {
        super("target_hud", "Цель", "Здоровье и дистанция ближайшего противника", false, 200, 200);
    }

    @Override
    public int width(Minecraft mc) {
        return WIDTH;
    }

    @Override
    public int height(Minecraft mc) {
        return HEIGHT;
    }

    private static LivingEntity target(Minecraft mc) {
        if (mc.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity living) return living;
        return mc.level.getEntitiesOfClass(Player.class, mc.player.getBoundingBox().inflate(RANGE),
                        p -> p != mc.player && p.isAlive() && mc.player.distanceTo(p) <= RANGE)
                .stream().min(Comparator.comparingDouble(mc.player::distanceTo)).orElse(null);
    }

    @Override
    public void render(GuiGraphicsExtractor g, Minecraft mc) {
        if (mc.player == null || mc.level == null) return;
        LivingEntity target = target(mc);
        if (target == null) return;
        int x = x(), y = y();
        g.fill(x, y, x + WIDTH, y + HEIGHT, BACKGROUND);
        g.fill(x, y, x + 2, y + HEIGHT, ACCENT);
        g.text(mc.font, target.getName().getString(), x + 7, y + 4, TEXT, true);

        float health = target.getHealth() + target.getAbsorptionAmount();
        float max = target.getMaxHealth();
        String hp = String.format(Locale.ROOT, "%.1f ❤", health);
        g.text(mc.font, hp, x + WIDTH - 5 - mc.font.width(hp), y + 4, 0xFFF87171, true);

        int barLeft = x + 7, barRight = x + WIDTH - 5, barY = y + 16;
        float fraction = Math.max(0, Math.min(1, target.getHealth() / max));
        int color = fraction > 0.5f ? 0xFF4ADE80 : fraction > 0.25f ? 0xFFFACC15 : 0xFFF87171;
        g.fill(barLeft, barY, barRight, barY + 4, 0xFF2A2A3A);
        g.fill(barLeft, barY, barLeft + (int) ((barRight - barLeft) * fraction), barY + 4, color);

        String info = String.format(Locale.ROOT, "%.1f м  броня %d", mc.player.distanceTo(target), target.getArmorValue());
        g.text(mc.font, info, x + 7, y + 23, 0xFF8A8A9A, false);
    }
}
