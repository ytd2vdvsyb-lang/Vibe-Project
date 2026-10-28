package vibe.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import vibe.client.module.HudModule;
import vibe.client.module.Module;
import vibe.client.module.ModuleManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Сохраняет включённые модули и позиции HUD в config/vibe-client.json. */
final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path file;

    Config(Path file) {
        this.file = file;
    }

    void load(ModuleManager modules) {
        if (!Files.isRegularFile(file)) return;
        try {
            JsonObject root = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), JsonObject.class);
            for (Module module : modules.all()) {
                if (!root.has(module.id())) continue;
                JsonObject data = root.getAsJsonObject(module.id());
                if (data.has("enabled")) module.setEnabled(data.get("enabled").getAsBoolean());
                if (module instanceof HudModule hud && data.has("x") && data.has("y")) {
                    hud.setPosition(data.get("x").getAsInt(), data.get("y").getAsInt());
                }
            }
        } catch (IOException | RuntimeException e) {
            VibeClient.LOG.warn("Не удалось прочитать {}", file, e);
        }
    }

    void save(ModuleManager modules) {
        JsonObject root = new JsonObject();
        for (Module module : modules.all()) {
            JsonObject data = new JsonObject();
            data.addProperty("enabled", module.isEnabled());
            if (module instanceof HudModule hud) {
                data.addProperty("x", hud.x());
                data.addProperty("y", hud.y());
            }
            root.add(module.id(), data);
        }
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            VibeClient.LOG.warn("Не удалось сохранить {}", file, e);
        }
    }
}
