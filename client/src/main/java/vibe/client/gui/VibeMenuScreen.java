package vibe.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import vibe.client.VibeClient;
import vibe.client.module.HudModule;
import vibe.client.module.Module;

import java.util.List;

/** Меню клиента: включение модулей и перетаскивание HUD мышью. */
public final class VibeMenuScreen extends Screen {
    private static final int BUTTON_WIDTH = 170;
    private static final int BUTTON_HEIGHT = 20;
    private static final int PANEL = 0xD0101018;

    private HudModule dragging;
    private int columns = 1;
    private double dragOffsetX;
    private double dragOffsetY;

    public VibeMenuScreen() {
        super(Component.literal(VibeClient.NAME));
    }

    @Override
    protected void init() {
        List<Module> modules = VibeClient.get().modules().all();
        int top = 36;
        int perColumn = Math.max(1, (height - top - BUTTON_HEIGHT - 20) / (BUTTON_HEIGHT + 4));
        columns = (modules.size() + perColumn - 1) / perColumn;
        int left = width - BUTTON_WIDTH - 12;
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int x = width - (columns - i / perColumn) * (BUTTON_WIDTH + 6) - 6;
            Button button = Button.builder(label(module), b -> {
                        module.toggle();
                        b.setMessage(label(module));
                    })
                    .bounds(x, top + i % perColumn * (BUTTON_HEIGHT + 4), BUTTON_WIDTH, BUTTON_HEIGHT)
                    .tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(module.description())))
                    .build();
            addRenderableWidget(button);
        }
        addRenderableWidget(Button.builder(Component.literal("Готово"), b -> onClose())
                .bounds(left, height - BUTTON_HEIGHT - 12, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    private static Component label(Module module) {
        return Component.literal(module.name() + ": " + (module.isEnabled() ? "§aВкл" : "§7Выкл"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        Minecraft mc = Minecraft.getInstance();
        int panelLeft = width - columns * (BUTTON_WIDTH + 6) - 12;
        graphics.fill(panelLeft, 0, width, height, PANEL);
        graphics.text(font, Component.literal(VibeClient.NAME), panelLeft + 12, 14, HudModule.ACCENT, true);
        graphics.text(font, Component.literal("Перетаскивай блоки HUD мышью"), 8, height - 16, HudModule.TEXT, true);

        for (Module module : VibeClient.get().modules().all()) {
            if (module instanceof HudModule hud && hud.isEnabled() && mc.player != null) {
                hud.render(graphics, mc);
                if (hud == dragging || hud.contains(mouseX, mouseY, mc)) {
                    outline(graphics, hud.x() - 1, hud.y() - 1, hud.width(mc) + 2, hud.height(mc) + 2, HudModule.ACCENT);
                }
            }
        }
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private static void outline(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;
        Minecraft mc = Minecraft.getInstance();
        if (event.button() == 0) {
            for (Module module : VibeClient.get().modules().all()) {
                if (module instanceof HudModule hud && hud.isEnabled() && hud.contains(event.x(), event.y(), mc)) {
                    dragging = hud;
                    dragOffsetX = event.x() - hud.x();
                    dragOffsetY = event.y() - hud.y();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (dragging != null) {
            Minecraft mc = Minecraft.getInstance();
            int x = (int) Math.round(event.x() - dragOffsetX);
            int y = (int) Math.round(event.y() - dragOffsetY);
            x = Math.min(x, width - dragging.width(mc));
            y = Math.min(y, height - dragging.height(mc));
            dragging.setPosition(x, y);
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = null;
        return super.mouseReleased(event);
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
