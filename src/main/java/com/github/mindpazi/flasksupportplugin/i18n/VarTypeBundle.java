package com.github.mindpazi.flasksupportplugin.i18n;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.function.Supplier;

public final class VarTypeBundle extends DynamicBundle {
    @NonNls
    private static final String BUNDLE = "messages.VarTypeBundle";
    private static final VarTypeBundle INSTANCE = new VarTypeBundle(); /*
                                                                        * we do this because the construtor of dynamic
                                                                        * bundle is protected
                                                                        */

    private VarTypeBundle() {
        super(BUNDLE);
    }

    public static @NotNull @Nls String message(
            @NotNull @PropertyKey(resourceBundle = BUNDLE) String key,
            @NotNull Object... params) {
        return INSTANCE.getMessage(key, params);
    }

    public static @NotNull Supplier<@Nls String> messagePointer(
            @NotNull @PropertyKey(resourceBundle = BUNDLE) String key,
            @NotNull Object... params) {
        return INSTANCE.getLazyMessage(key, params);
    }
}