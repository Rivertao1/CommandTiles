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

public final class Profile {
    private String id;
    private String name;
    private List<TileGroup> groups;

    public Profile() {
        this(ConfigIds.randomId(), "New profile", new ArrayList<>(List.of(TileGroup.createDefault())));
    }

    private Profile(String id, String name, List<TileGroup> groups) {
        this.id = id;
        this.name = name;
        this.groups = groups;
    }

    static Profile createDefault() {
        return new Profile("default", "Default", new ArrayList<>(List.of(TileGroup.createDefault())));
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public List<TileGroup> groups() {
        return groups;
    }

    public void setName(String name) {
        this.name = name;
    }

    void validate(Set<String> usedIds) {
        id = ConfigIds.unique(id, usedIds);
        name = ConfigIds.displayName(name, "New profile");
        if (groups == null) {
            groups = new ArrayList<>();
        }
        groups.removeIf(group -> group == null);
        if (groups.isEmpty()) {
            groups.add(TileGroup.createDefault());
        }
        Set<String> usedGroupIds = new HashSet<>();
        groups.forEach(group -> group.validate(usedGroupIds));
    }
}
