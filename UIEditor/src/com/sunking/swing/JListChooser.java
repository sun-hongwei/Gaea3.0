package com.sunking.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

/**
 * <p>Title: OpenSwing</p>
 * <p>Description: JListChooser �б�ѡ���� </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
 *  && <a href="mailto:zt9788@126.com">zt9788</a>
 * @version 1.0
 */
public class JListChooser extends JDialog implements ActionListener,
    MouseListener{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JPanel pCenter = new JPanel();
    JPanel pSouth = new JPanel();
    JButton bttOk = new JButton();
    JButton bttCancel = new JButton();

    DefaultListModel<String> modelSource = new DefaultListModel<>();
    DefaultListModel<Object> modelTarget = new DefaultListModel<>();
    JList<String> listSource = new JList<>(modelSource);
    JList<Object> listTarget = new JList<>(modelTarget);
    JScrollPane spLeft = new JScrollPane(listSource);
    JScrollPane spRight = new JScrollPane(listTarget);

    JPanel pOptions = new JPanel();
    JButton bttRemoveAll = new JButton();
    JButton bttRemove = new JButton();
    JButton bttAddAll = new JButton();
    JButton bttAdd = new JButton();

    boolean isOkButtonClicked = false;

    FlowLayout2 flowLayout2 = new FlowLayout2();
    public static String[] showDialog(Component c, String title, boolean modal,
                                      String initResources[]){
        JListChooser dialog;
        Window owner = JFontDialog.getRootWindow(c);
        if(owner instanceof Dialog){
            dialog = new JListChooser((Dialog)owner, title, modal);
        } else if(owner instanceof Frame){
            dialog = new JListChooser((Frame)owner, title, modal);
        } else{
            dialog = new JListChooser((Frame)owner, title, modal);
            dialog.setTitle(title);
        }
        if(initResources != null){
            dialog.setResource(initResources);
        }
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setLocation((d.width - dialog.getSize().width) / 2,
                           (d.height - dialog.getSize().height) / 2);
        dialog.setVisible(true);
        return dialog.getResource();
    }

    public JListChooser(Frame frame, String title, boolean modal){
        super(frame, title, modal);
        try{
            jbInit();
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public JListChooser(Dialog frame, String title, boolean modal){
        super(frame, title, modal);
        try{
            jbInit();
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public JListChooser(){
        this((Frame)null, "", false);
    }

    private void jbInit() throws Exception{
        this.setSize(400, 320);
        bttOk.setText(OpenSwingUtil.getOpenResource("OK"));
        bttCancel.setText(OpenSwingUtil.getOpenResource("Cancel"));
        bttCancel.setMnemonic('C');
        bttRemoveAll.setText("<<");
        bttRemove.setPreferredSize(new Dimension(47, 25));
        bttRemove.setText("<");
        bttAddAll.setText(">>");
        bttAdd.setPreferredSize(new Dimension(47, 25));
        bttAdd.setText(">");
        pOptions.setSize(60, 1);
        pOptions.add(bttAdd);
        pOptions.add(bttAddAll);
        pOptions.add(bttRemove);
        pOptions.add(bttRemoveAll);
        pSouth.add(bttOk);
        pSouth.add(bttCancel);
        pCenter.setLayout(flowLayout2);
        pCenter.add(spLeft, FlowLayout2.LEFT);
        pCenter.add(pOptions, FlowLayout2.CENTER);
        pCenter.add(spRight, FlowLayout2.RIGHT);

        this.getContentPane().add(pCenter, BorderLayout.CENTER);
        this.getContentPane().add(pSouth, BorderLayout.SOUTH);

        bttAdd.addActionListener(this);
        bttAddAll.addActionListener(this);
        bttRemove.addActionListener(this);
        bttRemoveAll.addActionListener(this);
        bttOk.addActionListener(this);
        bttCancel.addActionListener(this);
        listSource.addMouseListener(this);
        listTarget.addMouseListener(this);

        bttAdd.setFocusable(false);
        bttAddAll.setFocusable(false);
        bttRemove.setFocusable(false);
        bttRemoveAll.setFocusable(false);
        bttRemove.setEnabled(false);
        bttRemoveAll.setEnabled(false);

        listSource.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listTarget.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.getRootPane().setDefaultButton(bttOk);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if(!isOkButtonClicked){
                    modelTarget.removeAllElements();
                }
            }
        });
    }

    public void mouseClicked(MouseEvent e){
    }

    public void mousePressed(MouseEvent e){
        if(e.getClickCount() != 2){
            return;
        }
        if(e.getSource() == this.listSource){
            bttAdd.doClick();
        } else if(e.getSource() == this.listTarget){
            bttRemove.doClick();
        }
    }

    public void mouseReleased(MouseEvent e){
    }

    public void mouseEntered(MouseEvent e){
    }

    public void mouseExited(MouseEvent e){
    }

    public void setResource(String str[]){
        this.modelSource.clear();
        for(int i = 0; i < str.length; i++){
            modelSource.addElement(str[i]);
        }
        listSource.setSelectedIndex(0);
    }

    public String[] getResource(){
        String s[] = new String[modelTarget.getSize()];
        for(int i = 0; i < s.length; i++){
            s[i] = modelTarget.getElementAt(i).toString();
        }
        return s;
    }

    public void actionPerformed(ActionEvent e){
        Object obj = e.getSource();
        if(obj == bttAdd){
            int index = listSource.getSelectedIndex();
            if(index == -1){
                return;
            }
            modelTarget.addElement(listSource.getSelectedValue());
            modelSource.remove(index);
            listSource.setSelectedIndex(Math.max(0,
                                                 Math.min(index,
                modelSource.size() - 1)));
            listTarget.setSelectedIndex(modelTarget.size() - 1);
        } else if(obj == bttAddAll){
            while(modelSource.size() != 0){
                modelTarget.addElement(modelSource.get(0));
                modelSource.remove(0);
            }
            listTarget.setSelectedIndex(modelTarget.size() - 1);
        } else if(obj == bttRemove){
            int index = listTarget.getSelectedIndex();
            if(index == -1){
                return;
            }
            modelSource.addElement((String) listTarget.getSelectedValue());
            modelTarget.remove(index);
            listTarget.setSelectedIndex(Math.max(0,
                                                 Math.min(index,
                modelTarget.size() - 1)));
            listSource.setSelectedIndex(modelSource.size() - 1);
        } else if(obj == bttRemoveAll){
            while(modelTarget.size() != 0){
                modelSource.addElement((String) modelTarget.get(0));
                modelTarget.remove(0);
            }
            listSource.setSelectedIndex(modelSource.size() - 1);
        } else if(obj == bttOk){
            isOkButtonClicked = true;
            this.setVisible(false);
            return;
        } else if(obj == bttCancel){
            isOkButtonClicked = false;
            this.setVisible(false);
            return;
        }
        bttAdd.setEnabled(modelSource.size() != 0);
        bttAddAll.setEnabled(modelSource.size() != 0);
        bttRemove.setEnabled(modelTarget.size() != 0);
        bttRemoveAll.setEnabled(modelTarget.size() != 0);
        listSource.ensureIndexIsVisible(listSource.getSelectedIndex());
        listTarget.ensureIndexIsVisible(listTarget.getSelectedIndex());
    }

    public static void main(String[] args){
        JFrame frame = OpenSwingUtil.createDemoFrame("JListChooser Demo");
        frame.setSize(600, 400);
        JTextArea txt = new JTextArea("Current Selection:\n");
        frame.getContentPane().add(new JScrollPane(txt), BorderLayout.CENTER);
        frame.setVisible(true);
        String str[] =
            JListChooser.showDialog(txt, "JListChooser Demo",
                                    true,
                                    GraphicsEnvironment.
                                    getLocalGraphicsEnvironment().
                                    getAvailableFontFamilyNames());
        String s = "";
        for(int i = 0; i < str.length; i++){
            s += "\t" + str[i] + "\n";
        }
        txt.setText(txt.getText() + s);
    }

}

class FlowLayout2 implements LayoutManager, java.io.Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String LEFT = "LEFT";
    public static final String CENTER = "CENTER";
    public static final String RIGHT = "RIGHT";

    private Component leftC, centerC, rightC;
    int hgap;
    int vgap;
    public int getHgap(){
        return hgap;
    }

    public void setVgap(int vgap){
        this.vgap = vgap;
    }

    public void setHgap(int hgap){
        this.hgap = hgap;
    }

    public int getVgap(){
        return vgap;
    }

    public FlowLayout2(){
        this(5, 5);
    }

    public FlowLayout2(int align, int hgap){
        this.hgap = hgap;
    }

    public void addLayoutComponent(String name, Component comp){
        if(name.equals(LEFT)){
            leftC = comp;
        } else if(name.equals(CENTER)){
            centerC = comp;
        } else if(name.equals(RIGHT)){
            rightC = comp;
        } else{
            throw new IllegalArgumentException(
                "cannot add to layout: unknown constraint: " + name);
        }
    }

    public void removeLayoutComponent(Component comp){}

    public Dimension preferredLayoutSize(Container target){
        synchronized(target.getTreeLock()){
            Dimension dim = new Dimension(0, 0);
            int nmembers = target.getComponentCount();
            for(int i = 0; i < nmembers; i++){
                Component m = target.getComponent(i);
                Dimension d = m.getPreferredSize();
                dim.height = Math.max(dim.height, d.height);
                dim.width += d.width;
            }
            Insets insets = target.getInsets();
            dim.width += insets.left + insets.right + hgap * 4;
            dim.height += insets.top + insets.bottom + vgap * 2;
            return dim;
        }
    }

    public Dimension minimumLayoutSize(Container target){
        return preferredLayoutSize(target);
    }

    public void layoutContainer(Container target){
        synchronized(target.getTreeLock()){
            Insets insets = target.getInsets();
            int centerWidth = centerC.getWidth();
            int x = insets.left + hgap;
            int y = insets.top + vgap;
            int h = target.getHeight() - insets.top - insets.bottom - vgap * 2;
            int w = target.getWidth() - insets.right - insets.left - hgap * 2;
            int wlr = (w - centerWidth - hgap * 2) / 2;
            leftC.setBounds(x, y, wlr, h);
            x += wlr + hgap;
            centerC.setBounds(x, y, centerWidth, h);
            x += centerWidth + hgap;
            rightC.setBounds(x, y, wlr, h);
        }
    }
}
