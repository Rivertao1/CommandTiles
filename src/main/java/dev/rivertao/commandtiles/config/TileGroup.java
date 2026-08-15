/*
 * Copyright (C) 2026 River_tao
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 */
package dev.rivertao.commandtiles.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TileGroup {
    private String id;
    private String name;
    private List<CommandTile> tiles;

    public TileGroup() {
        this(ConfigIds.randomId(), "New group", new ArrayList<>());
    }

    private TileGroup(String id, String name, List<CommandTile> tiles) {
        this.id = id;
        this.name = name;
        this.tiles = tiles;
    }

    static TileGroup createDefault() {
        return new TileGroup("main", "Main", new ArrayList<>());
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public List<CommandTile> tiles() {
        return tiles;
    }

    public void setName(String name) {
        this.name = name;
    }

    void validate(Set<String> usedIds) {
        id = ConfigIds.unique(id, usedIds);
        name = ConfigIds.displayName(name, "New group");
        if (tiles == null) {
            tiles = new ArrayList<>();
        }
        tiles.removeIf(tile -> tile == null);
        Set<String> usedTileIds = new HashSet<>();
        tiles.forEach(tile -> tile.validate(usedTileIds));
    }
}
