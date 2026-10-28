package vibe.launcher.core;

import java.util.Locale;

/** Текущая ОС и архитектура в терминах, которые используют манифесты Mojang. */
public enum Os {
    WINDOWS("windows"),
    OSX("osx"),
    LINUX("linux");

    public final String mojangName;

    Os(String mojangName) {
        this.mojangName = mojangName;
    }

    public static final Os CURRENT = detect();
    public static final String ARCH = detectArch();

    private static Os detect() {
        String name = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (name.contains("win")) return WINDOWS;
        if (name.contains("mac") || name.contains("darwin")) return OSX;
        return LINUX;
    }

    private static String detectArch() {
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        if (arch.equals("aarch64") || arch.equals("arm64")) return "arm64";
        if (arch.contains("64")) return "x64";
        return "x86";
    }

    /** Ключ платформы в манифесте Java-рантаймов Mojang. */
    public static String javaRuntimePlatform() {
        switch (CURRENT) {
            case WINDOWS:
                return ARCH.equals("arm64") ? "windows-arm64" : ARCH.equals("x86") ? "windows-x86" : "windows-x64";
            case OSX:
                return ARCH.equals("arm64") ? "mac-os-arm64" : "mac-os";
            default:
                return ARCH.equals("x86") ? "linux-i386" : "linux";
        }
    }

    public static String classpathSeparator() {
        return CURRENT == WINDOWS ? ";" : ":";
    }
}
