package vibe.launcher;

import vibe.launcher.core.GameDirs;
import vibe.launcher.core.GameLaunch;
import vibe.launcher.core.Progress;
import vibe.launcher.core.Settings;
import vibe.launcher.ui.LauncherFrame;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.nio.file.Path;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws Exception {
        GameDirs dirs = GameDirs.defaultDirs();
        if (args.length > 0 && args[0].equals("--cli")) {
            runCli(dirs, args);
            return;
        }
        SwingUtilities.invokeLater(() -> {
            FlatDarkLaf.setGlobalExtraDefaults(java.util.Map.of("@accentColor", "#8B5CF6"));
            FlatDarkLaf.setup();
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            new LauncherFrame(dirs).setVisible(true);
        });
    }

    /** Запуск без окна: --cli --name Ник [--version 1.21.10] [--memory 4096] [--client путь.jar] */
    private static void runCli(GameDirs dirs, String[] args) throws Exception {
        Settings settings = Settings.load(dirs);
        Path client = null;
        for (int i = 1; i + 1 < args.length; i += 2) {
            switch (args[i]) {
                case "--name": settings.username = args[i + 1]; break;
                case "--version": settings.minecraftVersion = args[i + 1]; break;
                case "--memory": settings.memoryMb = Integer.parseInt(args[i + 1]); break;
                case "--java": settings.javaPath = args[i + 1]; break;
                case "--client": client = Path.of(args[i + 1]); break;
                default: throw new IllegalArgumentException("Неизвестный параметр " + args[i]);
            }
        }
        Progress console = new Progress() {
            @Override
            public void status(String text) {
                System.out.println("== " + text);
            }

            @Override
            public void progress(long done, long total) {
            }

            @Override
            public void log(String line) {
                System.out.println(line);
            }
        };
        Process game = new GameLaunch(dirs, settings, console).clientJar(client).start(false);
        System.exit(game.waitFor());
    }
}
