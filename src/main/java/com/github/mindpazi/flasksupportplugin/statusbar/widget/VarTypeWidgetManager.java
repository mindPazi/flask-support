package com.github.mindpazi.flasksupportplugin.statusbar.widget;

import com.github.mindpazi.flasksupportplugin.i18n.VarTypeBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Class responsible for interacting with the status bar widget.
 * Implements the composition pattern by separating the widget update logic from
 * the listener.
 */
public class VarTypeWidgetManager {
    private static final Logger LOG = Logger.getInstance(VarTypeWidgetManager.class);
    private final Project project;
    private final Supplier<String> statusbarNullMsg = () -> VarTypeBundle.message("log.statusbar.null");
    private final Supplier<String> errorUpdateStatusbarMsg = () -> VarTypeBundle.message("log.error.update.statusbar");

    public VarTypeWidgetManager(@NotNull Project project) {
        this.project = project;
    }

    /**
     * Updates the status bar widget with the provided variable type.
     *
     * @param varType The variable type to display or null if there is no type to
     *                show
     */
    public void updateStatusBarWidget(@Nullable String varType) {
        if (ApplicationManager.getApplication() == null || ApplicationManager.getApplication().isDisposed()) {
            return;
        }

        if (project == null || project.isDisposed()) {
            return;
        }

        try {
            WindowManager windowManager = WindowManager.getInstance();
            if (windowManager == null) {
                return;
            }

            StatusBar statusBar = windowManager.getStatusBar(project);
            if (statusBar == null) {
                LOG.warn(statusbarNullMsg.get());
                return;
            }

            VarTypeStatusBarWidget widget = (VarTypeStatusBarWidget) statusBar.getWidget(VarTypeStatusBarWidget.ID);
            if (widget != null) {
                widget.updateVarType(varType);
                LOG.info(VarTypeBundle.message("log.statusbar.updated", (varType != null ? varType : "null")));
            } else {
                LOG.warn(VarTypeBundle.message("log.widget.not.found", VarTypeStatusBarWidget.ID));
            }
        } catch (Exception e) {
            LOG.error(errorUpdateStatusbarMsg.get(), e);
        }
    }
}