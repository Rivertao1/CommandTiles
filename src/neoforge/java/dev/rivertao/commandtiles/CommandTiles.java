/*
 * Copyright (C) 2026 River_tao
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 */
package dev.rivertao.commandtiles;

import com.mojang.logging.LogUtils;
import dev.rivertao.commandtiles.config.ConfigManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(value = CommandTiles.MOD_ID, dist = Dist.CLIENT)
public final class CommandTiles {
    public static final String MOD_ID = "commandtiles";
    public static final String MOD_NAME = "CommandTiles";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static ConfigManager configManager;

    public CommandTiles() {
        configManager = new ConfigManager();
        configManager.load();
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
}
