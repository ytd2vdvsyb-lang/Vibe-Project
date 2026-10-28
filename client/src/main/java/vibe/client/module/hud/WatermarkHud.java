package vibe.client.module.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import vibe.client.VibeClient;
import vibe.client.gui.Draw;
import vibe.client.module.HudModule;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Строка плашек сверху: логотип, ник, FPS, пинг и время. */
public final class WatermarkHud extends HudModule {
    private static final int GAP = 3;
    private static final int DOT = 5;
    private static final int H = 15;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    public WatermarkHud() {
        super("watermark", "Ватермарк", "Логотип, ник, FPS, пинг и время", true, 4, 4);
    }

    private static List<String> pills(Minecraft mc) {
        List<String> pills = new ArrayList<>();
        pills.add(mc.getUser().getName());
        pills.add(mc.getFps() + " FPS");
        if (mc.getConnection() != null && mc.player != null && !mc.hasSingleplayerServer()) {
            PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
            if (info != null) pills.add(info.getLatency() + " ms");
        }
        pills.add(LocalTime.now().format(TIME));
        return pills;
    }

    private static int logoWidth(Minecraft mc) {
        return mc.font.width(VibeClient.NAME) + 12;
    }

    private static int pillWidth(Minecraft mc, String text) {
        return mc.font.width(text) + DOT + 13;
    }

    @Override
    public int width(Minecraft mc) {
        int w = logoWidth(mc);
        for (String pill : pills(mc)) w += GAP + pillWidth(mc, pill);
        return w;
    }

    @Override
    public int height(Minecraft mc) {
        return H;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Minecraft mc) {
        int x = x(), y = y();
        int textY = y + (H - mc.font.lineHeight) / 2 + 1;

        int logo = logoWidth(mc);
        Draw.round(g, x, y, logo, H, 7, BACKGROUND);
        g.text(mc.font, VibeClient.NAME, x + 6, textY, accent(), true);
        x += logo + GAP;

        for (String pill : pills(mc)) {
            int w = pillWidth(mc, pill);
            Draw.round(g, x, y, w, H, 7, BACKGROUND);
            Draw.circle(g, x + 6, y + (H - DOT) / 2, DOT, accent());
            g.text(mc.font, pill, x + DOT + 9, textY, TEXT, true);
            x += w + GAP;
        }
    }
}
