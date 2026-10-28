package vibe.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

import java.util.Locale;
import java.util.Set;

/**
 * Решает, где можно включать боевые модули: только в одиночной игре
 * и на HvH-серверах, где читы разрешены правилами.
 */
public final class ServerGuard {
    /**
     * HvH-серверы, где читы прямо разрешены в описании (без порта, в нижнем регистре).
     * Поддомены подходят, поэтому для бесплатных хостингов пишем полный адрес сервера.
     */
    private static final Set<String> HVH_SERVERS = Set.of(
            // "Cheats are allowed, which defines the HvH format" (mc.bestgames.to/servers/1839)
            "bravohvh.fun",
            // Прислал Тимур: HvH-сервер, на нём читы часть формата.
            "metahvh.su",
            // "Our Server Allows the Following: Cheating, Modifications" (minecraftiplist.com)
            "hvhcentral.falixsrv.me",
            // "focused around HvH Hacker vs. Hacker... absolutely no anti-cheat" (minecraftiplist.com)
            "207.244.242.28",
            // "you can hack backdoor kill, grief and anything you want" (minecraftiplist.com)
            "supernova.aternos.me");

    private ServerGuard() {
    }

    public static boolean combatAllowed(Minecraft mc) {
        if (mc.hasSingleplayerServer()) return true;
        ServerData server = mc.getCurrentServer();
        return server != null && isHvh(server.ip);
    }

    static boolean isHvh(String address) {
        String host = address.trim().toLowerCase(Locale.ROOT);
        int colon = host.lastIndexOf(':');
        if (colon > 0 && host.indexOf(':') == colon) host = host.substring(0, colon);
        if (host.endsWith(".")) host = host.substring(0, host.length() - 1);
        for (String allowed : HVH_SERVERS) {
            if (host.equals(allowed) || host.endsWith("." + allowed)) return true;
        }
        return false;
    }
}
