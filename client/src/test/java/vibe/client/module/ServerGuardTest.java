package vibe.client.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerGuardTest {
    @Test
    void matchesWhitelistedHostsOnly() {
        assertTrue(ServerGuard.isHvh("eu.bravohvh.fun"));
        assertTrue(ServerGuard.isHvh("BravoHvH.fun:25565"));
        assertTrue(ServerGuard.isHvh("supernova.aternos.me:56960"));
        assertFalse(ServerGuard.isHvh("aternos.me"));
        assertFalse(ServerGuard.isHvh("other.aternos.me"));
        assertFalse(ServerGuard.isHvh("notbravohvh.fun"));
        assertFalse(ServerGuard.isHvh("mc.hypixel.net"));
        assertFalse(ServerGuard.isHvh("falixsrv.me"));
    }
}
