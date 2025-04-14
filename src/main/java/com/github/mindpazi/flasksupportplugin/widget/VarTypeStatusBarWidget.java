package com.github.mindpazi.flasksupportplugin.widget;

import com.github.mindpazi.flasksupportplugin.i18n.VarTypeBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.function.Supplier;

public class VarTypeStatusBarWidget extends EditorBasedWidget implements StatusBarWidget.TextPresentation {
    public static final String ID = "VarType";
    private String currentVarType = "";
    private final Supplier<String> notAvailableMsg = () -> VarTypeBundle.message("widget.type.not.available");
    private final Supplier<String> tooltipMsg = () -> VarTypeBundle.message("widget.tooltip");

    public VarTypeStatusBarWidget(@NotNull Project project) {
        super(project);
    }

    @Override
    public @NotNull String ID() {
        return ID;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        super.install(statusBar);
    }

    @Override
    public @Nullable WidgetPresentation getPresentation() {
        return this;
    }

    @Override
    public @NotNull String getText() {
        return currentVarType.isEmpty() ? notAvailableMsg.get() : currentVarType;
    }

    public void updateVarType(@Nullable String VarType) {
        currentVarType = VarType != null ? VarType : "";
        if (myStatusBar != null) {
            myStatusBar.updateWidget(ID());
        }
    }

    @Override
    public float getAlignment() {
        return Component.LEFT_ALIGNMENT;
    }

    @Override
    public @Nullable @Nls String getTooltipText() {
        return tooltipMsg.get();
    }

    @Override
    public @Nullable Consumer<MouseEvent> getClickConsumer() {
        return null;
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}