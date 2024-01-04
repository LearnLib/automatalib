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

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.List;

import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import net.automatalib.common.util.Pair;

class DOTMultiDialog<I> extends JDialog {

    DOTMultiDialog(List<Pair<String, I>> dots, boolean modal, ThrowableExtractor<I, String> extractor)
            throws IOException {
        super((Dialog) null, modal);

        final DefaultListModel<PlottedGraph> graphs = new DefaultListModel<>();
        final DOTImageComponent cmp = new DOTImageComponent();
        final JList<PlottedGraph> listBox = new JList<>(graphs);

        final Action saveDotAction = cmp.getSaveDotAction();
        final Action savePngAction = cmp.getSavePngAction();

        for (Pair<String, I> d : dots) {
            final PlottedGraph pg = new PlottedGraph(d.getFirst(), extractor.extract(d.getSecond()));
            graphs.addElement(pg);
        }

        // configure content panel
        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());

        final GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 3;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;

        final JScrollPane scrollPane = new JScrollPane(cmp);
        mainPanel.add(scrollPane, c);

        c.gridx = 1;
        c.weightx = 0.0;
        c.gridheight = 1;

        listBox.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listBox.addListSelectionListener(e -> {
            final PlottedGraph pg = listBox.getSelectedValue();
            if (pg == null) {
                cmp.setData(null);
                saveDotAction.setEnabled(false);
                savePngAction.setEnabled(false);
            } else {
                cmp.setData(pg);
                saveDotAction.setEnabled(true);
                savePngAction.setEnabled(true);
            }
            scrollPane.validate();
        });

        mainPanel.add(listBox, c);

        if (!graphs.isEmpty()) {
            listBox.setSelectedIndex(0);
        } else {
            cmp.setData(null);
            saveDotAction.setEnabled(false);
            savePngAction.setEnabled(false);
        }

        // configure this window

        final JMenu menu = new JMenu("File");
        menu.add(cmp.getSaveDotAction());
        menu.add(cmp.getSavePngAction());
        menu.addSeparator();
        menu.add(DOTUtil.getCloseAction(this));

        final JMenuBar jMenuBar = new JMenuBar();
        jMenuBar.add(menu);

        setContentPane(mainPanel);
        setJMenuBar(jMenuBar);
        setPreferredSize(new Dimension(DOTUtil.DEFAULT_WIDTH, DOTUtil.DEFAULT_HEIGHT));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        addKeyListener(DOTUtil.closeOnEscapeAdapter(this));

        pack();
        setVisible(true);
    }

    interface ThrowableExtractor<I, O> {

        O extract(I input) throws IOException;
    }
}
