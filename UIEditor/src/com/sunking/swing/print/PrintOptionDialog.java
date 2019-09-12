package com.sunking.swing.print;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.print.Book;
import java.awt.print.PageFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import com.sunking.swing.OpenSwingUtil;

/**
 * <p>Title: OpenSwing</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
 * @version 1.0
 */
public class PrintOptionDialog
    extends JFrame
    implements ActionListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private PageFormat pageFormat;
    JToolBar toolbar = new JToolBar();
    JButton bttMulti = new JButton(OpenSwingUtil.getOpenSwingImage(
        "multipage.gif", new ImageIcon()));
    JButton bttSingle = new JButton(OpenSwingUtil.getOpenSwingImage(
        "singlepage.gif", new ImageIcon()));
    JButton bttPrint = new JButton(OpenSwingUtil.getOpenSwingImage(
        "print.gif", new ImageIcon()));
    JPanel canvas = new JPanel(new FlowLayout());
    JScrollPane sp = new JScrollPane(canvas, 22, 32) {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void doLayout() {
            super.doLayout();
            JScrollBar bar = this.getHorizontalScrollBar();
            bar.setValue( (bar.getMaximum() - bar.getVisibleAmount()) / 2);
        }
    };
    JButton bttPageSetup = new JButton(OpenSwingUtil.getOpenSwingImage(
        "page.gif", new ImageIcon()));
    JButton bttConfirm = new JButton();
    JComboBox<?> boxResize = new JComboBox<>(
        new String[] {
        "500%", "250%", "150%", "100%", "75%",
        "50%", "25%", "10%",
        OpenSwingUtil.getOpenResource("PageWidth"),
        OpenSwingUtil.getOpenResource("FontWidth"),
        OpenSwingUtil.getOpenResource("OnePage"),
        OpenSwingUtil.getOpenResource("DoublePage")
    });
    MultiPagePopup popupMulti = new MultiPagePopup();

    boolean popupIsLastClick = false;

    Window owner;
    public PrintOptionDialog(Window owner, String title) {
        super(title);
        this.owner = owner;
        boxResize.setMaximumSize(new Dimension(200, 33));
        boxResize.setMinimumSize(new Dimension(200, 33));
        boxResize.setPreferredSize(new Dimension(200, 33));
        boxResize.addActionListener(this);
        toolbar.add(bttPageSetup, null);
        toolbar.add(bttPrint, null);
        toolbar.add(bttSingle, null);
        toolbar.add(bttMulti, null);
        toolbar.add(boxResize, null);
        toolbar.add(bttConfirm, null);
        bttConfirm.setMaximumSize(new Dimension(60, 33));
        bttConfirm.setMinimumSize(new Dimension(60, 33));
        bttConfirm.setPreferredSize(new Dimension(60, 33));

        bttSingle.addActionListener(this);
        bttMulti.addActionListener(this);

        this.getContentPane().add(toolbar, BorderLayout.NORTH);
        this.getContentPane().add(sp, BorderLayout.CENTER);
        canvas.setLayout(new BoxLayout(canvas, BoxLayout.PAGE_AXIS));
        popupMulti.setAction(new PopupAction());
        boxResize.setSelectedIndex(10);
        sp.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                layoutPreview();
            }
        });
    }

    /*int ownerExtendedState = 0;
    public void show() {
        super.show();
        if (owner == null)return;
        if (owner instanceof Frame) {
            ownerExtendedState = ( (Frame) owner).getExtendedState();
            ( (Frame) owner).setExtendedState(JFrame.ICONIFIED);
            toFront();
            requestFocus();
        }
    }

    public void hide() {
        super.hide();
        if (owner == null)return;
        if (owner instanceof Frame) {
            ( (Frame) owner).setExtendedState(ownerExtendedState);
            ( (Frame) owner).toFront();
            ( (Frame) owner).requestFocus();
        }
    }*/

    /**
     * ����ȷ��\ȡ��\ҳ������\��ӡ\��ӡѡ��ͼ���Action
     * @param confirmAction
     * @param cancelAction
     * @param printAction
     * @param pageSetAction
     * @param printSelectionAction
     */
    public void setActions(Action confirmAction, Action printAction,
                           Action pageSetAction) {
        bttConfirm.setAction(confirmAction);
        bttPrint.addActionListener(printAction);
        bttPageSetup.addActionListener(pageSetAction);
    }

    public void setPageFormat(PageFormat pageFormat) {
        this.pageFormat = pageFormat;
    }

    public PageFormat getPageFormat() {
        return this.pageFormat;
    }

    JPanel pPreviews[];
    public void setBook(Book book) {
        pPreviews = getPreviewPanel(book);
        layoutPreview();
    }

    private JPanel[] getPreviewPanel(Book book) {
        JPanel result[] = new JPanel[book.getNumberOfPages()];
        for (int i = 0; i < book.getNumberOfPages(); i++) {
            PreviewCanvas previewCanvas = new PreviewCanvas(book, i);
            previewCanvas.setBackground(Color.white);
            previewCanvas.setPreferredSize(new Dimension(
                (int) pageFormat.getWidth(), (int) pageFormat.getHeight()));
            JPanel p = new JPanel(new BorderLayout());
            p.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 3));
            p.setBackground(Color.gray);
            p.add(previewCanvas);
            result[i] = p;
        }
        return result;
    }

    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj == bttMulti) {
            popupMulti.show(bttMulti, 0, bttMulti.getHeight());
        }
        else if (obj == bttSingle) {
            boxResize.setSelectedIndex(10);
        }
        else if (obj == boxResize) {
            popupIsLastClick = false;
            layoutPreview();
        }
    }

    class PopupAction
        extends AbstractAction {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
            popupIsLastClick = true;
            layoutPreview();
        }
    }

    /**
     * ���ݵ�ǰ�����õ�ѡ������Ԥ�����
     */
    public void layoutPreview() {
        if (pageFormat == null || pPreviews == null || pPreviews.length == 0)return;
        int spW = sp.getWidth() - sp.getVerticalScrollBar().getWidth() - 20;
        int spH = sp.getHeight() - sp.getHorizontalScrollBar().getHeight() - 20;
        double pw = pageFormat.getWidth();
        double ph = pageFormat.getHeight();

        int col = 0, row = 1;

        int w = getPreviewWidth();
        int h = (int) (ph * w / pw);

        canvas.removeAll();

        //��ҳ����ʽ�˵�\��ҳ\��ҳ\�Ϳ��С���ܿ�ȵ�һ���
        if (popupIsLastClick || boxResize.getSelectedIndex() > 9 ||
            w < (spW / 2)) {
            if (popupIsLastClick) {
                col = popupMulti.getVerticalPages();
                row = popupMulti.getHorizontalPages();
            }
            else if (boxResize.getSelectedIndex() == 10) col = 1;
            else if (boxResize.getSelectedIndex() == 11) col = 2;
            else col = spW / w;

            boolean isSingleLine = false;
            if (w > h) {
                w = spW / col;
                h = (int) (ph * w / pw);
            }
            else {
                h = spH / row;
                w = (int) (pw * h / ph);
                if (w * col >= spW) {
                    isSingleLine = true;
                    w = spW / col;
                    h = (int) (ph * w / pw);
                }
            }
            //��������Ԥ�����ĸ������Ӧ���ж�����
            if (col * row < pPreviews.length) {
                row = pPreviews.length / col +
                    ( (pPreviews.length % col == 0) ? 0 : 1);
            }
            int index = 0;
            for (int i = 0; i < row; i++) {
                JPanel p = new JPanel();
                for (int j = 0; j < col; j++) {
                    if (index == pPreviews.length)break; //����Ѿ�������,������
                    p.add(pPreviews[index]);
                    pPreviews[index].setPreferredSize(new Dimension(w, h));
                    index++;
                }
                //����ǵ��е��е���Ϊ�������ϱ߽�,�Դﵽ��ֱ���е�Ч��
                if (isSingleLine || row == 1) {
                    p.setPreferredSize(new Dimension(spW + 20, spH + 20));
                    p.setBorder(BorderFactory.createEmptyBorder(
                        (spH + 20 - h) / 2, -50, 0, -50
                        ));
                }
                if (p.getComponentCount() != 0) canvas.add(p);
            }
        }
        /**
         *һ�������
         */
        else {
            for (int i = 0; i < pPreviews.length; i++) {
                JPanel p = new JPanel();
                p.add(pPreviews[i]);
                pPreviews[i].setPreferredSize(new Dimension(w, h));
                canvas.add(p);
            }
        }
        canvas.setSize(canvas.getPreferredSize());
        canvas.validate();
        System.gc();
    }

    /**
     * ���ݵ�ǰ�����õ�ѡ��ȡ��Ԥ�����Ŀ��
     * @return
     */
    int getPreviewWidth() {
        int spW = sp.getWidth() - sp.getVerticalScrollBar().getWidth() - 20;
        int spH = sp.getHeight() - sp.getHorizontalScrollBar().getHeight() - 20;
        double w = pageFormat.getWidth();
        double h = pageFormat.getHeight();

        double fScale[] = {
            5, 2.5, 1.5, 1, 0.75, 0.5, 0.25, 0.1};
        int index = boxResize.getSelectedIndex();
        if (index < fScale.length) //"500%","250%","150%","100%","75%","50%","25%","10%"
            return (int) (w * fScale[index]);
        else if (index == 8) //"ҳ����"
            return (int) spW;
        else if (index == 9) { //"���ֿ��"
            double d = pageFormat.getImageableWidth();
            return (int) (w * spW / d) - 20;
        }
        else if (index == 10) { //"��ҳ"
            if (w > h) {
                return spW;
            }
            else {
                return (int) (w * spH / h);
            }
        }
        else { //"˫ҳ"
            if (w > h) {
                return spW / 2;
            }
            else {
                return (int) (w * spH / h / 2);
            }
        }
    }

}
