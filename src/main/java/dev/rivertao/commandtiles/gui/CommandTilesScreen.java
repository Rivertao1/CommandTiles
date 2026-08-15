/*
 * Copyright (C) 2026 River_tao
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 */
package dev.rivertao.commandtiles.gui;

import dev.rivertao.commandtiles.CommandTilesClient;
import dev.rivertao.commandtiles.config.CommandTile;
import dev.rivertao.commandtiles.config.MenuSettings;
import dev.rivertao.commandtiles.config.Profile;
import dev.rivertao.commandtiles.config.TileGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class CommandTilesScreen extends Screen {
    private static final int GAP = 4;
    private static final int TILE_HEIGHT = 24;
    private static final int FOOTER_BUTTON_WIDTH = 90;

    private final Screen parent;
    private boolean editMode;
    private int page;

    public CommandTilesScreen(Screen parent) {
        super(Component.translatable("screen.commandtiles.menu.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        Profile profile = CommandTilesClient.configManager().get().activeProfile();
        TileGroup group = profile.groups().getFirst();
        MenuSettings settings = CommandTilesClient.configManager().get().settings();
        List<CommandTile> tiles = editMode
                ? group.tiles()
                : group.tiles().stream().filter(CommandTile::visible).toList();

        int columns = Math.max(1, Math.min(settings.buttonsPerRow(), width / 72));
        int rows = Math.max(1, Math.min(settings.visibleRows(), Math.max(1, (height - 94) / (TILE_HEIGHT + GAP))));
        int pageSize = columns * rows;
        int pageCount = Math.max(1, (tiles.size() + pageSize - 1) / pageSize);
        page = Math.clamp(page, 0, pageCount - 1);

        addRenderableWidget(new StringWidget(
                width / 2 - 150,
                10,
                300,
                20,
                Component.literal(profile.name() + " / " + group.name()),
                font
        ));

        int gridWidth = Math.min(width - 24, columns * 120 + (columns - 1) * GAP);
        int tileWidth = (gridWidth - (columns - 1) * GAP) / columns;
        int gridX = (width - gridWidth) / 2;
        int gridY = 36;
        int firstTile = page * pageSize;
        int lastTile = Math.min(tiles.size(), firstTile + pageSize);

        for (int index = firstTile; index < lastTile; index++) {
            CommandTile tile = tiles.get(index);
            int position = index - firstTile;
            int x = gridX + (position % columns) * (tileWidth + GAP);
            int y = gridY + (position / columns) * (TILE_HEIGHT + GAP);
            Button button = Button.builder(Component.literal(tile.name()), ignored -> {
                if (editMode) {
                    openTile(group, tile);
                } else {
                    execute(tile, settings);
                }
            })
                    .pos(x, y)
                    .size(tileWidth, TILE_HEIGHT)
                    .build();
            addRenderableWidget(button);
        }

        if (tiles.isEmpty()) {
            addRenderableWidget(new StringWidget(
                    width / 2 - 150,
                    gridY + 16,
                    300,
                    20,
                    Component.translatable("screen.commandtiles.menu.empty"),
                    font
            ));
        }

        int footerY = height - 28;
        int footerX = width / 2 - 2 * FOOTER_BUTTON_WIDTH - 6;
        addRenderableWidget(Button.builder(
                Component.translatable(editMode ? "screen.commandtiles.menu.finish_editing" : "screen.commandtiles.menu.edit"),
                ignored -> {
                    editMode = !editMode;
                    page = 0;
                    rebuildWidgets();
                }
        ).pos(footerX, footerY).size(FOOTER_BUTTON_WIDTH, 20).build());

        Button addButton = Button.builder(
                Component.translatable("screen.commandtiles.menu.add"),
                ignored -> openTile(group, null)
        ).pos(footerX + FOOTER_BUTTON_WIDTH + GAP, footerY).size(FOOTER_BUTTON_WIDTH, 20).build();
        addButton.active = editMode;
        addRenderableWidget(addButton);

        addRenderableWidget(Button.builder(
                Component.translatable("screen.commandtiles.menu.settings"),
                ignored -> Minecraft.getInstance().setScreen(CommandTilesClient.createConfigScreen(this))
        ).pos(footerX + 2 * (FOOTER_BUTTON_WIDTH + GAP), footerY).size(FOOTER_BUTTON_WIDTH, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, ignored -> onClose())
                .pos(footerX + 3 * (FOOTER_BUTTON_WIDTH + GAP), footerY)
                .size(FOOTER_BUTTON_WIDTH, 20)
                .build());

        if (pageCount > 1) {
            int navigationY = footerY - 24;
            Button previous = Button.builder(Component.literal("<"), ignored -> {
                page--;
                rebuildWidgets();
            }).pos(width / 2 - 70, navigationY).size(30, 20).build();
            previous.active = page > 0;
            addRenderableWidget(previous);
            addRenderableWidget(new StringWidget(
                    width / 2 - 35,
                    navigationY,
                    70,
                    20,
                    Component.translatable("screen.commandtiles.menu.page", page + 1, pageCount),
                    font
            ));
            Button next = Button.builder(Component.literal(">"), ignored -> {
                page++;
                rebuildWidgets();
            }).pos(width / 2 + 40, navigationY).size(30, 20).build();
            next.active = page + 1 < pageCount;
            addRenderableWidget(next);
        }
    }

    private void openTile(TileGroup group, CommandTile tile) {
        Minecraft.getInstance().setScreen(new TileEditorScreen(this, group, tile));
    }

    private void execute(CommandTile tile, MenuSettings settings) {
        if (!CommandTilesClient.commandExecutor().queue(tile, Minecraft.getInstance())) {
            return;
        }
        boolean closeMenu = tile.closeMenuOverride() == null
                ? settings.closeOnAction()
                : tile.closeMenuOverride();
        if (closeMenu) {
            onClose();
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
