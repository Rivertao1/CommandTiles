/*
 * Copyright (C) 2026 River_tao
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 */
package dev.rivertao.commandtiles.gui;

import dev.rivertao.commandtiles.CommandTiles;
import dev.rivertao.commandtiles.config.MenuSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.io.IOException;

public final class SettingsScreen extends Screen {
    private final Screen parent;
    private int buttonsPerRow;
    private int visibleRows;
    private boolean closeOnAction;
    private boolean showExecutionMessage;
    private Component errorMessage;

    public SettingsScreen(Screen parent) {
        super(Component.translatable("screen.commandtiles.settings.title"));
        this.parent = parent;
        MenuSettings settings = CommandTiles.configManager().get().settings();
        buttonsPerRow = settings.buttonsPerRow();
        visibleRows = settings.visibleRows();
        closeOnAction = settings.closeOnAction();
        showExecutionMessage = settings.showExecutionMessage();
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int optionX = centerX - 100;
        int optionWidth = 200;

        addRenderableWidget(new StringWidget(centerX - 120, 18, 240, 20, title, font));
        addRenderableWidget(Button.builder(buttonsPerRowLabel(), ignored -> {
            buttonsPerRow = buttonsPerRow == 12 ? 1 : buttonsPerRow + 1;
            rebuildWidgets();
        }).pos(optionX, 50).size(optionWidth, 20).build());
        addRenderableWidget(Button.builder(visibleRowsLabel(), ignored -> {
            visibleRows = visibleRows == 10 ? 1 : visibleRows + 1;
            rebuildWidgets();
        }).pos(optionX, 74).size(optionWidth, 20).build());
        addRenderableWidget(Button.builder(closeOnActionLabel(), ignored -> {
            closeOnAction = !closeOnAction;
            rebuildWidgets();
        }).pos(optionX, 98).size(optionWidth, 20).build());
        addRenderableWidget(Button.builder(executionMessageLabel(), ignored -> {
            showExecutionMessage = !showExecutionMessage;
            rebuildWidgets();
        }).pos(optionX, 122).size(optionWidth, 20).build());

        if (errorMessage != null) {
            addRenderableWidget(new StringWidget(centerX - 150, 152, 300, 20, errorMessage, font));
        }

        int bottomY = height - 32;
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, ignored -> onClose())
                .pos(centerX - 102, bottomY).size(100, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, ignored -> saveAndClose())
                .pos(centerX + 2, bottomY).size(100, 20).build());
    }

    private Component buttonsPerRowLabel() {
        return Component.translatable("screen.commandtiles.settings.buttons_per_row", buttonsPerRow);
    }

    private Component visibleRowsLabel() {
        return Component.translatable("screen.commandtiles.settings.visible_rows", visibleRows);
    }

    private Component closeOnActionLabel() {
        return CommonComponents.optionStatus(
                Component.translatable("screen.commandtiles.settings.close_on_action"), closeOnAction
        );
    }

    private Component executionMessageLabel() {
        return CommonComponents.optionStatus(
                Component.translatable("screen.commandtiles.settings.execution_message"), showExecutionMessage
        );
    }

    private void saveAndClose() {
        MenuSettings settings = CommandTiles.configManager().get().settings();
        settings.setButtonsPerRow(buttonsPerRow);
        settings.setVisibleRows(visibleRows);
        settings.setCloseOnAction(closeOnAction);
        settings.setShowExecutionMessage(showExecutionMessage);
        try {
            CommandTiles.configManager().save();
            onClose();
        } catch (IOException exception) {
            CommandTiles.LOGGER.error("Unable to save CommandTiles settings", exception);
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
