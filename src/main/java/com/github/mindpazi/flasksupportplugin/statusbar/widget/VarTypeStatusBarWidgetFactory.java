package com.github.mindpazi.flasksupportplugin.statusbar.widget;

import com.github.mindpazi.flasksupportplugin.i18n.VarTypeBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class VarTypeStatusBarWidgetFactory implements StatusBarWidgetFactory {
    private static final Logger LOG = Logger.getInstance(VarTypeStatusBarWidgetFactory.class);
    @NonNls
    public static final String WIDGET_DISPLAY_NAME = "widget.display.name";
    private final Supplier<String> displayNameMsg = () -> VarTypeBundle.message("widget.display.name");
    private final Map<Project, Boolean> projectHasOpenEditors = new HashMap<>();
    private final Map<Project, MessageBusConnection> projectConnections = new HashMap<>();

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
        // Check if we've already determined the state for this project
        if (!projectHasOpenEditors.containsKey(project)) {
            // Initialize with current state
            boolean hasOpenEditors = FileEditorManager.getInstance(project).getAllEditors().length > 0;
            projectHasOpenEditors.put(project, hasOpenEditors);

            // Setup file editor listener
            setupEditorListeners(project);
        }

        return projectHasOpenEditors.getOrDefault(project, false);
    }

    private void setupEditorListeners(@NotNull Project project) {
        if (projectConnections.containsKey(project)) {
            return; // Already set up
        }

        // Subscribe to file editor events through message bus
        MessageBusConnection connection = project.getMessageBus().connect();
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                projectHasOpenEditors.put(project, true);
                updateWidgetVisibility(project);
            }

            @Override
            public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                boolean hasOpenEditors = source.getAllEditors().length > 0;
                projectHasOpenEditors.put(project, hasOpenEditors);
                updateWidgetVisibility(project);
            }
        });

        // Also listen to editor factory events as a backup
        EditorFactoryListener editorListener = new EditorFactoryListener() {
            @Override
            public void editorCreated(@NotNull EditorFactoryEvent event) {
                if (event.getEditor().getProject() == project) {
                    projectHasOpenEditors.put(project, true);
                    updateWidgetVisibility(project);
                }
            }
        };

        EditorFactory.getInstance().addEditorFactoryListener(editorListener, connection);
        projectConnections.put(project, connection);

        // Dispose connection when project is closed
        Disposer.register(project, () -> {
            connection.disconnect();
            projectConnections.remove(project);
            projectHasOpenEditors.remove(project);
        });
    }

    private void updateWidgetVisibility(@NotNull Project project) {
        if (project.isDisposed()) {
            return;
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
                if (statusBar != null) {
                    boolean hasOpenEditors = projectHasOpenEditors.getOrDefault(project, false);

                    if (hasOpenEditors) {
                        // Se ci sono file aperti, assicuriamoci che il widget sia presente e aggiornato
                        if (statusBar.getWidget(VarTypeStatusBarWidget.ID) == null) {
                            LOG.info("Re-adding widget as files are now open");
                            statusBar.addWidget(createWidget(project), project);
                        } else {
                            // Widget esiste già, aggiorniamolo
                            statusBar.updateWidget(VarTypeStatusBarWidget.ID);
                        }
                    } else {
                        // Se non ci sono file aperti, rimuoviamo completamente il widget
                        if (statusBar.getWidget(VarTypeStatusBarWidget.ID) != null) {
                            LOG.info("Removing widget as all files are closed");
                            statusBar.removeWidget(VarTypeStatusBarWidget.ID);
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("Error updating widget visibility", e);
            }
        });
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