package vibe.launcher.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreTest {
    private static JsonArray array(String json) {
        return JsonParser.parseString(json).getAsJsonArray();
    }

    private static JsonObject object(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    @Test
    void rulesFollowOsAndFeatures() {
        JsonArray onlyOsx = array("[{\"action\":\"allow\",\"os\":{\"name\":\"osx\"}}]");
        assertTrue(Rules.allowed(onlyOsx, Os.OSX, "arm64", "14.0"));
        assertFalse(Rules.allowed(onlyOsx, Os.WINDOWS, "x64", "10.0"));

        JsonArray allButOsx = array("[{\"action\":\"allow\"},{\"action\":\"disallow\",\"os\":{\"name\":\"osx\"}}]");
        assertTrue(Rules.allowed(allButOsx, Os.LINUX, "x64", "6.0"));
        assertFalse(Rules.allowed(allButOsx, Os.OSX, "x64", "14.0"));

        assertFalse(Rules.allowed(array("[{\"action\":\"allow\",\"features\":{\"is_demo_user\":true}}]"), Os.LINUX, "x64", ""));
        assertTrue(Rules.allowed(null, Os.LINUX, "x64", ""));
        assertTrue(Rules.allowed(array("[{\"action\":\"allow\",\"os\":{\"arch\":\"x86\"}}]"), Os.WINDOWS, "x86", ""));
        assertFalse(Rules.allowed(array("[{\"action\":\"allow\",\"os\":{\"arch\":\"x86\"}}]"), Os.WINDOWS, "x64", ""));
    }

    @Test
    void mavenPaths() {
        assertEquals("net/fabricmc/fabric-loader/0.19.3/fabric-loader-0.19.3.jar", Maven.path("net.fabricmc:fabric-loader:0.19.3"));
        assertEquals("org/lwjgl/lwjgl/3.3.3/lwjgl-3.3.3-natives-linux.jar", Maven.path("org.lwjgl:lwjgl:3.3.3:natives-linux"));
        assertEquals("a/b/1/b-1.zip", Maven.path("a:b:1@zip"));
        assertEquals("org.ow2.asm:asm", Maven.key("org.ow2.asm:asm:9.8"));
    }

    @Test
    void argumentsAreExpanded() {
        JsonArray args = array("[\"--username\",\"${auth_player_name}\","
                + "{\"rules\":[{\"action\":\"allow\",\"features\":{\"has_custom_resolution\":true}}],\"value\":[\"--width\",\"${resolution_width}\"]},"
                + "\"-Dpath=${natives_directory}/x\"]");
        List<String> result = Arguments.expand(args, Map.of("auth_player_name", "Steve", "natives_directory", "C:\\n$1"));
        assertEquals(List.of("--username", "Steve", "-Dpath=C:\\n$1/x"), result);
    }

    @Test
    void fabricOverridesVanilla() {
        JsonObject vanilla = object("{\"id\":\"26.2\",\"mainClass\":\"net.minecraft.client.main.Main\","
                + "\"libraries\":[{\"name\":\"org.ow2.asm:asm:9.1\"},{\"name\":\"org.lwjgl:lwjgl:3.3.3\"},"
                + "{\"name\":\"org.lwjgl:lwjgl:3.3.3:natives-linux\",\"rules\":[{\"action\":\"allow\"}]}],"
                + "\"arguments\":{\"game\":[\"--a\"],\"jvm\":[\"-cp\",\"${classpath}\"]}}");
        JsonObject fabric = object("{\"id\":\"fabric-loader-0.19.3-26.2\",\"inheritsFrom\":\"26.2\","
                + "\"mainClass\":\"net.fabricmc.loader.impl.launch.knot.KnotClient\","
                + "\"libraries\":[{\"name\":\"org.ow2.asm:asm:9.8\",\"url\":\"https://maven.fabricmc.net/\"}],"
                + "\"arguments\":{\"game\":[],\"jvm\":[\"-DFabricMcEmu= net.minecraft.client.main.Main \"]}}");
        JsonObject merged = Versions.merge(vanilla, fabric);
        assertEquals("net.fabricmc.loader.impl.launch.knot.KnotClient", merged.get("mainClass").getAsString());
        JsonArray libs = merged.getAsJsonArray("libraries");
        assertEquals(3, libs.size());
        assertEquals("org.ow2.asm:asm:9.8", libs.get(0).getAsJsonObject().get("name").getAsString());
        assertEquals(3, merged.getAsJsonObject("arguments").getAsJsonArray("jvm").size());
        assertFalse(merged.has("inheritsFrom"));
    }

    @Test
    void offlineAccountMatchesServerUuid() {
        assertEquals("b50ad385-829d-3141-a216-7e7d7539ba7f", Account.offline("Notch").uuid().toString());
        assertThrows(IllegalArgumentException.class, () -> Account.offline("ник с пробелом"));
    }
}
