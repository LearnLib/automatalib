package net.automatalib.util.ts.modal;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.testng.annotations.Test;

public class RegressionTests {

    @Test
    void loadAllTests() {

        Path config = Paths.get("src/test/resources/test_cases.jsn");

        Gson gson = new GsonBuilder().create();
        RegressionTestBundle bundle = null;

        try (Reader reader = Files.newBufferedReader(config)) {
            bundle = gson.fromJson(reader, RegressionTestBundle.class);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }


        for (CompositionTest merges : bundle.modalCompositionTests) {
            System.out.println(merges.merge);
        }
    }


}
