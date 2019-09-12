package com.sunking.swing.print;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

/**
 * <p>Title: OpenSwing</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
 * @version 1.0
 */
public class DrawOut
    implements Printable {
    PrintContent content;
    boolean isPreview = true;
    public DrawOut(boolean isPreview) {
        this.isPreview = isPreview;
    }

    public DrawOut(PrintContent content, boolean isPreview) {
        this.content = content;
        this.isPreview = isPreview;
    }

    public void setPreview(boolean isPreview) {
        this.isPreview = isPreview;
    }

    public boolean isPreview() {
        return isPreview;
    }

    public void setPrintContent(PrintContent content) {
        this.content = content;
    }

    public PrintContent getPrintContent() {
        return this.content;
    }

    public int print(Graphics g, PageFormat pf, int page) throws
        PrinterException {
        double pW = pf.getImageableWidth();
        double pH = pf.getImageableHeight();

        Graphics2D g2 = (Graphics2D) g;
        g2.setPaint(Color.black);
        g2.translate(pf.getImageableX(), pf.getImageableY());
        if (isPreview) drawCropMarks(g2, pf);
        /**
         * ��ҳ��
         */
        g2.drawString("" + (page + 1),
                      (int) (pW - g.getFontMetrics().stringWidth("" + (page + 1)) -
                             5),
                      (int) pH
                      );

        g2.clip(new Rectangle2D.Double(0, 0, pW, pH));
        g2.translate(0, -page * pH);

        return (page >= getPageCount(g2, pf)) ? Printable.NO_SUCH_PAGE :
            Printable.PAGE_EXISTS;
    }

    public int getPageCount(Graphics2D g2, PageFormat pf) {
        Point p = new Point(2, 2);
        if (content != null) content.draw(g2, p, pf);
        int height = p.y - g2.getFontMetrics().getHeight();
        return (int) Math.ceil(height / pf.getImageableHeight());
    }

    public void drawCropMarks(Graphics2D g2, PageFormat pf) {
        final double c = 36;
        double w = pf.getImageableWidth(),
            h = pf.getImageableHeight();
        g2.draw(new Line2D.Double(0, 0, 0, c));
        g2.draw(new Line2D.Double(0, 0, c, 0));
        g2.draw(new Line2D.Double(w, 0, w, c));
        g2.draw(new Line2D.Double(w, 0, w - c, 0));
        g2.draw(new Line2D.Double(0, h, 0, h - c));
        g2.draw(new Line2D.Double(0, h, c, h));
        g2.draw(new Line2D.Double(w, h, w, h - c));
        g2.draw(new Line2D.Double(w, h, w - c, h));
    }
}
