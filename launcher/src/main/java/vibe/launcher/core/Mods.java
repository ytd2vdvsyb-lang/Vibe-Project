package vibe.launcher.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Кладёт в папку mods сам Vibe Client и Fabric API, от которого он зависит. */
public final class Mods {
    static final String MODRINTH = "https://api.modrinth.com/v2";
    static final String FABRIC_API_PROJECT = "P7dR8mSH";
    /** Jar клиента, который вшивается в лаунчер при сборке. */
    static final String BUNDLED_CLIENT = "/client/vibe-client.jar";
    /** Свежий клиент из релиза nightly: лаунчер берёт его сам, переустанавливать приложение не нужно. */
    static final String UPDATE_URL = "https://github.com/ytd2vdvsyb-lang/Vibe-Project/releases/download/nightly/vibe-client.jar";

    private final Path modsDir;

    public Mods(GameDirs dirs) {
        this.modsDir = dirs.game.resolve("mods");
    }

    public void install(String mcVersion, Path clientOverride, Progress progress) throws IOException {
        Files.createDirectories(modsDir);
        installFabricApi(mcVersion, progress);
        installClient(clientOverride, progress);
    }

    private void installFabricApi(String mcVersion, Progress progress) throws IOException {
        progress.status("Проверяю Fabric API");
        String url = MODRINTH + "/project/" + FABRIC_API_PROJECT + "/version"
                + "?loaders=" + encode("[\"fabric\"]")
                + "&game_versions=" + encode("[\"" + mcVersion + "\"]");
        JsonArray versions;
        try {
            versions = Http.getJson(url).getAsJsonArray();
        } catch (IOException e) {
            if (findExisting("fabric-api") != null) {
                progress.log("Modrinth недоступен, оставляю уже скачанный Fabric API");
                return;
            }
            throw e;
        }
        if (versions.isEmpty()) {
            progress.log("Fabric API для " + mcVersion + " ещё не вышел, запускаю без него");
            return;
        }
        JsonObject file = primaryFile(versions.get(0).getAsJsonObject());
        String fileName = file.get("filename").getAsString();
        Path target = modsDir.resolve(fileName);
        Path existing = findExisting("fabric-api");
        if (existing != null && !existing.equals(target)) Files.delete(existing);
        Http.download(file.get("url").getAsString(), target,
                file.getAsJsonObject("hashes").get("sha1").getAsString(), file.get("size").getAsLong());
    }

    private void installClient(Path override, Progress progress) throws IOException {
        Path target = modsDir.resolve("vibe-client.jar");
        if (override != null) {
            Files.copy(override, target, StandardCopyOption.REPLACE_EXISTING);
            return;
        }
        try {
            // Рядом с jar лежит его sha1: файл скачивается только если клиент на GitHub поменялся.
            String sha1 = Http.getString(UPDATE_URL + ".sha1").trim().split("\\s+")[0];
            progress.status("Проверяю обновление клиента");
            Http.download(UPDATE_URL, target, sha1, -1);
            progress.log("Клиент Vibe Client: свежая сборка с GitHub (" + sha1.substring(0, 7) + ")");
            return;
        } catch (IOException | RuntimeException e) {
            progress.log("Обновление клиента недоступно (" + e.getMessage() + "), беру встроенный в лаунчер");
        }
        try (InputStream in = Mods.class.getResourceAsStream(BUNDLED_CLIENT)) {
            if (in == null) {
                progress.log("В эту сборку лаунчера клиент не вшит, запускаю чистый Fabric");
                return;
            }
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Path findExisting(String prefix) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(modsDir, prefix + "-*.jar")) {
            for (Path path : stream) return path;
        }
        return null;
    }

    private static JsonObject primaryFile(JsonObject version) {
        JsonArray files = version.getAsJsonArray("files");
        for (JsonElement element : files) {
            JsonObject file = element.getAsJsonObject();
            if (file.has("primary") && file.get("primary").getAsBoolean()) return file;
        }
        return files.get(0).getAsJsonObject();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
