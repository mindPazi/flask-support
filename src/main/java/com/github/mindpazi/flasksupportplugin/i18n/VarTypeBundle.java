package com.github.mindpazi.flasksupportplugin.i18n;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.function.Supplier;

/**
 * Internationalization bundle for VarType plugin.
 * This class handles message loading from properties files.
 */
public final class VarTypeBundle extends DynamicBundle {
    @NonNls
    private static final String BUNDLE = "messages.VarTypeBundle";
    private static final VarTypeBundle INSTANCE = new VarTypeBundle();

    private VarTypeBundle() {
        super(BUNDLE);
    }

    /**
     * Gets a localized message from the bundle.
     */
    public static @NotNull @Nls String message(
            @NotNull @PropertyKey(resourceBundle = BUNDLE) String key,
            @NotNull Object... params) {
        return INSTANCE.getMessage(key, params);
    }

    /**
     * Creates a lazy message supplier that will only resolve the message when
     * needed.
     */
    public static @NotNull Supplier<@Nls String> messagePointer(
            @NotNull @PropertyKey(resourceBundle = BUNDLE) String key,
            @NotNull Object... params) {
        return INSTANCE.getLazyMessage(key, params);
    }
}