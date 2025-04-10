package com.github.mindpazi.flasksupportplugin.error;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * Gestisce la segnalazione degli errori del plugin direttamente dall'IDE.
 * Permette agli utenti di inviare report di errori all'issue tracker su GitHub.
 */
public class VarTypeErrorReporter extends ErrorReportSubmitter {
    @NotNull
    @Override
    public String getReportActionText() {
        return "Segnala Problema agli Sviluppatori";
    }

    @Override
    public boolean submit(IdeaLoggingEvent @NotNull [] events, @Nullable String additionalInfo,
            @NotNull Component parentComponent, @NotNull Consumer<? super SubmittedReportInfo> consumer) {
        // Ottiene l'ID del plugin
        PluginId pluginId = PluginId.getId("com.github.mindpazi.flasksupportplugin");
        IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(pluginId);

        // URL dell'issue tracker - sostituisci "mindpazi" con il tuo username GitHub
        // effettivo
        String url = "https://github.com/mindpazi/flask-support-plugin/issues/new?template=bug_report.md";

        // Prepara il corpo dell'issue con i dettagli dell'errore
        StringBuilder body = new StringBuilder();
        body.append("### Dettagli dell'eccezione\n\n```\n");

        // Aggiungi dettagli dell'eccezione
        for (IdeaLoggingEvent event : events) {
            body.append(event.getMessage()).append("\n");
            if (event.getThrowable() != null) {
                body.append(event.getThrowableText()).append("\n");
            }
        }

        body.append("```\n\n");

        // Aggiungi informazioni aggiuntive fornite dall'utente
        if (additionalInfo != null) {
            body.append("### Informazioni aggiuntive\n\n").append(additionalInfo).append("\n\n");
        }

        // Aggiungi info di sistema
        body.append("### Informazioni di sistema\n\n");
        body.append("* Versione plugin: ").append(plugin != null ? plugin.getVersion() : "sconosciuta").append("\n");
        body.append("* Sistema operativo: ").append(System.getProperty("os.name")).append(" ")
                .append(System.getProperty("os.version")).append("\n");
        body.append("* Java: ").append(System.getProperty("java.version")).append("\n");

        try {
            // Codifica il corpo dell'URL
            String encodedBody = java.net.URLEncoder.encode(body.toString(), java.nio.charset.StandardCharsets.UTF_8);

            // Apri il browser con l'URL dell'issue tracker
            BrowserUtil.browse(url + "&body=" + encodedBody);

            // Notifica che la segnalazione è stata inviata
            consumer.consume(new SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE));

            return true;
        } catch (Exception e) {
            // In caso di errore durante la segnalazione
            return false;
        }
    }
}