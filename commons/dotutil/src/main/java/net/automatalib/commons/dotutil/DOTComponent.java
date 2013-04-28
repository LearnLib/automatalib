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

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.automatalib.commons.util.IOUtil;

public class DOTComponent extends ImageComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String dot;
	
	private final Action saveDotAction = new AbstractAction("Save DOT") {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser saveDlg = new JFileChooser();
			saveDlg.setFileFilter(DOTMisc.DOT_FILTER);
			int result = saveDlg.showSaveDialog(DOTComponent.this);
			if(result != JFileChooser.APPROVE_OPTION)
				return;
			try {
				Writer w = new BufferedWriter(new FileWriter(saveDlg.getSelectedFile()));
				w.write(dot);
				w.close();
			}
			catch(IOException ex) {
				JOptionPane.showMessageDialog(DOTComponent.this, "Could not save DOT file: " + ex.getMessage(), "Cannot save DOT", JOptionPane.ERROR_MESSAGE);
			}
		}
		
	};
	
	public DOTComponent() {}
	
	public DOTComponent(Reader dotReader) throws IOException {
		renderDot(dotReader);
	}
	
	
	private void renderDot(Reader dotReader) throws IOException {
		StringWriter w = new StringWriter();
		
		IOUtil.copy(dotReader, w);
		String dot = w.getBuffer().toString();
		 
		BufferedImage img = DOT.renderDOTImage(dot);
		
		super.setImage(img);
		this.dot = dot;
	}

	public String getDot() {
		return dot;
	}
	
	public Action getSaveDotAction() {
		return saveDotAction;
	}
}
