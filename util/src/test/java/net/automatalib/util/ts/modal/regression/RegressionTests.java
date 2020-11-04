package net.automatalib.util.ts.modal.regression;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.serialization.dot.DOTSerializationProvider;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.ts.modal.CompactMTS;
import net.automatalib.ts.modal.MTSTransition;
import net.automatalib.ts.modal.transitions.ModalEdgeProperty;
import net.automatalib.ts.modal.transitions.ModalEdgePropertyImpl;
import net.automatalib.ts.modal.transitions.MutableModalEdgeProperty;
import net.automatalib.util.ts.modal.MCUtil;
import net.automatalib.util.ts.modal.MTSUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RegressionTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegressionTests.class);

    @DataProvider(name="Decomp")
    public Object[][] loadDecompositionTests() {
        Path config = Paths.get("src/test/resources/test_cases.json");

        Gson gson = new GsonBuilder().create();
        RegressionTestBundle bundle = null;

        try (Reader reader = Files.newBufferedReader(config)) {
            bundle = gson.fromJson(reader, RegressionTestBundle.class);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("Found {} decomposition tests", bundle.modalDecompositionTests.size());
        Object[][] params = new Object[bundle.modalDecompositionTests.size()][1];
        int i = 0;
        for (DecompositionTest testCase : bundle.modalDecompositionTests) {
            params[i++] = new Object[] {testCase};
        }
        return params;
    }

    @DataProvider(name="Comp")
    public Object[][] loadCompositionTests() {
        Path config = Paths.get("src/test/resources/test_cases.json");

        Gson gson = new GsonBuilder().create();
        RegressionTestBundle bundle = null;

        try (Reader reader = Files.newBufferedReader(config)) {
            bundle = gson.fromJson(reader, RegressionTestBundle.class);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("Found {} composition tests", bundle.modalCompositionTests.size());
        Object[][] params = new Object[bundle.modalCompositionTests.size()][1];
        int i = 0;
        for (CompositionTest testCase : bundle.modalCompositionTests) {
            params[i++] = new Object[] {testCase};
        }
        return params;
    }

    @Test(dataProvider = "Comp")
    public void testMerge(CompositionTest testCase) throws IOException {
        testCase.input0 = "src/test/resources/phil3/" + testCase.input0;
        testCase.input1 = "src/test/resources/phil3/" + testCase.input1;
        testCase.merge = "src/test/resources/phil3/" + testCase.merge;
        CompositionInstance instance = new CompositionInstance(testCase);

        LOGGER.debug("Testing merge for "+testCase.merge);
        LOGGER.debug("Components: "+testCase.input0 + ", " +testCase.input1);
        LOGGER.debug("Input alphabets: ["+instance.input0.getInputAlphabet()+"] ["+instance.input1.getInputAlphabet()+"]");

        CompactMTS<String> currentMerge = MTSUtil.compose(instance.input0,
                                                          instance.input1);

        assertThat(currentMerge.getInputAlphabet(), containsInAnyOrder(instance.merge.getInputAlphabet().toArray()));
        assertThat(instance.merge.getInputAlphabet(), containsInAnyOrder(currentMerge.getInputAlphabet().toArray()));
        Assert.assertTrue(MTSUtil.isRefinementOf(currentMerge, instance.merge, currentMerge.getInputAlphabet()));
        Assert.assertTrue(MTSUtil.isRefinementOf(instance.merge, currentMerge, currentMerge.getInputAlphabet()));
    }

    @Test(dataProvider = "Decomp")
    public void testDecompContext(DecompositionTest testCase) throws IOException {
        if (testCase.modal_contract == null) {
            return;
        }
        testCase.context = "src/test/resources/phil3/" + testCase.context;
        testCase.modal_contract = "src/test/resources/phil3/" + testCase.modal_contract;
        testCase.system = "src/test/resources/phil3/" + testCase.system;
        testCase.orig_sys = "src/test/resources/phil3/" + testCase.orig_sys;
        DecompositionInstance instance = new DecompositionInstance(testCase);

        LOGGER.debug("Testing decomp for "+testCase.orig_sys);
        LOGGER.debug("Modal contract "+testCase.modal_contract);
        LOGGER.debug("Components: "+testCase.context + ", " +testCase.system);
        LOGGER.debug("Com. alphabet: {}", instance.modal_contract.getCommunicationAlphabet());

        MCUtil.SystemComponent<CompactMTS<String>, Integer, String, MTSTransition<String, MutableModalEdgeProperty>, MutableModalEdgeProperty>
                systemComponent = MCUtil.systemComponent(instance.modal_contract,
                                                         new CompactMTS.Creator<String>(),
                                                         (x) -> new ModalEdgePropertyImpl(x.getProperty().getType()),
                                                         () -> new ModalEdgePropertyImpl(ModalEdgeProperty.ModalType.MAY));

        CompactDFA<String> redLanguage = (CompactDFA<String>) MCUtil.redContextLanguage(systemComponent, instance.modal_contract.getCommunicationAlphabet());
        CompactMTS<String> redContext = MCUtil.redContextComponent(redLanguage,
                                                new CompactMTS.Creator<String>(),
                                                instance.modal_contract.getCommunicationAlphabet(),
                                                () -> new ModalEdgePropertyImpl(ModalEdgeProperty.ModalType.MAY));

        CompactDFA<String> greenLanguage = (CompactDFA<String>) MCUtil.greenContextLanguage(instance.modal_contract);
        CompactMTS<String> greenContext = MCUtil.greenContextComponent(greenLanguage,
                                                    new CompactMTS.Creator<String>(),
                                                    instance.modal_contract.getCommunicationAlphabet(),
                                                    () -> new ModalEdgePropertyImpl(ModalEdgeProperty.ModalType.MAY),
                                                    () -> new ModalEdgePropertyImpl(ModalEdgeProperty.ModalType.MUST));

        CompactMTS<String> context = MTSUtil.conjunction(greenContext, redContext);

        Assert.assertTrue(MTSUtil.isRefinementOf(context, instance.context, context.getInputAlphabet()));
    }

    @Test(dataProvider = "Decomp")
    public void testDecompContextReverse(DecompositionTest testCase) throws IOException {
        if (testCase.modal_contract == null) {
            return;
        }
        testCase.context = "src/test/resources/phil3/" + testCase.context;
        testCase.modal_contract = "src/test/resources/phil3/" + testCase.modal_contract;
        testCase.system = "src/test/resources/phil3/" + testCase.system;
        testCase.orig_sys = "src/test/resources/phil3/" + testCase.orig_sys;
        DecompositionInstance instance = new DecompositionInstance(testCase);

        LOGGER.debug("Testing decomp for "+testCase.orig_sys);
        LOGGER.debug("Modal contract "+testCase.modal_contract);
        LOGGER.debug("Components: "+testCase.context + ", " +testCase.system);
        LOGGER.debug("Com. alphabet: {}", instance.modal_contract.getCommunicationAlphabet());

        MCUtil.SystemComponent<CompactMTS<String>, Integer, String, MTSTransition<String, MutableModalEdgeProperty>, MutableModalEdgeProperty> systemComponent = MCUtil.systemComponent(instance.modal_contract,
                new CompactMTS.Creator<String>(),
                (x) -> new ModalEdgePropertyImpl(x.getProperty().getType()),
                () -> new ModalEdgePropertyImpl(ModalEdgeProperty.ModalType.MAY));

        CompactDFA<String> redLanguage = (CompactDFA<String>) MCUtil.redContextLanguage(systemComponent, instance.modal_contract.getCommunicationAlphabet());
        CompactMTS<String> redContext = MCUtil.redContextComponent(redLanguage,
                new CompactMTS.Creator<String>(),
                instance.modal_contract.getCommunicationAlphabet(),
                () -> new ModalEdgePropertyImpl(ModalEdgeProperty.ModalType.MAY));

        CompactDFA<String> greenLanguage = (CompactDFA<String>) MCUtil.greenContextLanguage(instance.modal_contract);
        CompactMTS<String> greenContext = MCUtil.greenContextComponent(greenLanguage,
                new CompactMTS.Creator<String>(),
                instance.modal_contract.getCommunicationAlphabet(),
                () -> new ModalEdgePropertyImpl(ModalEdgeProperty.ModalType.MAY),
                () -> new ModalEdgePropertyImpl(ModalEdgeProperty.ModalType.MUST));

        CompactMTS<String> context = MTSUtil.conjunction(greenContext, redContext);

        if (!MTSUtil.isRefinementOf(instance.context, context, context.getInputAlphabet())) {
            final StringWriter sw = new StringWriter();
            GraphDOT.write(context.graphView(), sw);
            Assert.fail("Conjunction is not a refinement of model: " + sw.toString());
        }
    }

    @Test(dataProvider = "Decomp")
    public void testDecompSystem(DecompositionTest testCase) throws IOException {
        if (testCase.modal_contract == null) {
            return;
        }
        testCase.context = "src/test/resources/phil3/" + testCase.context;
        testCase.modal_contract = "src/test/resources/phil3/" + testCase.modal_contract;
        testCase.system = "src/test/resources/phil3/" + testCase.system;
        testCase.orig_sys = "src/test/resources/phil3/" + testCase.orig_sys;
        DecompositionInstance instance = new DecompositionInstance(testCase);

        LOGGER.debug("Testing decomp for "+testCase.orig_sys);
        LOGGER.debug("Modal contract "+testCase.modal_contract);
        LOGGER.debug("Components: "+testCase.context + ", " +testCase.system);
        LOGGER.debug("Com. alphabet: {}", instance.modal_contract.getCommunicationAlphabet());

        MCUtil.SystemComponent<CompactMTS<String>, Integer, String, MTSTransition<String, MutableModalEdgeProperty>, MutableModalEdgeProperty> systemComponent = MCUtil.systemComponent(instance.modal_contract,
                                                                                                                                                                                        new CompactMTS.Creator<String>(),
                                                                                                                                                                                        (x) -> new ModalEdgePropertyImpl(x.getProperty().getType()),
                                                                                                                                                                                        () -> new ModalEdgePropertyImpl(ModalEdgeProperty.ModalType.MAY));

        Assert.assertTrue(MTSUtil.isRefinementOf(instance.system, systemComponent.systemComponent, systemComponent.systemComponent.getInputAlphabet()));
    }

}
