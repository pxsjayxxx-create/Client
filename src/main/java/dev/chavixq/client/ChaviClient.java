package dev.chavixq.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ChaviClient implements ClientModInitializer {

    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("chavixq-client.json");
    static Config config = new Config();
    static boolean hudVisible = true;

    private static KeyMapping toggleKey;
    private static KeyMapping menuKey;

    @Override
    public void onInitializeClient() {
        loadConfig();

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.chavixq.toggle_hud",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyMapping.Category.MISC));

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.chavixq.menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_INSERT,
                KeyMapping.Category.MISC));

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("chavixq", "overlay"),
                (graphics, tickCounter) -> renderOverlay(graphics));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (toggleKey.consumeClick()) {
                hudVisible = !hudVisible;
            }
            while (menuKey.consumeClick()) {
                mc.setScreen(new MenuScreen());
            }
            // Only count clicks in-game, not while a screen is open
            CpsCounter.tick(mc.screen == null && mc.options.keyAttack.isDown());
        });
    }

    private static void renderOverlay(GuiGraphics g) {
        if (!hudVisible) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.font == null) return;

        Font font = mc.font;
        int x = config.hudX;
        int y = config.hudY;
        int line = font.lineHeight + 2;

        if (config.showFps) {
            g.drawString(font, "FPS: " + mc.getFps(), x, y, config.hudColor, true);
            y += line;
        }
        if (config.showCps) {
            g.drawString(font, "CPS: " + CpsCounter.cps(), x, y, config.hudColor, true);
            y += line;
        }
        if (config.showCoords) {
            var p = mc.player.position();
            g.drawString(font,
                    String.format("XYZ: %.1f / %.1f / %.1f", p.x, p.y, p.z),
                    x, y, config.hudColor, true);
        }
    }

    static void loadConfig() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
                    Config loaded = GSON.fromJson(r, Config.class);
                    config = loaded != null ? loaded : new Config();
                }
            } else {
                saveConfig();
            }
        } catch (IOException e) {
            System.err.println("[ChaviXQ] config load failed, using defaults: " + e.getMessage());
            config = new Config();
        }
    }

    static void saveConfig() {
        try (Writer w = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(config, w);
        } catch (IOException e) {
            System.err.println("[ChaviXQ] config save failed: " + e.getMessage());
        }
    }
        }
