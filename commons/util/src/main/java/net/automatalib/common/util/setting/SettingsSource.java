/* Copyright (C) 2013-2024 TU Dortmund University
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
package net.automatalib.common.util.setting;

import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;

import net.automatalib.common.util.collection.IteratorUtil;

/**
 * Utility interface to mark the source of a setting.
 */
public interface SettingsSource {

    /**
     * Convenience method for parsing properties from a {@link SettingsSource}. Note that this method requires an
     * instantiated {@link ServiceLoader} instead of just the settings class, to support polymorphic
     * {@link SettingsSource}s in a JPMS environment. Otherwise, {@code this} module would have to declare a
     * {@code uses} clause and only true {@link SettingsSource}s could be read.
     *
     * @param loader
     *         the service loader
     * @param <S>
     *         concrete {@link SettingsSource} type
     *
     * @return the filled properties after all loaded {@link SettingsSource}s have been queried
     */
    static <S extends SettingsSource> Properties readSettings(ServiceLoader<S> loader) {
        Properties p = new Properties();
        readSettings(loader, p);
        return p;
    }

    /**
     * Convenience method for {@link #readSettings(ServiceLoader)} that directly writes to the given {@link Properties}
     * object.
     *
     * @param loader
     *         the service loader
     * @param p
     *         the properties object to write to
     * @param <S>
     *         concrete {@link SettingsSource} type
     */
    static <S extends SettingsSource> void readSettings(ServiceLoader<S> loader, Properties p) {
        List<S> sources = IteratorUtil.list(loader.iterator());
        sources.sort(Comparator.comparingInt(SettingsSource::getPriority));

        for (S source : sources) {
            source.loadSettings(p);
        }
    }

    /**
     * Load the parsed settings into the given property object.
     *
     * @param props
     *         the object to write the settings to
     */
    void loadSettings(Properties props);

    /**
     * Returns the priority of this source. This is used to decide which source wins, if multiple sources write the same
     * properties.
     *
     * @return the priority
     */
    default int getPriority() {
        return 0;
    }
}
