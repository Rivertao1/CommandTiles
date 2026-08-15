/*
 * Copyright (C) 2026 River_tao
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 */
package dev.rivertao.commandtiles.config;

public final class CommandStep {
    private static final int MAX_DELAY_TICKS = 72_000;

    private String content = "";
    private int delayTicks;
    private boolean enabled = true;

    public CommandStep() {
    }

    public CommandStep(String content, int delayTicks) {
        this.content = content;
        this.delayTicks = delayTicks;
    }

    public String content() {
        return content;
    }

    public int delayTicks() {
        return delayTicks;
    }

    public boolean enabled() {
        return enabled;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setDelayTicks(int delayTicks) {
        this.delayTicks = delayTicks;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    void validate() {
        if (content == null) {
            content = "";
        }
        delayTicks = Math.clamp(delayTicks, 0, MAX_DELAY_TICKS);
    }
}
