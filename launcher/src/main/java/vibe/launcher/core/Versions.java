package vibe.launcher.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Список версий Minecraft и сборка итогового профиля "ванилла + Fabric". */
public final class Versions {
    static final String MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    static final String FABRIC_META = "https://meta.fabricmc.net/v2";

    private final GameDirs dirs;
    private JsonObject manifest;

    public Versions(GameDirs dirs) {
        this.dirs = dirs;
    }

    public record Release(String id, String url, String sha1) {
    }

    public JsonObject manifest() throws IOException {
        if (manifest == null) {
            Path cached = dirs.versions.resolve("version_manifest_v2.json");
            try {
                String text = Http.getString(MANIFEST_URL);
                Files.createDirectories(cached.getParent());
                Files.writeString(cached, text, StandardCharsets.UTF_8);
                manifest = JsonParser.parseString(text).getAsJsonObject();
            } catch (IOException e) {
                // Без интернета запускаем то, что уже скачано.
                if (!Files.isRegularFile(cached)) throw e;
                manifest = JsonParser.parseString(Files.readString(cached)).getAsJsonObject();
            }
        }
        return manifest;
    }

    public String latestRelease() throws IOException {
        return manifest().getAsJsonObject("latest").get("release").getAsString();
    }

    public List<Release> releases() throws IOException {
        List<Release> result = new ArrayList<>();
        for (JsonElement element : manifest().getAsJsonArray("versions")) {
            JsonObject v = element.getAsJsonObject();
            if ("release".equals(v.get("type").getAsString())) {
                result.add(new Release(v.get("id").getAsString(), v.get("url").getAsString(), v.get("sha1").getAsString()));
            }
        }
        return result;
    }

    public JsonObject vanilla(String id) throws IOException {
        Path file = dirs.versions.resolve(id).resolve(id + ".json");
        for (JsonElement element : manifest().getAsJsonArray("versions")) {
            JsonObject v = element.getAsJsonObject();
            if (v.get("id").getAsString().equals(id)) {
                Http.download(v.get("url").getAsString(), file, v.get("sha1").getAsString(), -1);
                return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            }
        }
        throw new IOException("Версия Minecraft " + id + " не найдена");
    }

    /** Последний стабильный Fabric Loader для этой версии игры. */
    public String fabricLoader(String mcVersion) throws IOException {
        JsonArray loaders = Http.getJson(FABRIC_META + "/versions/loader/" + mcVersion).getAsJsonArray();
        if (loaders.isEmpty()) throw new IOException("Fabric пока не поддерживает Minecraft " + mcVersion);
        String fallback = null;
        for (JsonElement element : loaders) {
            JsonObject loader = element.getAsJsonObject().getAsJsonObject("loader");
            if (fallback == null) fallback = loader.get("version").getAsString();
            if (loader.has("stable") && loader.get("stable").getAsBoolean()) return loader.get("version").getAsString();
        }
        return fallback;
    }

    public JsonObject fabricProfile(String mcVersion, String loader) throws IOException {
        String id = "fabric-loader-" + loader + "-" + mcVersion;
        Path file = dirs.versions.resolve(id).resolve(id + ".json");
        try {
            String text = Http.getString(FABRIC_META + "/versions/loader/" + mcVersion + "/" + loader + "/profile/json");
            Files.createDirectories(file.getParent());
            Files.writeString(file, text, StandardCharsets.UTF_8);
        } catch (IOException e) {
            if (!Files.isRegularFile(file)) throw e;
        }
        return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
    }

    /**
     * Накладывает дочерний профиль (Fabric) на родительский (ванилла):
     * библиотеки дочернего идут первыми и вытесняют одноимённые, аргументы складываются.
     */
    public static JsonObject merge(JsonObject parent, JsonObject child) {
        JsonObject result = parent.deepCopy();
        for (Map.Entry<String, JsonElement> entry : child.entrySet()) {
            String key = entry.getKey();
            if (key.equals("libraries") || key.equals("arguments") || key.equals("inheritsFrom")) continue;
            result.add(key, entry.getValue().deepCopy());
        }

        Map<String, JsonElement> libraries = new LinkedHashMap<>();
        for (JsonArray source : new JsonArray[]{child.getAsJsonArray("libraries"), parent.getAsJsonArray("libraries")}) {
            if (source == null) continue;
            for (JsonElement lib : source) {
                String name = lib.getAsJsonObject().get("name").getAsString();
                String key = Maven.key(name);
                // У нативных библиотек одинаковый ключ с разными правилами, их не схлопываем.
                if (lib.getAsJsonObject().has("rules")) key = name + "#" + libraries.size();
                libraries.putIfAbsent(key, lib.deepCopy());
            }
        }
        JsonArray mergedLibs = new JsonArray();
        libraries.values().forEach(mergedLibs::add);
        result.add("libraries", mergedLibs);

        if (child.has("arguments")) {
            JsonObject args = result.has("arguments") ? result.getAsJsonObject("arguments") : new JsonObject();
            for (String kind : new String[]{"game", "jvm"}) {
                JsonArray merged = args.has(kind) ? args.getAsJsonArray(kind) : new JsonArray();
                JsonArray extra = child.getAsJsonObject("arguments").getAsJsonArray(kind);
                if (extra != null) merged.addAll(extra.deepCopy());
                args.add(kind, merged);
            }
            result.add("arguments", args);
        }
        return result;
    }
}
