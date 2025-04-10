package com.github.mindpazi.flasksupportplugin.statusbar.listener;

import com.github.mindpazi.flasksupportplugin.statusbar.analyzer.VarTypeAnalyzer;
import com.github.mindpazi.flasksupportplugin.statusbar.statusbar.VarTypeWidgetManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Listener per gli eventi di movimento del cursore che aggiorna il widget nella
 * status bar
 * con il tipo della variabile alla posizione corrente.
 * Usa composizione per delegare l'analisi PSI e l'aggiornamento del widget a
 * classi specializzate.
 */
public class VarTypeListener implements CaretListener {
    private static final Logger LOG = Logger.getInstance(VarTypeListener.class);
    private final Project project;
    private final VarTypeAnalyzer analyzer;
    private final VarTypeWidgetManager widgetManager;

    public VarTypeListener(Project project) {
        this.project = project;
        this.analyzer = new VarTypeAnalyzer(project);
        this.widgetManager = new VarTypeWidgetManager(project);
    }

    @Override
    public void caretPositionChanged(@NotNull CaretEvent event) {
        try {
            if (project.isDisposed()) {
                return;
            }

            Editor editor = event.getEditor();
            int offset = event.getCaret().getOffset();

            String typeText = analyzer.getVariableTypeAtCaret(editor, offset);
            widgetManager.updateStatusBarWidget(typeText);
        } catch (Exception e) {
            LOG.error("Error while getting variable type", e);
            widgetManager.updateStatusBarWidget("Error: " + e.getMessage());
        }
    }
}