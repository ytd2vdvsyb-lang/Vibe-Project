package vibe.client.module.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import vibe.client.module.HudModule;

import java.util.ArrayList;
import java.util.List;

/** Активные эффекты зелий со временем. */
public final class EffectsHud extends HudModule {
    private static final String[] ROMAN = {"", " II", " III", " IV", " V", " VI", " VII", " VIII", " IX", " X"};

    public EffectsHud() {
        super("effects", "Эффекты", "Эффекты зелий и их время", false, 4, 260);
    }

    private static List<String> lines(Minecraft mc) {
        List<String> lines = new ArrayList<>();
        if (mc.player == null) return lines;
        for (MobEffectInstance effect : mc.player.getActiveEffects()) {
            int level = effect.getAmplifier();
            String name = effect.getEffect().value().getDisplayName().getString()
                    + (level < ROMAN.length ? ROMAN[level] : " " + (level + 1));
            lines.add(name + "  " + time(effect));
        }
        return lines;
    }

    private static String time(MobEffectInstance effect) {
        if (effect.isInfiniteDuration()) return "∞";
        int seconds = effect.getDuration() / 20;
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }

    @Override
    public int width(Minecraft mc) {
        int max = mc.font.width("Эффекты");
        for (String line : lines(mc)) max = Math.max(max, mc.font.width(line));
        return max + PADDING * 2;
    }

    @Override
    public int height(Minecraft mc) {
        return Math.max(1, lines(mc).size()) * (mc.font.lineHeight + 2) + PADDING * 2 - 2;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Minecraft mc) {
        List<String> lines = lines(mc);
        if (lines.isEmpty()) return;
        panel(g, x(), y(), width(mc), height(mc));
        int lineY = y() + PADDING;
        for (String line : lines) {
            g.text(mc.font, Component.literal(line), x() + PADDING, lineY, TEXT, true);
            lineY += mc.font.lineHeight + 2;
        }
    }
}
