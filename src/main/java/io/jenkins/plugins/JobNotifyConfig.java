package io.jenkins.plugins;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Extension
public class JobNotifyConfig extends GlobalConfiguration {
    private static final Logger log = LoggerFactory.getLogger(JobNotifyConfig.class);

    /**
     * 启动时执行的命令
     */
    private String onStartedCmd;

    /**
     * 完成时执行的命令
     */
    private String onCompletedCmd;

    /**
     * 错误执行命令
     */
    private String onErrorCmd;

    public JobNotifyConfig() {
        load();
    }

    public static JobNotifyConfig get() {
        return GlobalConfiguration.all().get(JobNotifyConfig.class);
    }

    @NonNull
    @Override
    public String getDisplayName() {
        return "Job通知全局配置";
    }

    public String getOnStartedCmd() {
        return onStartedCmd;
    }

    @DataBoundSetter
    public void setOnStartedCmd(String onStartedCmd) {
        this.onStartedCmd = onStartedCmd;
        save();
    }

    public String getOnCompletedCmd() {
        return onCompletedCmd;
    }
    @DataBoundSetter
    public void setOnCompletedCmd(String onCompletedCmd) {
        this.onCompletedCmd = onCompletedCmd;
        save();
    }

    @DataBoundSetter
    public void setOnErrorCmd(String onErrorCmd) {
        this.onErrorCmd = onErrorCmd;
        save();
    }

    public String getOnErrorCmd() {
        return onErrorCmd;
    }
}
