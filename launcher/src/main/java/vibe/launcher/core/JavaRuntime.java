package vibe.launcher.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/** Скачивает Java той версии, которую требует Minecraft (официальные сборки Mojang). */
public final class JavaRuntime {
    static final String RUNTIMES_URL =
            "https://launchermeta.mojang.com/v1/products/java-runtime/2ec0cc96c44e5a76b9c8b7c39df7210883d12871/all.json";

    private final GameDirs dirs;

    public JavaRuntime(GameDirs dirs) {
        this.dirs = dirs;
    }

    /** Возвращает путь к java, установив её при необходимости. */
    public Path ensure(JsonObject version, Progress progress) throws IOException {
        String component = version.has("javaVersion")
                ? version.getAsJsonObject("javaVersion").get("component").getAsString()
                : "jre-legacy";
        Path home = dirs.runtimes.resolve(component);
        Path java = javaExecutable(home);

        Path marker = home.resolve(".installed");
        JsonObject all;
        try {
            all = Http.getJson(RUNTIMES_URL).getAsJsonObject();
        } catch (IOException e) {
            if (Files.isRegularFile(java)) return java;
            throw e;
        }
        JsonObject platform = all.getAsJsonObject(Os.javaRuntimePlatform());
        JsonArray candidates = platform == null ? null : platform.getAsJsonArray(component);
        if (candidates == null || candidates.isEmpty()) {
            if (Files.isRegularFile(java)) return java;
            throw new IOException("Mojang не выдаёт Java " + component + " для " + Os.javaRuntimePlatform()
                    + ". Укажи путь к Java в настройках.");
        }
        JsonObject manifestRef = candidates.get(0).getAsJsonObject().getAsJsonObject("manifest");
        String manifestSha1 = manifestRef.get("sha1").getAsString();
        if (Files.isRegularFile(java) && Files.isRegularFile(marker)
                && Files.readString(marker).trim().equals(manifestSha1)) {
            return java;
        }
        Path manifestFile = dirs.runtimes.resolve(component + ".json");
        Http.download(manifestRef.get("url").getAsString(), manifestFile,
                manifestSha1, manifestRef.get("size").getAsLong());
        JsonObject files = JsonParser.parseString(Files.readString(manifestFile))
                .getAsJsonObject().getAsJsonObject("files");

        progress.status("Устанавливаю Java (" + component + ")");
        List<Callable<Void>> tasks = new ArrayList<>();
        List<Map.Entry<String, JsonElement>> links = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : files.entrySet()) {
            JsonObject file = entry.getValue().getAsJsonObject();
            Path target = home.resolve(entry.getKey()).normalize();
            if (!target.startsWith(home)) throw new IOException("Подозрительный путь в манифесте Java: " + entry.getKey());
            switch (file.get("type").getAsString()) {
                case "directory":
                    Files.createDirectories(target);
                    break;
                case "link":
                    links.add(entry);
                    break;
                case "file": {
                    JsonObject raw = file.getAsJsonObject("downloads").getAsJsonObject("raw");
                    boolean executable = file.has("executable") && file.get("executable").getAsBoolean();
                    tasks.add(() -> {
                        Http.download(raw.get("url").getAsString(), target, raw.get("sha1").getAsString(), raw.get("size").getAsLong());
                        if (executable) target.toFile().setExecutable(true);
                        return null;
                    });
                    break;
                }
                default:
                    break;
            }
        }
        Parallel.run(tasks, progress);

        if (Os.CURRENT != Os.WINDOWS) {
            for (Map.Entry<String, JsonElement> entry : links) {
                Path link = home.resolve(entry.getKey());
                if (Files.exists(link, LinkOption.NOFOLLOW_LINKS)) continue;
                Files.createDirectories(link.getParent());
                Files.createSymbolicLink(link, Path.of(entry.getValue().getAsJsonObject().get("target").getAsString()));
            }
        }
        if (!Files.isRegularFile(java)) throw new IOException("После установки не найден " + java);
        Files.writeString(marker, manifestSha1);
        return java;
    }

    static Path javaExecutable(Path home) {
        switch (Os.CURRENT) {
            case WINDOWS:
                return home.resolve("bin").resolve("javaw.exe");
            case OSX:
                return home.resolve("jre.bundle/Contents/Home/bin/java");
            default:
                return home.resolve("bin").resolve("java");
        }
    }
}
