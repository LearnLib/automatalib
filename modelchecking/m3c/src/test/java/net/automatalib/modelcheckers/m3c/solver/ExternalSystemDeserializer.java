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
package net.automatalib.modelcheckers.m3c.solver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.automatalib.graphs.ModalContextFreeProcessSystem;
import net.automatalib.graphs.MutableModalProcessGraph;
import net.automatalib.graphs.base.DefaultMCFPS;
import net.automatalib.graphs.base.compact.CompactMPG;
import net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType;
import net.automatalib.ts.modal.transition.MutableProceduralModalEdgeProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

final class ExternalSystemDeserializer {

    private ExternalSystemDeserializer() {
        // prevent instantiation
    }

    static <AP> ModalContextFreeProcessSystem<String, AP> parse(InputStream is)
            throws ParserConfigurationException, IOException, SAXException {

        final Map<String, Integer> idToNode = new HashMap<>();
        final CompactMPG<String, AP> mpg = new CompactMPG<>("");

        final Element root = getRoot(is);

        final Element statesElement = getFirstElementByTagName(root, "states");
        final NodeList states = statesElement.getElementsByTagName("state");
        for (int i = 0; i < states.getLength(); i++) {
            final Element state = (Element) states.item(i);
            addNode(mpg, state, idToNode);
        }

        final Element transitionsElement = getFirstElementByTagName(root, "transitions");
        final NodeList transitions = transitionsElement.getElementsByTagName("transition");
        for (int i = 0; i < transitions.getLength(); i++) {
            final Element transition = (Element) transitions.item(i);
            addEdge(mpg, transition, idToNode);
        }

        return new DefaultMCFPS<>("main", Collections.singletonMap("main", mpg));
    }

    private static Element getRoot(InputStream is) throws ParserConfigurationException, IOException, SAXException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document document = builder.parse(is);
        return document.getDocumentElement();
    }

    private static Element getFirstElementByTagName(Element elem, String tagName) {
        final Node node = elem.getElementsByTagName(tagName).item(0);
        assert node != null;
        return (Element) node;
    }

    private static <N> void addNode(MutableModalProcessGraph<N, String, ?, ?, ?> mpg,
                                    Element state,
                                    Map<String, N> idToNode) {

        final String id = getFirstElementByTagName(state, "id").getTextContent();
        final Element attributes = getFirstElementByTagName(state, "attributes");
        boolean isInitial = "true".equals(getFirstElementByTagName(attributes, "isInitial").getTextContent());

        final N node = mpg.addNode(Collections.emptySet());

        if (isInitial) {
            mpg.setInitialNode(node);
        } else if ("end".equals(id)) {
            mpg.setFinalNode(node);
        }

        idToNode.put(id, node);
    }

    private static <N, E> void addEdge(MutableModalProcessGraph<N, String, E, ?, ? extends MutableProceduralModalEdgeProperty> mpg,
                                       Element transition,
                                       Map<String, N> idToNode) {
        final String sourceId = getFirstElementByTagName(transition, "sourceId").getTextContent();
        final String targetId = getFirstElementByTagName(transition, "targetId").getTextContent();
        final String label = getFirstElementByTagName(transition, "label").getTextContent();

        final Element attributesElement = getFirstElementByTagName(transition, "attributes");
        final String isMust = getFirstElementByTagName(attributesElement, "isMust").getTextContent();

        final E edge = mpg.connect(idToNode.get(sourceId), idToNode.get(targetId));
        mpg.getEdgeProperty(edge).setInternal();
        mpg.getEdgeProperty(edge).setModalType("true".equals(isMust) ? ModalType.MUST : ModalType.MAY);
        mpg.setEdgeLabel(edge, label);
    }

}
