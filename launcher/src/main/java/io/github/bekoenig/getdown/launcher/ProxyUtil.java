//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2018 Getdown authors
// https://github.com/bekoenig/getdown/blob/master/LICENSE

package io.github.bekoenig.getdown.launcher;

import ca.beq.util.win32.registry.RegistryKey;
import ca.beq.util.win32.registry.RegistryValue;
import ca.beq.util.win32.registry.RootKey;
import io.github.bekoenig.getdown.data.Application;
import io.github.bekoenig.getdown.net.Connector;
import io.github.bekoenig.getdown.spi.ProxyAuth;
import io.github.bekoenig.getdown.util.Config;
import io.github.bekoenig.getdown.util.LaunchUtil;
import io.github.bekoenig.getdown.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.ServiceLoader;

public final class ProxyUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyUtil.class);

    public static boolean autoDetectProxy(Application app) {
        String host = null, port = null;

        // check for a proxy configured via system properties
        if (System.getProperty("https.proxyHost") != null) {
            host = System.getProperty("https.proxyHost");
            port = System.getProperty("https.proxyPort");
        }
        if (StringUtil.isBlank(host) && System.getProperty("http.proxyHost") != null) {
            host = System.getProperty("http.proxyHost");
            port = System.getProperty("http.proxyPort");
        }

        // check the Windows registry
        if (StringUtil.isBlank(host) && LaunchUtil.isWindows()) {
            try {
                String rhost = null, rport = null;
                boolean enabled = false;
                RegistryKey.initialize();
                RegistryKey r = new RegistryKey(RootKey.HKEY_CURRENT_USER, PROXY_REGISTRY);
                for (Iterator<?> iter = r.values(); iter.hasNext(); ) {
                    RegistryValue value = (RegistryValue) iter.next();
                    if ("ProxyEnable".equals(value.getName())) {
                        enabled = "1".equals(value.getStringValue());
                    }
                    if (value.getName().equals("ProxyServer")) {
                        String[] hostPort = splitHostPort(value.getStringValue());
                        rhost = hostPort[0];
                        rport = hostPort[1];
                    }
                    if (value.getName().equals("AutoConfigURL")) {
                        String acurl = value.getStringValue();
                        Reader acjs = new InputStreamReader(new URL(acurl).openStream());
                        // technically we should be returning all this info and trying each proxy
                        // in succession, but that's complexity we'll leave for another day
                        URL configURL = app.getConfigResource().getRemote();
                        for (String proxy : findPACProxiesForURL(acjs, configURL)) {
                            if (proxy.startsWith("PROXY ")) {
                                String[] hostPort = splitHostPort(proxy.substring(6));
                                rhost = hostPort[0];
                                rport = hostPort[1];
                                // TODO: is this valid? Does AutoConfigURL imply proxy enabled?
                                enabled = true;
                                break;
                            }
                        }
                    }
                }

                if (enabled) {
                    host = rhost;
                    port = rport;
                } else {
                    LOGGER.info("Detected no proxy settings in the registry.");
                }

            } catch (Throwable t) {
                LOGGER.atInfo()
                    .setMessage("Failed to find proxy settings in Windows registry")
                    .addKeyValue("error", t)
                    .log();
            }
        }

        // look for a proxy.txt file
        if (StringUtil.isBlank(host)) {
            String[] hostPort = loadProxy(app);
            host = hostPort[0];
            port = hostPort[1];
        }

        if (StringUtil.isBlank(host)) {
            return false;
        }

        // yay, we found a proxy configuration, configure it in the app
        initProxy(app, host, port, null, null);
        return true;
    }

    public static boolean canLoadWithoutProxy(URL rurl, int timeoutSeconds) {
        LOGGER.info("Attempting to fetch without proxy: {}", rurl);
        try {
            URLConnection conn = Connector.DEFAULT.open(rurl, timeoutSeconds, timeoutSeconds);
            // if the appbase is not an HTTP/S URL (like file:), then we don't need a proxy
            if (!(conn instanceof HttpURLConnection)) {
                return true;
            }
            // otherwise, try to make a HEAD request for this URL
            HttpURLConnection hcon = (HttpURLConnection) conn;
            try {
                hcon.setRequestMethod("HEAD");
                hcon.connect();
                // make sure we got a satisfactory response code
                int rcode = hcon.getResponseCode();
                if (rcode == HttpURLConnection.HTTP_PROXY_AUTH ||
                    rcode == HttpURLConnection.HTTP_FORBIDDEN) {
                    LOGGER.atWarn()
                        .setMessage("Got an 'HTTP credentials needed' response")
                        .addKeyValue("code", rcode)
                        .log();
                } else {
                    return true;
                }
            } finally {
                hcon.disconnect();
            }
        } catch (IOException ioe) {
            LOGGER.info("Failed to HEAD {}", rurl, ioe);
            LOGGER.info("We probably need a proxy, but auto-detection failed.");
        }
        return false;
    }

    public static void configProxy(Application app, String host, String port,
                                   String username, String password) {
        // save our proxy host and port in a local file
        saveProxy(app, host, port);

        // save our credentials via the SPI
        if (!StringUtil.isBlank(username) && !StringUtil.isBlank(password)) {
            ServiceLoader<ProxyAuth> loader = ServiceLoader.load(ProxyAuth.class);
            Iterator<ProxyAuth> iterator = loader.iterator();
            String appDir = app.getAppDir().getAbsolutePath();
            while (iterator.hasNext()) {
                iterator.next().saveCredentials(appDir, username, password);
            }
        }

        // also configure them in the app
        initProxy(app, host, port, username, password);
    }

    public static String[] loadProxy(Application app) {
        File pfile = app.getLocalPath("proxy.txt");
        if (pfile.exists()) {
            try {
                Config pconf = Config.parseConfig(pfile, Config.createOpts(false));
                return new String[]{pconf.getString("host"), pconf.getString("port")};
            } catch (IOException ioe) {
                LOGGER.warn("Failed to read '{}'", pfile, ioe);
            }
        }
        return new String[]{null, null};
    }

    public static void saveProxy(Application app, String host, String port) {
        File pfile = app.getLocalPath("proxy.txt");
        try (PrintStream pout = new PrintStream(Files.newOutputStream(pfile.toPath()))) {
            if (!StringUtil.isBlank(host)) {
                pout.println("host = " + host);
            }
            if (!StringUtil.isBlank(port)) {
                pout.println("port = " + port);
            }
        } catch (IOException ioe) {
            LOGGER.warn("Error creating proxy file '{}'", pfile, ioe);
        }
    }

    public static void initProxy(Application app, String host, String port,
                                 String username, String password) {
        // check whether we have saved proxy credentials
        String appDir = app.getAppDir().getAbsolutePath();
        ServiceLoader<ProxyAuth> loader = ServiceLoader.load(ProxyAuth.class);
        Iterator<ProxyAuth> iter = loader.iterator();
        ProxyAuth.Credentials creds = iter.hasNext() ? iter.next().loadCredentials(appDir) : null;
        if (creds != null) {
            username = creds.username;
            password = creds.password;
        }
        boolean haveCreds = !StringUtil.isBlank(username) && !StringUtil.isBlank(password);

        if (StringUtil.isBlank(host)) {
            LOGGER.info("Using no proxy");
            app.conn = new Connector();
        } else {
            int pp = StringUtil.isBlank(port) ? 80 : Integer.parseInt(port);
            LOGGER.atInfo()
                .setMessage("Using proxy")
                .addKeyValue("host", host)
                .addKeyValue("port", pp)
                .addKeyValue("haveCreds", haveCreds)
                .log();
            app.conn = new Connector(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, pp)));
        }

        if (haveCreds) {
            final String fuser = username;
            final char[] fpass = password.toCharArray();
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fuser, fpass);
                }
            });
        }
    }

    public static class Resolver {
        public String dnsResolve(String host) {
            try {
                return InetAddress.getByName(host).getHostAddress();
            } catch (UnknownHostException uhe) {
                return null;
            }
        }

        public String myIpAddress() {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException uhe) {
                return null;
            }
        }
    }

    public static String[] findPACProxiesForURL(Reader pac, URL url) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("javascript");
            Bindings globals = engine.createBindings();
            globals.put("resolver", new Resolver());
            engine.setBindings(globals, ScriptContext.GLOBAL_SCOPE);
            URL utils = ProxyUtil.class.getResource("PacUtils.js");
            if (utils == null) {
                LOGGER.error("Unable to load PacUtils.js");
                return new String[0];
            }
            engine.eval(new InputStreamReader(utils.openStream()));
            Object res = engine.eval(pac);
            if (engine instanceof Invocable) {
                Object[] args = new Object[]{url.toString(), url.getHost()};
                res = ((Invocable) engine).invokeFunction("FindProxyForURL", args);
            }
            String[] proxies = res.toString().split(";");
            for (int ii = 0; ii < proxies.length; ii += 1) {
                proxies[ii] = proxies[ii].trim();
            }
            return proxies;
        } catch (Exception | NoClassDefFoundError e) {
            LOGGER.warn("Failed to resolve PAC proxy", e);
        }
        return new String[0];
    }

    private static String[] splitHostPort(String hostPort) {
        int cidx = hostPort.indexOf(":");
        if (cidx == -1) {
            return new String[]{hostPort, null};
        } else {
            return new String[]{hostPort.substring(0, cidx), hostPort.substring(cidx + 1)};
        }
    }

    private static final String PROXY_REGISTRY =
        "Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings";
}
