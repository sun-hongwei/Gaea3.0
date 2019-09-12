package com.sunking.swing.print.content;

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

public class PrintLine
    implements PrintContentInterface {

    public PrintLine() {
    }

    public void draw(Graphics g, Point p, PageFormat pf, int startPos) {
        int pw = (int) (pf.getImageableWidth());

        g.drawLine(0, p.y + 2, pw, p.y + 2);
    }
}
