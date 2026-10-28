package vibe.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class BoolSetting extends Setting<Boolean> {
    public BoolSetting(String name, boolean value) {
        super(name, value);
    }

    public boolean on() {
        return value;
    }

    public void toggle() {
        value = !value;
    }

    @Override
    public String display() {
        return value ? "Да" : "Нет";
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement json) {
        value = json.getAsBoolean();
    }
}
