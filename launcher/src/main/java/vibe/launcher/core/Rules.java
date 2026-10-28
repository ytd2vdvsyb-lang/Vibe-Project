package vibe.launcher.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.regex.Pattern;

/** Правила "rules" из JSON версии: решают, нужна ли библиотека или аргумент на этой системе. */
public final class Rules {
    private Rules() {
    }

    public static boolean allowed(JsonArray rules) {
        return allowed(rules, Os.CURRENT, Os.ARCH, System.getProperty("os.version", ""));
    }

    static boolean allowed(JsonArray rules, Os os, String arch, String osVersion) {
        if (rules == null || rules.isEmpty()) return true;
        boolean allowed = false;
        for (JsonElement element : rules) {
            JsonObject rule = element.getAsJsonObject();
            if (matches(rule, os, arch, osVersion)) {
                allowed = "allow".equals(rule.get("action").getAsString());
            }
        }
        return allowed;
    }

    private static boolean matches(JsonObject rule, Os os, String arch, String osVersion) {
        // Фичи (демо-режим, своё разрешение, quick play) лаунчер не включает.
        if (rule.has("features")) {
            for (var entry : rule.getAsJsonObject("features").entrySet()) {
                if (entry.getValue().getAsBoolean()) return false;
            }
        }
        if (rule.has("os")) {
            JsonObject osRule = rule.getAsJsonObject("os");
            if (osRule.has("name") && !osRule.get("name").getAsString().equals(os.mojangName)) return false;
            if (osRule.has("arch")) {
                String wanted = osRule.get("arch").getAsString();
                boolean is32 = arch.equals("x86");
                if (wanted.equals("x86") != is32) return false;
            }
            if (osRule.has("version")
                    && !Pattern.compile(osRule.get("version").getAsString()).matcher(osVersion).find()) return false;
        }
        return true;
    }
}
