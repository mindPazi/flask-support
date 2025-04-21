package com.github.mindpazi.flasksupportplugin.statusbar.widget;

import com.github.mindpazi.flasksupportplugin.i18n.VarTypeBundle;
import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class VarTypeStatusBarWidgetFactory implements StatusBarWidgetFactory {
    private static final Logger LOG = Logger.getInstance(VarTypeStatusBarWidgetFactory.class);

    private static final String WIDGET_ID = VarTypeStatusBarWidget.ID;

    private static final Supplier<String> EDITOR_CREATED_MSG = VarTypeBundle.messagePointer("log.editor.created");
    private static final Supplier<String> EDITOR_RELEASED_MSG = VarTypeBundle.messagePointer("log.editor.released");
    private static final Supplier<String> WIDGET_ADDED_MSG = VarTypeBundle.messagePointer("log.widget.added");
    private static final Supplier<String> WIDGET_REMOVED_MSG = VarTypeBundle.messagePointer("log.widget.removed");

    private final Map<Project, Boolean> projectsWithOpenEditors = new ConcurrentHashMap<>();
    private final Map<Project, Integer> editorCountByProject = new ConcurrentHashMap<>();

    private volatile boolean listenerRegistered = false;

    public VarTypeStatusBarWidgetFactory() {
        LOG.debug(VarTypeBundle.message("log.factory.initialized"));

        ApplicationManager.getApplication().getMessageBus().connect()
                .subscribe(AppLifecycleListener.TOPIC, new AppLifecycleListener() {
                    @Override
                    public void appWillBeClosed(boolean isRestart) {
                        LOG.debug(VarTypeBundle.message("log.app.will.be.closed"));
                        cleanup();
                    }
                });
    }

    private void cleanup() {
        projectsWithOpenEditors.clear();
        editorCountByProject.clear();
    }

    @Override
    public @NotNull String getId() {
        return WIDGET_ID;
    }

    @Override
    public @Nls @NotNull String getDisplayName() {
        return VarTypeBundle.message("widget.display.name");
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        if (project.isDisposed()) {
            return false;
        }

        setupListener();

        // Forziamo a true per debug - RIMUOVERE DOPO I TEST
        LOG.info("isAvailable called for project: " + project.getName() + " - FORZATO A TRUE per debug");
        return true;

        // Codice originale commentato
        /*
         * if (!editorCountByProject.containsKey(project)) {
         * int openEditors = EditorFactory.getInstance().getAllEditors().length;
         * if (openEditors > 0) {
         * editorCountByProject.put(project, openEditors);
         * projectsWithOpenEditors.put(project, true);
         * LOG.debug("Initialized project with " + openEditors + " open editors");
         * return true;
         * }
         * }
         * 
         * return hasOpenEditors(project);
         */
    }

    private boolean hasOpenEditors(@NotNull Project project) {
        if (project.isDisposed()) {
            projectsWithOpenEditors.remove(project);
            editorCountByProject.remove(project);
            return false;
        }

        int count = editorCountByProject.getOrDefault(project, 0);
        boolean hasOpenEditors = count > 0;
        projectsWithOpenEditors.put(project, hasOpenEditors);
        return hasOpenEditors;
    }

    private void setupListener() {
        if (!listenerRegistered) {
            synchronized (this) {
                if (!listenerRegistered) {
                    EditorFactory.getInstance().addEditorFactoryListener(new EditorFactoryListener() {
                        @Override
                        public void editorCreated(@NotNull EditorFactoryEvent event) {
                            Project project = event.getEditor().getProject();
                            if (project != null) {
                                int currentCount = editorCountByProject.getOrDefault(project, 0);
                                int newCount = currentCount + 1;
                                editorCountByProject.put(project, newCount);
                                projectsWithOpenEditors.put(project, true);

                                LOG.debug(String.format(EDITOR_CREATED_MSG.get(), project.getName(), newCount));

                                updateWidgetVisibility(project);
                            }
                        }

                        @Override
                        public void editorReleased(@NotNull EditorFactoryEvent event) {
                            Project project = event.getEditor().getProject();
                            if (project != null) {
                                int currentCount = editorCountByProject.getOrDefault(project, 0);
                                int newCount = Math.max(0, currentCount - 1);
                                editorCountByProject.put(project, newCount);

                                projectsWithOpenEditors.put(project, newCount > 0);

                                LOG.debug(String.format(EDITOR_RELEASED_MSG.get(), project.getName(), newCount));

                                updateWidgetVisibility(project);
                            }
                        }
                    }, ApplicationManager.getApplication());

                    listenerRegistered = true;
                    LOG.debug(VarTypeBundle.message("log.editor.factory.listener.registered"));
                }
            }
        }
    }

    private void updateWidgetVisibility(@NotNull Project project) {
        if (project.isDisposed()) {
            return;
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
                if (statusBar == null) {
                    return;
                }

                boolean hasOpenEditors = projectsWithOpenEditors.getOrDefault(project, false);
                int editorCount = editorCountByProject.getOrDefault(project, 0);

                projectsWithOpenEditors.put(project, hasOpenEditors);

                if (hasOpenEditors) {
                    LOG.debug(String.format(WIDGET_ADDED_MSG.get(), editorCount));
                    if (statusBar.getWidget(WIDGET_ID) == null) {
                        statusBar.addWidget(createWidget(project), project);
                    } else {
                        statusBar.updateWidget(WIDGET_ID);
                    }
                } else {
                    LOG.debug(WIDGET_REMOVED_MSG.get());
                    if (statusBar.getWidget(WIDGET_ID) != null) {
                        statusBar.removeWidget(WIDGET_ID);
                    }
                }
            } catch (Exception e) {
                LOG.error(VarTypeBundle.message("log.widget.visibility.error"), e);
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