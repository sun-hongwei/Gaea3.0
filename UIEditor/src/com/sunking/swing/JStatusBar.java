package com.sunking.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.io.Serializable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

/**
 * <p>Title: OpenSwing</p>
 * <p>Description: JStatusBar ģ��Windows��״̬��</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
 * @version 1.0
 */
public class JStatusBar
    extends JComponent
    implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Vector<String> vecCellWidth = new Vector<>();
    public JStatusBar() {
        this.setPreferredSize(new Dimension(10, 20));
        this.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setBorder(BorderFactory.createLoweredBevelBorder());
    }

    /**
     * ���һ��״̬��
     * @param width int
     */
    public void addStatusCell(int width) {
        JLabel lb = new JLabel() {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void paint(Graphics g) {
                super.paint(g);
                int w = getWidth();
                int h = getHeight();
                g.setColor(Color.white);
                g.drawLine(w - 4, 0, w - 4, h - 2);
                g.setColor(new Color(128, 128, 128));
                g.drawLine(w - 1, 0, w - 1, h - 5);
            }

            public Insets getInsets() {
                return new Insets(0, 0, 0, 5);
            }
        };

        lb.setPreferredSize(new Dimension(width, getPreferredSize().height));
        add(lb);
        vecCellWidth.add("" + width);
    }

    /**
     * ȡ��״̬��������
     * @return int
     */
    public int getCellCount() {
        return getComponentCount();
    }

    /**
     * ����״̬��Ϣ
     * @param cellIndex int ����
     * @param status String ״̬��Ϣ
     */
    public void setStatus(int cellIndex, String status) {
        if (getLabel(cellIndex) != null)
            getLabel(cellIndex).setText(status);
    }

    /**
     * ȡ��״̬��
     * @param cellIndex int ����
     * @return JLabel
     */
    private JLabel getLabel(int cellIndex) {
        if (cellIndex >= getCellCount())
            return null;
        JLabel lb = (JLabel) getComponent(cellIndex);
        return lb;
    }

    /**
     * ȡ��״̬��Ϣ
     * @param cellIndex int ����
     * @return String
     */
    public String getStatusText(int cellIndex) {
        if (getLabel(cellIndex) != null)
            return getLabel(cellIndex).getText();
        return null;
    }

    public void paintChildren(Graphics g) {
        super.paintChildren(g);
        int w = getWidth();
        int h = getHeight();
        Color oldColor = g.getColor();
        // draw ///
        g.setColor(Color.white);
        g.drawLine(w, h - 12, w - 12, h);
        g.drawLine(w, h - 8, w - 8, h);
        g.drawLine(w, h - 4, w - 4, h);
        g.setColor(new Color(128, 128, 128));
        g.drawLine(w, h - 11, w - 11, h);
        g.drawLine(w, h - 10, w - 10, h);
        g.drawLine(w, h - 7, w - 7, h);
        g.drawLine(w, h - 6, w - 6, h);
        g.drawLine(w, h - 3, w - 3, h);
        g.drawLine(w, h - 2, w - 2, h);
        g.setColor(UIManager.getColor("Panel.background"));
        int cellW = 0;
        for (int i = 0; i < vecCellWidth.size(); i++) {
            cellW += Integer.parseInt(vecCellWidth.get(i).toString());
            g.drawLine(cellW - 2, 0, cellW, 0);
            g.drawLine(cellW - 2, 1, cellW, 1);
        }
        g.setColor(oldColor);
    }

    public static void main(String[] args) {
        JStatusBar bar = new JStatusBar();
        bar.addStatusCell(200);
        bar.addStatusCell(100);
        bar.addStatusCell(100);
        bar.setStatus(0, "Status 0 ");
        bar.setStatus(1, "Status 1 ");
        bar.setStatus(2, "Status 2 ");

        JFrame frame = OpenSwingUtil.createDemoFrame("JStatusBar Demo");
        frame.getContentPane().add(bar, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(new JTextArea()),
                                   BorderLayout.CENTER);
        frame.setVisible(true);
    }
}
