package vibe.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.List;

/** Выбор одного варианта из списка. */
public final class ModeSetting extends Setting<String> {
    private final List<String> modes;

    public ModeSetting(String name, String... modes) {
        super(name, modes[0]);
        this.modes = List.of(modes);
    }

    public boolean is(String mode) {
        return value.equals(mode);
    }

    public void cycle(int direction) {
        int i = Math.floorMod(modes.indexOf(value) + direction, modes.size());
        value = modes.get(i);
    }

    @Override
    public String display() {
        return value;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement json) {
        String mode = json.getAsString();
        if (modes.contains(mode)) value = mode;
    }
}
