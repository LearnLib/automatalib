/* Copyright (C) 2013-2018 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.modelcheckers.ltsmin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for describing LTSmin version.
 *
 * @author frohme
 */
@EqualsAndHashCode
public final class LTSminVersion {

    private static final Logger LOGGER = LoggerFactory.getLogger(LTSminVersion.class);

    /**
     * The pattern for LTSmin versioning scheme: 'v[major].[minor].[patch][ignoredSuffix]'.
     */
    private static final Pattern VERSION_PATTERN = Pattern.compile("v([0-9]+)\\.([0-9]+)\\.([0-9]+)\\.*");

    private final int major, minor, patch;

    private LTSminVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    /**
     * Returns an {@link LTSminVersion} instance described by the provided versions.
     *
     * @param major the major version
     * @param minor the minor version
     * @param patch the patch version
     *
     * @return A {@link LTSminVersion} instance described by the provided versions.
     *
     */
    public static LTSminVersion of(int major, int minor, int patch) {
        return new LTSminVersion(major, minor, patch);
    }

    /**
     * Parses an LTSmin version string and transforms it into an {@link LTSminVersion} instance. If the version string
     * cannot be parsed, this method returns a fallback version described by "v0.0.0".
     *
     * @param version
     *         the version string to parse.
     *
     * @return A {@link LTSminVersion} representation of the provided version
     */
    public static LTSminVersion parse(String version) {

        final Matcher matcher = VERSION_PATTERN.matcher(version);

        if (matcher.find()) {
            final int major = Integer.parseInt(matcher.group(1));
            final int minor = Integer.parseInt(matcher.group(2));
            final int patch = Integer.parseInt(matcher.group(3));
            final LTSminVersion result = of(major, minor, patch);

            LOGGER.debug("Found version '{}'", version);
            LOGGER.debug("Parsed as '{}'", result);

            return result;
        } else {
            final LTSminVersion fallback = of(0, 0, 0);
            LOGGER.warn("Couldn't parse LTSmin version '{}'", version);
            LOGGER.warn("Falling back to version '{}'", fallback);
            return fallback;
        }
    }

    /**
     * Checks whether {@code this} version supports the given required version.
     *
     * @param required
     *         the required version that needs to be supported
     *
     * @return {@code true}, if {@code this} version supports the given required version, {@code false} otherwise.
     */
    public boolean supports(LTSminVersion required) {
        return major >= required.major && minor >= required.minor && patch >= required.patch;
    }

    @Override
    public String toString() {
        return String.format("v%d.%d.%d", major, minor, patch);
    }
}
