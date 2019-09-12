package com.sunking.swing.refer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.border.AbstractBorder;

/**
 * <p>Title: OpenSwing</p>
 * <p>Description: TopBottomLineBorderֻ�����������ߵı߽�Border</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
 * @version 1.0
 */
public class TopBottomLineBorder extends AbstractBorder{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Color lineColor;
    public TopBottomLineBorder(Color color){
        lineColor = color;
    }

    public void paintBorder(Component c, Graphics g, int x, int y,
                            int width, int height){
        g.setColor(lineColor);
        g.drawLine(0, 0, c.getWidth(), 0);
        g.drawLine(0, c.getHeight() - 1, c.getWidth(),
                   c.getHeight() - 1);
    }
}
