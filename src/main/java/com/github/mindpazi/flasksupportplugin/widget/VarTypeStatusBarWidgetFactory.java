package com.github.mindpazi.flasksupportplugin.widget;

import com.github.mindpazi.flasksupportplugin.i18n.VarTypeBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class VarTypeStatusBarWidgetFactory implements StatusBarWidgetFactory {
    @NonNls
    public static final String WIDGET_DISPLAY_NAME = "widget.display.name";
    private final Supplier<String> displayNameMsg = () -> VarTypeBundle.message("widget.display.name");

    @Override
    public @NotNull String getId() {
        return VarTypeStatusBarWidget.ID;
    }

    @Override
    public @Nls @NotNull String getDisplayName() {
        return displayNameMsg.get();
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        return true;
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new VarTypeStatusBarWidget(project);
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        widget.dispose();
    }

    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return true;
    }
}