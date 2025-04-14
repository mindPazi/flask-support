package com.github.mindpazi.flasksupportplugin.activity;

import com.github.mindpazi.flasksupportplugin.i18n.VarTypeBundle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class VarTypeStartupActivity implements StartupActivity, DumbAware {
    private static final Logger LOG = Logger.getInstance(VarTypeStartupActivity.class);
    private final Supplier<String> startupRunningMsg = () -> VarTypeBundle.message("log.startup.running");
    private final Supplier<String> startupRegisteredMsg = () -> VarTypeBundle.message("log.startup.registered");
    private final Supplier<String> startupFailedMsg = () -> VarTypeBundle.message("log.startup.failed");
    private final Supplier<String> editorFactoryNotAvailableMsg = () -> VarTypeBundle
            .message("log.editor.factory.not.available");

    @Override
    public void runActivity(@NotNull Project project) {
        // Check if the project is disposed before proceeding with the activity.
        if (project.isDisposed()) {
            return;
        }

        // Format and supply the project name for the message
        String formattedMessage = startupRunningMsg.get().replace("{0}", project.getName());
        LOG.info(formattedMessage);

        try {
            if (EditorFactory.getInstance() == null) {
                LOG.warn(editorFactoryNotAvailableMsg.get());
                return;
            }

            VarTypeListener listener = new VarTypeListener(project);
            EditorFactory.getInstance().getEventMulticaster().addCaretListener(listener, project);
            LOG.info(startupRegisteredMsg.get());
        } catch (Exception e) {
            LOG.error(startupFailedMsg.get(), e);
        }
    }
}