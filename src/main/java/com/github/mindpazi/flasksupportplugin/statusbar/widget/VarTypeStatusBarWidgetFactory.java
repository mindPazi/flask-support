package com.github.mindpazi.flasksupportplugin.statusbar.widget;

import com.github.mindpazi.flasksupportplugin.i18n.VarTypeBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class VarTypeStatusBarWidgetFactory implements StatusBarWidgetFactory {
    private static final Logger LOG = Logger.getInstance(VarTypeStatusBarWidgetFactory.class);
    @NonNls
    public static final String WIDGET_DISPLAY_NAME = "widget.display.name";
    private static final Supplier<String> displayNameMsg = () -> VarTypeBundle.message("widget.display.name");

    private final Map<Project, Boolean> projectsWithOpenEditors = new ConcurrentHashMap<>();

    private volatile boolean listenerRegistered = false;

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
        setupListenerIfNeeded();

        return checkAndUpdateOpenEditorsState(project);
    }

    private void setupListenerIfNeeded() {
        if (!listenerRegistered) {
            synchronized (this) {
                if (!listenerRegistered) {
                    EditorFactory.getInstance().addEditorFactoryListener(new EditorFactoryListener() {
                        @Override
                        public void editorCreated(@NotNull EditorFactoryEvent event) {
                            Project project = event.getEditor().getProject();
                            if (project != null) {
                                projectsWithOpenEditors.put(project, true);
                                updateWidgetVisibility(project);
                            }
                        }

                        @Override
                        public void editorReleased(@NotNull EditorFactoryEvent event) {
                            Project project = event.getEditor().getProject();
                            if (project != null) {
                                checkAndUpdateOpenEditorsState(project);
                                updateWidgetVisibility(project);
                            }
                        }
                    }, ApplicationManager.getApplication());

                    listenerRegistered = true;
                    LOG.info("Editor factory listener registered");
                }
            }
        }
    }

    private boolean checkAndUpdateOpenEditorsState(@NotNull Project project) {
        if (project.isDisposed()) {
            projectsWithOpenEditors.remove(project);
            return false;
        }

        boolean hasOpenEditors = FileEditorManager.getInstance(project).getAllEditors().length > 0;
        projectsWithOpenEditors.put(project, hasOpenEditors);
        return hasOpenEditors;
    }

    private void updateWidgetVisibility(@NotNull Project project) {
        if (project.isDisposed()) {
            return;
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
                if (statusBar != null) {
                    boolean hasOpenEditors = projectsWithOpenEditors.getOrDefault(project, false);

                    if (hasOpenEditors) {
                        if (statusBar.getWidget(VarTypeStatusBarWidget.ID) == null) {
                            LOG.info("Adding widget as files are open");
                            statusBar.addWidget(createWidget(project), project);
                        } else {
                            statusBar.updateWidget(VarTypeStatusBarWidget.ID);
                        }
                    } else {
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