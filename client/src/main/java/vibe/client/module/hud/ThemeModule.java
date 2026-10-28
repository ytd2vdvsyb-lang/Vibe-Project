package vibe.client.module.hud;

import vibe.client.gui.Theme;
import vibe.client.module.Category;
import vibe.client.module.Module;
import vibe.client.setting.ModeSetting;

/** Цвет акцента для меню и HUD. Включать не нужно: цвет действует всегда. */
public final class ThemeModule extends Module {
    private final ModeSetting color = add(new ModeSetting("Цвет", Theme.ACCENTS));

    public ThemeModule() {
        super("theme", "Тема", "Цвет меню и HUD, нажми «+»", true);
        Theme.use(color::get);
    }

    @Override
    public Category category() {
        return Category.HUD;
    }
}
