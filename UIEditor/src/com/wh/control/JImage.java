package com.wh.control;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class JImage extends JLabel implements IControl {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	DefaultControl defaultControl = new DefaultControl();

	@Override
	public Object getTag(String key) {
		return defaultControl.getTag(key);
	}

	@Override
	public void setTag(String key, Object value) {
		defaultControl.setTag(key, value);
	}

	public void setImage(BufferedImage image) {
		super.setIcon(new ImageIcon(image));
	}
}
