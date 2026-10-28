package vibe.launcher.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Настройки лаунчера, хранятся в settings.json. */
public final class Settings {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final String LATEST = "latest";

    public String username = "";
    /** Версия Minecraft или "latest". */
    public String minecraftVersion = LATEST;
    public int memoryMb = 4096;
    /** Пусто = скачать подходящую Java автоматически. */
    public String javaPath = "";
    public String extraJvmArgs = "";
    public boolean closeOnLaunch = false;
    /** Client id приложения Azure для входа через Microsoft. Можно задать и через -Dvibe.msClientId. */
    public String msClientId = "";
    /** Refresh token Microsoft. Пусто = офлайн-аккаунт по нику. */
    public String microsoftRefreshToken = "";
    /** Ник лицензионного аккаунта, только для показа в лаунчере. */
    public String microsoftName = "";

    public String effectiveMsClientId() {
        String fromProperty = System.getProperty("vibe.msClientId", "");
        return fromProperty.isBlank() ? msClientId : fromProperty;
    }

    public static Settings load(GameDirs dirs) {
        Path file = file(dirs);
        try {
            if (Files.isRegularFile(file)) {
                Settings settings = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), Settings.class);
                if (settings != null) return settings;
            }
        } catch (IOException | RuntimeException ignored) {
            // Битый файл настроек не должен мешать запуску.
        }
        return new Settings();
    }

    public void save(GameDirs dirs) throws IOException {
        Path file = file(dirs);
        Files.createDirectories(file.getParent());
        Files.writeString(file, GSON.toJson(this), StandardCharsets.UTF_8);
    }

    private static Path file(GameDirs dirs) {
        return dirs.root.resolve("settings.json");
    }
}
