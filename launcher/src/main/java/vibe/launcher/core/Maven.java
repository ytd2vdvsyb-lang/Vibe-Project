package vibe.launcher.core;

/** Преобразует maven-координаты "group:artifact:version[:classifier][@ext]" в путь к файлу. */
public final class Maven {
    private Maven() {
    }

    public static String path(String coordinates) {
        String ext = "jar";
        int at = coordinates.indexOf('@');
        if (at >= 0) {
            ext = coordinates.substring(at + 1);
            coordinates = coordinates.substring(0, at);
        }
        String[] parts = coordinates.split(":");
        if (parts.length < 3) throw new IllegalArgumentException("Bad maven coordinates: " + coordinates);
        String group = parts[0].replace('.', '/');
        String artifact = parts[1];
        String version = parts[2];
        String file = artifact + "-" + version + (parts.length > 3 ? "-" + parts[3] : "") + "." + ext;
        return group + "/" + artifact + "/" + version + "/" + file;
    }

    /** Ключ для удаления дублей: group:artifact[:classifier] без версии. */
    public static String key(String coordinates) {
        String[] parts = coordinates.split("@")[0].split(":");
        return parts[0] + ":" + parts[1] + (parts.length > 3 ? ":" + parts[3] : "");
    }
}
