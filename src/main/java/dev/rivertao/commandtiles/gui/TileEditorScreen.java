/*
 * Copyright (C) 2026 River_tao
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 */
package dev.rivertao.commandtiles.gui;

import dev.rivertao.commandtiles.CommandTilesClient;
import dev.rivertao.commandtiles.config.CommandStep;
import dev.rivertao.commandtiles.config.CommandTile;
import dev.rivertao.commandtiles.config.TileGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.io.IOException;

public final class TileEditorScreen extends Screen {
    private static final int LABEL_WIDTH = 90;
    private static final int FIELD_WIDTH = 210;

    private final Screen parent;
    private final TileGroup group;
    private final CommandTile tile;
    private final boolean creating;
    private EditBox nameField;
    private EditBox descriptionField;
    private EditBox iconField;
    private EditBox commandField;
    private EditBox delayField;
    private Component errorMessage;

    public TileEditorScreen(Screen parent, TileGroup group, CommandTile tile) {
        super(Component.translatable(tile == null
                ? "screen.commandtiles.tile_editor.create_title"
                : "screen.commandtiles.tile_editor.edit_title"));
        this.parent = parent;
        this.group = group;
        this.tile = tile == null ? new CommandTile() : tile;
        this.creating = tile == null;
    }

    @Override
    protected void init() {
        int left = width / 2 - (LABEL_WIDTH + FIELD_WIDTH) / 2;
        int fieldX = left + LABEL_WIDTH;
        int y = 34;

        addRenderableWidget(new StringWidget(width / 2 - 130, 8, 260, 20, title, font));
        nameField = addField(left, fieldX, y, "screen.commandtiles.tile_editor.name", tile.name(), 80);
        descriptionField = addField(left, fieldX, y + 26, "screen.commandtiles.tile_editor.description", tile.description(), 200);
        iconField = addField(left, fieldX, y + 52, "screen.commandtiles.tile_editor.icon", tile.icon(), 128);
        String command = tile.steps().isEmpty() ? "" : tile.steps().getFirst().content();
        commandField = addField(left, fieldX, y + 78, "screen.commandtiles.tile_editor.command", command, 1024);
        int delay = tile.steps().isEmpty() ? 0 : tile.steps().getFirst().delayTicks();
        delayField = addField(left, fieldX, y + 104, "screen.commandtiles.tile_editor.delay", Integer.toString(delay), 6);
        delayField.setFilter(value -> value.isEmpty() || value.chars().allMatch(Character::isDigit));

        if (errorMessage != null) {
            addRenderableWidget(new StringWidget(width / 2 - 150, y + 132, 300, 20, errorMessage, font));
        }

        int bottomY = height - 32;
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, ignored -> onClose())
                .pos(width / 2 - 102, bottomY).size(100, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, ignored -> save())
                .pos(width / 2 + 2, bottomY).size(100, 20).build());
        setInitialFocus(nameField);
    }

    private EditBox addField(int labelX, int fieldX, int y, String labelKey, String value, int maxLength) {
        Component label = Component.translatable(labelKey);
        addRenderableWidget(new StringWidget(labelX, y, LABEL_WIDTH - 6, 20, label, font));
        EditBox field = new EditBox(font, fieldX, y, FIELD_WIDTH, 20, label);
        field.setMaxLength(maxLength);
        field.setValue(value);
        addRenderableWidget(field);
        return field;
    }

    private void save() {
        String name = nameField.getValue().trim();
        if (name.isEmpty()) {
            errorMessage = Component.translatable("screen.commandtiles.tile_editor.name_required");
            rebuildWidgets();
            return;
        }

        int delay;
        try {
            delay = delayField.getValue().isEmpty() ? 0 : Integer.parseInt(delayField.getValue());
        } catch (NumberFormatException exception) {
            errorMessage = Component.translatable("screen.commandtiles.tile_editor.invalid_delay");
            rebuildWidgets();
            return;
        }

        tile.setName(name);
        tile.setDescription(descriptionField.getValue().trim());
        tile.setIcon(iconField.getValue().trim());
        String command = commandField.getValue().trim();
        if (tile.steps().isEmpty()) {
            if (!command.isEmpty()) {
                tile.steps().add(new CommandStep(command, delay));
            }
        } else if (command.isEmpty()) {
            tile.steps().removeFirst();
        } else {
            tile.steps().getFirst().setContent(command);
            tile.steps().getFirst().setDelayTicks(delay);
        }
        if (creating) {
            group.tiles().add(tile);
        }

        try {
            CommandTilesClient.configManager().save();
            onClose();
        } catch (IOException exception) {
            if (creating) {
                group.tiles().remove(tile);
            }
            CommandTilesClient.LOGGER.error("Unable to save command tile", exception);
            errorMessage = Component.translatable("screen.commandtiles.save_failed");
            rebuildWidgets();
        }
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
