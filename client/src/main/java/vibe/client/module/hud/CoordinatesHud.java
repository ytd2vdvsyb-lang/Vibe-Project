package vibe.client.module.hud;

import net.minecraft.client.Minecraft;
import vibe.client.module.TextHudModule;

public final class CoordinatesHud extends TextHudModule {
    public CoordinatesHud() {
        super("coordinates", "Координаты", "XYZ и направление взгляда", true, 4, 22);
    }

    @Override
    protected String text(Minecraft mc) {
        if (mc.player == null) return "";
        return String.format("XYZ %d %d %d  %s", mc.player.getBlockX(), mc.player.getBlockY(), mc.player.getBlockZ(),
                facing(mc.player.getYRot()));
    }

    private static String facing(float yaw) {
        String[] names = {"Юг", "Юго-запад", "Запад", "Северо-запад", "Север", "Северо-восток", "Восток", "Юго-восток"};
        int index = Math.floorMod(Math.round(yaw / 45f), 8);
        return names[index];
    }
}
