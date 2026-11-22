package dev.chavixq.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class MenuScreen extends Screen {
    private static final String[] TABS = {"HUD", "MISC", "CONFIG"};
    private static final int W = 320, H = 220, HEADER = 18, TAB_H = 16, ROW_H = 16;

    private record Entry(String label, BooleanSupplier state, Runnable action) {}

    private int tab = 0;
    private int left, top;
    private int dragOffX = -1, dragOffY = -1;

    public MenuScreen() {
        super(Component.literal("ChaviXQ"));
    }

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        if (dragOffX >= 0) {
            left = mx - dragOffX;
            top = my - dragOffY;
        }

        g.fill(left, top, left + W, top + H, 0xE0101018);
        g.fill(left, top, left + W, top + HEADER, 0xFF2A2A44);
        g.drawString(font, "ChaviXQ", left + 6, top + 5, 0xFFFFFFFF, false);

        int tabW = W / TABS.length;
        for (int i = 0; i < TABS.length; i++) {
            int tx = left + i * tabW;
            int ty = top + HEADER;
            int bg = i == tab ? 0xFF3A3A60 : 0xFF1C1C2C;
            g.fill(tx, ty, tx + tabW, ty + TAB_H, bg);
            g.drawCenteredString(font, TABS[i], tx + tabW / 2, ty + 4, 0xFFFFFFFF);
        }

        List<Entry> entries = entries();
        int y = top + HEADER + TAB_H + 6;
        for (Entry e : entries) {
            boolean hover = mx >= left + 8 && mx <= left + W - 8 && my >= y && my < y + ROW_H;
            if (hover) g.fill(left + 8, y, left + W - 8, y + ROW_H, 0x30FFFFFF);
            g.drawString(font, e.label(), left + 14, y + 4, 0xFFFFFFFF, false);
            boolean on = e.state().getAsBoolean();
            g.drawString(font, on ? "[ON]" : "[OFF]", left + W - 40, y + 4,
                    on ? 0xFF55FF55 : 0xFFFF5555, false);
            y += ROW_H;
        }

        super.render(g, mx, my, partialTick);
    }

    private List<Entry> entries() {
        List<Entry> list = new ArrayList<>();
        Config c = ChaviClient.config;
        switch (tab) {
            case 0 -> {
                list.add(new Entry("Show FPS", () -> c.showFps, () -> c.showFps = !c.showFps));
                list.add(new Entry("Show CPS", () -> c.showCps, () -> c.showCps = !c.showCps));
                list.add(new Entry("Show Coords", () -> c.showCoords, () -> c.showCoords = !c.showCoords));
            }
            case 1 -> list.add(new Entry("HUD visible",
                    () -> ChaviClient.hudVisible, () -> ChaviClient.hudVisible = !ChaviClient.hudVisible));
            case 2 -> {
                list.add(new Entry("Save config", () -> false, ChaviClient::saveConfig));
                list.add(new Entry("Reload config", () -> false, ChaviClient::loadConfig));
                list.add(new Entry("Reset to defaults", () -> false, () -> {
                    ChaviClient.config = new Config();
                    ChaviClient.saveConfig();
                }));
            }
        }
        return list;
    }

    // Verify this signature against 1.21.10 Screen. Newer builds pass a MouseButtonEvent.
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return super.mouseClicked(mx, my, button);

        if (my >= top && my < top + HEADER && mx >= left && mx < left + W) {
            dragOffX = (int) (mx - left);
            dragOffY = (int) (my - top);
            return true;
        }

        int tabW = W / TABS.length;
        int ty = top + HEADER;
        if (my >= ty && my < ty + TAB_H && mx >= left && mx < left + W) {
            tab = Math.min((int) ((mx - left) / tabW), TABS.length - 1);
            return true;
        }

        int y = top + HEADER + TAB_H + 6;
        for (Entry e : entries()) {
            if (mx >= left + 8 && mx <= left + W - 8 && my >= y && my < y + ROW_H) {
                e.action().run();
                return true;
            }
            y += ROW_H;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragOffX = -1;
        dragOffY = -1;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
        }
