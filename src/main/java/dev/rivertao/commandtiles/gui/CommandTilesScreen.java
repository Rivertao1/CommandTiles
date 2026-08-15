/*
 * Copyright (C) 2026 River_tao
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 */
package dev.rivertao.commandtiles.gui;

import dev.rivertao.commandtiles.CommandTilesClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class CommandTilesScreen extends Screen {
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 20;

    private final Screen parent;

    public CommandTilesScreen(Screen parent) {
        super(Component.translatable("screen.commandtiles.menu.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = width / 2;

        addRenderableWidget(new StringWidget(
                centerX - 100,
                height / 2 - 50,
                200,
                20,
                title,
                font
        ));
        addRenderableWidget(new StringWidget(
                centerX - 100,
                height / 2 - 20,
                200,
                20,
                Component.translatable("screen.commandtiles.menu.empty"),
                font
        ));
        addRenderableWidget(Button.builder(
                Component.translatable("screen.commandtiles.menu.settings"),
                button -> Minecraft.getInstance().setScreen(CommandTilesClient.createConfigScreen(this))
        ).pos(centerX - BUTTON_WIDTH - 2, height / 2 + 20)
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .pos(centerX + 2, height / 2 + 20)
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
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
