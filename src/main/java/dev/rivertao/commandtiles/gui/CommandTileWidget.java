/*
 * Copyright (C) 2026 River_tao
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 */
package dev.rivertao.commandtiles.gui;

import dev.rivertao.commandtiles.config.CommandTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class CommandTileWidget extends AbstractButton {
    private static final int NORMAL_BACKGROUND = 0xD0202329;
    private static final int HOVERED_BACKGROUND = 0xEE34465F;
    private static final int EDITING_BACKGROUND = 0xDD3E3826;
    private static final int NORMAL_BORDER = 0xFF777777;
    private static final int HOVERED_BORDER = 0xFFFFFFFF;
    private static final int EDITING_BORDER = 0xFFE0B84F;

    private final Font font;
    private final ItemStack icon;
    private final Component subtitle;
    private final Runnable onPress;
    private final boolean editing;

    public CommandTileWidget(
            int x,
            int y,
            int width,
            int height,
            CommandTile tile,
            boolean editing,
            Runnable onPress
    ) {
        super(x, y, width, height, Component.literal(tile.name()));
        this.font = Minecraft.getInstance().font;
        this.editing = editing;
        this.onPress = onPress;

        ResourceLocation iconId = ResourceLocation.tryParse(tile.icon());
        boolean invalidIcon = iconId == null || !BuiltInRegistries.ITEM.containsKey(iconId);
        this.icon = invalidIcon
                ? Items.COMMAND_BLOCK.getDefaultInstance()
                : BuiltInRegistries.ITEM.get(iconId).getDefaultInstance();
        MutableComponent subtitle = Component.translatable(
                "screen.commandtiles.menu.action_count",
                tile.steps().size()
        );
        if (tile.keyBinding().isBound()) {
            subtitle.append(" · ").append(tile.keyBinding().displayName());
        }
        this.subtitle = subtitle;
        setTooltip(Tooltip.create(createTooltip(tile, invalidIcon)));
    }

    private static MutableComponent createTooltip(CommandTile tile, boolean invalidIcon) {
        MutableComponent tooltip = Component.literal(tile.name());
        if (!tile.description().isBlank()) {
            tooltip.append("\n").append(Component.literal(tile.description()));
        }
        tooltip.append("\n").append(Component.translatable(
                "screen.commandtiles.menu.action_count",
                tile.steps().size()
        ));
        if (tile.keyBinding().isBound()) {
            tooltip.append("\n").append(Component.translatable(
                    "screen.commandtiles.menu.shortcut",
                    tile.keyBinding().displayName()
            ));
        }
        if (invalidIcon) {
            tooltip.append("\n").append(Component.translatable(
                    "screen.commandtiles.menu.invalid_icon",
                    tile.icon()
            ));
        }
        return tooltip;
    }

    @Override
    public void onPress() {
        onPress.run();
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean highlighted = isHoveredOrFocused();
        int background = highlighted ? HOVERED_BACKGROUND : editing ? EDITING_BACKGROUND : NORMAL_BACKGROUND;
        int border = highlighted ? HOVERED_BORDER : editing ? EDITING_BORDER : NORMAL_BORDER;
        graphics.fill(getX(), getY(), getRight(), getBottom(), background);
        graphics.renderOutline(getX(), getY(), getWidth(), getHeight(), border);

        int iconX = getX() + 7;
        int iconY = getY() + (getHeight() - 16) / 2;
        graphics.renderItem(icon, iconX, iconY);

        int textX = iconX + 22;
        int availableWidth = Math.max(1, getRight() - textX - 5);
        String name = fit(getMessage().getString(), availableWidth);
        String detail = fit(subtitle.getString(), availableWidth);
        int firstLineY = getY() + (getHeight() - font.lineHeight * 2 - 2) / 2;
        graphics.drawString(font, name, textX, firstLineY, 0xFFFFFFFF, true);
        graphics.drawString(font, detail, textX, firstLineY + font.lineHeight + 2, 0xFFAAAAAA, false);
    }

    private String fit(String value, int width) {
        if (font.width(value) <= width) {
            return value;
        }
        String ellipsis = "…";
        return font.plainSubstrByWidth(value, Math.max(0, width - font.width(ellipsis))) + ellipsis;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
