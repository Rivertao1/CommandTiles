/*
 * Copyright (C) 2026 River_tao
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 */
package dev.rivertao.commandtiles;

import com.mojang.blaze3d.platform.InputConstants;
import dev.rivertao.commandtiles.gui.CommandTilesScreen;
import dev.rivertao.commandtiles.gui.SettingsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CommandTilesClient implements ClientModInitializer {
    public static final String MOD_ID = "commandtiles";
    public static final String MOD_NAME = "CommandTiles";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(MOD_ID, "main")
    );
    private static final KeyMapping OPEN_MENU_KEY = new KeyMapping(
            "key.commandtiles.open_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            KEY_CATEGORY
    );

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(OPEN_MENU_KEY);
        ClientTickEvents.END_CLIENT_TICK.register(CommandTilesClient::onEndClientTick);
        LOGGER.info("Initialized {}", MOD_NAME);
    }

    private static void onEndClientTick(Minecraft client) {
        while (OPEN_MENU_KEY.consumeClick()) {
            client.setScreen(new CommandTilesScreen(client.screen));
        }
    }

    public static Screen createConfigScreen(Screen parent) {
        return new SettingsScreen(parent);
    }

    private CommandTilesClient() {
    }
}
