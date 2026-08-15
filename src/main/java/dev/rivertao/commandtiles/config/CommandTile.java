/*
 * Copyright (C) 2026 River_tao
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 */
package dev.rivertao.commandtiles.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class CommandTile {
    private String id = ConfigIds.randomId();
    private String name = "New tile";
    private String description = "";
    private String icon = "minecraft:command_block";
    private boolean visible = true;
    private Boolean closeMenuOverride;
    private ExecutionMode executionMode = ExecutionMode.SEND;
    private KeyChord keyBinding = new KeyChord();
    private List<CommandStep> steps = new ArrayList<>();

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public String icon() {
        return icon;
    }

    public boolean visible() {
        return visible;
    }

    public Boolean closeMenuOverride() {
        return closeMenuOverride;
    }

    public ExecutionMode executionMode() {
        return executionMode;
    }

    public KeyChord keyBinding() {
        return keyBinding;
    }

    public List<CommandStep> steps() {
        return steps;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setCloseMenuOverride(Boolean closeMenuOverride) {
        this.closeMenuOverride = closeMenuOverride;
    }

    public void setExecutionMode(ExecutionMode executionMode) {
        this.executionMode = executionMode;
    }

    public void setKeyBinding(KeyChord keyBinding) {
        this.keyBinding = keyBinding;
    }

    void validate(Set<String> usedIds) {
        id = ConfigIds.unique(id, usedIds);
        name = ConfigIds.displayName(name, "New tile");
        if (description == null) {
            description = "";
        }
        if (icon == null || icon.isBlank()) {
            icon = "minecraft:command_block";
        }
        if (executionMode == null) {
            executionMode = ExecutionMode.SEND;
        }
        if (keyBinding == null) {
            keyBinding = new KeyChord();
        }
        keyBinding.validate();
        if (steps == null) {
            steps = new ArrayList<>();
        }
        steps.removeIf(step -> step == null);
        steps.forEach(CommandStep::validate);
    }
}
