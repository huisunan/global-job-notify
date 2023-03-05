package io.jenkins.plugins;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.util.LineEndingConversion;
import jenkins.model.Jenkins;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Extension
public class JobNotifyListener extends RunListener<Run<?, ?>> {

    /**
     * 参考CommandInterpreter
     *
     * @param run      The started build.
     * @param listener The listener for this build. This can be used to produce log messages, for example,
     *                 which becomes a part of the "console output" of this build.
     */
    @Override
    public void onStarted(Run<?, ?> run, TaskListener listener) {
        Jenkins jenkins = Jenkins.get();
        JobNotifyConfig notifyConfig = jenkins.getDescriptorByType(JobNotifyConfig.class);
        PrintStream logger = listener.getLogger();
        logger.println("任务全局监听");
        String command = notifyConfig.getOnStartedCmd();
        File comandFile = null;
        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);
        String convertCommand = null;
        try {
            if (isWindows()) {
                comandFile = File.createTempFile("jenkins", ".bat");
                convertCommand = LineEndingConversion.convertEOL(command + "\r\nexit %ERRORLEVEL%", LineEndingConversion.EOLType.Windows);
                pb.command("cmd", "/c", comandFile.getAbsolutePath());
            } else if (isUnix()) {
                comandFile = File.createTempFile("jenkins", ".sh");
            } else {
                logger.println("未知的系统，通知跳过");
                return;
            }
            if (convertCommand == null) {
                return;
            }
            try (FileOutputStream fileOutputStream = new FileOutputStream(comandFile)) {
                IOUtils.write(convertCommand, fileOutputStream, StandardCharsets.UTF_8);
            }
            Process p = pb.start();
            p.waitFor(); // 等待进程执行完成
            String s = IOUtils.toString(p.getInputStream(), Charset.defaultCharset());
            logger.println("result:" + s);
            int exitCode = p.exitValue();
            // 执行成功
            if (exitCode == 0) {
                logger.println("The script has been executed successfully.");
            } else { // 执行失败
                logger.println("Failed to execute the script.");
            }
        } catch (Exception e) {
            logger.println("execute error:" + e.getMessage());
        } finally {
            if (comandFile != null) {
                comandFile.deleteOnExit();
            }
        }
    }

    @Override
    public void onCompleted(Run<?, ?> run, @NonNull TaskListener listener) {
        super.onCompleted(run, listener);
    }

    public static boolean isUnix() {
        String osName = System.getProperty("os.name");
        return osName.toLowerCase().startsWith("linux") || osName.toLowerCase().startsWith("unix");
    }

    public static boolean isWindows() {
        String osName = System.getProperty("os.name");
        return osName.toLowerCase().startsWith("windows");
    }


}
