package vibe.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import vibe.client.VibeClient;
import vibe.client.module.Category;
import vibe.client.module.CheatModule;
import vibe.client.module.HudModule;
import vibe.client.module.Module;
import vibe.client.setting.BoolSetting;
import vibe.client.setting.ModeSetting;
import vibe.client.setting.NumberSetting;
import vibe.client.setting.Setting;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ClickGUI: по панели на категорию. ЛКМ по модулю включает его, ПКМ раскрывает настройки.
 * Блоки HUD вне панелей можно перетаскивать мышью.
 */
public final class VibeMenuScreen extends Screen {
    private static final int PANEL_WIDTH = 116;
    private static final int HEADER = 16;
    private static final int ROW = 14;
    private static final int SETTING_ROW = 12;

    private static final int SHADE = 0x60000000;
    private static final int HEADER_COLOR = 0xF0261A3D;
    private static final int BODY = 0xE0101018;
    private static final int ROW_ON = 0xC07C3AED;
    private static final int HOVER = 0x30FFFFFF;
    private static final int MUTED = 0xFF8A8A9A;
    private static final int SLIDER = 0xFF8B5CF6;
    private static final int LOCKED = 0xFFF87171;

    /** Позиции панелей живут, пока открыт клиент: меню открывается там же, где закрыли. */
    private static final Map<Category, int[]> PANELS = new EnumMap<>(Category.class);
    private static final Set<Module> EXPANDED = new HashSet<>();

    private Category draggingPanel;
    private HudModule draggingHud;
    private NumberSetting draggingSlider;
    private int sliderLeft;
    private double dragOffsetX;
    private double dragOffsetY;
    private Module binding;

    public VibeMenuScreen() {
        super(Component.literal(VibeClient.NAME));
    }

    @Override
    protected void init() {
        Category[] categories = Category.values();
        for (int i = 0; i < categories.length; i++) {
            int x = 10 + i * (PANEL_WIDTH + 8);
            PANELS.computeIfAbsent(categories[i], c -> new int[]{x, 30});
        }
    }

    private List<Module> modules(Category category) {
        return VibeClient.get().modules().all().stream().filter(m -> m.category() == category).toList();
    }

    // ---------- отрисовка ----------

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        Minecraft mc = Minecraft.getInstance();
        g.fill(0, 0, width, height, SHADE);

        for (Module module : VibeClient.get().modules().all()) {
            if (module instanceof HudModule hud && hud.isEnabled() && mc.player != null) {
                hud.render(g, mc);
                if (hud == draggingHud || hud.contains(mouseX, mouseY, mc)) {
                    outline(g, hud.x() - 1, hud.y() - 1, hud.width(mc) + 2, hud.height(mc) + 2, HudModule.ACCENT);
                }
            }
        }

        Module hovered = null;
        for (Category category : Category.values()) {
            Module h = renderPanel(g, category, mouseX, mouseY);
            if (h != null) hovered = h;
        }

