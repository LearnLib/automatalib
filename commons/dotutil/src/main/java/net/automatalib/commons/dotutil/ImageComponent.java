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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


/**
 * Component that displays a {@link BufferedImage}.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 */
public class ImageComponent extends JComponent  {
	private static final long serialVersionUID = -1L;
	
	private BufferedImage img;
	
	private boolean scale = false;
	
	
	private final Action savePngAction = new AbstractAction("Save PNG") {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(DOTMisc.PNG_FILTER);
			int res = chooser.showSaveDialog(ImageComponent.this);
			if(res != JFileChooser.APPROVE_OPTION)
				return;
			File f = chooser.getSelectedFile();
			try {
				ImageIO.write(img, "png", f);
			}
			catch(IOException ex) {
				JOptionPane.showMessageDialog(ImageComponent.this, "Couldn't save image: " + ex.getMessage(), "Couldn't save image", JOptionPane.ERROR_MESSAGE);
			}
		}
	};
	
	
	/**
	 * Default constructor.
	 */
	public ImageComponent() {
		setPreferredSize(new Dimension(320, 240));
	}
	
	public void listActions(List<Action> actions) {
		actions.add(savePngAction);
	}
	
	/**
	 * Constructor. Initializes the component to display the given image.
	 * @param img the image to be displayed
	 */
	public ImageComponent(BufferedImage img) {
		this.img = img;
		Dimension dim = new Dimension(img.getWidth(), img.getHeight());
		setSize(dim);
		setPreferredSize(dim);
	}
	
	/**
	 * Sets the image to be displayed.
	 * @param img the image to be displayed
	 */
	public void setImage(BufferedImage img) {
		this.img = img;
		Dimension dim;
		if(img != null)
			dim = new Dimension(img.getWidth(), img.getHeight());
		else
			dim = new Dimension(320, 240);
		
		setSize(dim);
		setPreferredSize(dim);
		repaint();
	}
	
	/**
	 * Retrieves the image to be displayed
	 * @return the image to be displayed
	 */
	public BufferedImage getImage() {
		return img;
	}
	
	/**
	 * Retrieves an {@link Action} to save the image in a PNG file.
	 * @return the action
	 */
	public Action getSavePngAction() {
		return savePngAction;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		if(img != null) {
			if(!scale)
				g.drawImage(img, 0, 0, null);
			else
				g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
		}
	}
	
	
	public void setScale(boolean scale) {
		this.scale = scale;
		repaint();
	}
	
	public void toggleScale() {
		setScale(!scale);
	}
}
