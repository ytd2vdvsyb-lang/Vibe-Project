package vibe.client.module.hud;

import net.minecraft.client.Minecraft;
import vibe.client.module.TextHudModule;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class ClockHud extends TextHudModule {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public ClockHud() {
        super("clock", "Часы", "Реальное время", false, 4, 40);
    }

    @Override
    protected String text(Minecraft mc) {
        return LocalTime.now().format(FORMAT);
    }
}
