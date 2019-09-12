package com.sunking.swing.print.content;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.print.PageFormat;

/**
 * <p>Title: OpenSwing</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
 * @version 1.0
 */

public class PrintComponent
    implements PrintContentInterface {
    Component com;
    public PrintComponent(Component com) {
        this.com = com;
    }

    public void draw(Graphics g, Point p, PageFormat pf, int startPos) {
//        FontMetrics fm = g.getFontMetrics();
//        int pw = (int) (pf.getImageableWidth() - startPos);
//        int ph = (int) pf.getImageableHeight();
        com.paint(g.create(p.x, p.y, com.getWidth(), com.getHeight()));
        p.x += com.getWidth();
    }
}
