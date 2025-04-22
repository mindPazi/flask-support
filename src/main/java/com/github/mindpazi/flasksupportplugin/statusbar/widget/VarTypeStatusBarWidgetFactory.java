package com.github.mindpazi.flasksupportplugin.statusbar.widget;

import com.github.mindpazi.flasksupportplugin.i18n.VarTypeBundle;
import com.intellij.ide.AppLifecycleListener;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import com.intellij.openapi.wm.impl.status.widget.StatusBarWidgetsManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VarTypeStatusBarWidgetFactory implements StatusBarWidgetFactory {
    private static final Logger LOG = Logger.getInstance(VarTypeStatusBarWidgetFactory.class);
    private static final String factoryInitializedMsg = VarTypeBundle.message("log.factory.initialized");
    private static final String appWillBeClosedMsg = VarTypeBundle.message("log.app.will.be.closed");

    private static final String WIDGET_ID = VarTypeStatusBarWidget.ID;

    private final Map<Project, Integer> editorCountByProject = new ConcurrentHashMap<>();
    private volatile boolean listenerRegistered = false;

    public VarTypeStatusBarWidgetFactory() {
        LOG.debug(factoryInitializedMsg);

        setupListener();

        ApplicationManager.getApplication().getMessageBus().connect()
                .subscribe(AppLifecycleListener.TOPIC, new AppLifecycleListener() {
                    @Override
                    public void appWillBeClosed(boolean isRestart) {
                        LOG.debug(appWillBeClosedMsg);
                        cleanup();
                    }
                });
    }

    private void cleanup() {
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
        return editorCountByProject.getOrDefault(project, 0) > 0;
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Override
    public boolean isConfigurable() {
        return true;
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

    private void setupListener() {
        if (!listenerRegistered) {
            synchronized (this) {
                if (!listenerRegistered) {
                    EditorFactory.getInstance().addEditorFactoryListener(new EditorFactoryListener() {
                        @Override
                        public void editorCreated(@NotNull EditorFactoryEvent event) {
                            Project project = event.getEditor().getProject();
                            if (project != null) {
                                editorCountByProject.merge(project, 1, Integer::sum);
                                updateWidget(project);
                            }
                        }

                        @Override
                        public void editorReleased(@NotNull EditorFactoryEvent event) {
                            Project project = event.getEditor().getProject();
                            if (project != null) {
                                editorCountByProject.computeIfPresent(project,
                                        (proj, count) -> Math.max(0, count - 1)); // avoid
                                // negative
                                // values
                                updateWidget(project);
                            }
                        }
                    }, ApplicationManager.getApplication());
                    listenerRegistered = true;
                }
            }
        }
    }

    private void updateWidget(@NotNull Project project) {
        ApplicationManager.getApplication().invokeLater(() -> {
            StatusBarWidgetsManager manager = project.getService(StatusBarWidgetsManager.class);
            if (manager != null) {
                manager.updateWidget(VarTypeStatusBarWidgetFactory.class);
            }
        });
    }

    private void showNotification(Project project) {
        int count = editorCountByProject.getOrDefault(project, 0);
        String message = "Current editor count for project: " + count;
        Notification notification = NotificationGroupManager.getInstance()
                .getNotificationGroup("VarType Notifications")
                .createNotification(message, NotificationType.INFORMATION);
        Notifications.Bus.notify(notification, project);
    }
}