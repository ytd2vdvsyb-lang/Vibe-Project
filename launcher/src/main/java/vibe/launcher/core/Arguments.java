package vibe.launcher.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Разворачивает аргументы запуска из JSON версии с подстановкой ${переменных}. */
public final class Arguments {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([a-zA-Z0-9_]+)}");

    private Arguments() {
    }

    public static List<String> jvm(JsonObject version, Map<String, String> vars) {
        if (version.has("arguments") && version.getAsJsonObject("arguments").has("jvm")) {
            return expand(version.getAsJsonObject("arguments").getAsJsonArray("jvm"), vars);
        }
        // Старые версии без arguments.jvm.
        return substitute(List.of("-Djava.library.path=${natives_directory}", "-cp", "${classpath}"), vars);
    }

    public static List<String> game(JsonObject version, Map<String, String> vars) {
        if (version.has("arguments") && version.getAsJsonObject("arguments").has("game")) {
            return expand(version.getAsJsonObject("arguments").getAsJsonArray("game"), vars);
        }
        String legacy = version.has("minecraftArguments") ? version.get("minecraftArguments").getAsString() : "";
        return substitute(Arrays.asList(legacy.trim().split("\\s+")), vars);
    }

    static List<String> expand(JsonArray args, Map<String, String> vars) {
        List<String> raw = new ArrayList<>();
        for (JsonElement element : args) {
            if (element.isJsonPrimitive()) {
                raw.add(element.getAsString());
                continue;
            }
            JsonObject conditional = element.getAsJsonObject();
            if (!Rules.allowed(conditional.getAsJsonArray("rules"))) continue;
            JsonElement value = conditional.get("value");
            if (value.isJsonArray()) {
                value.getAsJsonArray().forEach(v -> raw.add(v.getAsString()));
            } else {
                raw.add(value.getAsString());
            }
        }
        return substitute(raw, vars);
    }

    static List<String> substitute(List<String> raw, Map<String, String> vars) {
        List<String> result = new ArrayList<>(raw.size());
        for (String arg : raw) {
            if (arg.isEmpty()) continue;
            Matcher m = PLACEHOLDER.matcher(arg);
            StringBuilder sb = new StringBuilder();
            while (m.find()) {
                String replacement = vars.getOrDefault(m.group(1), m.group(0));
                m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            m.appendTail(sb);
            result.add(sb.toString());
        }
        return result;
    }
}
