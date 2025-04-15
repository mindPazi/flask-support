package com.github.mindpazi.flasksupportplugin.statusbar;

import com.github.mindpazi.flasksupportplugin.i18n.VarTypeBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.function.Supplier;

/**
 * Class responsible for analyzing the PSI to determine the type of a variable.
 * Implements the composition pattern by separating the analysis logic from the
 * listener.
 */
public class VarTypeAnalyzer {
    private static final Logger LOG = Logger.getInstance(VarTypeAnalyzer.class);
    private final Project project;
    private static final Supplier<String> applicationNotAvailableMsg = VarTypeBundle
            .messagePointer("log.application.not.available");
    private static final Supplier<String> projectNullMsg = VarTypeBundle.messagePointer("log.project.null");

    public VarTypeAnalyzer(@NotNull Project project) {
        this.project = project;
    }

    /**
     * Analyzes the PSI element at the cursor position and returns the variable
     * type, if present.
     */
    @Nullable
    public String getVariableTypeAtCaret(@NotNull Editor editor, int offset) {
        if (ApplicationManager.getApplication() == null) {
            LOG.warn(applicationNotAvailableMsg.get());
            return null;
        }
        if (project.isDisposed() || project == null) {
            LOG.warn(projectNullMsg.get());
            return null;
        }

        try {
            return ApplicationManager.getApplication().runReadAction((Computable<String>) () -> {

                PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);

                if (psiFile == null) {
                    return null;
                }

                LOG.info(VarTypeBundle.message("log.analyzing.element", offset, psiFile.getName()));
                PsiElement element = psiFile.findElementAt(offset);
                if (element == null) {
                    return null;
                }

                // Check if the element is an identifier
                if (!(element instanceof PsiIdentifier)) {
                    return null;
                }

                // Parent of the identifier (could be PsiVariable or others)
                PsiElement parent = element.getParent();

                // Case 1: Check if it is a variable declaration
                if (parent instanceof PsiVariable) {
                    PsiVariable variable = (PsiVariable) parent;
                    PsiType type = variable.getType();

                    String typeLabel = VarTypeBundle.message("widget.type.label", type.getPresentableText());
                    LOG.info(VarTypeBundle.message("log.found.variable.declaration", typeLabel));
                    return typeLabel;
                }

                // Case 2: Check if it is a reference to a variable
                if (parent instanceof PsiReferenceExpression) {
                    PsiReferenceExpression refExpr = (PsiReferenceExpression) parent;
                    PsiElement resolvedElement = refExpr.resolve();

                    if (resolvedElement instanceof PsiVariable) {
                        PsiVariable variable = (PsiVariable) resolvedElement;
                        PsiType type = variable.getType();

                        String typeLabel = VarTypeBundle.message("widget.type.label", type.getPresentableText());

                        LOG.info(VarTypeBundle.message("log.found.variable.reference", typeLabel));
                        return typeLabel;
                    }
                }

                // If it is neither a declaration nor a reference, show nothing
                return null;
            });
        } catch (Exception e) {
            LOG.warn("Error during PSI analysis", e);
            return null;
        }
    }
}