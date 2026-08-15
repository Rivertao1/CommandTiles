/*
 * Copyright (C) 2026 River_tao
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 */
package dev.rivertao.commandtiles.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

public final class AddTileWidget extends AbstractButton {
    private final Font font = Minecraft.getInstance().font;
    private final Runnable onPress;

    public AddTileWidget(int x, int y, int width, int height, Runnable onPress) {
        super(x, y, width, height, Component.translatable("screen.commandtiles.menu.add"));
        this.onPress = onPress;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        onPress.run();
    }

    @Override
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int background = isHoveredOrFocused() ? 0xEE315A3C : 0xD0243F2C;
        int border = isHoveredOrFocused() ? 0xFFFFFFFF : 0xFF62B678;
        graphics.fill(getX(), getY(), getRight(), getBottom(), background);
        graphics.renderOutline(getX(), getY(), getWidth(), getHeight(), border);
        graphics.drawCenteredString(font, "+", getX() + getWidth() / 2, getY() + 7, 0xFFFFFFFF);
        graphics.drawCenteredString(font, getMessage(), getX() + getWidth() / 2, getY() + 23, 0xFFBFE8C8);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
