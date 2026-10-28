package vibe.client.setting;

import com.google.gson.JsonElement;

/** Настройка модуля, которую видно и можно менять в меню. */
public abstract class Setting<T> {
    private final String name;
    protected T value;

    protected Setting(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    /** Текст значения справа в строке меню. */
    public abstract String display();

    public abstract JsonElement toJson();

    public abstract void fromJson(JsonElement json);
}
