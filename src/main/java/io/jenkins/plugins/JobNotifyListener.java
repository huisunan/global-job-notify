package io.jenkins.plugins;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Util;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.tasks.Builder;
import hudson.util.LineEndingConversion;
import jenkins.model.Jenkins;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
		logger.println("global job notify");
		String command = notifyConfig.getOnStartedCmd();
		File comandFile = null;
		ProcessBuilder pb = new ProcessBuilder();
		pb.redirectErrorStream(true);
		String convertCommand;
		try {
			EnvVars environment = run.getEnvironment(listener);
			pb.environment().putAll(environment);
			if (isWindows()) {
				comandFile = File.createTempFile("jenkins", ".bat");
				convertCommand = LineEndingConversion.convertEOL(command + "\r\nexit %ERRORLEVEL%", LineEndingConversion.EOLType.Windows);
				pb.command("cmd", "/c", comandFile.getAbsolutePath());
			} else if (isUnix()) {
				comandFile = File.createTempFile("jenkins", ".sh");
				convertCommand = addLineFeedForNonASCII(LineEndingConversion.convertEOL(command, LineEndingConversion.EOLType.Unix));
				pb.command(buildCommandLine(command, new FilePath(comandFile)));
			} else {
				logger.println("unknown os,skip notify");
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

	public String[] buildCommandLine(String command, FilePath script) {
		if (command.startsWith("#!")) {
			// interpreter override
			int end = command.indexOf('\n');
			if (end < 0) end = command.length();
			List<String> args = new ArrayList<>(Arrays.asList(Util.tokenize(command.substring(0, end).trim())));
			args.add(script.getRemote());
			args.set(0, args.get(0).substring(2));   // trim off "#!"
			return args.toArray(new String[0]);
		} else
			return new String[]{"sh", "-xe", script.getRemote()};
	}


	private static String addLineFeedForNonASCII(String s) {
		if (!s.startsWith("#!")) {
			if (s.indexOf('\n') != 0) {
				return "\n" + s;
			}
		}

		return s;
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
