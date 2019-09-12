package com.sunking.swing.print;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.print.Book;
import java.awt.print.PageFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

//import javax.swing.text.*;
import com.sunking.swing.print.content.PrintString;

public class PrintDemo
    extends JFrame
    implements BookOfPrint {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JMenuBar jMenuBar1 = new JMenuBar();
    JToolBar jToolBar1 = new JToolBar();
    JMenu jMenu1 = new JMenu("File");
    PrintAction pa = new PrintAction(this);
    JTextArea txt = new JTextArea("Print Demo");
    JLabel lbV = new JLabel();

    public Book makeBook(boolean isPreview, PageFormat pageFormat) {
        PrintContent content = new PrintContent();
        String lines[] = txt.getText().split("\n");
        for(int i = 0; i < lines.length; i ++){
                content.println(new PrintString(lines[i]));
        }
        DrawOut printable = new DrawOut(content, isPreview);

        Component c = lbV;
        Graphics2D g2 = (Graphics2D) (c.getGraphics());
        Book book = new Book();
        book.append(printable, pageFormat,
                    printable.getPageCount(g2, pageFormat));
        c.repaint();
        return book;
    }


    public PrintDemo() {
        try {
            jbInit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        this.setJMenuBar(jMenuBar1);
        lbV.setText("");
        this.getContentPane().add(jToolBar1, BorderLayout.NORTH);
        jMenuBar1.add(jMenu1);
        jMenu1.add(pa.getAction(PrintAction.PAGESET_ACTION));
        jMenu1.add(pa);
        jMenu1.add(pa.getAction(PrintAction.PRINT_ACTION));
        jMenu1.addSeparator();
        jMenu1.add(new ExitAction());
        jToolBar1.add(pa);
        jToolBar1.add(pa.getAction(PrintAction.PRINT_ACTION));
        jToolBar1.add(lbV, null);
        setJMenuBar(jMenuBar1);
        getContentPane().add(new JScrollPane(txt), BorderLayout.CENTER);
    }

    class ExitAction
        extends AbstractAction {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ExitAction() {
            super.putValue(Action.NAME, "Exit");
        }

        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        }
//        catch (Exception e) {
//        }
         JFrame frame = new PrintDemo();
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Print Demo");
        frame.setSize(400, 320);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation( (d.width - frame.getSize().width) / 2,
                          (d.height - frame.getSize().height) / 2);
        frame.setVisible(true);
    }
}
