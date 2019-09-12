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

public class PrintGridString
    implements PrintContentInterface {
    String s[];
    public PrintGridString(String s[]) {
        this.s = s;
    }

    public void draw(Graphics g, Point p, PageFormat pf, int startPos) {
        FontMetrics fm = g.getFontMetrics();
        int pw = (int) (pf.getImageableWidth() - startPos);
        int ph = (int) pf.getImageableHeight();

        int maxTab = getMaxLen(fm);
        for (int j = 0; j < s.length; j++) {
            g.drawString(s[j], p.x, p.y);
            p.x += maxTab;
            if (p.x > pw - maxTab + startPos) {
                p.y += fm.getHeight();
                if (p.y % ph < fm.getHeight() * 1.25) p.y += fm.getHeight();
                p.x = startPos;
            }
        }
    }

    int getMaxLen(FontMetrics fm) {
        int max = 0;
        for (int i = 0; i < s.length; i++) {
            max = Math.max(max, fm.stringWidth(s[i]));
        }
        return max + 5;
    }
}
