package vibe.client.module.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import vibe.client.VibeClient;
import vibe.client.module.HudModule;
import vibe.client.module.Module;

import java.util.Comparator;
import java.util.List;

/** Список включённых модулей, длинные сверху. */
public final class ModuleListHud extends HudModule {
    public ModuleListHud() {
        super("module_list", "Список модулей", "Какие модули включены", false, 300, 4);
    }

    private List<String> names(Minecraft mc) {
        return VibeClient.get().modules().all().stream()
                .filter(m -> m.isEnabled() && !(m instanceof HudModule))
                .map(Module::name)
                .sorted(Comparator.comparingInt((String n) -> -mc.font.width(n)))
                .toList();
    }

    @Override
    public int width(Minecraft mc) {
        int max = mc.font.width(name());
        for (String n : names(mc)) max = Math.max(max, mc.font.width(n));
        return max + PADDING * 2;
    }

    @Override
    public int height(Minecraft mc) {
        return Math.max(1, names(mc).size()) * (mc.font.lineHeight + 3);
    }

    @Override
    public void render(GuiGraphicsExtractor g, Minecraft mc) {
        int right = x() + width(mc);
        int lineY = y();
        for (String n : names(mc)) {
            int w = mc.font.width(n) + PADDING * 2;
            g.fill(right - w, lineY, right, lineY + mc.font.lineHeight + 3, BACKGROUND);
            g.fill(right - 1, lineY, right, lineY + mc.font.lineHeight + 3, ACCENT);
            g.text(mc.font, Component.literal(n), right - w + PADDING, lineY + 2, TEXT, true);
            lineY += mc.font.lineHeight + 3;
        }
    }
}
