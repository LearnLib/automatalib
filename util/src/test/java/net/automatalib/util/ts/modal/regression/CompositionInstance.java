package net.automatalib.util.ts.modal.regression;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.ts.modal.CompactMTS;

public class CompositionInstance {

    public CompactMTS<String> input0;
    public CompactMTS<String> input1;
    public CompactMTS<String> merge;

    public CompositionInstance(CompositionTest compositionTest) throws IOException {
        input0 = loadMTSFromPath(compositionTest.input0);
        input1 = loadMTSFromPath(compositionTest.input1);
        merge = loadMTSFromPath(compositionTest.merge);
    }

    protected CompactMTS<String> loadMTSFromPath(String path) throws IOException {
        Path file = Paths.get(path);
        if (!Files.exists(file) || !file.toString().endsWith(".dot")) {
            throw new FileNotFoundException("Expected "+path+" to be an existing .dot file!");
        }

         return DOTParsers.mts().readModel(file.toFile()).model;
    }

}
