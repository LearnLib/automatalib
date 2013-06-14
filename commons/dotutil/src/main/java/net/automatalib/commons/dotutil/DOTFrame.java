/* Copyright (C) 2013 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * AutomataLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * AutomataLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with AutomataLib; if not, see
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package net.automatalib.commons.dotutil;

import java.io.IOException;
import java.io.Reader;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class DOTFrame extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	private final DOTPanel dotPanel = new DOTPanel(); 
	
	public DOTFrame() {
		this("Graphs");
	}
	
	public DOTFrame(String title) {
		super(title);
		setContentPane(dotPanel);
		
		JMenu menu = new JMenu("File");
		menu.add(dotPanel.getSavePngAction());
		menu.add(dotPanel.getSaveDotAction());
		menu.add(dotPanel.getRenameAction());
		menu.addSeparator();
		menu.add(dotPanel.getClearAction());
		setJMenuBar(new JMenuBar());
		getJMenuBar().add(menu);
		
		pack();
	}
	
	public void addGraph(String name, Reader dotText) throws IOException {
		dotPanel.addGraph(name, dotText);
	}
	
	public void addGraph(String name, String dotText) {
		dotPanel.addGraph(name, dotText);
	}
	
}
