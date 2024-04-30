//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2018 Getdown authors
// https://github.com/bekoenig/getdown/blob/master/LICENSE

package io.github.bekoenig.getdown.util;

import io.github.bekoenig.getdown.data.Build;
import io.github.bekoenig.getdown.data.SysProps;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Optional support for compiling a URL host whitelist into the Getdown JAR or defined by system
 * property.
 * Useful if you're on the paranoid end of the security spectrum.
 *
 * @see Build#hostWhitelist()
 * @see SysProps#hostWhitelist()
 */
public final class HostWhitelist {

    /**
     * Returns the active host whitelist. If no host whitelist was set in the build (default
     * behavior), the system property with the same name is used.
     */
    private static List<String> activeHostWhitelist() {
        // The compiled host whitelist has the highest priority.
        String hostWhitelist = Build.hostWhitelist();
        // If the compiled host whitelist is blank, the system property can be used.
        if (StringUtil.isBlank(hostWhitelist)) {
            hostWhitelist = SysProps.hostWhitelist();
        }

        return Arrays.asList(StringUtil.parseStringArray(hostWhitelist));
    }

    /**
     * Verifies that the specified URL should be accessible, per the built-in host whitelist.
     * See {@link Build#hostWhitelist()} and {@link #verify(List, URL)}.
     */
    public static URL verify(URL url) throws MalformedURLException {
        return verify(activeHostWhitelist(), url);
    }

    /**
     * Verifies that the specified URL should be accessible, per the supplied host whitelist.
     * If the URL should not be accessible, this method throws a {@link MalformedURLException}.
     * If the URL should be accessible, this method simply returns the {@link URL} passed in.
     */
    public static URL verify(List<String> hosts, URL url) throws MalformedURLException {
        if (url == null || hosts.isEmpty()) {
            // either there is no URL to validate or no whitelist was configured
            return url;
        }

        String urlHost = url.getHost();
        for (String host : hosts) {
            String regex = host.replace(".", "\\.").replace("*", ".*");
            if (urlHost.matches(regex)) {
                return url;
            }
        }

        throw new MalformedURLException(
            "The host for the specified URL (" + url + ") is not in the host whitelist: " + hosts);
    }
}
