/* Copyright (C) 2013-2022 TU Dortmund
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
package net.automatalib.counterExamples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Run the examples as part of (integration) testing.
 *
 * @author frohme
 */
public class GraphGeneratorTest {

    static Logger logger = LoggerFactory.getLogger(GraphGeneratorTest.class);
    //GraphGenerator g = new GraphGenerator(false);

    /*public GraphGeneratorTest() throws IOException, ParserConfigurationException, SAXException {
    }

    @Test
    public void testGraphConstructionP() throws IOException, ParserConfigurationException, SAXException {
        checkJVMCompatibility();

        CompactPMPG g1 = g.pmpgList.get(0).get(0);
        Assert.assertEquals(4, g1.getNodes().size());

        int edgeCount = 0;
        for (Object node: g1.getNodes()) {
            for (Object edge: g1.getOutgoingEdges((Integer) node)) {
                edgeCount++;
            }
        }
        Assert.assertEquals(4, edgeCount);
    }
    @Test
    public void testGraphConstructionPwithNoEnd() throws IOException, ParserConfigurationException, SAXException {
        checkJVMCompatibility();

        CompactPMPG g1 = g.pmpgList.get(0).get(0);
        Assert.assertEquals(5, g1.getNodes().size());

        int edgeCount = 0;
        for (Object node: g1.getNodes()) {
            for (Object edge: g1.getOutgoingEdges((Integer) node)) {
                edgeCount++;
            }
        }
        Assert.assertEquals(5, edgeCount);
    }

    @Test
    public void testGraphConstructionS() throws IOException, ParserConfigurationException, SAXException {
        checkJVMCompatibility();

        CompactPMPG g1 = g.pmpgList.get(2).get(0);

        Assert.assertEquals(8, g1.getNodes().size());

        int edgeCount = 0;
        for (Object node: g1.getNodes()) {
            for (Object edge: g1.getOutgoingEdges((Integer) node)) {
                edgeCount++;
            }
        }
        Assert.assertEquals(12, edgeCount);
    }

    @Test
    public void testGraphConstructionT() throws IOException, ParserConfigurationException, SAXException {
        checkJVMCompatibility();

        CompactPMPG g1 = g.pmpgList.get(2).get(1);

        Assert.assertEquals(6, g1.getNodes().size());

        int edgeCount = 0;
        for (Object node: g1.getNodes()) {
            for (Object edge: g1.getOutgoingEdges((Integer) node)) {
                edgeCount++;
            }
        }
        Assert.assertEquals(7, edgeCount);
    }

    private static void checkJVMCompatibility() {
        final int canonicalSpecVersion = JVMUtil.getCanonicalSpecVersion();
        if (!(canonicalSpecVersion <= 8 || canonicalSpecVersion == 11)) {
            throw new SkipException("The headless AWT environment currently only works with Java 11 or <= 8");
        }
    }*/

}
