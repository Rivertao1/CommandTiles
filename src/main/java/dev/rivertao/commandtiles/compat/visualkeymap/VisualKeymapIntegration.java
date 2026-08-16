/*
 * Copyright (C) 2026 River_tao
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 */
package dev.rivertao.commandtiles.compat.visualkeymap;

import com.mojang.blaze3d.platform.InputConstants;
import dev.rivertao.commandtiles.CommandTilesClient;
import dev.rivertao.commandtiles.config.CommandTile;
import dev.rivertao.commandtiles.config.KeyChord;
import dev.xef2.visualkeymap.api.KeyBinding;
import dev.xef2.visualkeymap.api.VisualKeymapApi;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class VisualKeymapIntegration implements VisualKeymapApi<VisualKeymapIntegration.TileKeyBinding> {
    @Override
    public List<TileKeyBinding> getKeyBindings() {
        return CommandTilesClient.configManager().get().activeProfile().groups().stream()
                .flatMap(group -> group.tiles().stream())
                .map(TileKeyBinding::new)
                .toList();
    }

    @Override
    public void save() {
        try {
            CommandTilesClient.configManager().save();
        } catch (IOException exception) {
            CommandTilesClient.LOGGER.error("Unable to save key bindings changed by Visual Keymap", exception);
        }
    }

    @Override
    public String getProviderName() {
        return CommandTilesClient.MOD_ID;
    }

    public static final class TileKeyBinding extends KeyBinding {
        private static final int MAX_BOUND_KEYS = 5;
        private final CommandTile tile;

        private TileKeyBinding(CommandTile tile) {
            super(
                    Component.translatable("key.category.commandtiles.main"),
                    Component.literal(tile.name()),
                    MAX_BOUND_KEYS
            );
            this.tile = tile;
        }

        @Override
        public String getId() {
            return CommandTilesClient.MOD_ID + ":" + tile.id();
        }

        @Override
        public Component getComment() {
            if (!tile.description().isBlank()) {
                return Component.literal(tile.description());
            }
            if (!tile.steps().isEmpty() && !tile.steps().getFirst().content().isBlank()) {
                return Component.literal(tile.steps().getFirst().content());
            }
            return null;
        }

        @Override
        public List<Integer> getKeyCodes() {
            return tile.keyBinding().isBound()
                    ? List.of(tile.keyBinding().key().getValue())
                    : List.of();
        }

        @Override
        public List<Integer> getModifierKeyCodes() {
            int modifiers = tile.keyBinding().modifiers();
            List<Integer> keys = new ArrayList<>(4);
            addModifier(keys, modifiers, InputConstants.MOD_CONTROL, InputConstants.KEY_LCONTROL);
            addModifier(keys, modifiers, InputConstants.MOD_ALT, InputConstants.KEY_LALT);
            addModifier(keys, modifiers, InputConstants.MOD_SHIFT, InputConstants.KEY_LSHIFT);
            addModifier(keys, modifiers, InputConstants.MOD_SUPER, InputConstants.KEY_LSUPER);
            return keys;
        }

        @Override
        public void setBoundKeys(List<InputConstants.Key> keys) {
            if (keys.isEmpty()) {
                tile.setKeyBinding(new KeyChord());
                return;
            }

            InputConstants.Key mainKey = keys.getLast();
            int modifiers = 0;
            for (int index = 0; index < keys.size() - 1; index++) {
                modifiers |= KeyChord.modifierMask(keys.get(index));
            }
            tile.setKeyBinding(new KeyChord(mainKey, modifiers));
        }

        @Override
        public boolean isDefault() {
            return !tile.keyBinding().isBound();
        }

        @Override
        public void resetToDefault() {
            tile.setKeyBinding(new KeyChord());
        }

        private static void addModifier(List<Integer> keys, int modifiers, int mask, int keyCode) {
            if ((modifiers & mask) != 0) {
                keys.add(keyCode);
            }
        }
    }
}
