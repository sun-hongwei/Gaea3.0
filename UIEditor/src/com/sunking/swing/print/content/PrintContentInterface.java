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

public interface PrintContentInterface {
    void draw(Graphics g, Point p, PageFormat pf, int startPos);
}
