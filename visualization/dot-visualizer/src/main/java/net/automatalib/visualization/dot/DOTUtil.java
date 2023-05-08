/* Copyright (C) 2013-2023 TU Dortmund
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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.checkerframework.checker.initialization.qual.UnderInitialization;

final class DOTUtil {

    static final FileFilter DOT_FILTER = new FileNameExtensionFilter("GraphVIZ file (*.dot)", "dot");

    static final FileFilter PNG_FILTER = new FileNameExtensionFilter("Portable Network Graphics (*.png)", "png");

    static final int DEFAULT_WIDTH = 800;

    static final int DEFAULT_HEIGHT = 600;

    private DOTUtil() {}

    static AbstractAction getCloseAction(@UnderInitialization(Window.class) Window w) {
        return new AbstractAction("Close") {

            @Override
            public void actionPerformed(ActionEvent e) {
                w.dispatchEvent(new WindowEvent(w, WindowEvent.WINDOW_CLOSING));
            }
        };
    }

    static KeyAdapter closeOnEscapeAdapter(@UnderInitialization(Window.class) Window w) {
        return new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
                    w.dispatchEvent(new WindowEvent(w, WindowEvent.WINDOW_CLOSING));
                }
            }
        };
    }
}
