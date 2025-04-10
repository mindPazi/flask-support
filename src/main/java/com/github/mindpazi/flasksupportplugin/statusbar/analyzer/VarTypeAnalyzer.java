package com.github.mindpazi.flasksupportplugin.statusbar.analyzer;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Classe responsabile dell'analisi del PSI per determinare il tipo di una
 * variabile.
 * Implementa il pattern di composizione separando la logica di analisi dal
 * listener.
 */
public class VarTypeAnalyzer {
    private static final Logger LOG = Logger.getInstance(VarTypeAnalyzer.class);
    private final Project project;

    public VarTypeAnalyzer(@NotNull Project project) {
        this.project = project;
    }

    /**
     * Analizza l'elemento PSI alla posizione del cursore e restituisce il tipo di
     * variabile, se presente.
     *
     * @param editor L'editor attivo
     * @param offset La posizione del cursore
     * @return Il tipo di variabile formattato o null se non è stata trovata una
     *         variabile
     */
    @Nullable
    public String getVariableTypeAtCaret(@NotNull Editor editor, int offset) {
        return ApplicationManager.getApplication().runReadAction((Computable<String>) () -> {
            PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);

            if (psiFile == null) {
                return null;
            }

            LOG.info("Analyzing element at offset: " + offset + " in file: " + psiFile.getName());
            PsiElement element = psiFile.findElementAt(offset);
            if (element == null) {
                return null;
            }

            // Verifica se l'elemento è un identificatore
            if (!(element instanceof PsiIdentifier)) {
                return null;
            }

            // Genitore dell'identificatore (potrebbe essere PsiVariable o altro)
            PsiElement parent = element.getParent();

            // Caso 1: Verificare se è una dichiarazione di variabile
            if (parent instanceof PsiVariable) {
                PsiVariable variable = (PsiVariable) parent;
                PsiType type = variable.getType();
                String result = "Type: " + type.getPresentableText();
                LOG.info("Found variable declaration with type: " + result);
                return result;
            }

            // Caso 2: Verificare se è un riferimento a una variabile
            if (parent instanceof PsiReferenceExpression) {
                PsiReferenceExpression refExpr = (PsiReferenceExpression) parent;
                PsiElement resolvedElement = refExpr.resolve();

                if (resolvedElement instanceof PsiVariable) {
                    PsiVariable variable = (PsiVariable) resolvedElement;
                    PsiType type = variable.getType();
                    String result = "Type: " + type.getPresentableText();
                    LOG.info("Found variable reference with type: " + result);
                    return result;
                }
            }

            // Se non è né una dichiarazione né un riferimento, non mostrare nulla
            return null;
        });
    }
}