package vibe.client.module.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import vibe.client.VibeClient;
import vibe.client.gui.Draw;
import vibe.client.gui.Theme;
import vibe.client.module.HudModule;
import vibe.client.module.Module;

import java.util.Comparator;
import java.util.List;

/** Список включённых модулей, длинные сверху, прижат к правому краю блока. */
public final class ModuleListHud extends HudModule {
    private static final int LINE = 12;

    public ModuleListHud() {
        super("module_list", "Список модулей", "Какие модули включены", false, 300, 4);
    }

    private List<String> names(Minecraft mc) {
        return VibeClient.get().modules().all().stream()
                .filter(m -> m.isEnabled() && !(m instanceof HudModule) && !(m instanceof ThemeModule))
                .map(Module::name)
                .sorted(Comparator.comparingInt((String n) -> -mc.font.width(n)))
                .toList();
    }

    @Override
    public int width(Minecraft mc) {
        int max = mc.font.width(name());
        for (String n : names(mc)) max = Math.max(max, mc.font.width(n));
        return max + PADDING * 2 + 2;
    }

    @Override
    public int height(Minecraft mc) {
        return Math.max(1, names(mc).size()) * LINE;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Minecraft mc) {
        int right = x() + width(mc);
        int lineY = y();
        int i = 0;
        for (String n : names(mc)) {
            int w = mc.font.width(n) + PADDING * 2 + 2;
            int color = Theme.accent(i++);
            Draw.round(g, right - w, lineY, w, LINE, 3, BACKGROUND);
            g.fill(right - 2, lineY + 2, right - 1, lineY + LINE - 2, color);
            g.text(mc.font, n, right - w + PADDING, lineY + 2, color, true);
            lineY += LINE;
        }
    }
}
