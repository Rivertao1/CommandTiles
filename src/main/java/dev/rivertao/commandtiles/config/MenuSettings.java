/*
 * Copyright (C) 2026 River_tao
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 */
package dev.rivertao.commandtiles.config;

public final class MenuSettings {
    private static final int MIN_BUTTONS_PER_ROW = 1;
    private static final int MAX_BUTTONS_PER_ROW = 12;
    private static final int MIN_VISIBLE_ROWS = 1;
    private static final int MAX_VISIBLE_ROWS = 10;

    private int buttonsPerRow = 5;
    private int visibleRows = 3;
    private boolean closeOnAction = true;
    private boolean showExecutionMessage = true;

    public int buttonsPerRow() {
        return buttonsPerRow;
    }

    public int visibleRows() {
        return visibleRows;
    }

    public boolean closeOnAction() {
        return closeOnAction;
    }

    public boolean showExecutionMessage() {
        return showExecutionMessage;
    }

    public void setButtonsPerRow(int buttonsPerRow) {
        this.buttonsPerRow = buttonsPerRow;
    }

    public void setVisibleRows(int visibleRows) {
        this.visibleRows = visibleRows;
    }

    public void setCloseOnAction(boolean closeOnAction) {
        this.closeOnAction = closeOnAction;
    }

    public void setShowExecutionMessage(boolean showExecutionMessage) {
        this.showExecutionMessage = showExecutionMessage;
    }

    void validate() {
        buttonsPerRow = Math.clamp(buttonsPerRow, MIN_BUTTONS_PER_ROW, MAX_BUTTONS_PER_ROW);
        visibleRows = Math.clamp(visibleRows, MIN_VISIBLE_ROWS, MAX_VISIBLE_ROWS);
    }
}
