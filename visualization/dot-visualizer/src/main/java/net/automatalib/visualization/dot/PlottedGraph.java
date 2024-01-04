/* Copyright (C) 2013-2024 TU Dortmund University
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
package net.automatalib.visualization.dot;

import java.awt.image.BufferedImage;
import java.io.IOException;

class PlottedGraph {

    private final String name;
    private final String dotText;
    private final BufferedImage image;

    PlottedGraph(String name, String dotText) throws IOException {
        this.name = name;
        this.dotText = dotText;
        this.image = DOT.renderDOTImage(dotText);
    }

    String getDotText() {
        return dotText;
    }

    BufferedImage getImage() {
        return image;
    }

    @Override
    public String toString() {
        return name;
    }
}
