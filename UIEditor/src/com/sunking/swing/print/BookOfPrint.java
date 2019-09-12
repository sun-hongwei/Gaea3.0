package com.sunking.swing.print;

import java.awt.print.Book;
import java.awt.print.PageFormat;

/**
 * <p>Title: OpenSwing</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
 * @version 1.0
 */

public interface BookOfPrint {
    Book makeBook(boolean isPreview,PageFormat pageFormat);
}
