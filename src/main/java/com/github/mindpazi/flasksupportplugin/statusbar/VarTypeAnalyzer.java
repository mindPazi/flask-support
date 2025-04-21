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

public class VarTypeAnalyzer {
    private static final Logger LOG = Logger.getInstance(VarTypeAnalyzer.class);
    private final Project project;

    public VarTypeAnalyzer(@NotNull Project project) {
        this.project = project;
    }

    @Nullable
    public String getVariableTypeAtCaret(@NotNull Editor editor, int offset) {
        if (ApplicationManager.getApplication() == null) {
            LOG.warn(VarTypeBundle.message("log.application.not.available"));
            return null;
        }
        if (project == null || project.isDisposed()) {
            LOG.warn(VarTypeBundle.message("log.project.null"));
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

                if (!(element instanceof PsiIdentifier)) {
                    return null;
                }

                PsiElement parent = element.getParent();

                if (parent instanceof PsiVariable) {
                    PsiVariable variable = (PsiVariable) parent;
                    PsiType type = variable.getType();

                    String typeLabel = VarTypeBundle.message("widget.type.label", type.getPresentableText());
                    LOG.info(VarTypeBundle.message("log.found.variable.declaration", typeLabel));
                    return typeLabel;
                }

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

                return null;
            });
        } catch (Exception e) {
            LOG.warn(VarTypeBundle.message("log.error.psi.analysis"), e);
            return null;
        }
    }
}