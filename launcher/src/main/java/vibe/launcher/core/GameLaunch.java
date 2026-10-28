package vibe.launcher.core;

import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Полный цикл: выбрать версию, поставить Fabric, скачать файлы, запустить игру. */
public final class GameLaunch {
    public static final String LAUNCHER_NAME = "VibeLauncher";

    private final GameDirs dirs;
    private final Settings settings;
    private final Progress progress;
    private Path clientOverride;

    public GameLaunch(GameDirs dirs, Settings settings, Progress progress) {
        this.dirs = dirs;
        this.settings = settings;
        this.progress = progress;
    }

    /** Для разработки: запустить конкретный jar клиента вместо вшитого. */
    public GameLaunch clientJar(Path jar) {
        this.clientOverride = jar;
        return this;
    }

    /**
     * @param detach true, если лаунчер закроется сразу после запуска:
     *               тогда вывод игры пишется в файл, а не в окно лаунчера.
     */
    public Process start(boolean detach) throws IOException {
        Account account = Account.offline(settings.username);

        progress.status("Получаю список версий");
        Versions versions = new Versions(dirs);
        String mcVersion = Settings.LATEST.equals(settings.minecraftVersion)
                ? versions.latestRelease() : settings.minecraftVersion;
        progress.log("Minecraft " + mcVersion);

        JsonObject vanilla = versions.vanilla(mcVersion);
        progress.status("Подключаю Fabric");
        String loader = versions.fabricLoader(mcVersion);
        progress.log("Fabric Loader " + loader);
        JsonObject profile = Versions.merge(vanilla, versions.fabricProfile(mcVersion, loader));

        Installer.Installed installed = new Installer(dirs).install(profile, mcVersion, progress);
        new Mods(dirs).install(mcVersion, clientOverride, progress);

        Path java = settings.javaPath.isBlank()
                ? new JavaRuntime(dirs).ensure(vanilla, progress)
                : Path.of(settings.javaPath);

        Files.createDirectories(dirs.game);
        List<String> command = buildCommand(profile, mcVersion, installed, account, java);
        progress.log("Запуск: " + String.join(" ", redact(command, account)));
        progress.status("Запускаю игру");

        ProcessBuilder builder = new ProcessBuilder(command)
                .directory(dirs.game.toFile())
                .redirectErrorStream(true);
        if (detach) {
            Path log = dirs.game.resolve("logs").resolve("launcher-output.log");
            Files.createDirectories(log.getParent());
            builder.redirectOutput(log.toFile());
        }
        Process process = builder.start();
        if (!detach) pipeOutput(process);
        return process;
    }

    List<String> buildCommand(JsonObject profile, String mcVersion, Installer.Installed installed, Account account, Path java) {
        Map<String, String> vars = new HashMap<>();
        vars.put("auth_player_name", account.name());
        vars.put("auth_uuid", account.uuid().toString().replace("-", ""));
        vars.put("auth_access_token", account.accessToken());
        vars.put("auth_session", account.accessToken());
        vars.put("auth_xuid", account.xuid());
        vars.put("clientid", "0");
        vars.put("user_type", account.userType());
        vars.put("user_properties", "{}");
        vars.put("version_name", mcVersion);
        vars.put("version_type", "Vibe Client");
        vars.put("game_directory", dirs.game.toString());
        vars.put("assets_root", dirs.assets.toString());
        vars.put("game_assets", dirs.assets.toString());
        vars.put("assets_index_name", profile.getAsJsonObject("assetIndex").get("id").getAsString());
        vars.put("natives_directory", installed.nativesDir().toString());
        vars.put("library_directory", dirs.libraries.toString());
        vars.put("classpath_separator", Os.classpathSeparator());
        vars.put("launcher_name", LAUNCHER_NAME);
        vars.put("launcher_version", String.valueOf(GameLaunch.class.getPackage().getImplementationVersion()));
        vars.put("classpath", installed.classpath().stream().map(Path::toString)
                .distinct().collect(Collectors.joining(Os.classpathSeparator())));

        List<String> command = new ArrayList<>();
        command.add(java.toString());
        command.add("-Xmx" + settings.memoryMb + "M");
        command.add("-Xms" + Math.min(512, settings.memoryMb) + "M");
        if (!settings.extraJvmArgs.isBlank()) command.addAll(List.of(settings.extraJvmArgs.trim().split("\\s+")));
        if (installed.loggingArgument() != null) command.add(installed.loggingArgument());
        command.addAll(Arguments.jvm(profile, vars));
        command.add(profile.get("mainClass").getAsString());
        command.addAll(Arguments.game(profile, vars));
        return command;
    }

    private static List<String> redact(List<String> command, Account account) {
        if (account.accessToken().equals("0")) return command;
        return command.stream().map(arg -> arg.equals(account.accessToken()) ? "***" : arg).collect(Collectors.toList());
    }

    private void pipeOutput(Process process) {
        Thread thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) progress.log(line);
            } catch (IOException ignored) {
                // Игра закрылась.
            }
        }, "vibe-game-output");
        thread.setDaemon(true);
        thread.start();
    }
}
