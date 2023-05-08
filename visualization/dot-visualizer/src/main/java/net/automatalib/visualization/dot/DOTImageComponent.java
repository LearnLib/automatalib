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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.automatalib.commons.util.IOUtil;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component that displays a {@link BufferedImage}.
 *
 * @author Malte Isberner
 */
class DOTImageComponent extends JComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(DOTImageComponent.class);

    private @Nullable String dot;
    private @Nullable BufferedImage img;

    private final Action saveDotAction = new AbstractAction("Save DOT") {

        @Override
        public void actionPerformed(ActionEvent e) {
            final JFileChooser saveDlg = new JFileChooser();
            saveDlg.setFileFilter(DOTUtil.DOT_FILTER);
            final int result = saveDlg.showSaveDialog(DOTImageComponent.this);
            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }
            final String dot = DOTImageComponent.this.dot;
            if (dot == null) {
                throw new IllegalStateException("No DOT text has been set");
            }
            try (Writer w = IOUtil.asBufferedUTF8Writer(saveDlg.getSelectedFile())) {
                w.write(dot);
            } catch (IOException ex) {
                LOGGER.error("Cannot save DOT", ex);
                JOptionPane.showMessageDialog(DOTImageComponent.this,
                                              "Could not save DOT file: " + ex.getMessage(),
                                              "Cannot save DOT",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    };

    private final Action savePngAction = new AbstractAction("Save PNG") {

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(DOTUtil.PNG_FILTER);
            int result = chooser.showSaveDialog(DOTImageComponent.this);
            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }
            final BufferedImage img = DOTImageComponent.this.img;
            if (img == null) {
                throw new IllegalStateException("No image has been set");
            }
            try {
                ImageIO.write(img, "png", chooser.getSelectedFile());
            } catch (IOException ex) {
                LOGGER.error("Cannot save PNG", ex);
                JOptionPane.showMessageDialog(DOTImageComponent.this,
                                              "Could not save PNG file: " + ex.getMessage(),
                                              "Could not save PNG",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    };

    /**
     * Default constructor.
     */
    DOTImageComponent() {
        setPreferredSize(new Dimension(DOTUtil.DEFAULT_WIDTH, DOTUtil.DEFAULT_HEIGHT));
    }

    /**
     * Sets the image to be displayed.
     *
     * @param img
     *         the image to be displayed
     */
    void setImage(@UnknownInitialization(DOTImageComponent.class) DOTImageComponent this, @Nullable BufferedImage img) {
        this.img = img;
        Dimension dim;
        if (img != null) {
            dim = new Dimension(img.getWidth(), img.getHeight());
        } else {
            dim = new Dimension(DOTUtil.DEFAULT_WIDTH, DOTUtil.DEFAULT_HEIGHT);
        }

        setSize(dim);
        setPreferredSize(dim);
        repaint();
    }

    void setDotText(@Nullable String dot) {
        this.dot = dot;
    }

    void setData(@Nullable PlottedGraph pg) {
        if (pg == null) {
            this.setDotText(null);
            this.setImage(null);
        } else {
            this.setDotText(pg.getDotText());
            this.setImage(pg.getImage());
        }
    }

    /**
     * Retrieves an {@link Action} to save the image in a PNG file.
     *
     * @return the action
     */
    Action getSavePngAction() {
        return savePngAction;
    }

    Action getSaveDotAction() {
        return saveDotAction;
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (img != null) {
            g.drawImage(img, 0, 0, null);
        }
    }
}
