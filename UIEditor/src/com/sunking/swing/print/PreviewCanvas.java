package com.sunking.swing.print;

/**
 * <p>Title: OpenSwing</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
 * @version 1.0
 */
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import javax.swing.JPanel;

public class PreviewCanvas
    extends JPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Book book;
    int currentPage = 0;
    public PreviewCanvas(Book b, int pageNumber) {
        book = b;
        currentPage = pageNumber;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        PageFormat pageFormat = book.getPageFormat(currentPage);
        double px = pageFormat.getWidth(),
            py = pageFormat.getHeight(),
            sx = getWidth() - 1,
            sy = getHeight() - 1,
            scale = sy / py,
            xoff = 0.5 * (sx - scale * px),
            yoff = 0;
        g2.translate( (float) xoff, (float) yoff);
        g2.scale( (float) scale, (float) scale);
        Rectangle2D page = new Rectangle2D.Double(0, 0, px, py);
        g2.setPaint(Color.white);
        g2.fill(page);
        Printable printable = book.getPrintable(currentPage);
        try {
            printable.print(g2, pageFormat, currentPage);
        }
        catch (PrinterException eee) {
            g2.draw(new Line2D.Double(0, 0, px, py));
            g2.draw(new Line2D.Double(0, px, 0, py));
        }
    }

    /*public int filpPage(int by)
         {
        int newPage=currentPage+by;
        if (0<=newPage&&newPage<book.getNumberOfPages())
        {
            currentPage=newPage;
            repaint();
        }
        return currentPage;
         }*/
}
