package com.github.mindpazi.flasksupportplugin.statusbar.statusbar;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Classe responsabile dell'interazione con il widget nella status bar.
 * Implementa il pattern di composizione separando la logica di aggiornamento
 * del widget dal listener.
 */
public class VarTypeWidgetManager {
    private static final Logger LOG = Logger.getInstance(VarTypeWidgetManager.class);
    private final Project project;

    public VarTypeWidgetManager(@NotNull Project project) {
        this.project = project;
    }

    /**
     * Aggiorna il widget nella status bar con il tipo di variabile fornito.
     *
     * @param varType Il tipo di variabile da visualizzare o null se non c'è un tipo
     *                da mostrare
     */
    public void updateStatusBarWidget(@Nullable String varType) {
        try {
            StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
            if (statusBar == null) {
                LOG.warn("Status bar is null");
                return;
            }

            VarTypeStatusBarWidget widget = (VarTypeStatusBarWidget) statusBar.getWidget(VarTypeStatusBarWidget.ID);
            if (widget != null) {
                widget.updateVarType(varType);
                LOG.info("Status bar updated with: " + (varType != null ? varType : "null"));
            } else {
                LOG.warn("Widget not found: " + VarTypeStatusBarWidget.ID);
            }
        } catch (Exception e) {
            LOG.error("Error updating status bar", e);
        }
    }
}