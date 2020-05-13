package net.automatalib.util.ts.modal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.ts.modal.CompactMC;
import net.automatalib.ts.modal.CompactMTS;
import net.automatalib.ts.modal.MTSTransition;
import net.automatalib.ts.modal.ModalContractEdgeProperty;
import net.automatalib.ts.modal.ModalContractEdgePropertyImpl;
import net.automatalib.ts.modal.MutableModalContractEdgeProperty;
import net.automatalib.ts.modal.MutableModalEdgeProperty;

public class DecompositionInstance {

    public CompactMTS<String> system;
    public CompactMTS<String> context;
    public CompactMTS<String> orig_sys;
    public CompactMC<String> modal_contract;

    public DecompositionInstance(DecompositionTest decompositionTest) throws IOException {
        system = loadMTSFromPath(decompositionTest.system);
        context = loadMTSFromPath(decompositionTest.context);
        orig_sys = loadMTSFromPath(decompositionTest.orig_sys);
        modal_contract = loadMCFromPath(decompositionTest.modal_contract);
    }

    protected CompactMTS<String> loadMTSFromPath(String path) throws IOException {
        Path file = Paths.get(path);
        if (!Files.exists(file) || !file.toString().endsWith(".dot")) {
            throw new FileNotFoundException("Expected " + path + " to be an existing .dot file!");
        }

        return DOTParsers.mts().readModel(file.toFile()).model;
    }

    protected CompactMC<String> loadMCFromPath(String path) throws IOException {
        Path file = Paths.get(path);
        if (!Files.exists(file) || !file.toString().endsWith(".dot")) {
            throw new FileNotFoundException("Expected " + path + " to be an existing .dot file!");
        }

        CompactMC<String> parsed = DOTParsers.mc().readModel(file.toFile()).model;

        for (Integer s : parsed.getStates()) {
            for (String label : parsed.getInputAlphabet()) {
                for (MTSTransition<String, MutableModalContractEdgeProperty> transition : parsed.getTransitions(s, label)) {

                    if (transition.getProperty().getColor() == ModalContractEdgeProperty.EdgeColor.RED ||
                        transition.getProperty().getColor() == ModalContractEdgeProperty.EdgeColor.GREEN) {
                        parsed.getCommunicationAlphabet().add(label);
                    }

                }
            }
        }

        return parsed;
    }
}
