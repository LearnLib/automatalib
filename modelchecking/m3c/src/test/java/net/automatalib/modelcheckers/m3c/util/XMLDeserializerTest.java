/* Copyright (C) 2013-2021 TU Dortmund
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
package net.automatalib.modelcheckers.m3c.util;

import java.io.File;

import net.automatalib.modelcheckers.m3c.cfps.CFPS;
import org.testng.annotations.Test;

import static net.automatalib.modelcheckers.m3c.util.TestUtil.assertCorrectlyCreated;

class XMLDeserializerTest {

    @Test
    void testPaperExample() {
        String pathToFile = "src/test/resources/example_models/paper_model.xml";
        XMLDeserializer deserializer = new XMLDeserializer(new File(pathToFile));
        CFPS cfps = deserializer.deserialize();
        assertCorrectlyCreated(cfps);
    }

    @Test
    void testSerialization() {
        String pathToFile = "src/test/resources/example_models/paper_model.xml";
        XMLDeserializer deserializer = new XMLDeserializer(new File(pathToFile));
        CFPS cfps = deserializer.deserialize();
        XMLSerializer serializer = new XMLSerializer(cfps);
        String cfpsAsString = serializer.serialize();
        deserializer = new XMLDeserializer(cfpsAsString);
        cfps = deserializer.deserialize();
        assertCorrectlyCreated(cfps);
    }

}