        g.text(font, VibeClient.NAME + " " + VibeClient.version(), 6, 8, HudModule.ACCENT, true);
        String hint = binding != null
                ? "Нажми клавишу для " + binding.name() + " (Esc или Delete — убрать)"
                : hovered != null ? hovered.description()
                : "ЛКМ — вкл/выкл, ПКМ — настройки, блоки HUD можно двигать мышью";
        g.text(font, hint, 6, height - 14, binding != null ? HudModule.ACCENT : HudModule.TEXT, true);
        super.extractRenderState(g, mouseX, mouseY, delta);
    }

    /** Рисует панель категории и возвращает модуль под курсором. */
    private Module renderPanel(GuiGraphicsExtractor g, Category category, int mouseX, int mouseY) {
        int[] pos = PANELS.get(category);
        int x = pos[0], y = pos[1];
        Module hovered = null;
        g.fill(x, y, x + PANEL_WIDTH, y + HEADER, HEADER_COLOR);
        g.fill(x, y + HEADER - 1, x + PANEL_WIDTH, y + HEADER, SLIDER);
        g.text(font, category.title(), x + 5, y + 4, HudModule.TEXT, true);
        y += HEADER;

        Minecraft mc = Minecraft.getInstance();
        for (Module module : modules(category)) {
            boolean hover = inside(mouseX, mouseY, x, y, PANEL_WIDTH, ROW);
            if (hover) hovered = module;
            g.fill(x, y, x + PANEL_WIDTH, y + ROW, module.isEnabled() ? ROW_ON : BODY);
            if (hover) g.fill(x, y, x + PANEL_WIDTH, y + ROW, HOVER);
            boolean locked = module instanceof CheatModule && mc.player != null && !CheatModule.allowed(mc);
            g.text(font, module.name(), x + 5, y + 3, locked ? LOCKED : HudModule.TEXT, true);
            g.text(font, EXPANDED.contains(module) ? "-" : "+", x + PANEL_WIDTH - 9, y + 3, MUTED, false);
            y += ROW;
            if (EXPANDED.contains(module)) y = renderSettings(g, module, x, y, mouseX, mouseY);
        }
        g.fill(x, y, x + PANEL_WIDTH, y + 2, HEADER_COLOR);
        return hovered;
    }

    private int renderSettings(GuiGraphicsExtractor g, Module module, int x, int y, int mouseX, int mouseY) {
        for (Setting<?> setting : module.settings()) {
            g.fill(x, y, x + PANEL_WIDTH, y + SETTING_ROW, BODY);
            if (setting instanceof NumberSetting number) {
                int filled = (int) ((PANEL_WIDTH - 12) * number.fraction());
                g.fill(x + 6, y + SETTING_ROW - 2, x + PANEL_WIDTH - 6, y + SETTING_ROW - 1, 0xFF2A2A3A);
                g.fill(x + 6, y + SETTING_ROW - 2, x + 6 + filled, y + SETTING_ROW - 1, SLIDER);
            }
            if (inside(mouseX, mouseY, x, y, PANEL_WIDTH, SETTING_ROW)) g.fill(x, y, x + PANEL_WIDTH, y + SETTING_ROW, HOVER);
            g.text(font, setting.name(), x + 8, y + 2, MUTED, false);
            String value = setting.display();
            int color = setting instanceof BoolSetting b ? (b.on() ? 0xFF4ADE80 : MUTED) : HudModule.TEXT;
            g.text(font, value, x + PANEL_WIDTH - 5 - font.width(value), y + 2, color, false);
            y += SETTING_ROW;
        }
        g.fill(x, y, x + PANEL_WIDTH, y + SETTING_ROW, BODY);
        if (inside(mouseX, mouseY, x, y, PANEL_WIDTH, SETTING_ROW)) g.fill(x, y, x + PANEL_WIDTH, y + SETTING_ROW, HOVER);
        g.text(font, "Клавиша", x + 8, y + 2, MUTED, false);
        String key = binding == module ? "..." : Keys.name(module.key());
        g.text(font, key, x + PANEL_WIDTH - 5 - font.width(key), y + 2, HudModule.TEXT, false);
        return y + SETTING_ROW;
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && my >= y && mx < x + w && my < y + h;
    }

    private static void outline(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    // ---------- мышь ----------

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x(), my = event.y();
        int button = event.button();
        binding = null;
        for (Category category : Category.values()) {
            if (clickPanel(category, mx, my, button)) return true;
        }
        Minecraft mc = Minecraft.getInstance();
        if (button == 0) {
            for (Module module : VibeClient.get().modules().all()) {
                if (module instanceof HudModule hud && hud.isEnabled() && hud.contains(mx, my, mc)) {
                    draggingHud = hud;
                    dragOffsetX = mx - hud.x();
                    dragOffsetY = my - hud.y();
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean clickPanel(Category category, double mx, double my, int button) {
        int[] pos = PANELS.get(category);
        int x = pos[0], y = pos[1];
        if (inside(mx, my, x, y, PANEL_WIDTH, HEADER)) {
            if (button == 0) {
                draggingPanel = category;
                dragOffsetX = mx - x;
                dragOffsetY = my - y;
            }
            return true;
        }
        y += HEADER;
        for (Module module : modules(category)) {
            if (inside(mx, my, x, y, PANEL_WIDTH, ROW)) {
                if (button == 0) module.toggle();
                else if (button == 1 && !EXPANDED.remove(module)) EXPANDED.add(module);
                return true;
            }
            y += ROW;
            if (!EXPANDED.contains(module)) continue;
            for (Setting<?> setting : module.settings()) {
                if (inside(mx, my, x, y, PANEL_WIDTH, SETTING_ROW)) {
                    clickSetting(setting, x, mx, button);
                    return true;
                }
                y += SETTING_ROW;
            }
            if (inside(mx, my, x, y, PANEL_WIDTH, SETTING_ROW)) {
                if (button == 0) binding = module;
                else module.setKey(-1);
                return true;
            }
            y += SETTING_ROW;
        }
        return false;
    }

    private void clickSetting(Setting<?> setting, int panelX, double mx, int button) {
        if (setting instanceof BoolSetting bool) {
            bool.toggle();
        } else if (setting instanceof ModeSetting mode) {
            mode.cycle(button == 1 ? -1 : 1);
        } else if (setting instanceof NumberSetting number) {
            draggingSlider = number;
            sliderLeft = panelX + 6;
            number.setFraction((mx - sliderLeft) / (PANEL_WIDTH - 12));
        }
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (draggingSlider != null) {
            draggingSlider.setFraction((event.x() - sliderLeft) / (PANEL_WIDTH - 12));
            return true;
        }
        if (draggingPanel != null) {
            int[] pos = PANELS.get(draggingPanel);
            pos[0] = (int) Math.max(0, Math.min(width - PANEL_WIDTH, event.x() - dragOffsetX));
            pos[1] = (int) Math.max(0, Math.min(height - HEADER, event.y() - dragOffsetY));
            return true;
        }
        if (draggingHud != null) {
            Minecraft mc = Minecraft.getInstance();
            int x = (int) Math.round(event.x() - dragOffsetX);
            int y = (int) Math.round(event.y() - dragOffsetY);
            draggingHud.setPosition(Math.min(x, width - draggingHud.width(mc)), Math.min(y, height - draggingHud.height(mc)));
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        draggingHud = null;
        draggingPanel = null;
        draggingSlider = null;
        return super.mouseReleased(event);
    }

    // ---------- клавиатура ----------

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (binding != null) {
            int key = event.key();
            binding.setKey(key == Keys.ESCAPE || key == Keys.DELETE || key == Keys.BACKSPACE ? -1 : key);
            binding = null;
            return true;
        }
        if (event.key() == Keys.RIGHT_SHIFT) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        VibeClient.get().saveConfig();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
