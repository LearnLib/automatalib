/* Copyright (C) 2013-2018 TU Dortmund
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DOTPanel extends JPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(DOTPanel.class);

    private static final long serialVersionUID = 1L;
    private final ImageComponent imgComponent;
    private final JList<PlottedGraph> listBox;
    private final DefaultListModel<PlottedGraph> graphs;
    private final Action saveDotAction = new AbstractAction() {

        private static final long serialVersionUID = -1L;

        {
            putValue(NAME, "Save DOT");
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            saveDOT();
        }
    };
    private final Action savePngAction = new AbstractAction() {

        private static final long serialVersionUID = -1L;

        {
            putValue(NAME, "Save PNG");
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            savePNG();
        }
    };
    private final Action renameAction = new AbstractAction() {

        private static final long serialVersionUID = -1L;

        {
            putValue(NAME, "Rename");
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            rename();
        }
    };
    private final Action clearAction = new AbstractAction() {

        private static final long serialVersionUID = -1L;

        {
            putValue(NAME, "Clear");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            clear();
        }
    };

    public DOTPanel() {
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 3;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        this.imgComponent = new ImageComponent();
        final JScrollPane scrollPane = new JScrollPane(imgComponent);
        add(scrollPane, c);

        c.gridx = 1;
        c.weightx = 0.0;
        c.gridheight = 1;
        this.graphs = new DefaultListModel<>();
        listBox = new JList<>(graphs);
        listBox.addListSelectionListener(e -> {
            int idx = listBox.getSelectedIndex();
            boolean activeSelection = (idx != -1);
            if (!activeSelection) {
                imgComponent.setImage(null);
            } else {
                PlottedGraph pg = graphs.get(idx);
                imgComponent.setImage(pg.getImage());
            }
            saveDotAction.setEnabled(activeSelection);
            savePngAction.setEnabled(activeSelection);
            renameAction.setEnabled(activeSelection);
            scrollPane.validate();
        });
        add(new JScrollPane(listBox), c);

        c.gridy = 1;
        c.weighty = 0.0;
        JButton savePngBtn = new JButton(savePngAction);
        add(savePngBtn, c);

        c.gridy = 2;
        JButton saveDotBtn = new JButton(saveDotAction);
        add(saveDotBtn, c);
    }

    public Action getSaveDotAction() {
        return saveDotAction;
    }

    public Action getSavePngAction() {
        return savePngAction;
    }

    public Action getRenameAction() {
        return renameAction;
    }

    public Action getClearAction() {
        return clearAction;
    }

    public void saveDOT() {
        PlottedGraph pg = listBox.getSelectedValue();
        if (pg == null) {
            JOptionPane.showMessageDialog(this,
                                          "Cannot save DOT: No active graph!",
                                          "Cannot save DOT",
                                          JOptionPane.ERROR_MESSAGE);
            return;
        }
        JFileChooser saveDlg = new JFileChooser();
        saveDlg.setFileFilter(DOTMisc.DOT_FILTER);
        int result = saveDlg.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            pg.saveDot(saveDlg.getSelectedFile());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                                          "Could not save DOT file: " + e.getMessage(),
                                          "Cannot save DOT",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }

    public void savePNG() {
        PlottedGraph pg = listBox.getSelectedValue();
        if (pg == null) {
            JOptionPane.showMessageDialog(this,
                                          "Cannot save PNG: No active graph!",
                                          "Cannot save PNG",
                                          JOptionPane.ERROR_MESSAGE);
            return;
        }
        JFileChooser saveDlg = new JFileChooser();
        saveDlg.setFileFilter(DOTMisc.PNG_FILTER);
        File f = new File(saveDlg.getCurrentDirectory(), pg.getName() + ".png");
        saveDlg.setSelectedFile(f);
        int result = saveDlg.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            pg.savePng(saveDlg.getSelectedFile());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                                          "Could not save PNG file: " + e.getMessage(),
                                          "Cannot save PNG",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }

    public void addGraph(String name, String dotText) {
        try {
            addGraph(name, new StringReader(dotText));
        } catch (IOException e) {
            LOGGER.error("Could not add graph", e);
        }
    }

    public void addGraph(String name, Reader dotText) throws IOException {
        PlottedGraph pg = new PlottedGraph(name, dotText);
        graphs.addElement(pg);
    }

    public void clear() {
        graphs.clear();
    }

    public void rename() {
        PlottedGraph pg = listBox.getSelectedValue();
        if (pg == null) {
            JOptionPane.showMessageDialog(this,
                                          "Cannot rename: No active graph!",
                                          "Cannot rename",
                                          JOptionPane.ERROR_MESSAGE);
            return;
        }

        String input = JOptionPane.showInputDialog(this, "Enter new name: ", "Enter name");
        if (input == null) {
            return;
        }
        pg.setName(input);
    }

    public Writer createDotWriter(String name) {
        return new DotWriter(name);
    }

    private final class DotWriter extends StringWriter {

        private final String name;

        DotWriter(String name) {
            this.name = name;
        }

        @Override
        public void close() throws IOException {
            addGraph(name, getBuffer().toString());
            super.close();
        }
    }
}
