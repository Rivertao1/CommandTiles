/*
 * Copyright (C) 2026 River_tao
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 */
package dev.rivertao.commandtiles.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class SettingsScreen extends Screen {
    private final Screen parent;

    public SettingsScreen(Screen parent) {
        super(Component.translatable("screen.commandtiles.settings.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = width / 2;

        addRenderableWidget(new StringWidget(
                centerX - 120,
                24,
                240,
                20,
                title,
                font
        ));
        addRenderableWidget(new StringWidget(
                centerX - 160,
                height / 2 - 10,
                320,
                20,
                Component.translatable("screen.commandtiles.settings.placeholder"),
                font
        ));
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .pos(centerX - 100, height - 32)
                .size(200, 20)
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
