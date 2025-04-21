package com.github.mindpazi.flasksupportplugin.statusbar;

import com.github.mindpazi.flasksupportplugin.i18n.VarTypeBundle;
import com.github.mindpazi.flasksupportplugin.statusbar.widget.VarTypeStatusBarWidget;
import com.github.mindpazi.flasksupportplugin.statusbar.widget.VarTypeWidgetManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;

public class VarTypeStartupActivity implements StartupActivity, DumbAware {
    private static final Logger LOG = Logger.getInstance(VarTypeStartupActivity.class);

    @Override
    public void runActivity(@NotNull Project project) {

        if (project.isDisposed()) {
            return;
        }

        String startupMessage = VarTypeBundle.message("log.startup.running", project.getName());
        LOG.info(startupMessage);

        try {
            if (EditorFactory.getInstance() == null) {
                String editorFactoryNotAvailableMsg = VarTypeBundle.message("log.editor.factory.not.available");
                LOG.warn(editorFactoryNotAvailableMsg);
                return;
            }

            VarTypeListener listener = new VarTypeListener(project);
            EditorFactory.getInstance().getEventMulticaster().addCaretListener(listener, project);
            String startupRegisteredMsg = VarTypeBundle.message("log.startup.registered");
            LOG.info(startupRegisteredMsg);

            ApplicationManager.getApplication().invokeLater(() -> {
                if (!project.isDisposed()) {

                    VarTypeWidgetManager widgetManager = new VarTypeWidgetManager(project);
                    widgetManager.updateStatusBarWidget(VarTypeBundle.message("widget.type.not.available"));

                    StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
                    if (statusBar != null) {
                        statusBar.updateWidget(VarTypeStatusBarWidget.ID);
                    }
                }
            });
        } catch (Exception e) {
            String startupFailedMsg = VarTypeBundle.message("log.startup.failed");
            LOG.error(startupFailedMsg, e);
        }
    }
}