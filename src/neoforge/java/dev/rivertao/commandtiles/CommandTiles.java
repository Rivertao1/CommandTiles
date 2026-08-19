/*
 * Copyright (C) 2026 River_tao
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 */
package dev.rivertao.commandtiles;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(value = CommandTiles.MOD_ID, dist = Dist.CLIENT)
public final class CommandTiles {
    public static final String MOD_ID = "commandtiles";
    public static final String MOD_NAME = "CommandTiles";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CommandTiles() {
        LOGGER.info("Initialized {} for NeoForge 1.21.1", MOD_NAME);
    }
}
