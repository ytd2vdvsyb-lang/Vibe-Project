package vibe.launcher.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Скачивает всё, что нужно для запуска: jar игры, библиотеки, нативы, ассеты. */
public final class Installer {
    static final String RESOURCES_URL = "https://resources.download.minecraft.net/";
    static final String DEFAULT_MAVEN = "https://libraries.minecraft.net/";

    private final GameDirs dirs;

    public Installer(GameDirs dirs) {
        this.dirs = dirs;
    }

    public record Installed(List<Path> classpath, Path nativesDir, String loggingArgument) {
    }

    public Installed install(JsonObject version, String vanillaId, Progress progress) throws IOException {
        List<Path> classpath = new ArrayList<>();
        List<Callable<Void>> downloads = new ArrayList<>();
        List<Path> nativeJars = new ArrayList<>();

        for (JsonElement element : version.getAsJsonArray("libraries")) {
            JsonObject lib = element.getAsJsonObject();
            if (!Rules.allowed(lib.getAsJsonArray("rules"))) continue;
            String name = lib.get("name").getAsString();
            JsonObject downloadsInfo = lib.getAsJsonObject("downloads");

            if (downloadsInfo != null && downloadsInfo.has("artifact")) {
                JsonObject artifact = downloadsInfo.getAsJsonObject("artifact");
                Path file = dirs.libraries.resolve(artifact.get("path").getAsString());
                classpath.add(file);
                downloads.add(download(artifact.get("url").getAsString(), file, artifact));
            } else if (downloadsInfo == null) {
                // Формат Fabric: только name + url репозитория.
                String base = lib.has("url") ? lib.get("url").getAsString() : DEFAULT_MAVEN;
                if (!base.endsWith("/")) base += "/";
                String path = Maven.path(name);
                Path file = dirs.libraries.resolve(path);
                classpath.add(file);
                downloads.add(download(base + path, file, lib));
            }

            // Старые версии: нативы лежат отдельным classifier и распаковываются.
            if (lib.has("natives") && downloadsInfo != null && downloadsInfo.has("classifiers")) {
                JsonElement classifierName = lib.getAsJsonObject("natives").get(Os.CURRENT.mojangName);
                if (classifierName != null) {
                    String classifier = classifierName.getAsString()
                            .replace("${arch}", Os.ARCH.equals("x86") ? "32" : "64");
                    JsonObject nativeArtifact = downloadsInfo.getAsJsonObject("classifiers").getAsJsonObject(classifier);
                    if (nativeArtifact != null) {
                        Path file = dirs.libraries.resolve(nativeArtifact.get("path").getAsString());
                        nativeJars.add(file);
                        downloads.add(download(nativeArtifact.get("url").getAsString(), file, nativeArtifact));
                    }
                }
            }
        }

        JsonObject client = version.getAsJsonObject("downloads").getAsJsonObject("client");
        Path clientJar = dirs.versions.resolve(vanillaId).resolve(vanillaId + ".jar");
        downloads.add(download(client.get("url").getAsString(), clientJar, client));
        classpath.add(clientJar);

        String loggingArgument = null;
        if (version.has("logging") && version.getAsJsonObject("logging").has("client")) {
            JsonObject logging = version.getAsJsonObject("logging").getAsJsonObject("client");
            JsonObject file = logging.getAsJsonObject("file");
            Path config = dirs.assets.resolve("log_configs").resolve(file.get("id").getAsString());
            downloads.add(download(file.get("url").getAsString(), config, file));
            loggingArgument = logging.get("argument").getAsString().replace("${path}", config.toString());
        }

        progress.status("Скачиваю Minecraft " + vanillaId + " и библиотеки");
        Parallel.run(downloads, progress);

        Path nativesDir = dirs.versions.resolve(vanillaId).resolve("natives");
        Files.createDirectories(nativesDir);
        for (Path jar : nativeJars) extractNatives(jar, nativesDir);

        installAssets(version, progress);
        return new Installed(classpath, nativesDir, loggingArgument);
    }

    private void installAssets(JsonObject version, Progress progress) throws IOException {
        JsonObject index = version.getAsJsonObject("assetIndex");
        String id = index.get("id").getAsString();
        Path indexFile = dirs.assets.resolve("indexes").resolve(id + ".json");
        Http.download(index.get("url").getAsString(), indexFile, index.get("sha1").getAsString(), index.get("size").getAsLong());

        JsonObject objects = JsonParser.parseString(Files.readString(indexFile)).getAsJsonObject().getAsJsonObject("objects");
        List<Callable<Void>> downloads = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : objects.entrySet()) {
            JsonObject object = entry.getValue().getAsJsonObject();
            String hash = object.get("hash").getAsString();
            String path = hash.substring(0, 2) + "/" + hash;
            Path file = dirs.assets.resolve("objects").resolve(path);
            long size = object.get("size").getAsLong();
            // Хеш проверяется при загрузке; для уже скачанных хватает размера, иначе запуск долгий.
            downloads.add(() -> {
                if (!Files.isRegularFile(file) || Files.size(file) != size) Http.download(RESOURCES_URL + path, file, hash, size);
                return null;
            });
        }
        progress.status("Скачиваю ресурсы игры");
        Parallel.run(downloads, progress);
    }

    private static Callable<Void> download(String url, Path file, JsonObject info) {
        String sha1 = info.has("sha1") ? info.get("sha1").getAsString() : null;
        long size = info.has("size") ? info.get("size").getAsLong() : -1;
        return () -> {
            Http.download(url, file, sha1, size);
            return null;
        };
    }

    private static void extractNatives(Path jar, Path target) throws IOException {
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            for (ZipEntry entry : java.util.Collections.list(zip.entries())) {
                if (entry.isDirectory() || entry.getName().startsWith("META-INF")) continue;
                Path out = target.resolve(entry.getName()).normalize();
                if (!out.startsWith(target)) continue;
                Files.createDirectories(out.getParent());
                try (InputStream in = zip.getInputStream(entry)) {
                    Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}
