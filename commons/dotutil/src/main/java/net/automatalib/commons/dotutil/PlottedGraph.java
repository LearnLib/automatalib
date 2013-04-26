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
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package net.automatalib.commons.dotutil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

final class PlottedGraph {
	private String name;
	private String dotText;
	private BufferedImage image;
	
	
	public PlottedGraph(String name, Reader dotText) throws IOException {
		this.name = name;
		
		StringBuilder sb = new StringBuilder();
		
		char[] buf = new char[1024];
		int len;
		while((len = dotText.read(buf)) != -1)
			sb.append(buf, 0, len);
		
		try {
			dotText.close();
		}
		catch(IOException e) {}
		
		updateDOTText(sb.toString());
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public void setImage(BufferedImage image) {
		this.image = image;
	}
	
	public String getDOTText() {
		return dotText;
	}
	
	public boolean updateDOTText(String dotText) {
		try {
			InputStream pngIs = DOT.runDOT(dotText, "png");
			try {
				BufferedImage img = ImageIO.read(pngIs);
				this.image = img;
			}
			finally {
				pngIs.close();
			}
		}
		catch(IOException e) {
			JOptionPane.showMessageDialog(null, "Failed to invoke the DOT command!", "Failure rendering graph", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		this.dotText = dotText;
		return true;
	}

	public void saveDot(File file) throws IOException {
		FileWriter fw = new FileWriter(file);
		try {
			fw.write(dotText);
		}
		finally {
			fw.close();
		}
	}
	
	public void savePng(File file) throws IOException {
		ImageIO.write(this.image, "png", file);
	}
}
