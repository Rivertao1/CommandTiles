/*
 * Copyright (C) 2026 River_tao
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 */
package dev.rivertao.commandtiles.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.rivertao.commandtiles.CommandTilesClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public final class ConfigManager {
    private static final String CONFIG_FILE_NAME = "config.json";
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private final Path configDirectory;
    private final Path configFile;
    private final Path backupFile;
    private CommandTilesConfig config;

    public ConfigManager() {
        this(FabricLoader.getInstance().getConfigDir().resolve(CommandTilesClient.MOD_ID));
    }

    ConfigManager(Path configDirectory) {
        this.configDirectory = configDirectory;
        this.configFile = configDirectory.resolve(CONFIG_FILE_NAME);
        this.backupFile = configDirectory.resolve(CONFIG_FILE_NAME + ".bak");
    }

    public synchronized CommandTilesConfig load() {
        try {
            Files.createDirectories(configDirectory);
            if (Files.notExists(configFile)) {
                config = new CommandTilesConfig();
                config.validate();
                write(false);
                return config;
            }

            String json = Files.readString(configFile, StandardCharsets.UTF_8);
            CommandTilesConfig loaded = GSON.fromJson(json, CommandTilesConfig.class);
            if (loaded == null) {
                throw new IOException("Config file contains no JSON object");
            }

            String beforeValidation = GSON.toJson(loaded);
            loaded.validate();
            config = loaded;
            if (!beforeValidation.equals(GSON.toJson(loaded))) {
                write(true);
            }
            return config;
        } catch (Exception exception) {
            preserveUnreadableConfig();
            CommandTilesClient.LOGGER.error("Unable to load {}; using defaults", configFile, exception);
            config = new CommandTilesConfig();
            config.validate();
            try {
                write(false);
            } catch (IOException writeException) {
                CommandTilesClient.LOGGER.error("Unable to write default config to {}", configFile, writeException);
            }
            return config;
        }
    }

    public synchronized void save() throws IOException {
        requireLoaded().validate();
        write(true);
    }

    public synchronized CommandTilesConfig get() {
        return requireLoaded();
    }

    public Path configFile() {
        return configFile;
    }

    private CommandTilesConfig requireLoaded() {
        if (config == null) {
            throw new IllegalStateException("Config has not been loaded");
        }
        return config;
    }

    private void write(boolean backupCurrent) throws IOException {
        Files.createDirectories(configDirectory);
        Path temporaryFile = configDirectory.resolve(CONFIG_FILE_NAME + ".tmp");
        Path temporaryBackup = configDirectory.resolve(CONFIG_FILE_NAME + ".bak.tmp");
        byte[] bytes = (GSON.toJson(requireLoaded()) + System.lineSeparator())
                .getBytes(StandardCharsets.UTF_8);

        try {
            try (FileChannel channel = FileChannel.open(
                    temporaryFile,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            )) {
                channel.write(ByteBuffer.wrap(bytes));
                channel.force(true);
            }

            if (backupCurrent && Files.isRegularFile(configFile)) {
                Files.copy(configFile, temporaryBackup, StandardCopyOption.REPLACE_EXISTING);
                moveReplacing(temporaryBackup, backupFile);
            }
            moveReplacing(temporaryFile, configFile);
        } finally {
            Files.deleteIfExists(temporaryFile);
            Files.deleteIfExists(temporaryBackup);
        }
    }

    private void preserveUnreadableConfig() {
        if (!Files.isRegularFile(configFile)) {
            return;
        }
        Path preserved = configDirectory.resolve(
                CONFIG_FILE_NAME + ".unreadable-" + System.currentTimeMillis()
        );
        try {
            Files.copy(configFile, preserved, StandardCopyOption.COPY_ATTRIBUTES);
        } catch (IOException exception) {
            CommandTilesClient.LOGGER.error("Unable to preserve unreadable config as {}", preserved, exception);
        }
    }

    private static void moveReplacing(Path source, Path target) throws IOException {
        try {
            Files.move(
                    source,
                    target,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
