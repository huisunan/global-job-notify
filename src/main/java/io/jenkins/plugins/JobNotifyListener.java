package io.jenkins.plugins;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
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

    @Override
    public void onStarted(Run<?, ?> run, TaskListener listener) {
        Jenkins jenkins = Jenkins.get();
        JobNotifyConfig notifyConfig = jenkins.getDescriptorByType(JobNotifyConfig.class);
        PrintStream logger = listener.getLogger();
        logger.println("任务全局监听");
        String onStartedCmd = notifyConfig.getOnStartedCmd();
        try {
            File batFile = File.createTempFile("jenkins", ".bat");
            try (FileOutputStream fileOutputStream = new FileOutputStream(batFile)){
                IOUtils.write(onStartedCmd,fileOutputStream, StandardCharsets.UTF_8);
            }
            logger.println("执行命令:cmd /c " + batFile.getAbsolutePath());
            //创建shell文件
            ProcessBuilder pb = new ProcessBuilder(
                    "cmd", "/c", batFile.getAbsolutePath()
            );
            pb.redirectErrorStream(true);
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
        } catch (IOException | InterruptedException e) {
            logger.println("execute error:" + e.getMessage());
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
