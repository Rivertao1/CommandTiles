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

public final class CommandTilesConfig {
    public static final int CURRENT_VERSION = 1;

    private int version = CURRENT_VERSION;
    private MenuSettings settings = new MenuSettings();
    private String activeProfileId = "default";
    private List<Profile> profiles = new ArrayList<>(List.of(Profile.createDefault()));

    public int version() {
        return version;
    }

    public MenuSettings settings() {
        return settings;
    }

    public String activeProfileId() {
        return activeProfileId;
    }

    public List<Profile> profiles() {
        return profiles;
    }

    public void setActiveProfileId(String activeProfileId) {
        this.activeProfileId = activeProfileId;
    }

    public Profile activeProfile() {
        return profiles.stream()
                .filter(profile -> profile.id().equals(activeProfileId))
                .findFirst()
                .orElse(profiles.getFirst());
    }

    void validate() {
        if (version > CURRENT_VERSION) {
            throw new IllegalStateException(
                    "Config version " + version + " is newer than supported version " + CURRENT_VERSION
            );
        }
        version = CURRENT_VERSION;
        if (settings == null) {
            settings = new MenuSettings();
        }
        settings.validate();
        if (profiles == null) {
            profiles = new ArrayList<>();
        }
        profiles.removeIf(profile -> profile == null);
        if (profiles.isEmpty()) {
            profiles.add(Profile.createDefault());
        }

        Set<String> usedProfileIds = new HashSet<>();
        profiles.forEach(profile -> profile.validate(usedProfileIds));
        if (activeProfileId == null || !usedProfileIds.contains(activeProfileId)) {
            activeProfileId = profiles.getFirst().id();
        }
    }
}
