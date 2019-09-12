package com.sunking.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import com.wh.control.EditorEnvironment;

/**
 * <p>Title: OpenSwing</p>
 * <p>Description: JDirChooser Ŀ¼ѡ����</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
 *  && <a href="mailto:zt9788@126.com">zt9788</a>
 * @version 1.0
 */
public class JDirChooser extends JDialog implements TreeSelectionListener,
    ActionListener, Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	boolean hasCancel = true;

    JPanel pCenter = new JPanel(new BorderLayout());
    JFileTree fileTree = new JFileTree();
    JScrollPane spTree = new JScrollPane(fileTree);
    JPanel pSouth = new JPanel(new BorderLayout());
    JPanel pButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
    JPanel pResult = new JPanel(new BorderLayout());
    JLabel lbFolder = new JLabel(OpenSwingUtil.getOpenResource("Folder"));
    JTextField txtResult = new JTextField();
    JButton bttCreateNew = new JButton(OpenSwingUtil.getOpenResource(
        "CreateNew"));
    JButton bttCancel = new JButton(OpenSwingUtil.getOpenResource("Cancel"));
    JButton bttOK = new JButton(OpenSwingUtil.getOpenResource("OK"));
    JLabel lbView = new JLabel();
    JPanel pAdjust = new JPanel(){
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void paintChildren(Graphics g){
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
            g.setColor(oldColor);
        }
    };

    MouseInputAdapter adjustWindowListener = new MouseInputAdapter(){
        Point oldP = null;
        public void mouseDragged(MouseEvent e){
            if(oldP != null){
                Point newP = e.getPoint();
                JDirChooser c = JDirChooser.this;
                c.setBounds(c.getX(), c.getY(), c.getWidth() + (newP.x - oldP.x),
                            c.getHeight() + (newP.y - oldP.y));
                c.validate();
                oldP = newP;
            }
        }

        public void mouseMoved(MouseEvent e){
            Component c = e.getComponent();
            Rectangle r = new Rectangle(c.getWidth() - 12, 0, 12, c.getHeight());
            if(r.contains(e.getPoint())){
                JDirChooser.this.setCursor(Cursor.getPredefinedCursor(Cursor.
                    SE_RESIZE_CURSOR));
            } else{
                JDirChooser.this.setCursor(Cursor.getDefaultCursor());
            }
        }

        public void mousePressed(MouseEvent e){
            Component c = e.getComponent();
            Rectangle r = new Rectangle(c.getWidth() - 12, 0, 12, c.getHeight());
            if(r.contains(e.getPoint())){
                oldP = e.getPoint();
            } else{
                oldP = null;
            }
        }

        public void mouseExited(MouseEvent e){
            JDirChooser.this.setCursor(Cursor.getDefaultCursor());
        }

        public void mouseReleased(MouseEvent e){
            oldP = null;
        }
    };

    public static File showDialog(Component c, String title, boolean modal,
                                  File initDir, String msg){
        JDirChooser dialog;
        Window owner = getRootWindow(c);
        if(owner instanceof Dialog){
            dialog = new JDirChooser((Dialog)owner, title, modal);
        } else if(owner instanceof Frame){
            dialog = new JDirChooser((Frame)owner, title, modal);
        } else{
            dialog = new JDirChooser();
            dialog.setTitle(title);
        }
        if(initDir != null){
            try{
                dialog.setSelectFile(initDir);
            } catch(Exception ex){
            }
        }
        if(msg != null){
            dialog.setMsg(msg);
        }
//        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
//        dialog.setLocation((d.width - dialog.getSize().width) / 2,
//                           (d.height - dialog.getSize().height) / 2);
        dialog.setVisible(true);
        return dialog.getSelectFile();
    }

    /**
     * ȡ�ø�����
     * @param c Component
     * @return Window
     */
    static Window getRootWindow(Component c){
        if(c == null)return null;
        Container parent = c.getParent();
        if(c instanceof Window)
            return(Window)c;
        while(!(parent instanceof Window))
            parent = parent.getParent();
        return(Window)parent;
    }

    public JDirChooser(Frame frame, String title, boolean modal){
        super(frame, title, modal);
        try{
            jbInit();
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public JDirChooser(Dialog frame, String title, boolean modal){
        super(frame, title, modal);
        try{
            jbInit();
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public JDirChooser(){
        this((Frame)null, "", false);
    }

    private void jbInit() throws Exception{
        bttCreateNew.setPreferredSize(new Dimension(88, 24));
        bttCreateNew.setMargin(new Insets(0, 0, 0, 0));
        bttCancel.setPreferredSize(new Dimension(88, 24));
        bttCancel.setMargin(new Insets(0, 0, 0, 0));
        bttOK.setPreferredSize(new Dimension(88, 24));
        pCenter.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));
        lbView.setPreferredSize(new Dimension(190, 50));
        pButtons.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, -7));
        pResult.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        pAdjust.setPreferredSize(new Dimension(10, 15));
        this.getContentPane().add(pCenter, BorderLayout.CENTER);
        pCenter.add(spTree, BorderLayout.CENTER);
        pCenter.add(pSouth, BorderLayout.SOUTH);
        pResult.add(txtResult, BorderLayout.CENTER);
        pResult.add(lbFolder, BorderLayout.WEST);
        pSouth.add(pButtons, BorderLayout.CENTER);
        pSouth.add(pResult, BorderLayout.NORTH);
        pButtons.add(bttOK, null);
        pButtons.add(bttCancel, null);
        pButtons.add(bttCreateNew, null);
        pCenter.add(lbView, BorderLayout.NORTH);
        this.getContentPane().add(pAdjust, BorderLayout.SOUTH);
        this.setSize(500, 500);
        this.setResizable(false);
        this.txtResult.setEditable(false);
        this.txtResult.setBackground(Color.white);

        fileTree.addTreeSelectionListener(this);
        getRootPane().registerKeyboardAction(this,
                                             KeyStroke.getKeyStroke(KeyEvent.
            VK_ESCAPE,
            0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        bttOK.setEnabled(false);
        bttCreateNew.setEnabled(false);
        bttOK.addActionListener(this);
        bttCancel.addActionListener(this);
        bttCreateNew.addActionListener(this);

        pAdjust.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        pAdjust.addMouseListener(adjustWindowListener);
        pAdjust.addMouseMotionListener(adjustWindowListener);
        setLocationRelativeTo(null);
    }

    public void setMsg(String msg){
        this.lbView.setText(msg);
    }

    public String getMsg(){
        return this.lbView.getText();
    }

    public void valueChanged(TreeSelectionEvent event){
        File f = fileTree.getSelectFile();
        boolean enabled = (f != null);
        bttOK.setEnabled(enabled);
        enabled = enabled && JFileTree.fileSystemView.isFileSystem(f);
        if(f != null && JFileTree.fileSystemView.isDrive(f)){
            enabled = enabled && f.canWrite();
        }
        bttCreateNew.setEnabled(enabled);
        if(f != null){
            txtResult.setText(JFileTree.fileSystemView.getSystemDisplayName(f));
            //fileTree.setEditable(f.renameTo(f));
        }
    }

    public JFileTree getFileTree(){
        return this.fileTree;
    }

    public void setFileTree(JFileTree tree){
        if(tree == null || tree == this.fileTree){
            return;
        }
        this.spTree.getViewport().setView(tree);
        this.spTree.doLayout();
    }

    public File getSelectFile(){
        if(hasCancel)
            return null;
        return fileTree.getSelectFile();
    }

    public void setSelectFile(File f) throws Exception{
        fileTree.setSelectFile(f);
    }

    public void actionPerformed(ActionEvent actionEvent){
        Object obj = actionEvent.getSource();
        if(obj == bttCreateNew){
            String dirName = EditorEnvironment.showInputDialog("", "new");
            if(dirName == null || dirName.trim().length() == 0){
                return;
            }
            File f = fileTree.getSelectFile();
            f = new File(f.getAbsolutePath() + File.separator + dirName);
            if(f.mkdir()){
                fileTree.getSelectFileNode().removeAllChildren();
                fileTree.getSelectFileNode().setExplored(false);
                fileTree.getSelectFileNode().explore();
            } else{
            	EditorEnvironment.showMessage(null, "错误", "错误",
                                              JOptionPane.ERROR_MESSAGE);
            }
        } else{
            if(obj == bttOK){
                hasCancel = false;
            }
            this.setVisible(false);
        }
    }

    public static void main(String[] args){
        JFrame frame = OpenSwingUtil.createDemoFrame("JDirChooser Demo");
        JTextArea txt = new JTextArea();
        frame.getContentPane().add(new JScrollPane(txt), BorderLayout.CENTER);
        frame.setVisible(true);
        File f = JDirChooser.showDialog(frame, "Please Select directory", true, null,
                                        "Please Select");
        if(f != null){
            txt.setText(f.getAbsolutePath());
        }
    }
}
