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
    public static final int SUPPORTED_MODIFIERS = InputConstants.MOD_SHIFT
            | InputConstants.MOD_CONTROL
            | InputConstants.MOD_ALT
            | InputConstants.MOD_SUPER;

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
        appendModifier(result, InputConstants.MOD_CONTROL, "Ctrl");
        appendModifier(result, InputConstants.MOD_ALT, "Alt");
        appendModifier(result, InputConstants.MOD_SHIFT, "Shift");
        appendModifier(result, InputConstants.MOD_SUPER, "Super");
        return result.append(key().getDisplayName());
    }

    public boolean isDown(Window window) {
        InputConstants.Key key = key();
        int mainModifier = modifierMask(key);
        if (!isBound() || (currentModifiers(window) & ~mainModifier) != modifiers) {
            return false;
        }
        if (key.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(window.handle(), key.getValue()) == GLFW.GLFW_PRESS;
        }
        return InputConstants.isKeyDown(window, key.getValue());
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
            case InputConstants.KEY_LCONTROL, InputConstants.KEY_RCONTROL,
                 InputConstants.KEY_LALT, InputConstants.KEY_RALT,
                 InputConstants.KEY_LSHIFT, InputConstants.KEY_RSHIFT,
                 InputConstants.KEY_LSUPER, InputConstants.KEY_RSUPER -> true;
            default -> false;
        };
    }

    public static int modifierMask(InputConstants.Key key) {
        if (key.getType() != InputConstants.Type.KEYSYM) {
            return 0;
        }
        return switch (key.getValue()) {
            case InputConstants.KEY_LCONTROL, InputConstants.KEY_RCONTROL -> InputConstants.MOD_CONTROL;
            case InputConstants.KEY_LALT, InputConstants.KEY_RALT -> InputConstants.MOD_ALT;
            case InputConstants.KEY_LSHIFT, InputConstants.KEY_RSHIFT -> InputConstants.MOD_SHIFT;
            case InputConstants.KEY_LSUPER, InputConstants.KEY_RSUPER -> InputConstants.MOD_SUPER;
            default -> 0;
        };
    }

    private void appendModifier(MutableComponent target, int mask, String name) {
        if ((modifiers & mask) != 0) {
            target.append(name).append(" + ");
        }
    }

    private static int currentModifiers(Window window) {
        int modifiers = 0;
        if (down(window, InputConstants.KEY_LCONTROL) || down(window, InputConstants.KEY_RCONTROL)) {
            modifiers |= InputConstants.MOD_CONTROL;
        }
        if (down(window, InputConstants.KEY_LALT) || down(window, InputConstants.KEY_RALT)) {
            modifiers |= InputConstants.MOD_ALT;
        }
        if (down(window, InputConstants.KEY_LSHIFT) || down(window, InputConstants.KEY_RSHIFT)) {
            modifiers |= InputConstants.MOD_SHIFT;
        }
        if (down(window, InputConstants.KEY_LSUPER) || down(window, InputConstants.KEY_RSUPER)) {
            modifiers |= InputConstants.MOD_SUPER;
        }
        return modifiers;
    }

    private static boolean down(Window window, int key) {
        return InputConstants.isKeyDown(window, key);
    }
}
