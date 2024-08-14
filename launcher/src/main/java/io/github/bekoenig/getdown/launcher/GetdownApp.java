//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2018 Getdown authors
// https://github.com/bekoenig/getdown/blob/master/LICENSE

package io.github.bekoenig.getdown.launcher;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import io.github.bekoenig.getdown.data.EnvConfig;
import io.github.bekoenig.getdown.data.SysProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * The main application entry point for Getdown.
 */
public class GetdownApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetdownApp.class);

    /**
     * The main entry point of the Getdown launcher application.
     */
    public static void main(String[] argv) {
        try {
            start(argv);
        } catch (Exception e) {
            LOGGER.warn("main() failed.", e);
        }
    }

    /**
     * Runs Getdown as an application, using the arguments supplie as {@code argv}.
     *
     * @return the {@code Getdown} instance that is running. {@link Getdown#run} will have been
     * called on it.
     */
    public static Getdown start(String[] argv) {
        List<EnvConfig.Note> notes = new ArrayList<>();
        EnvConfig envc = EnvConfig.create(argv, notes);
        if (envc == null) {
            if (!notes.isEmpty()) for (EnvConfig.Note n : notes) LOGGER.error(n.message);
            else System.err.println("Usage: java -jar getdown.jar [app_dir] [app_id] [app args]");
            System.exit(-1);
        }

        // pipe our output into a file in the application directory
        if (!SysProps.noLogRedir() && !SysProps.debug()) {
            LoggerContext loggerFactory = (LoggerContext) LoggerFactory.getILoggerFactory();
            loggerFactory.reset();
            loggerFactory.putProperty("appdir", envc.appDir.getPath());

            InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("io/github/bekoenig/getdown/logback-appdir.xml");

            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(loggerFactory);
            try {
                configurator.doConfigure(inputStream);
            } catch (JoranException je) {
                throw new RuntimeException("Failed to configure logging for appdir");
            }
        }

        // log all uncaught exceptions
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
            LOGGER.error("Exception in thread \"{}\"", thread.getName(), throwable));

        // report any notes from reading our env config, and abort if necessary
        boolean abort = false;
        for (EnvConfig.Note note : notes) {
            switch (note.level) {
                case INFO:
                    LOGGER.info(note.message);
                    break;
                case WARN:
                    LOGGER.warn(note.message);
                    break;
                case ERROR:
                    LOGGER.error(note.message);
                    abort = true;
                    break;
            }
        }
        if (abort) System.exit(-1);

        // record a few things for posterity
        LOGGER.info("------------------ VM Info ------------------");
        LOGGER.info("-- OS Name: {}", System.getProperty("os.name"));
        LOGGER.info("-- OS Arch: {}", System.getProperty("os.arch"));
        LOGGER.info("-- OS Vers: {}", System.getProperty("os.version"));
        LOGGER.info("-- Java Vers: {}", System.getProperty("java.version"));
        LOGGER.info("-- Java Home: {}", System.getProperty("java.home"));
        LOGGER.info("-- User Name: {}", System.getProperty("user.name"));
        LOGGER.info("-- User Home: {}", System.getProperty("user.home"));
        LOGGER.info("-- Cur dir: {}", System.getProperty("user.dir"));
        LOGGER.info("---------------------------------------------");

        Getdown getdown = new GetdownDialog(envc);
        Getdown.run(getdown);
        return getdown;
    }

}
