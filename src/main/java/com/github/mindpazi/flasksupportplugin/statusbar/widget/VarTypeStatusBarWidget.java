package com.github.mindpazi.flasksupportplugin.statusbar.widget;

import com.github.mindpazi.flasksupportplugin.i18n.VarTypeBundle;
import com.intellij.openapi.diagnostic.Logger;
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
    private static final Logger LOG = Logger.getInstance(VarTypeStatusBarWidget.class);
    public static final String ID = "VarType";
    private String currentVarType = "";
    private boolean disposed = false;
    private static final Supplier<String> notAvailableMsg = () -> VarTypeBundle.message("widget.type.not.available");
    private static final Supplier<String> tooltipMsg = VarTypeBundle.messagePointer("widget.tooltip");

    public VarTypeStatusBarWidget(@NotNull Project project) {
        super(project);
        LOG.debug("Widget instance created for project: " + project.getName());
    }

    @Override
    public @NotNull String ID() {
        return ID;
    }

    @Override
    public @Nullable WidgetPresentation getPresentation() {
        return this;
    }

    @Override
    public @NotNull String getText() {
        if (disposed) {
            return "";
        }
        return currentVarType.isEmpty() ? notAvailableMsg.get() : currentVarType;
    }

    public void updateVarType(@Nullable String VarType) {
        if (disposed) {
            return;
        }
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
        if (disposed) {
            return null;
        }
        return tooltipMsg.get();
    }

    @Override
    public @Nullable Consumer<MouseEvent> getClickConsumer() {
        return null;
    }

    /**
     * Questo metodo viene chiamato quando il widget viene rimosso dalla barra di
     * stato
     * o quando l'applicazione viene chiusa.
     */
    @Override
    public void dispose() {
        if (disposed) {
            return;
        }

        LOG.debug("Disposing widget");
        disposed = true;
        currentVarType = "";

        if (myStatusBar != null) {
            StatusBar statusBar = myStatusBar;
            myStatusBar = null;

            try {
                if (statusBar.getWidget(ID()) != null) {
                    LOG.debug("Removing widget from status bar during dispose");
                    statusBar.removeWidget(ID());
                }

                statusBar.getComponent().repaint();
            } catch (Exception e) {
                LOG.warn("Error during widget disposal", e);
            }
        }

        super.dispose();
    }
}