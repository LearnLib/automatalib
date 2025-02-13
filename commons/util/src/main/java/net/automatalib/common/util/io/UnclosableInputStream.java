/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of AutomataLib <https://automatalib.net>.
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
package net.automatalib.common.util.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * A delegating input stream that throws an exception when closed. This is mainly used for testing purposes.
 */
public class UnclosableInputStream extends InputStream {

    private final InputStream delegate;

    public UnclosableInputStream(InputStream delegate) {
        this.delegate = delegate;
    }

    @Override
    public void close() {
        throw new AssertionError("This stream must not be closed");
    }

    @Override
    public int read() throws IOException {
        return this.delegate.read();
    }
}
