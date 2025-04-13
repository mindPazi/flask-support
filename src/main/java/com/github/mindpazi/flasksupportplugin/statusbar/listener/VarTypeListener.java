package com.github.mindpazi.flasksupportplugin.statusbar.listener;

import com.github.mindpazi.flasksupportplugin.i18n.VarTypeBundle;
import com.github.mindpazi.flasksupportplugin.statusbar.analyzer.VarTypeAnalyzer;
import com.github.mindpazi.flasksupportplugin.statusbar.widget.VarTypeWidgetManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Listener for cursor movement events that updates the status bar widget
 * with the variable type at the current position.
 * Uses composition to delegate PSI analysis and widget updating to dedicated
 * classes.
 */
public class VarTypeListener implements CaretListener {
    private static final Logger LOG = Logger.getInstance(VarTypeListener.class);
    private final Project project;
    private final VarTypeAnalyzer analyzer;
    private final VarTypeWidgetManager widgetManager;
    private final Supplier<String> errorVariableTypeMsg = () -> VarTypeBundle.message("log.error.variable.type");
    private final Supplier<String> typeErrorMsg = () -> VarTypeBundle.message("widget.type.error");

    public VarTypeListener(Project project) {
        this.project = project;
        this.analyzer = new VarTypeAnalyzer(project);
        this.widgetManager = new VarTypeWidgetManager(project);
    }

    @Override
    public void caretPositionChanged(@NotNull CaretEvent event) {
        if (ApplicationManager.getApplication() == null || ApplicationManager.getApplication().isDisposed()) {
            return;
        }

        if (project == null || project.isDisposed()) {
            return;
        }

        try {
            Editor editor = event.getEditor();
            int offset = event.getCaret().getOffset();

            String typeText = analyzer.getVariableTypeAtCaret(editor, offset);
            widgetManager.updateStatusBarWidget(typeText);
        } catch (Exception e) {
            LOG.error(errorVariableTypeMsg.get(), e);
            widgetManager.updateStatusBarWidget(typeErrorMsg.get().replace("{0}", e.getMessage()));
        }
    }
}