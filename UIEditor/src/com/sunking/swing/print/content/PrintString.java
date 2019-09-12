package com.sunking.swing.print.content;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.print.PageFormat;
import java.util.Vector;

/**
 * <p>Title: OpenSwing</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
 * @version 1.0
 */

public class PrintString
    implements PrintContentInterface {
    String str;
    boolean isDrawUnderLine = false;
    public PrintString(String s) {
        this.str = s;
    }

    public void setDrawUnderLine(boolean b) {
        this.isDrawUnderLine = b;
    }

    public boolean isDrawUnderLine() {
        return this.isDrawUnderLine;
    }

    public void draw(Graphics g, Point p, PageFormat pf, int startPos) {
        String s = "" + str;
        FontMetrics fm = g.getFontMetrics();
        int pw = (int) (pf.getImageableWidth() - startPos);
        int ph = (int) pf.getImageableHeight();

        String ss[] = fenjie(s, fm, pw);
        for (int i = 0; i < ss.length; i++) {
            drawString(g, ss[i], p);
            if (ss.length > 1) {
                p.y += fm.getHeight();
                p.x = startPos;
            }
            if (p.y % ph < fm.getHeight() * 1.25) p.y += fm.getHeight();
        }
    }

    void drawString(Graphics g, String s, Point p) {
        int oldX = p.x;
        g.drawString(s, p.x, p.y);
        p.x += g.getFontMetrics().stringWidth(s);
        if (isDrawUnderLine())
            g.drawLine(oldX, p.y + 2, p.x, p.y + 2);
    }

    String[] fenjie(String s, FontMetrics fm, int len) {
        if (s == null)return new String[] {
            ""};
        if (fm.stringWidth(s) <= len)return new String[] {
            s};
        Vector<String> v = new Vector<>();
        while (true) {
            if (s == null)break;
            if (fm.stringWidth(s) <= len) {
                v.add(s);
                break;
            }
            for (int i = 0; i < s.length(); i++) {
                String sub = s.substring(0, i + 1);
                if (sub.indexOf("\n") != -1) {
                    v.add(s.substring(0, i));
                    s = s.substring(i + 1);
                }
                else if (fm.stringWidth(sub) >= len) {
                    v.add(s.substring(0, i));
                    s = s.substring(i);
                    break;
                }
                else if (sub.length() == s.length()) {
                    v.add(s);
                    s = null;
                    break;
                }
            }
        }
        String ss[] = new String[v.size()];
        v.copyInto(ss);
        return ss;
    }

}
