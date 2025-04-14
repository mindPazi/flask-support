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

    private IdeaPluginDescriptor retrievePluginDescriptor() {
        PluginId pluginId = PluginId.getId("com.github.mindpazi.flasksupportplugin");
        return PluginManagerCore.getPlugin(pluginId);
    }

    @Override
    public boolean submit(IdeaLoggingEvent @NotNull [] events, @Nullable String additionalInfo,
            @NotNull Component parentComponent, @NotNull Consumer<? super SubmittedReportInfo> consumer) {
        try {
            // Get plugin information
            IdeaPluginDescriptor plugin = retrievePluginDescriptor();

            // Build the issue body with all required information
            String body = buildIssueBody(events, additionalInfo, plugin);

            // Encode and submit the issue
            return submitIssueToTracker(body, consumer);
        } catch (Exception e) {
            // In case of error during reporting
            return false;
        }
    }

    private String buildIssueBody(IdeaLoggingEvent[] events, String additionalInfo, IdeaPluginDescriptor plugin) {
        StringBuilder body = new StringBuilder();

        appendExceptionDetails(body, events);
        appendAdditionalInfo(body, additionalInfo);
        appendSystemInfo(body, plugin);

        return body.toString();
    }

    private void appendExceptionDetails(StringBuilder body, IdeaLoggingEvent[] events) {
        body.append("### ").append(exceptionDetailsMsg.get()).append("\n\n```\n");

        for (IdeaLoggingEvent event : events) {
            body.append(event.getMessage()).append("\n");
            if (event.getThrowable() != null) {
                body.append(event.getThrowableText()).append("\n");
            }
        }

        body.append("```\n\n");
    }

    private void appendAdditionalInfo(StringBuilder body, String additionalInfo) {
        if (additionalInfo != null) {
            body.append("### ").append(additionalInfoMsg.get()).append("\n\n")
                    .append(additionalInfo).append("\n\n");
        }
    }

    private void appendSystemInfo(StringBuilder body, IdeaPluginDescriptor plugin) {
        body.append("### ").append(systemInfoMsg.get()).append("\n\n");

        // Plugin version
        String pluginVersionText = pluginVersionMsg.get().replace("{0}",
                plugin != null ? plugin.getVersion() : unknownMsg.get());
        body.append("* ").append(pluginVersionText).append("\n");

        // OS information
        String osText = osMsg.get().replace("{0}", System.getProperty("os.name"))
                .replace("{1}", System.getProperty("os.version"));
        body.append("* ").append(osText).append("\n");

        // Java version
        String javaVersionText = javaVersionMsg.get().replace("{0}", System.getProperty("java.version"));
        body.append("* ").append(javaVersionText).append("\n");
    }

    private boolean submitIssueToTracker(String body, Consumer<? super SubmittedReportInfo> consumer) {
        // Encode the URL body
        String encodedBody = java.net.URLEncoder.encode(body, java.nio.charset.StandardCharsets.UTF_8);

        // Open the browser with the issue tracker URL
        BrowserUtil.browse(GITHUB_ISSUE_URL + "&body=" + encodedBody);

        // Notify that the report has been submitted
        consumer.consume(new SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE));

        return true;
    }
}