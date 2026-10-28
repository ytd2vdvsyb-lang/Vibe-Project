package vibe.launcher.core;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;

/** Игровой аккаунт. Пока только офлайн-режим (одиночная игра и сервера без лицензии). */
public record Account(String name, UUID uuid, String accessToken, String userType, String xuid) {
    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_]{3,16}");

    public static boolean isValidName(String name) {
        return name != null && VALID_NAME.matcher(name).matches();
    }

    public static Account offline(String name) {
        if (!isValidName(name)) {
            throw new IllegalArgumentException("Ник должен быть 3-16 символов: латиница, цифры и _");
        }
        // Тот же UUID, что выдаёт сервер в offline-mode, так что инвентарь на сервере сохранится.
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        return new Account(name, uuid, "0", "msa", "0");
    }
}
