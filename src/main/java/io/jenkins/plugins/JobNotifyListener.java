package io.jenkins.plugins;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Extension
public class JobNotifyListener extends RunListener<Run<?, ?>> {
    private final static Logger log = LoggerFactory.getLogger(JobNotifyListener.class);

    @Override
    public void onStarted(Run<?, ?> run, TaskListener listener) {
        log.debug("监听到了任务启动:{}",run.getId());
        NotifyBuilder builder = new NotifyBuilder("echo 123");
    }
}
