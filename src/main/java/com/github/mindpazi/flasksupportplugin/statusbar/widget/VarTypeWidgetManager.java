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

public class VarTypeWidgetManager {
    private static final Logger LOG = Logger.getInstance(VarTypeWidgetManager.class);
    private final Project project;
    private final static Supplier<String> applicationNotAvailableMsg = VarTypeBundle
            .messagePointer("log.application.not.available");
    private final static Supplier<String> projectNullMsg = VarTypeBundle.messagePointer("log.project.null");

    public VarTypeWidgetManager(@NotNull Project project) {
        this.project = project;
    }

    public void updateStatusBarWidget(@Nullable String varType) {
        if (ApplicationManager.getApplication() == null || ApplicationManager.getApplication().isDisposed()) {
            LOG.warn(applicationNotAvailableMsg.get());
            return;
        }

        if (project == null || project.isDisposed()) {
            LOG.warn(projectNullMsg.get());
            return;
        }

        try {
            WindowManager windowManager = WindowManager.getInstance();
            if (windowManager == null) {
                return;
            }

            StatusBar statusBar = windowManager.getStatusBar(project);
            if (statusBar == null) {
                String statusbarNullMsg = VarTypeBundle.message("log.statusbar.null");
                LOG.warn(statusbarNullMsg);
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
            String errorUpdateStatusbarMsg = VarTypeBundle.message("log.error.update.statusbar");
            LOG.error(errorUpdateStatusbarMsg, e);
        }
    }
}