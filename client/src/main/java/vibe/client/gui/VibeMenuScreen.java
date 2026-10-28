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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;

/**
 * ClickGUI: одно окно, слева категории, справа карточки модулей с переключателями.
 * Клик по карточке включает модуль, «+» или ПКМ раскрывает настройки и клавишу.
 * Блоки HUD вне окна можно перетаскивать мышью.
 */
public final class VibeMenuScreen extends Screen {
    private static final int SIDEBAR = 96;
    private static final int TOP = 30;
    private static final int PAD = 8;
    private static final int CARD = 25;
    private static final int GAP = 4;
    private static final int SETTING = 16;
    private static final int TAB = 18;
    /** Зона «+» в карточке: обычный клик по ней раскрывает настройки (на тачпаде ПКМ неочевиден). */
    private static final int EXPAND = 16;

    /** Положение окна, вкладка и раскрытые модули живут, пока открыт клиент. */
    private static int winX = Integer.MIN_VALUE;
    private static int winY;
    private static Category selected = Category.COMBAT;
    private static final Map<Category, Float> SCROLL = new EnumMap<>(Category.class);
    private static final Set<Module> EXPANDED = new HashSet<>();

    /** Анимации переключателей: модуль или BoolSetting → 0..1. */
    private final Map<Object, Float> anim = new HashMap<>();
    /** Кликабельные зоны последнего кадра, в порядке проверки. */
    private final List<Hit> hits = new ArrayList<>();
    private long lastFrame;
    private float fade;
    private float maxScroll;

    private boolean draggingWindow;
    private HudModule draggingHud;
    private NumberSetting draggingSlider;
    private int sliderLeft;
    private int sliderWidth;
    private double dragOffsetX;
    private double dragOffsetY;
    private double pressX;
    private double pressY;
    private Module binding;

    private record Hit(int x, int y, int w, int h, IntConsumer action) {
        boolean contains(double mx, double my) {
            return mx >= x && my >= y && mx < x + w && my < y + h;
        }
    }

    public VibeMenuScreen() {
        super(Component.literal(VibeClient.NAME));
    }

    private int windowWidth() {
        return Math.min(420, width - 16);
    }

    private int windowHeight() {
        return Math.min(260, height - 34);
    }

    private static List<Module> modules(Category category) {
        return VibeClient.get().modules().all().stream().filter(m -> m.category() == category).toList();
    }

    // ---------- отрисовка ----------

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        Minecraft mc = Minecraft.getInstance();
        long now = System.currentTimeMillis();
        float dt = lastFrame == 0 ? 0 : Math.min(100, now - lastFrame);
        lastFrame = now;
        fade = Draw.approach(fade, 1, dt, 70);
        int accent = Theme.accent();

        g.fill(0, 0, width, height, Draw.alpha(0x88000000, fade));

        int w = windowWidth(), h = windowHeight();
        if (winX == Integer.MIN_VALUE) {
            winX = (width - w) / 2;
            winY = (height - h) / 2;
        }
        winX = Math.max(0, Math.min(width - w, winX));
        winY = Math.max(0, Math.min(height - h - 14, winY));
        int x = winX, y = winY;

        for (Module module : VibeClient.get().modules().all()) {
            if (module instanceof HudModule hud && hud.isEnabled() && mc.player != null) {
                hud.render(g, mc);
                boolean overWindow = inside(mouseX, mouseY, x, y, w, h);
                if (hud == draggingHud || !overWindow && hud.contains(mouseX, mouseY, mc)) {
                    outline(g, hud.x() - 1, hud.y() - 1, hud.width(mc) + 2, hud.height(mc) + 2, accent);
                }
            }
        }

        hits.clear();
        Draw.round(g, x - 1, y - 1, w + 2, h + 2, 9, Draw.alpha(accent, 0.35f));
        Draw.round(g, x, y, w, h, 8, Theme.WINDOW);
        renderSidebar(g, x, y, h, mouseX, mouseY, accent);
        renderContent(g, mc, x + SIDEBAR, y, w - SIDEBAR, h, mouseX, mouseY, dt, accent);

