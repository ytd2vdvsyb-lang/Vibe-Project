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
    /** Адреса HvH-серверов (без порта, в нижнем регистре). Поддомены тоже подходят. */
    private static final Set<String> HVH_SERVERS = Set.of();

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
