/*
 * Copyright (C) 2026 River_tao
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 */
package dev.rivertao.commandtiles.execution;

import dev.rivertao.commandtiles.CommandTilesClient;
import dev.rivertao.commandtiles.config.CommandStep;
import dev.rivertao.commandtiles.config.CommandTile;
import dev.rivertao.commandtiles.config.ExecutionMode;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public final class CommandExecutor {
    private final PriorityQueue<ScheduledAction> pending = new PriorityQueue<>(
            Comparator.comparingLong(ScheduledAction::dueTick)
                    .thenComparingLong(ScheduledAction::sequence)
    );
    private long currentTick;
    private long nextSequence;
    private long nextBatchId;

    public boolean queue(CommandTile tile, Minecraft client) {
        if (client.player == null || client.getConnection() == null) {
            show(client, "message.commandtiles.not_connected");
            return false;
        }
        if (tile.executionMode() != ExecutionMode.SEND) {
            show(client, "message.commandtiles.unsupported_mode");
            return false;
        }

        List<StepSnapshot> steps = new ArrayList<>();
        for (CommandStep step : tile.steps()) {
            String content = step.content().strip();
            if (step.enabled() && !content.isEmpty()) {
                steps.add(new StepSnapshot(content, step.delayTicks()));
            }
        }
        if (steps.isEmpty()) {
            show(client, "message.commandtiles.no_actions");
            return false;
        }

        long batchId = nextBatchId++;
        long delay = 0;
        for (int index = 0; index < steps.size(); index++) {
            StepSnapshot step = steps.get(index);
            delay += step.delayTicks();
            pending.add(new ScheduledAction(
                    batchId,
                    currentTick + delay,
                    nextSequence++,
                    step.content(),
                    index + 1 == steps.size()
            ));
        }
        return true;
    }

    public void tick(Minecraft client) {
        currentTick++;
        if (client.player == null || client.getConnection() == null) {
            clear();
            return;
        }

        while (!pending.isEmpty() && pending.peek().dueTick() <= currentTick) {
            ScheduledAction action = pending.remove();
            try {
                send(client, action.content());
                if (action.lastInBatch()
                        && CommandTilesClient.configManager().get().settings().showExecutionMessage()) {
                    show(client, "message.commandtiles.executed");
                }
            } catch (RuntimeException exception) {
                pending.removeIf(candidate -> candidate.batchId() == action.batchId());
                CommandTilesClient.LOGGER.error("Unable to execute command tile action", exception);
                show(client, "message.commandtiles.execution_failed");
            }
        }
    }

    public void clear() {
        pending.clear();
    }

    private static void send(Minecraft client, String content) {
        if (content.startsWith("/")) {
            String command = content.substring(1).strip();
            if (command.isEmpty()) {
                throw new IllegalArgumentException("Command cannot be empty");
            }
            client.getConnection().sendCommand(command);
        } else {
            client.getConnection().sendChat(content);
        }
    }

    private static void show(Minecraft client, String translationKey) {
        if (client.player != null) {
            client.player.displayClientMessage(Component.translatable(translationKey), true);
        }
    }

    private record StepSnapshot(String content, int delayTicks) {
    }

    private record ScheduledAction(
            long batchId,
            long dueTick,
            long sequence,
            String content,
            boolean lastInBatch
    ) {
    }
}
