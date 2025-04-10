package com.github.mindpazi.flasksupportplugin.statusbar.startup;

import com.github.mindpazi.flasksupportplugin.statusbar.listener.VarTypeListener;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class VarTypeStartupActivity implements StartupActivity, DumbAware {
    private static final Logger LOG = Logger.getInstance(VarTypeStartupActivity.class);

    @Override
    public void runActivity(@NotNull Project project) {
        LOG.info("VarType startup activity running for project: " + project.getName());

        try {
            VarTypeListener listener = new VarTypeListener(project);
            EditorFactory.getInstance().getEventMulticaster().addCaretListener(listener, project);
            LOG.info("VarType caret listener registered successfully");
        } catch (Exception e) {
            LOG.error("Failed to register VarType caret listener", e);
        }
    }
}