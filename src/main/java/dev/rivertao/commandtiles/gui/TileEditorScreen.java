/*
 * Copyright (C) 2026 River_tao
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 */
package dev.rivertao.commandtiles.gui;

import com.mojang.blaze3d.platform.InputConstants;
import dev.rivertao.commandtiles.CommandTiles;
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
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

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
    private InputConstants.Key pendingModifierKey;
    private int pendingModifierModifiers;
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
            CommandTiles.configManager().save();
            onClose();
        } catch (IOException exception) {
            if (creating) {
                group.tiles().remove(tile);
            }
            CommandTiles.LOGGER.error("Unable to save command tile", exception);
            setError(Component.translatable("screen.commandtiles.save_failed"));
        }
    }

    private void beginListening() {
        listeningForKey = true;
        pendingModifierKey = null;
        pendingModifierModifiers = 0;
        keyBindingButton.setMessage(Component.translatable("screen.commandtiles.keybind.listening"));
        bindingWarningWidget.setMessage(CommonComponents.EMPTY);
    }

    private void acceptBinding(InputConstants.Key key, int modifiers) {
        keyBinding = new KeyChord(key, modifiers & ~KeyChord.modifierMask(key));
        listeningForKey = false;
        pendingModifierKey = null;
        pendingModifierModifiers = 0;
        keyBindingButton.setMessage(bindingLabel());
        bindingWarningWidget.setMessage(bindingWarning());
        setError(CommonComponents.EMPTY);
    }

    private void clearBinding() {
        keyBinding = new KeyChord();
        listeningForKey = false;
        pendingModifierKey = null;
        pendingModifierModifiers = 0;
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
        return CommandTiles.configManager().get().activeProfile().groups().stream()
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
            boolean keyMatches = keyBinding.key().getType() == InputConstants.Type.MOUSE
                    ? mapping.matchesMouse(keyBinding.key().getValue())
                    : mapping.matches(keyBinding.key().getValue(), 0);
            boolean matches = keyMatches && matchesModifiers(mapping.getKeyModifier());
            if (matches) {
                return mapping;
            }
        }
        return null;
    }

    private boolean matchesModifiers(KeyModifier modifier) {
        int expected = switch (modifier) {
            case CONTROL -> GLFW.GLFW_MOD_CONTROL;
            case SHIFT -> GLFW.GLFW_MOD_SHIFT;
            case ALT -> GLFW.GLFW_MOD_ALT;
            case NONE -> 0;
        };
        return keyBinding.modifiers() == expected;
    }

    private void setError(Component message) {
        errorMessage = message;
        if (errorWidget != null) {
            errorWidget.setMessage(message);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!listeningForKey) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            listeningForKey = false;
            pendingModifierKey = null;
            pendingModifierModifiers = 0;
            keyBindingButton.setMessage(bindingLabel());
            bindingWarningWidget.setMessage(bindingWarning());
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
            clearBinding();
            return true;
        }
        if (CommandTiles.matchesMenuKey(keyCode, scanCode)) {
            setError(Component.translatable("screen.commandtiles.tile_editor.keybind_conflict_menu"));
            return true;
        }
        InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);
        if (KeyChord.isModifierKey(key)) {
            pendingModifierKey = key;
            pendingModifierModifiers = modifiers & ~KeyChord.modifierMask(key);
            keyBindingButton.setMessage(Component.translatable(
                    "screen.commandtiles.keybind.modifier_pending",
                    key.getDisplayName()
            ));
            return true;
        }
        acceptBinding(key, modifiers);
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (!listeningForKey || pendingModifierKey == null) {
            return super.keyReleased(keyCode, scanCode, modifiers);
        }
        if (pendingModifierKey.getType() == InputConstants.Type.KEYSYM
                && pendingModifierKey.getValue() == keyCode) {
            acceptBinding(pendingModifierKey, pendingModifierModifiers);
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!listeningForKey) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (CommandTiles.matchesMenuMouseButton(button)) {
            setError(Component.translatable("screen.commandtiles.tile_editor.keybind_conflict_menu"));
            return true;
        }
        acceptBinding(
                InputConstants.Type.MOUSE.getOrCreate(button),
                KeyChord.currentModifiers(Minecraft.getInstance().getWindow())
        );
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
            CommandTiles.configManager().save();
            Minecraft.getInstance().setScreen(parent);
        } catch (IOException exception) {
            group.tiles().add(originalIndex, tile);
            CommandTiles.LOGGER.error("Unable to delete command tile", exception);
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
