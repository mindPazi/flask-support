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
    private static final String GITHUB_ISSUE_URL ="https://github.com/mindpazi/flask-support-plugin/issues/new?template=bug_report.md";

    private static final Supplier<String> reportActionTextMsg = VarTypeBundle
            .messagePointer("error.report.action.text");
    private static final Supplier<String> exceptionDetailsMsg = VarTypeBundle
            .messagePointer("error.report.exception.details");
    private static final Supplier<String> additionalInfoMsg = VarTypeBundle
            .messagePointer("error.report.additional.info");
    private static final Supplier<String> systemInfoMsg = VarTypeBundle.messagePointer("error.report.system.info");

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

            IdeaPluginDescriptor plugin = retrievePluginDescriptor();

            String body = buildIssueBody(events, additionalInfo, plugin);

            return submitIssueToTracker(body, consumer);
        } catch (Exception e) {

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

        String pluginVersionText = VarTypeBundle.message("error.report.plugin.version",
                plugin != null ? plugin.getVersion() : "Unknown");
        body.append("* ").append(pluginVersionText).append("\n");

        String osText = VarTypeBundle.message("error.report.os",
                System.getProperty("os.name"), System.getProperty("os.version"));
        body.append("* ").append(osText).append("\n");

        String javaVersionText = VarTypeBundle.message("error.report.java.version",
                System.getProperty("java.version"));
        body.append("* ").append(javaVersionText).append("\n");
    }

    private boolean submitIssueToTracker(String body, Consumer<? super SubmittedReportInfo> consumer) {

        String encodedBody = java.net.URLEncoder.encode(body, java.nio.charset.StandardCharsets.UTF_8);

        BrowserUtil.browse(GITHUB_ISSUE_URL + "&body=" + encodedBody);

        consumer.consume(new SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE));

        return true;
    }
}