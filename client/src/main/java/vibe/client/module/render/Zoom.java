package vibe.client.module.render;

import vibe.client.module.Category;
import vibe.client.module.Module;
import vibe.client.setting.NumberSetting;

/** Зум, пока зажата клавиша C. Само приближение делает CameraMixin. */
public final class Zoom extends Module {
    public static final int KEY_C = 67;
    private final NumberSetting factor = add(new NumberSetting("Приближение", 4, 2, 10, 0.5));
    private boolean held;

    public Zoom() {
        super("zoom", "Zoom", "Приближение на зажатую C", true);
    }

    @Override
    public Category category() {
        return Category.RENDER;
    }

    public void setHeld(boolean held) {
        this.held = held;
    }

    public float apply(float fov) {
        return isEnabled() && held ? fov / factor.floatValue() : fov;
    }
}
