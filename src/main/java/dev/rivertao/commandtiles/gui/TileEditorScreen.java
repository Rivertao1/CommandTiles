/*
 * Copyright (C) 2026 River_tao
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 */
package dev.rivertao.commandtiles.gui;

import com.mojang.blaze3d.platform.InputConstants;
import dev.rivertao.commandtiles.CommandTilesClient;
import dev.rivertao.commandtiles.config.CommandStep;
import dev.rivertao.commandtiles.config.CommandTile;
import dev.rivertao.commandtiles.config.KeyChord;
import dev.rivertao.commandtiles.config.TileGroup;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
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
    private Button keyBindingButton;
    private StringWidget bindingWarningWidget;
    private StringWidget errorWidget;
    private KeyChord keyBinding;
    private boolean listeningForKey;
    private Component errorMessage;

    public TileEditorScreen(Screen parent, TileGroup group, CommandTile tile) {
        super(Component.translatable(tile == null
                ? "screen.commandtiles.tile_editor.create_title"
                : "screen.commandtiles.tile_editor.edit_title"));
        this.parent = parent;
        this.group = group;
        this.tile = tile == null ? new CommandTile() : tile;
        this.creating = tile == null;
        this.keyBinding = new KeyChord(this.tile.keyBinding());
    }

    @Override
    protected void init() {
        int left = width / 2 - (LABEL_WIDTH + FIELD_WIDTH) / 2;
        int fieldX = left + LABEL_WIDTH;
        int y = 30;

        addRenderableWidget(new StringWidget(width / 2 - 130, 8, 260, 20, title, font));
        nameField = addField(left, fieldX, y, "screen.commandtiles.tile_editor.name", tile.name(), 80);
        descriptionField = addField(left, fieldX, y + 24, "screen.commandtiles.tile_editor.description", tile.description(), 200);
        iconField = addField(left, fieldX, y + 48, "screen.commandtiles.tile_editor.icon", tile.icon(), 128);
        String command = tile.steps().isEmpty() ? "" : tile.steps().getFirst().content();
        commandField = addField(left, fieldX, y + 72, "screen.commandtiles.tile_editor.command", command, 1024);
        int delay = tile.steps().isEmpty() ? 0 : tile.steps().getFirst().delayTicks();
        delayField = addField(left, fieldX, y + 96, "screen.commandtiles.tile_editor.delay", Integer.toString(delay), 6);
        delayField.setFilter(value -> value.isEmpty() || value.chars().allMatch(Character::isDigit));

        Component keyBindingLabel = Component.translatable("screen.commandtiles.tile_editor.keybind");
        addRenderableWidget(new StringWidget(left, y + 120, LABEL_WIDTH - 6, 20, keyBindingLabel, font));
        keyBindingButton = addRenderableWidget(Button.builder(bindingLabel(), ignored -> beginListening())
                .pos(fieldX, y + 120).size(FIELD_WIDTH, 20).build());
        bindingWarningWidget = addRenderableWidget(new StringWidget(
                width / 2 - 150, y + 142, 300, 12, bindingWarning(), font
        ));
        errorWidget = addRenderableWidget(new StringWidget(
                width / 2 - 150,
                y + 154,
                300,
                12,
                errorMessage == null ? CommonComponents.EMPTY : errorMessage,
                font
        ));

        int bottomY = height - 32;
        if (creating) {
            addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, ignored -> onClose())
                    .pos(width / 2 - 102, bottomY).size(100, 20).build());
            addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, ignored -> save())
                    .pos(width / 2 + 2, bottomY).size(100, 20).build());
        } else {
            addRenderableWidget(Button.builder(
                    Component.translatable("screen.commandtiles.tile_editor.delete"),
                    ignored -> confirmDelete()
            ).pos(width / 2 - 154, bottomY).size(100, 20).build());
            addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, ignored -> onClose())
                    .pos(width / 2 - 50, bottomY).size(100, 20).build());
            addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, ignored -> save())
                    .pos(width / 2 + 54, bottomY).size(100, 20).build());
        }
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
            setError(Component.translatable("screen.commandtiles.tile_editor.name_required"));
            return;
        }

        CommandTile conflict = findInternalConflict();
        if (conflict != null) {
            setError(Component.translatable(
                    "screen.commandtiles.tile_editor.keybind_conflict_internal",
                    conflict.name()
            ));
            return;
        }

        int delay;
        try {
            delay = delayField.getValue().isEmpty() ? 0 : Integer.parseInt(delayField.getValue());
        } catch (NumberFormatException exception) {
            setError(Component.translatable("screen.commandtiles.tile_editor.invalid_delay"));
            return;
        }

        tile.setName(name);
        tile.setDescription(descriptionField.getValue().trim());
        tile.setIcon(iconField.getValue().trim());
        tile.setKeyBinding(new KeyChord(keyBinding));
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
            setError(Component.translatable("screen.commandtiles.save_failed"));
        }
    }

    private void beginListening() {
        listeningForKey = true;
        keyBindingButton.setMessage(Component.translatable("screen.commandtiles.keybind.listening"));
        bindingWarningWidget.setMessage(CommonComponents.EMPTY);
    }

    private void acceptBinding(InputConstants.Key key, int modifiers) {
        if (KeyChord.isModifierKey(key)) {
            return;
        }
        keyBinding = new KeyChord(key, modifiers);
        listeningForKey = false;
        keyBindingButton.setMessage(bindingLabel());
        bindingWarningWidget.setMessage(bindingWarning());
        setError(CommonComponents.EMPTY);
    }

    private void clearBinding() {
        keyBinding = new KeyChord();
        listeningForKey = false;
        keyBindingButton.setMessage(bindingLabel());
        bindingWarningWidget.setMessage(CommonComponents.EMPTY);
        setError(CommonComponents.EMPTY);
    }

    private Component bindingLabel() {
        return Component.translatable("screen.commandtiles.tile_editor.keybind_value", keyBinding.displayName());
    }

    private Component bindingWarning() {
        CommandTile conflict = findInternalConflict();
        if (conflict != null) {
            return Component.translatable(
                    "screen.commandtiles.tile_editor.keybind_conflict_internal",
                    conflict.name()
            );
        }
        KeyMapping vanillaConflict = findVanillaConflict();
        if (vanillaConflict != null) {
            return Component.translatable(
                    "screen.commandtiles.tile_editor.keybind_conflict_vanilla",
                    Component.translatable(vanillaConflict.getName())
            );
        }
        return CommonComponents.EMPTY;
    }

    private CommandTile findInternalConflict() {
        if (!keyBinding.isBound()) {
            return null;
        }
        return CommandTilesClient.configManager().get().activeProfile().groups().stream()
                .flatMap(candidateGroup -> candidateGroup.tiles().stream())
                .filter(candidate -> candidate != tile)
                .filter(candidate -> keyBinding.sameBinding(candidate.keyBinding()))
                .findFirst()
                .orElse(null);
    }

    private KeyMapping findVanillaConflict() {
        if (!keyBinding.isBound()) {
            return null;
        }
        for (KeyMapping mapping : Minecraft.getInstance().options.keyMappings) {
            boolean matches;
            if (keyBinding.key().getType() == InputConstants.Type.MOUSE) {
                matches = mapping.matchesMouse(new MouseButtonEvent(
                        0,
                        0,
                        new MouseButtonInfo(keyBinding.key().getValue(), keyBinding.modifiers())
                ));
            } else {
                matches = mapping.matches(new KeyEvent(
                        keyBinding.key().getValue(),
                        0,
                        keyBinding.modifiers()
                ));
            }
            if (matches) {
                return mapping;
            }
        }
        return null;
    }

    private void setError(Component message) {
        errorMessage = message;
        if (errorWidget != null) {
            errorWidget.setMessage(message);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!listeningForKey) {
            return super.keyPressed(event);
        }
        if (event.key() == InputConstants.KEY_ESCAPE) {
            listeningForKey = false;
            keyBindingButton.setMessage(bindingLabel());
            bindingWarningWidget.setMessage(bindingWarning());
            return true;
        }
        if (event.key() == InputConstants.KEY_BACKSPACE || event.key() == InputConstants.KEY_DELETE) {
            clearBinding();
            return true;
        }
        if (CommandTilesClient.matchesMenuKey(event)) {
            setError(Component.translatable("screen.commandtiles.tile_editor.keybind_conflict_menu"));
            return true;
        }
        acceptBinding(InputConstants.Type.KEYSYM.getOrCreate(event.key()), event.modifiers());
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!listeningForKey) {
            return super.mouseClicked(event, doubleClick);
        }
        if (CommandTilesClient.matchesMenuKey(event)) {
            setError(Component.translatable("screen.commandtiles.tile_editor.keybind_conflict_menu"));
            return true;
        }
        acceptBinding(InputConstants.Type.MOUSE.getOrCreate(event.button()), event.modifiers());
        return true;
    }

    private void confirmDelete() {
        Minecraft.getInstance().setScreen(new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        delete();
                    } else {
                        Minecraft.getInstance().setScreen(this);
                    }
                },
                Component.translatable("screen.commandtiles.tile_editor.delete_title"),
                Component.translatable("screen.commandtiles.tile_editor.delete_message", tile.name()),
                Component.translatable("screen.commandtiles.tile_editor.delete_confirm"),
                CommonComponents.GUI_CANCEL
        ));
    }

    private void delete() {
        int originalIndex = group.tiles().indexOf(tile);
        if (originalIndex < 0) {
            Minecraft.getInstance().setScreen(parent);
            return;
        }
        group.tiles().remove(originalIndex);
        try {
            CommandTilesClient.configManager().save();
            Minecraft.getInstance().setScreen(parent);
        } catch (IOException exception) {
            group.tiles().add(originalIndex, tile);
            CommandTilesClient.LOGGER.error("Unable to delete command tile", exception);
            errorMessage = Component.translatable("screen.commandtiles.save_failed");
            Minecraft.getInstance().setScreen(this);
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
