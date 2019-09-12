package com.sunking.swing.print;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.sunking.swing.OpenSwingUtil;

/**
 * <p>Title: OpenSwing</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
 * @version 1.0
 */
public class PrintAction
    extends AbstractAction {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String PRINT_ACTION = "PrintAction";
    public static final String PAGESET_ACTION = "PageSetAction";

    private PrintOptionDialog dialog;
    private PageFormat pageFormat;

    private Window owner;
    private BookOfPrint bookOfPrint;
    public PrintAction(BookOfPrint bookOfPrint) {
        super(OpenSwingUtil.getOpenResource("Preview"),
              OpenSwingUtil.getOpenSwingImage(
            "preview.gif", new ImageIcon()));
        putValue(Action.SHORT_DESCRIPTION,
                 OpenSwingUtil.getOpenResource("Preview"));
        putValue(Action.ACCELERATOR_KEY,
                 KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
                                        KeyEvent.CTRL_DOWN_MASK));

        PrinterJob printJob = PrinterJob.getPrinterJob();
        pageFormat = printJob.defaultPage();

        super.putValue("ConfirmAction", new ConfirmAction());
        super.putValue(PRINT_ACTION, new PrintPageAction());
        super.putValue(PAGESET_ACTION, new PageSetAction());
        this.bookOfPrint = bookOfPrint;
    }

    public void actionPerformed(ActionEvent e) {
        if (dialog == null) {
            if (owner == null) owner = SwingUtilities.getWindowAncestor( (
                Component) e.getSource());
            dialog = new PrintOptionDialog(owner,
                                           OpenSwingUtil.getOpenResource("Preview"));
            dialog.setActions(
                (Action) getValue("ConfirmAction"),
                (Action) getValue(PRINT_ACTION),
                (Action) getValue(PAGESET_ACTION)
                );
            dialog.setSize(800, 600);
            dialog.setLocationRelativeTo(owner);
        }
        /**
         * ����Book
         */
        dialog.setPageFormat(pageFormat);

        if (owner instanceof Dialog) {
            dialog.setBook(bookOfPrint.makeBook(true,pageFormat));
            JDialog d = new JDialog( (Dialog) owner, dialog.getTitle(), true);
            d.getContentPane().add(dialog.getContentPane());
            d.setBounds(dialog.getBounds());
            d.setVisible(true);
        }
        else {
            dialog.setVisible(true);
            dialog.setExtendedState(JFrame.MAXIMIZED_BOTH);
            dialog.setBook(bookOfPrint.makeBook(true,pageFormat));
        }
    }

    public Action getAction(String actionName) {
        Object obj = getValue(actionName);
        if (obj != null && obj instanceof Action)return (Action) obj;
        return null;
    }

    class ConfirmAction
        extends AbstractAction {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ConfirmAction() {
            super.putValue(Action.NAME, OpenSwingUtil.getOpenResource("Close"));
            super.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
        }

        public void actionPerformed(ActionEvent e) {
            SwingUtilities.getWindowAncestor(
                (Component) e.getSource()).setVisible(false);
        }
    }

    class PrintPageAction
        extends AbstractAction {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public PrintPageAction() {
            super.putValue(Action.NAME, OpenSwingUtil.getOpenResource("Print"));
            super.putValue(Action.SMALL_ICON,
                           OpenSwingUtil.getOpenSwingImage("print.gif",
                new ImageIcon()));
            super.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
        }

        public void actionPerformed(ActionEvent e) {
            PrinterJob printJob = PrinterJob.getPrinterJob();
            printJob.setPageable(bookOfPrint.makeBook(false,pageFormat));
            if (printJob.printDialog()) {
                try {
                    printJob.print();
                }
                catch (Exception ee) {}
            }
        }
    }

    class PageSetAction
        extends AbstractAction {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public PageSetAction() {
            super.putValue(Action.NAME,
                           OpenSwingUtil.getOpenResource("PrintSetup"));
            super.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
        }

        public void actionPerformed(ActionEvent e) {
            PrinterJob printJob = PrinterJob.getPrinterJob();
            if (dialog != null) pageFormat = dialog.getPageFormat();
            pageFormat = printJob.pageDialog(pageFormat);
            if (dialog != null) {
                dialog.setPageFormat(pageFormat);
                dialog.setBook(bookOfPrint.makeBook(true,pageFormat));
            }
        }
    }
}
