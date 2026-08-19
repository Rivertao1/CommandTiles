/*
 * Copyright (C) 2026 River_tao
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 */
package dev.rivertao.commandtiles.config;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.lwjgl.glfw.GLFW;

public final class KeyChord {
    public static final int SUPPORTED_MODIFIERS = GLFW.GLFW_MOD_SHIFT
            | GLFW.GLFW_MOD_CONTROL
            | GLFW.GLFW_MOD_ALT
            | GLFW.GLFW_MOD_SUPER;

    private String keyName = InputConstants.UNKNOWN.getName();
    private int modifiers;

    public KeyChord() {
    }

    public KeyChord(InputConstants.Key key, int modifiers) {
        this.keyName = key.getName();
        this.modifiers = modifiers & SUPPORTED_MODIFIERS;
    }

    public KeyChord(KeyChord other) {
        this(other.key(), other.modifiers);
    }

    public InputConstants.Key key() {
        try {
            return InputConstants.getKey(keyName);
        } catch (IllegalArgumentException ignored) {
            return InputConstants.UNKNOWN;
        }
    }

    public int modifiers() {
        return modifiers;
    }

    public boolean isBound() {
        return !key().equals(InputConstants.UNKNOWN);
    }

    public boolean sameBinding(KeyChord other) {
        return isBound() && other != null && key().equals(other.key()) && modifiers == other.modifiers;
    }

    public Component displayName() {
        if (!isBound()) {
            return Component.translatable("screen.commandtiles.keybind.unbound");
        }
        MutableComponent result = Component.empty();
        appendModifier(result, GLFW.GLFW_MOD_CONTROL, "Ctrl");
        appendModifier(result, GLFW.GLFW_MOD_ALT, "Alt");
        appendModifier(result, GLFW.GLFW_MOD_SHIFT, "Shift");
        appendModifier(result, GLFW.GLFW_MOD_SUPER, "Super");
        return result.append(key().getDisplayName());
    }

    public boolean isDown(Window window) {
        InputConstants.Key key = key();
        int mainModifier = modifierMask(key);
        if (!isBound() || (currentModifiers(window) & ~mainModifier) != modifiers) {
            return false;
        }
        if (key.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(window.getWindow(), key.getValue()) == GLFW.GLFW_PRESS;
        }
        return InputConstants.isKeyDown(window.getWindow(), key.getValue());
    }

    void validate() {
        InputConstants.Key parsed = key();
        keyName = parsed.getName();
        modifiers &= SUPPORTED_MODIFIERS;
    }

    public static boolean isModifierKey(InputConstants.Key key) {
        if (key.getType() != InputConstants.Type.KEYSYM) {
            return false;
        }
        return switch (key.getValue()) {
            case GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL,
                 GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT,
                 GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT,
                 GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER -> true;
            default -> false;
        };
    }

    public static int modifierMask(InputConstants.Key key) {
        if (key.getType() != InputConstants.Type.KEYSYM) {
            return 0;
        }
        return switch (key.getValue()) {
            case GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL -> GLFW.GLFW_MOD_CONTROL;
            case GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT -> GLFW.GLFW_MOD_ALT;
            case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT -> GLFW.GLFW_MOD_SHIFT;
            case GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER -> GLFW.GLFW_MOD_SUPER;
            default -> 0;
        };
    }

    private void appendModifier(MutableComponent target, int mask, String name) {
        if ((modifiers & mask) != 0) {
            target.append(name).append(" + ");
        }
    }

    public static int currentModifiers(Window window) {
        int modifiers = 0;
        if (down(window, GLFW.GLFW_KEY_LEFT_CONTROL) || down(window, GLFW.GLFW_KEY_RIGHT_CONTROL)) {
            modifiers |= GLFW.GLFW_MOD_CONTROL;
        }
        if (down(window, GLFW.GLFW_KEY_LEFT_ALT) || down(window, GLFW.GLFW_KEY_RIGHT_ALT)) {
            modifiers |= GLFW.GLFW_MOD_ALT;
        }
        if (down(window, GLFW.GLFW_KEY_LEFT_SHIFT) || down(window, GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            modifiers |= GLFW.GLFW_MOD_SHIFT;
        }
        if (down(window, GLFW.GLFW_KEY_LEFT_SUPER) || down(window, GLFW.GLFW_KEY_RIGHT_SUPER)) {
            modifiers |= GLFW.GLFW_MOD_SUPER;
        }
        return modifiers;
    }

    private static boolean down(Window window, int key) {
        return InputConstants.isKeyDown(window.getWindow(), key);
    }
}