        String hint = binding != null
                ? "Нажми клавишу для «" + binding.name() + "»  (Esc или Delete — убрать)"
                : "Клик — вкл/выкл     «+» или ПКМ — настройки и клавиша     Правый Shift — закрыть";
        g.text(font, hint, x + (w - font.width(hint)) / 2, y + h + 5, binding != null ? accent : Theme.MUTED, true);
        super.extractRenderState(g, mouseX, mouseY, delta);
    }

    private void renderSidebar(GuiGraphicsExtractor g, int x, int y, int h, int mouseX, int mouseY, int accent) {
        Draw.round(g, x, y, SIDEBAR, h, 8, Theme.SIDEBAR);
        g.fill(x + SIDEBAR - 8, y, x + SIDEBAR, y + h, Theme.SIDEBAR);
        g.fill(x + SIDEBAR - 1, y + 8, x + SIDEBAR, y + h - 8, 0xFF1C1C26);

        Draw.circle(g, x + 9, y + 9, 12, accent);
        g.text(font, "V", x + 12, y + 11, 0xFF101016, false);
        g.text(font, "Vibe", x + 26, y + 7, Theme.TEXT, true);
        g.text(font, VibeClient.version(), x + 26, y + 17, Theme.MUTED, false);
        hits.add(new Hit(x, y, SIDEBAR, TOP, b -> startWindowDrag()));

        int tabY = y + TOP + 6;
        for (Category category : Category.values()) {
            int tx = x + 6, tw = SIDEBAR - 12;
            boolean active = category == selected;
            boolean hover = inside(mouseX, mouseY, tx, tabY, tw, TAB);
            if (active) {
                Draw.round(g, tx, tabY, tw, TAB, 5, Draw.alpha(accent, 0.18f));
                Draw.round(g, tx, tabY + 4, 2, TAB - 8, 1, accent);
            } else if (hover) {
                Draw.round(g, tx, tabY, tw, TAB, 5, Theme.CARD_HOVER);
            }
            g.text(font, category.title(), tx + 8, tabY + 5, active ? accent : hover ? Theme.TEXT : Theme.MUTED, false);
            long on = modules(category).stream().filter(Module::isEnabled).count();
            if (on > 0) {
                String count = String.valueOf(on);
                g.text(font, count, tx + tw - 6 - font.width(count), tabY + 5, Theme.MUTED, false);
            }
            hits.add(new Hit(tx, tabY, tw, TAB, b -> selected = category));
            tabY += TAB + 3;
        }
    }

    private void renderContent(GuiGraphicsExtractor g, Minecraft mc, int x, int y, int w, int h,
                               int mouseX, int mouseY, float dt, int accent) {
        int left = x + PAD, cw = w - PAD * 2;
        g.text(font, selected.title(), left, y + 11, Theme.TEXT, true);
        hits.add(new Hit(x, y, w, TOP, b -> startWindowDrag()));

        int top = y + TOP, bottom = y + h - PAD;
        float scroll = SCROLL.getOrDefault(selected, 0f);
        int cy = top - Math.round(scroll);
        boolean allowed = mc.player == null || CheatModule.allowed(mc);

        for (Module module : modules(selected)) {
            boolean locked = module instanceof CheatModule && !allowed;
            if (visible(cy, CARD, top, bottom)) {
                renderCard(g, module, locked, left, cy, cw, mouseX, mouseY, dt, accent);
            }
            cy += CARD;
            if (EXPANDED.contains(module)) {
                List<Setting<?>> settings = module.settings();
                int blockH = (settings.size() + 1) * SETTING + 4;
                int sy = cy + 2;
                for (Setting<?> setting : settings) {
                    if (visible(sy, SETTING, top, bottom)) {
                        renderSetting(g, setting, left + 4, sy, cw - 8, mouseX, mouseY, dt, accent);
                    }
                    sy += SETTING;
                }
                if (visible(sy, SETTING, top, bottom)) renderKeyRow(g, module, left + 4, sy, cw - 8, mouseX, mouseY, accent);
                cy += blockH;
            }
            cy += GAP;
        }

        float content = cy + Math.round(scroll) - top;
        maxScroll = Math.max(0, content - (bottom - top));
        SCROLL.put(selected, Math.max(0, Math.min(maxScroll, scroll)));
        if (maxScroll > 0) {
            int track = bottom - top;
            int thumb = Math.max(16, (int) (track * track / content));
            int thumbY = top + (int) ((track - thumb) * (Math.min(scroll, maxScroll) / maxScroll));
            Draw.round(g, x + w - 4, thumbY, 2, thumb, 1, Draw.alpha(accent, 0.6f));
        }
    }

    private void renderCard(GuiGraphicsExtractor g, Module module, boolean locked, int x, int y, int w,
                            int mouseX, int mouseY, float dt, int accent) {
        boolean hover = inside(mouseX, mouseY, x, y, w, CARD);
        Draw.round(g, x, y, w, CARD, 5, hover ? Theme.CARD_HOVER : Theme.CARD);

        float t = Draw.approach(anim.getOrDefault(module, module.isEnabled() ? 1f : 0f), module.isEnabled() ? 1 : 0, dt, 60);
        anim.put(module, t);
        if (t > 0.01f) Draw.round(g, x, y + 5, 2, CARD - 10, 1, Draw.alpha(accent, t));

        int ex = x + w - 30 - EXPAND;
        int textW = ex - x - 30;
        int nameColor = locked ? Theme.LOCKED : Draw.mix(Theme.TEXT, accent, t * 0.4f);
        g.text(font, fit(module.name(), textW), x + 8, y + 4, nameColor, true);
        String desc = locked ? "Только в одиночке и на HvH-серверах из списка" : module.description();
        g.text(font, fit(desc, textW), x + 8, y + 14, Theme.MUTED, false);

        Draw.toggle(g, x + w - 26, y + (CARD - 10) / 2, t, accent);

        boolean overExpand = inside(mouseX, mouseY, ex, y, EXPAND, CARD);
        if (overExpand) Draw.round(g, ex, y + 4, EXPAND, CARD - 8, 4, 0x20FFFFFF);
        String sign = EXPANDED.contains(module) ? "-" : "+";
        g.text(font, sign, ex + (EXPAND - font.width(sign)) / 2 + 1, y + 8, overExpand ? Theme.TEXT : Theme.MUTED, false);
        if (module.key() >= 0) {
            String key = Keys.name(module.key());
            g.text(font, key, ex - 2 - font.width(key), y + 8, Theme.MUTED, false);
        }

        // «+» добавляется раньше карточки, поэтому клик по нему не включает модуль.
        hits.add(new Hit(ex, y, EXPAND, CARD, b -> toggleExpanded(module)));
        hits.add(new Hit(x, y, w, CARD, b -> {
            if (b == 0) module.toggle();
            else if (b == 1) toggleExpanded(module);
        }));
    }

    private void renderSetting(GuiGraphicsExtractor g, Setting<?> setting, int x, int y, int w,
                               int mouseX, int mouseY, float dt, int accent) {
        Draw.round(g, x, y, w, SETTING, 3, inside(mouseX, mouseY, x, y, w, SETTING) ? 0xFF181820 : Theme.SETTINGS);
        g.text(font, setting.name(), x + 8, y + 4, Theme.MUTED, false);
        int right = x + w - 8;

        if (setting instanceof BoolSetting bool) {
            float t = Draw.approach(anim.getOrDefault(bool, bool.on() ? 1f : 0f), bool.on() ? 1 : 0, dt, 60);
            anim.put(bool, t);
            Draw.toggle(g, right - 18, y + 3, t, accent);
            hits.add(new Hit(x, y, w, SETTING, b -> bool.toggle()));
        } else if (setting instanceof NumberSetting number) {
            String value = number.display();
            g.text(font, value, right - font.width(value), y + 2, Theme.TEXT, false);
            int barX = x + 8, barW = w - 16, barY = y + 12;
            Draw.round(g, barX, barY, barW, 2, 1, 0xFF2A2A36);
            int filled = (int) (barW * number.fraction());
            if (filled > 0) g.fill(barX, barY, barX + filled, barY + 2, accent);
            Draw.circle(g, barX + filled - 2, barY - 1, 4, 0xFFFFFFFF);
            hits.add(new Hit(x, y, w, SETTING, b -> {
                draggingSlider = number;
                sliderLeft = barX;
                sliderWidth = barW;
                number.setFraction((pressX - barX) / barW);
            }));
        } else if (setting instanceof ModeSetting mode) {
            String value = "< " + mode.display() + " >";
            g.text(font, value, right - font.width(value), y + 4, accent, false);
            hits.add(new Hit(x, y, w, SETTING, b -> mode.cycle(b == 1 ? -1 : 1)));
        }
    }

    private void renderKeyRow(GuiGraphicsExtractor g, Module module, int x, int y, int w, int mouseX, int mouseY, int accent) {
        Draw.round(g, x, y, w, SETTING, 3, inside(mouseX, mouseY, x, y, w, SETTING) ? 0xFF181820 : Theme.SETTINGS);
        g.text(font, "Клавиша", x + 8, y + 4, Theme.MUTED, false);
        boolean waiting = binding == module;
        String key = waiting ? "нажми клавишу..." : Keys.name(module.key());
        g.text(font, key, x + w - 8 - font.width(key), y + 4, waiting ? accent : Theme.TEXT, false);
        hits.add(new Hit(x, y, w, SETTING, b -> {
            if (b == 0) binding = module;
            else module.setKey(-1);
        }));
    }

    private static boolean visible(int y, int h, int top, int bottom) {
        return y >= top && y + h <= bottom;
    }

    private String fit(String text, int max) {
        if (font.width(text) <= max) return text;
        while (text.length() > 1 && font.width(text + "...") > max) text = text.substring(0, text.length() - 1);
        return text + "...";
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

    private static void toggleExpanded(Module module) {
        if (!EXPANDED.remove(module)) EXPANDED.add(module);
    }

    // ---------- мышь ----------

    private void startWindowDrag() {
        draggingWindow = true;
        dragOffsetX = pressX - winX;
        dragOffsetY = pressY - winY;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x(), my = event.y();
        int button = event.button();
        binding = null;
        pressX = mx;
        pressY = my;
        for (Hit hit : hits) {
            if (hit.contains(mx, my)) {
                hit.action().accept(button);
                return true;
            }
        }
        if (inside(mx, my, winX, winY, windowWidth(), windowHeight())) return true;

        Minecraft mc = Minecraft.getInstance();
        if (button == 0 && mc.player != null) {
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

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (draggingSlider != null) {
            draggingSlider.setFraction((event.x() - sliderLeft) / sliderWidth);
            return true;
        }
        if (draggingWindow) {
            winX = (int) Math.round(event.x() - dragOffsetX);
            winY = (int) Math.round(event.y() - dragOffsetY);
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
        draggingWindow = false;
        draggingSlider = null;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (inside(mouseX, mouseY, winX + SIDEBAR, winY, windowWidth() - SIDEBAR, windowHeight())) {
            float scroll = SCROLL.getOrDefault(selected, 0f) - (float) scrollY * 18;
            SCROLL.put(selected, Math.max(0, Math.min(maxScroll, scroll)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
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
