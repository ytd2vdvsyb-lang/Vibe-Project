package vibe.client.module;

import net.minecraft.client.Minecraft;
import vibe.client.setting.Setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Module {
    private final String id;
    private final String name;
    private final String description;
    private final List<Setting<?>> settings = new ArrayList<>();
    private boolean enabled;
    /** Код клавиши из InputConstants, -1 если не назначена. */
    private int key = -1;

    protected Module(String id, String name, String description, boolean enabledByDefault) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = enabledByDefault;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Category category() {
        return Category.PLAYER;
    }

    protected final <S extends Setting<?>> S add(S setting) {
        settings.add(setting);
        return setting;
    }

    public List<Setting<?>> settings() {
        return Collections.unmodifiableList(settings);
    }

    public int key() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) onEnable(); else onDisable();
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public void tick(Minecraft mc) {
    }
}
