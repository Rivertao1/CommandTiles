/*
 * Copyright (C) 2026 River_tao
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 */
package dev.rivertao.commandtiles.config;

import java.util.Set;
import java.util.UUID;

final class ConfigIds {
    private ConfigIds() {
    }

    static String randomId() {
        return UUID.randomUUID().toString();
    }

    static String unique(String candidate, Set<String> usedIds) {
        String result = candidate == null ? "" : candidate.trim();
        if (result.isEmpty() || usedIds.contains(result)) {
            do {
                result = randomId();
            } while (usedIds.contains(result));
        }
        usedIds.add(result);
        return result;
    }

    static String displayName(String candidate, String fallback) {
        if (candidate == null || candidate.isBlank()) {
            return fallback;
        }
        return candidate.trim();
    }
}
