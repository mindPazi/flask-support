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
    private final Supplier<String> displayNameMsg = () -> VarTypeBundle.message("widget.display.name");

    // Mappa che tiene traccia dello stato per ogni progetto
    private final Map<Project, Boolean> projectsWithOpenEditors = new ConcurrentHashMap<>();

    // Flag per controllare se il listener è già stato registrato
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
        // Registriamo il listener una sola volta per l'intera applicazione
        setupListenerIfNeeded();

        // Aggiorniamo e restituiamo lo stato corrente
        return checkAndUpdateOpenEditorsState(project);
    }

    /**
     * Configura il listener globale per tutti gli editor, se non è già stato fatto
     */
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
                                // Verifichiamo quanti editor sono ancora aperti
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

    /**
     * Controlla se ci sono editor aperti nel progetto e aggiorna lo stato interno
     */
    private boolean checkAndUpdateOpenEditorsState(@NotNull Project project) {
        if (project.isDisposed()) {
            projectsWithOpenEditors.remove(project);
            return false;
        }

        boolean hasOpenEditors = FileEditorManager.getInstance(project).getAllEditors().length > 0;
        projectsWithOpenEditors.put(project, hasOpenEditors);
        return hasOpenEditors;
    }

    /**
     * Aggiorna la visibilità del widget in base allo stato degli editor
     */
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
                        // Se ci sono file aperti, assicuriamoci che il widget sia presente
                        if (statusBar.getWidget(VarTypeStatusBarWidget.ID) == null) {
                            LOG.info("Adding widget as files are open");
                            statusBar.addWidget(createWidget(project), project);
                        } else {
                            // Aggiorniamo il widget esistente
                            statusBar.updateWidget(VarTypeStatusBarWidget.ID);
                        }
                    } else {
                        // Se non ci sono file aperti, rimuoviamo il widget
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