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
package net.automatalib.common.util.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import net.automatalib.common.util.IOUtil;

/**
 * A utility interface for an input stream consumer that is allowed to throw {@link IOException}s.
 */
interface InputStreamConsumer {

    void consume(InputStream is) throws IOException;

    /**
     * Consumes an input stream by throwing away all of its content.
     */
    class NOPConsumer implements InputStreamConsumer {

        @Override
        public void consume(InputStream inputStream) throws IOException {
            final byte[] buf = new byte[IOUtil.DEFAULT_BUFFER_SIZE];
            while (inputStream.read(buf) >= 0) {
                // do nothing
            }
        }
    }

    /**
     * Consumes an input stream by delegating its contents line by lines to a given String {@link Consumer}.
     */
    class DelegatingConsumer implements InputStreamConsumer {

        private final Consumer<String> delegate;

        DelegatingConsumer(Consumer<String> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void consume(InputStream inputStream) throws IOException {
            try (BufferedReader r = new BufferedReader(IOUtil.asUTF8Reader(inputStream))) {
                String line;
                while ((line = r.readLine()) != null) {
                    delegate.accept(line);
                }
            }
        }
    }
}
