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
