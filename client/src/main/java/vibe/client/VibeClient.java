package vibe.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vibe.client.module.ModuleManager;

public final class VibeClient implements ClientModInitializer {
    public static final String NAME = "Vibe Client";
    public static final Logger LOG = LoggerFactory.getLogger(NAME);

    private static VibeClient instance;
    private ModuleManager modules;
    private Config config;

    public static VibeClient get() {
        return instance;
    }

    public static String version() {
        return FabricLoader.getInstance().getModContainer("vibe-client")
                .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                .orElse("dev");
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        modules = new ModuleManager();
        config = new Config(FabricLoader.getInstance().getConfigDir().resolve("vibe-client.json"));
        config.load(modules);

        ClientTickEvents.END_CLIENT_TICK.register(client -> modules.tick(client));
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> saveConfig());
        LOG.info("{} {} загружен", NAME, version());
    }

    public ModuleManager modules() {
        return modules;
    }

    public void saveConfig() {
        config.save(modules);
    }
}
