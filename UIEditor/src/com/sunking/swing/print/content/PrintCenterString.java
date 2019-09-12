package com.sunking.swing.print.content;

import java.awt.FontMetrics;
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
public class PrintCenterString
    implements PrintContentInterface {
    String s;
    boolean isDrawUnderLine = false;
    public PrintCenterString(String s) {
        this.s = s;
    }

    public void setDrawUnderLine(boolean b) {
        this.isDrawUnderLine = b;
    }

    public boolean isDrawUnderLine() {
        return this.isDrawUnderLine;
    }

    public void draw(Graphics g, Point p, PageFormat pf, int startPos) {
        FontMetrics fm = g.getFontMetrics();
        int pw = (int) (pf.getImageableWidth() - startPos);
        p.x = (int) ( (pw - fm.stringWidth(s)) / 2);
        g.drawString(s, p.x, p.y);
        if (isDrawUnderLine) {
            for (int i = 3; i < 5; i++) {
                g.drawLine(p.x, p.y + i, p.x + fm.stringWidth(s), p.y + i);
            }
        }
        p.x += fm.stringWidth(s);
    }
}
