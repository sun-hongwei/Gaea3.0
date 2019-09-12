package com.sunking.swing.print.content;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
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

public class PrintImage
    implements PrintContentInterface {
    Image img;
    Component c;
    public PrintImage(Image img, Component consult) {
        this.img = img;
        c = consult;
    }

    public void draw(Graphics g, Point p, PageFormat pf, int startPos) {
//        FontMetrics fm = g.getFontMetrics();
//        int pw = (int) (pf.getImageableWidth() - startPos);
//        int ph = (int) pf.getImageableHeight();

        g.drawImage(img, p.x, p.y, c);
        p.x += img.getWidth(c);
    }
}
