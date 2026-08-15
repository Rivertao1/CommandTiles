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
    private static final int GAP = 5;
    private static final int TILE_HEIGHT = 44;
    private static final int PREFERRED_TILE_WIDTH = 138;
    private static final int MIN_TILE_WIDTH = 104;

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

        int availableColumns = Math.max(1, (width - 24 + GAP) / (MIN_TILE_WIDTH + GAP));
        int columns = Math.max(1, Math.min(settings.buttonsPerRow(), availableColumns));
        int rows = Math.max(1, Math.min(
                settings.visibleRows(),
                Math.max(1, (height - 78) / (TILE_HEIGHT + GAP))
        ));
        int pageSize = columns * rows;
        int entryCount = tiles.size() + (editMode ? 1 : 0);
        int pageCount = Math.max(1, (entryCount + pageSize - 1) / pageSize);
        page = Math.clamp(page, 0, pageCount - 1);

        Component heading = editMode
                ? Component.translatable("screen.commandtiles.menu.editing_heading", profile.name(), group.name())
                : Component.literal(profile.name() + " / " + group.name());
        addRenderableWidget(new StringWidget(width / 2 - 180, 8, 360, 20, heading, font));

        int gridWidth = Math.min(width - 24, columns * PREFERRED_TILE_WIDTH + (columns - 1) * GAP);
        int tileWidth = (gridWidth - (columns - 1) * GAP) / columns;
        int gridX = (width - gridWidth) / 2;
        int gridY = 32;
        int firstEntry = page * pageSize;
        int lastEntry = Math.min(entryCount, firstEntry + pageSize);

        for (int index = firstEntry; index < lastEntry; index++) {
            int position = index - firstEntry;
            int x = gridX + (position % columns) * (tileWidth + GAP);
            int y = gridY + (position / columns) * (TILE_HEIGHT + GAP);
            if (index < tiles.size()) {
                CommandTile tile = tiles.get(index);
                addRenderableWidget(new CommandTileWidget(
                        x,
                        y,
                        tileWidth,
                        TILE_HEIGHT,
                        tile,
                        editMode,
                        () -> {
                            if (editMode) {
                                openTile(group, tile);
                            } else {
                                execute(tile, settings);
                            }
                        }
                ));
            } else {
                addRenderableWidget(new AddTileWidget(
                        x, y, tileWidth, TILE_HEIGHT, () -> openTile(group, null)
                ));
            }
        }

        if (tiles.isEmpty() && !editMode) {
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
        if (editMode) {
            addRenderableWidget(Button.builder(
                    Component.translatable("screen.commandtiles.menu.finish_editing"),
                    ignored -> setEditMode(false)
            ).pos(width / 2 - 75, footerY).size(150, 20).build());
        } else {
            int buttonWidth = Math.min(100, (width - 28) / 3);
            int footerWidth = buttonWidth * 3 + GAP * 2;
            int footerX = (width - footerWidth) / 2;
            addRenderableWidget(Button.builder(
                    Component.translatable("screen.commandtiles.menu.edit"),
                    ignored -> setEditMode(true)
            ).pos(footerX, footerY).size(buttonWidth, 20).build());
            addRenderableWidget(Button.builder(
                    Component.translatable("screen.commandtiles.menu.settings"),
                    ignored -> Minecraft.getInstance().setScreen(CommandTilesClient.createConfigScreen(this))
            ).pos(footerX + buttonWidth + GAP, footerY).size(buttonWidth, 20).build());
            addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, ignored -> onClose())
                    .pos(footerX + 2 * (buttonWidth + GAP), footerY)
                    .size(buttonWidth, 20)
                    .build());
        }

        if (pageCount > 1) {
            int navigationY = footerY - 23;
            Button previous = Button.builder(Component.literal("<"), ignored -> changePage(-1))
                    .pos(width / 2 - 70, navigationY).size(30, 20).build();
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
            Button next = Button.builder(Component.literal(">"), ignored -> changePage(1))
                    .pos(width / 2 + 40, navigationY).size(30, 20).build();
            next.active = page + 1 < pageCount;
            addRenderableWidget(next);
        }
    }

    private void setEditMode(boolean editMode) {
        this.editMode = editMode;
        page = 0;
        rebuildWidgets();
    }

    private void changePage(int change) {
        page += change;
        rebuildWidgets();
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
