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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JViewport;
import javax.swing.event.MouseInputAdapter;

import net.automatalib.common.util.IOUtil;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component that displays a {@link BufferedImage}.
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
        final DnDMover dnDMover = new DnDMover();

        setPreferredSize(new Dimension(DOTUtil.DEFAULT_WIDTH, DOTUtil.DEFAULT_HEIGHT));
        addMouseListener(dnDMover);
        addMouseMotionListener(dnDMover);
    }

    /**
     * Sets the image to be displayed.
     *
     * @param img
     *         the image to be displayed
     */
    void setImage(@UnknownInitialization(DOTImageComponent.class)DOTImageComponent this, @Nullable BufferedImage img) {
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

    /*
     * Based on https://stackoverflow.com/questions/31171502/scroll-jscrollpane-by-dragging-mouse-java-swing/58815302#58815302
     */
    private class DnDMover extends MouseInputAdapter {

        private final DOTImageComponent cmp;
        private Point holdPointOnView;

        DnDMover() {
            this.cmp = DOTImageComponent.this;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            cmp.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            holdPointOnView = e.getPoint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            cmp.setCursor(null);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            final Point dragEventPoint = e.getPoint();
            final JViewport viewport = (JViewport) cmp.getParent();

            assert viewport != null;

            final Point viewPos = viewport.getViewPosition();

            final int maxViewPosX = cmp.getWidth() - viewport.getWidth();
            final int maxViewPosY = cmp.getHeight() - viewport.getHeight();

            assert holdPointOnView != null;

            if (cmp.getWidth() > viewport.getWidth()) {
                viewPos.x -= dragEventPoint.x - holdPointOnView.x;

                if (viewPos.x < 0) {
                    viewPos.x = 0;
                    holdPointOnView.x = dragEventPoint.x;
                }

                if (viewPos.x > maxViewPosX) {
                    viewPos.x = maxViewPosX;
                    holdPointOnView.x = dragEventPoint.x;
                }
            }

            if (cmp.getHeight() > viewport.getHeight()) {
                viewPos.y -= dragEventPoint.y - holdPointOnView.y;

                if (viewPos.y < 0) {
                    viewPos.y = 0;
                    holdPointOnView.y = dragEventPoint.y;
                }

                if (viewPos.y > maxViewPosY) {
                    viewPos.y = maxViewPosY;
                    holdPointOnView.y = dragEventPoint.y;
                }
            }

            viewport.setViewPosition(viewPos);
        }
    }
}
