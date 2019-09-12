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

public class PrintSpace
    implements PrintContentInterface {
    int space = 5;
    public PrintSpace() {
    }

    public PrintSpace(int space) {
        this.space = space;
    }

    public void draw(Graphics g, Point p, PageFormat pf, int startPos) {
        p.y += space;
    }
}
