/*
 * Copyright (C) 2026 River_tao
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 */
package dev.rivertao.commandtiles.execution;

import dev.rivertao.commandtiles.CommandTilesClient;
import dev.rivertao.commandtiles.config.CommandTile;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ShortcutManager {
    private final Map<String, Boolean> previousState = new HashMap<>();

    public void tick(Minecraft client) {
        Set<String> activeTileIds = new HashSet<>();
        CommandTilesClient.configManager().get().activeProfile().groups().forEach(group ->
                group.tiles().forEach(tile -> poll(tile, client, activeTileIds))
        );
        previousState.keySet().retainAll(activeTileIds);
    }

    public void clear() {
        previousState.clear();
    }

    private void poll(CommandTile tile, Minecraft client, Set<String> activeTileIds) {
        if (!tile.keyBinding().isBound()) {
            return;
        }
        activeTileIds.add(tile.id());
        boolean down = tile.keyBinding().isDown(client.getWindow());
        boolean wasDown = previousState.put(tile.id(), down) == Boolean.TRUE;
        if (down && !wasDown && client.screen == null && client.player != null && client.getConnection() != null) {
            CommandTilesClient.commandExecutor().queue(tile, client);
        }
    }
}
