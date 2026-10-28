package vibe.launcher.core;

import java.nio.file.Path;
import java.nio.file.Paths;

/** Папки лаунчера. Всё хранится отдельно от обычного .minecraft. */
public final class GameDirs {
    public final Path root;
    public final Path versions;
    public final Path libraries;
    public final Path assets;
    public final Path runtimes;
    public final Path game;

    public GameDirs(Path root) {
        this.root = root;
        this.versions = root.resolve("versions");
        this.libraries = root.resolve("libraries");
        this.assets = root.resolve("assets");
        this.runtimes = root.resolve("runtime");
        this.game = root.resolve("game");
    }

    public static GameDirs defaultDirs() {
        String override = System.getProperty("vibe.home");
        if (override != null && !override.isBlank()) return new GameDirs(Paths.get(override));
        String home = System.getProperty("user.home");
        switch (Os.CURRENT) {
            case WINDOWS: {
                String appData = System.getenv("APPDATA");
                return new GameDirs(Paths.get(appData != null ? appData : home, ".vibeclient"));
            }
            case OSX:
                return new GameDirs(Paths.get(home, "Library", "Application Support", "vibeclient"));
            default:
                return new GameDirs(Paths.get(home, ".vibeclient"));
        }
    }
}
