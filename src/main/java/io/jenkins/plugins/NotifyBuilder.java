package io.jenkins.plugins;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.Builder;

import java.io.IOException;
import java.io.PrintStream;

public class NotifyBuilder extends Builder {

    private final String command;

    NotifyBuilder(String command) {
        this.command = command;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        PrintStream logger = listener.getLogger();
        try {
            logger.println("executing command :" + command);
            int result = launcher.launch().cmds(command.split(" ")).stdout(logger).join();
            if (result != 0) {
                logger.println("Command failed with " + result + " exit code");
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.println("Failed to execute command: " + e.getMessage());
            return false;
        }
    }
}
