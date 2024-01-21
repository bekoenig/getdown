//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2018 Getdown authors
// https://github.com/bekoenig/getdown/blob/master/LICENSE

package io.github.bekoenig.getdown.launcher;

import java.awt.Color;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import io.github.bekoenig.getdown.launcher.swing.util.SwingUtil;
import io.github.bekoenig.getdown.data.EnvConfig;
import io.github.bekoenig.getdown.data.SysProps;
import io.github.bekoenig.getdown.util.LaunchUtil;
import io.github.bekoenig.getdown.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main application entry point for Getdown.
 */
public class GetdownApp
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GetdownApp.class);

    /**
     * The main entry point of the Getdown launcher application.
     */
    public static void main (String[] argv) {
        try {
            start(argv);
        } catch (Exception e) {
            LOGGER.warn("main() failed.", e);
        }
    }

    /**
     * Runs Getdown as an application, using the arguments supplie as {@code argv}.
     * @return the {@code Getdown} instance that is running. {@link Getdown#run} will have been
     * called on it.
     */
    public static Getdown start (String[] argv) {
        List<EnvConfig.Note> notes = new ArrayList<>();
        EnvConfig envc = EnvConfig.create(argv, notes);
        if (envc == null) {
            if (!notes.isEmpty()) for (EnvConfig.Note n : notes) System.err.println(n.message);
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
            case INFO: LOGGER.info(note.message); break;
            case WARN: LOGGER.warn(note.message); break;
            case ERROR: LOGGER.error(note.message); abort = true; break;
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

        Getdown getdown = new Getdown(envc) {
            @Override
            protected Container createContainer () {
                // create our user interface, and display it
                if (_frame == null) {
                    _frame = new JFrame("");
                    _frame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing (WindowEvent evt) {
                            handleWindowClose();
                        }
                    });
                    // handle close on ESC
                    String cancelId = "Cancel"; // $NON-NLS-1$
                    _frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelId);
                    _frame.getRootPane().getActionMap().put(cancelId, new AbstractAction() {
                        public void actionPerformed (ActionEvent e) {
                            handleWindowClose();
                        }
                    });
                    // this cannot be called in configureContainer as it is only allowed before the
                    // frame has been displayed for the first time
                    _frame.setUndecorated(_ifc.hideDecorations);
                    _frame.setResizable(false);
                } else {
                    _frame.getContentPane().removeAll();
                }
                _frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                return _frame.getContentPane();
            }

            @Override
            protected void configureContainer () {
                if (_frame == null) return;

                _frame.setTitle(_ifc.name);

                try {
                    _frame.setBackground(new Color(_ifc.background, true));
                } catch (Exception e) {
                    LOGGER.atWarn()
                        .setMessage("Failed to set background")
                        .addKeyValue("bg", _ifc.background)
                        .setCause(e)
                        .log();
                }

                if (_ifc.iconImages != null) {
                    List<Image> icons = new ArrayList<>();
                    for (String path : _ifc.iconImages) {
                        Image img = loadImage(path);
                        if (img == null) {
                            LOGGER.atWarn()
                                .setMessage("Error loading icon image")
                                .addKeyValue("path", path)
                                .log();
                        } else {
                            icons.add(img);
                        }
                    }
                    if (icons.isEmpty()) {
                        LOGGER.atWarn()
                            .setMessage("Failed to load any icons")
                            .addKeyValue("iconImages", _ifc.iconImages)
                            .log();
                    } else {
                        _frame.setIconImages(icons);
                    }
                }
            }

            @Override
            protected void showContainer () {
                if (_frame != null) {
                    _frame.pack();
                    SwingUtil.centerWindow(_frame);
                    _frame.setVisible(true);
                }
            }

            @Override
            protected void disposeContainer () {
                if (_frame != null) {
                    _frame.dispose();
                    _frame = null;
                }
            }

            @Override
            protected void showDocument (String url) {
                if (!StringUtil.couldBeValidUrl(url)) {
                    // command injection would be possible if we allowed e.g. spaces and double quotes
                    LOGGER.atWarn()
                        .setMessage("Invalid document URL.")
                        .addKeyValue("url", url)
                        .log();
                    return;
                }
                String[] cmdarray;
                if (LaunchUtil.isWindows()) {
                    String osName = System.getProperty("os.name", "");
                    if (osName.contains("9") || osName.contains("Me")) {
                        cmdarray = new String[] {
                            "command.com", "/c", "start", "\"" + url + "\"" };
                    } else {
                        cmdarray = new String[] {
                            "cmd.exe", "/c", "start", "\"\"", "\"" + url + "\"" };
                    }
                } else if (LaunchUtil.isMacOS()) {
                    cmdarray = new String[] { "open", url };
                } else { // Linux, Solaris, etc.
                    cmdarray = new String[] { "firefox", url };
                }
                try {
                    Runtime.getRuntime().exec(cmdarray);
                } catch (Exception e) {
                    LOGGER.atWarn()
                        .setMessage("Failed to open browser.")
                        .addKeyValue("cmdarray", cmdarray)
                        .setCause(e)
                        .log();
                }
            }

            @Override
            protected void exit (int exitCode) {
                // if we're running the app in the same JVM, don't call System.exit, but do
                // make double sure that the download window is closed.
                if (invokeDirect()) {
                    disposeContainer();
                } else {
                    System.exit(exitCode);
                }
            }

            @Override
            protected void fail (String message) {
                super.fail(message);
                // super.fail causes the UI to be created (if needed) on the next UI tick, so we
                // want to wait until that happens before we attempt to redecorate the window
                EventQueue.invokeLater(() -> {
                    // if the frame was set to be undecorated, make window decoration available
                    // to allow the user to close the window
                    if (_frame != null && _frame.isUndecorated()) {
                        _frame.dispose();
                        Color bg = _frame.getBackground();
                        if (bg != null && bg.getAlpha() < 255) {
                            // decorated windows do not allow alpha backgrounds
                            _frame.setBackground(
                                new Color(bg.getRed(), bg.getGreen(), bg.getBlue()));
                        }
                        _frame.setUndecorated(false);
                        showContainer();
                    }
                });
            }

            private JFrame _frame;
        };
        Getdown.run(getdown);
        return getdown;
    }
}
