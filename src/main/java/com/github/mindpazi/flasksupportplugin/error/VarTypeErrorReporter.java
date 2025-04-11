package com.github.mindpazi.flasksupportplugin.error;

import com.github.mindpazi.flasksupportplugin.i18n.VarTypeBundle;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.function.Supplier;

/**
 * Handles plugin error reporting directly from the IDE.
 * Allows users to submit error reports to the issue tracker on GitHub.
 */
public class VarTypeErrorReporter extends ErrorReportSubmitter {
    @NonNls
    private static final String GITHUB_ISSUE_URL = "https://github.com/mindpazi/flask-support-plugin/issues/new?template=bug_report.md";

    private final Supplier<String> reportActionTextMsg = () -> VarTypeBundle.message("error.report.action.text");
    private final Supplier<String> exceptionDetailsMsg = () -> VarTypeBundle.message("error.report.exception.details");
    private final Supplier<String> additionalInfoMsg = () -> VarTypeBundle.message("error.report.additional.info");
    private final Supplier<String> systemInfoMsg = () -> VarTypeBundle.message("error.report.system.info");
    private final Supplier<String> pluginVersionMsg = () -> VarTypeBundle.message("error.report.plugin.version");
    private final Supplier<String> unknownMsg = () -> VarTypeBundle.message("error.report.unknown");
    private final Supplier<String> osMsg = () -> VarTypeBundle.message("error.report.os");
    private final Supplier<String> javaVersionMsg = () -> VarTypeBundle.message("error.report.java.version");

    @NotNull
    @Override
    public String getReportActionText() {
        return reportActionTextMsg.get();
    }

    @Override
    public boolean submit(IdeaLoggingEvent @NotNull [] events, @Nullable String additionalInfo,
            @NotNull Component parentComponent, @NotNull Consumer<? super SubmittedReportInfo> consumer) {
        // Gets the plugin ID
        PluginId pluginId = PluginId.getId("com.github.mindpazi.flasksupportplugin");
        IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(pluginId);

        // Issue tracker URL - replace "mindpazi" with your actual GitHub username
        String url = GITHUB_ISSUE_URL;

        // Prepares the issue body with error details
        StringBuilder body = new StringBuilder();
        body.append("### ").append(exceptionDetailsMsg.get()).append("\n\n```\n");

        // Add exception details
        for (IdeaLoggingEvent event : events) {
            body.append(event.getMessage()).append("\n");
            if (event.getThrowable() != null) {
                body.append(event.getThrowableText()).append("\n");
            }
        }

        body.append("```\n\n");

        // Add additional information provided by the user
        if (additionalInfo != null) {
            body.append("### ").append(additionalInfoMsg.get()).append("\n\n")
                    .append(additionalInfo).append("\n\n");
        }

        // Add system info
        body.append("### ").append(systemInfoMsg.get()).append("\n\n");

        // Format the plugin version message
        String pluginVersionText = pluginVersionMsg.get().replace("{0}",
                plugin != null ? plugin.getVersion() : unknownMsg.get());
        body.append("* ").append(pluginVersionText).append("\n");

        // Format the OS message
        String osText = osMsg.get().replace("{0}", System.getProperty("os.name"))
                .replace("{1}", System.getProperty("os.version"));
        body.append("* ").append(osText).append("\n");

        // Format the Java version message
        String javaVersionText = javaVersionMsg.get().replace("{0}", System.getProperty("java.version"));
        body.append("* ").append(javaVersionText).append("\n");

        try {
            // Encode the URL body
            String encodedBody = java.net.URLEncoder.encode(body.toString(), java.nio.charset.StandardCharsets.UTF_8);

            // Open the browser with the issue tracker URL
            BrowserUtil.browse(url + "&body=" + encodedBody);

            // Notify that the report has been submitted
            consumer.consume(new SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE));

            return true;
        } catch (Exception e) {
            // In case of error during reporting
            return false;
        }
    }
}