package com.github.mindpazi.flasksupportplugin.statusbar;

import com.github.mindpazi.flasksupportplugin.i18n.VarTypeBundle;
import com.github.mindpazi.flasksupportplugin.statusbar.widget.VarTypeWidgetManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.ModalityState;
import com.intellij.util.concurrency.NonUrgentExecutor;
import com.intellij.openapi.project.DumbAware;
import java.util.function.Supplier;

public class VarTypeListener implements CaretListener, DumbAware {
    private static final Logger LOG = Logger.getInstance(VarTypeListener.class);
    private final Project project;
    private final VarTypeAnalyzer analyzer;
    private final VarTypeWidgetManager widgetManager;
    private final static Supplier<String> applicationNotAvailableMsg = VarTypeBundle
            .messagePointer("log.application.not.available");
    private final static Supplier<String> projectNullMsg = VarTypeBundle.messagePointer("log.project.null");

    public VarTypeListener(Project project) {
        this.project = project;
        this.analyzer = new VarTypeAnalyzer(project);
        this.widgetManager = new VarTypeWidgetManager(project);
    }

    @Override
    public void caretPositionChanged(@NotNull CaretEvent event) {
        if (ApplicationManager.getApplication() == null || ApplicationManager.getApplication().isDisposed()) {
            LOG.warn(applicationNotAvailableMsg.get());
            return;
        }

        if (project == null || project.isDisposed()) {
            LOG.warn(projectNullMsg.get());
            return;
        }

        Editor editor = event.getEditor();
        int offset = event.getCaret().getOffset();

        ReadAction.nonBlocking(() -> analyzer.getVariableTypeAtCaret(editor, offset))
                .expireWith(project)
                .coalesceBy(project, VarTypeListener.class)
                .finishOnUiThread(ModalityState.defaultModalityState(),
                        typeText -> widgetManager.updateStatusBarWidget(typeText)) // typetext is returned by
                                                                                   // getVariableTypeAtCaret
                .submit(NonUrgentExecutor.getInstance())
                .onError(throwable -> {
                    LOG.error(VarTypeBundle.message("log.error.variable.type"), throwable);
                    widgetManager.updateStatusBarWidget(
                            VarTypeBundle.message("widget.type.error", throwable.getMessage()));
                });
    }
}