package com.sunking.swing.print;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.sunking.swing.OpenSwingUtil;


/**
 * <p>Title: OpenSwing</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
 * @version 1.0
 */
public class MultiPagePopup
    extends JPopupMenu {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int hPage = 0;
    int vPage = 0;
    JTable tbl = new JTable(5, 11);
    JPanel pStatus = new JPanel();
    JLabel lbStatus = new JLabel(OpenSwingUtil.getOpenResource("Cancel"));
    TitledBorder titledBorder1;
    Action action;
    public MultiPagePopup() {
        tbl.setRowHeight(24);
        TableColumnModel colModel = tbl.getColumnModel();
        for (int i = 0; i < tbl.getColumnCount(); i++) {
            TableColumn col = colModel.getColumn(i);
            col.setPreferredWidth(24);
            col.setResizable(false);
            col.setCellRenderer(new MultiPageRenderer());
        }
        tbl.setEnabled(false);
        tbl.setRequestFocusEnabled(false);
        tbl.setRowSelectionAllowed(true);
        tbl.setColumnSelectionAllowed(true);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        tbl.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int rows[] = tbl.getSelectedRows();
                int cols[] = tbl.getSelectedColumns();
                setPages(rows.length, cols.length);
                MultiPagePopup.this.setVisible(false);
                if (action != null) {
                    action.actionPerformed(new ActionEvent(tbl,
                        ActionEvent.ACTION_PERFORMED, ""));
                }
            }

            public void mouseExited(MouseEvent e) {
                tbl.clearSelection();
                setPages(0, 0);
            }
        });
        tbl.addMouseMotionListener(new MouseMotionListener() {
            public void mouseMoved(MouseEvent e) {
                for (int row = 0; row < tbl.getRowCount(); row++) {
                    for (int col = 0; col < tbl.getColumnCount(); col++) {
                        if (tbl.getCellRect(row, col, true).contains(e.getPoint())) {
                            tbl.setRowSelectionInterval(0, row);
                            tbl.setColumnSelectionInterval(0, col);
                            setPages(row + 1, col + 1);
                            break;
                        }
                    }
                }
            }

            public void mouseDragged(MouseEvent e) {
                this.mouseMoved(e);
            }
        });

        this.setLayout(new BorderLayout());
        this.setBorder(new TitledBorder(""));
        this.add(tbl, BorderLayout.CENTER);
        this.add(pStatus, BorderLayout.SOUTH);
        pStatus.add(lbStatus);
        pStatus.setBorder(BorderFactory.createLoweredBevelBorder());
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public void setPages(int hPage, int vPage) {
        this.hPage = hPage;
        this.vPage = vPage;
        if (hPage == 0 && vPage == 0)this.setStatus(OpenSwingUtil.
            getOpenResource("Cancel"));
        else this.setStatus(hPage + " * " + vPage +
                            OpenSwingUtil.getOpenResource("Page"));
    }

    public int getHorizontalPages() {
        return hPage;
    }

    public int getVerticalPages() {
        return vPage;
    }

    void setStatus(String status) {
        lbStatus.setText(status);
    }
}

class MultiPageRenderer
    extends DefaultTableCellRenderer {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ImageIcon icon = OpenSwingUtil.getOpenSwingImage(
        "page.gif", new ImageIcon());
    public MultiPageRenderer() {}

    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
        hasFocus = false;
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                                            row, column);
        this.setIcon(icon);
        this.setText("");
        return this;
    }
}
