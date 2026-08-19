/*
 * Copyright (C) 2026 River_tao
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 */
package dev.rivertao.commandtiles;

import com.mojang.logging.LogUtils;
import com.mojang.blaze3d.platform.InputConstants;
import dev.rivertao.commandtiles.config.ConfigManager;
import dev.rivertao.commandtiles.execution.CommandExecutor;
import dev.rivertao.commandtiles.execution.ShortcutManager;
import dev.rivertao.commandtiles.gui.CommandTilesScreen;
import dev.rivertao.commandtiles.gui.SettingsScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Mod(value = CommandTiles.MOD_ID, dist = Dist.CLIENT)
public final class CommandTiles {
    public static final String MOD_ID = "commandtiles";
    public static final String MOD_NAME = "CommandTiles";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final KeyMapping OPEN_MENU_KEY = new KeyMapping(
            "key.commandtiles.open_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.commandtiles"
    );
    private static final CommandExecutor COMMAND_EXECUTOR = new CommandExecutor();
    private static final ShortcutManager SHORTCUT_MANAGER = new ShortcutManager();
    private static ConfigManager configManager;

    public CommandTiles(IEventBus modBus) {
        configManager = new ConfigManager();
        configManager.load();
        modBus.addListener(CommandTiles::registerKeyMappings);
        NeoForge.EVENT_BUS.addListener(CommandTiles::onClientTick);
        LOGGER.info(
                "Initialized {} for NeoForge 1.21.1 with config at {}",
                MOD_NAME,
                configManager.configFile()
        );
    }

    public static ConfigManager configManager() {
        if (configManager == null) {
            throw new IllegalStateException("CommandTiles has not been initialized");
        }
        return configManager;
    }

    public static CommandExecutor commandExecutor() {
        return COMMAND_EXECUTOR;
    }

    public static Screen createConfigScreen(Screen parent) {
        return new SettingsScreen(parent);
    }

    public static boolean matchesMenuKey(int keyCode, int scanCode) {
        return OPEN_MENU_KEY.matches(keyCode, scanCode);
    }

    public static boolean matchesMenuMouseButton(int button) {
        return OPEN_MENU_KEY.matchesMouse(button);
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_MENU_KEY);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        while (OPEN_MENU_KEY.consumeClick()) {
            if (client.screen == null) {
                client.setScreen(new CommandTilesScreen(null));
            } else if (client.screen instanceof CommandTilesScreen commandTilesScreen) {
                commandTilesScreen.onClose();
            }
        }
        SHORTCUT_MANAGER.tick(client);
        COMMAND_EXECUTOR.tick(client);
    }
}
