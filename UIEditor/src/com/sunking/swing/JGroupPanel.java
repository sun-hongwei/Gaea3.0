package com.sunking.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

/**
 * <p>Title:OpenSwing </p>
 * <p>Description: JGroupPanel ��Ⱥ���<BR>
 *  ����QQ�������Ⱥ�������
 * </p>
 * ����:<BR>
 *     2004/07/24   ��SunKing����<BR>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author <a href="mailto:sunkingxie@hotmail.com"'>Sunking</a>
 * @version 1.0
 */
public class JGroupPanel
    extends JPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/*�������������������*/
    private JPanel pNorth = new JPanel() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
    };
    private JPanel pCenter = new JPanel();
    private JPanel pSouth = new JPanel();

    /*��ǰȫ����ļ���*/
    private ArrayList<JGroupContainer> groupList = new ArrayList<>();

    /*�Ƿ��ѽ�ֹ������*/
    private boolean forbidFlag = false;

    /*��ǰ�������*/
    private JGroupContainer activeGroup = null;
    transient ActionListener al = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            JButton bttTitle = (JButton) e.getSource();
            expandGroup( (JGroupContainer) bttTitle.getParent());
        }
    };

    private boolean hasCreateDefaultGroup = false;
    public JGroupPanel() {
        initComponents();
        createDefaultGroup();
    }
    private void initComponents(){
        this.setLayout(new BorderLayout());
        this.add(pNorth, BorderLayout.NORTH);
        this.add(pCenter, BorderLayout.CENTER);
        this.add(pSouth, BorderLayout.SOUTH);
        pNorth.setLayout(new GroupLayout());
        pCenter.setLayout(new BorderLayout());
        pSouth.setLayout(new GroupLayout());
        forbidFlag = true;
    }
    private void createDefaultGroup(){
        //Default Group
        Color bg[] = {
            Color.black, Color.red, Color.orange, Color.yellow, Color.green,
            Color.cyan, Color.blue, Color.white};
        for (int i = 1; i <= bg.length; i++) {
            insertGroup(i - 1, "Group " + i, bg[i - 1]);
            Color mc = new Color(255 - bg[i - 1].getRed(),
                                 255 - bg[i - 1].getGreen(),
                                 255 - bg[i - 1].getBlue());
            for (int j = 1; j <= 5; j++) {
                JButton bttMember = new JButton("Member " + j + " of " + i);
                addMember(i - 1, bttMember);
                bttMember.setPreferredSize(new Dimension(1, 40));
                bttMember.setOpaque(false);
                bttMember.setForeground(mc);
            }
            getGroup(i - 1).setMemberGap(20, 5);
            getGroup(i - 1).getTitleButton().setForeground(bg[i - 1]);
        }
        expandGroup(0);
        hasCreateDefaultGroup = true;
    }

    /**
     * @param groupNames String[] Ԥ������
     */
    public JGroupPanel(String groupNames[]) {
        initComponents();
        addGroup(groupNames);
    }

    /**
     * չ����
     * @param name String ����
     */
    public void expandGroup(String name) {
        for (int i = getGroupCount() - 1; i >= 0; i--) {
            if (getGroupName(i).equals(name)) {
                expandGroup(i);
            }
        }
    }

    /**
     * չ����
     * @param index int ���˳���
     */
    public void expandGroup(int index) {
        expandGroup(getGroup(index));
    }

    /**
     * չ����
     * @param group JGroupContainer ��
     */
    protected void expandGroup(JGroupContainer group) {
        pNorth.removeAll();
        pCenter.removeAll();
        pSouth.removeAll();
        boolean hasAddCenter = false;
        for (int i = 0; i < groupList.size(); i++) {
            Component c = (Component) groupList.get(i);
            if (hasAddCenter) {
                pSouth.add(c);
            }
            else if (c == group) {
                pCenter.add(c, BorderLayout.CENTER);
                hasAddCenter = true;
            }
            else {
                pNorth.add(c);
            }
        }
        if (activeGroup != null) {
            activeGroup.collapse();
        }
        activeGroup = group;
        activeGroup.expand();
        pNorth.doLayout();
        pCenter.doLayout();
        pSouth.doLayout();
        doLayout();
    }

    /**
     * ������
     * @param name String ����
     */
    public void collapseGroup(String name) {
        for (int i = getGroupCount() - 1; i >= 0; i--) {
            if (getGroupName(i).equals(name)) {
                collapseGroup(i);
            }
        }
    }

    /**
     * ������
     * @param index int ���˳���
     */
    public void collapseGroup(int index) {
        collapseGroup(getGroup(index));
    }

    /**
     * ������
     * @param group JGroupContainer ��
     */
    protected void collapseGroup(JGroupContainer group) {
        if (group == activeGroup) {
            activeGroup.collapse();
            activeGroup = null;
        }
    }

    /**
     * �����
     * @param name String ����
     */
    public void addGroup(String name) {
        this.insertGroup(getGroupCount(), name);
    }

    /**
     * ��Ӷ����
     * @param names String[] ����
     */
    public void addGroup(String names[]) {
        for (int i = 0; i < names.length; i++) {
            addGroup(names[i]);
        }
    }

    /**
     * ����һ����
     * @param index int ˳���
     * @param name String ����
     * @param bg Color ����ɫ
     */
    public void insertGroup(int index, String name, Color bg) {
        if (index < 0 || index > groupList.size()) {
            throw new ArrayIndexOutOfBoundsException("index:" + index +
                " >count:" + groupList.size());
        }
        if(hasCreateDefaultGroup){
            while(getGroupCount()>0){
                removeGroup(0);
            }
            hasCreateDefaultGroup = false;
        }
        int countNorth = pNorth.getComponentCount();
        int countCenter = pCenter.getComponentCount();
        int countSouth = pSouth.getComponentCount();
        JGroupContainer group;
        if (index <= countNorth) {
            group = insertGroup(pNorth, index, name, bg);
        }
        else if (index <= countNorth + countCenter) {
            group = insertGroup(pCenter, index - countNorth, name, bg);
        }
        else if (index <= countNorth + countCenter + countSouth) {
            group = insertGroup(pSouth, index - countNorth - countCenter, name,
                                bg);
        }
        else {
            group = insertGroup(pSouth, countSouth, name, bg);
        }
        group.getTitleButton().addActionListener(al);
        groupList.add(index, group);

    }

    /**
     * ����һ����
     * @param index int ˳���
     * @param name String ����
     */
    public void insertGroup(int index, String name) {
        insertGroup(index, name, UIManager.getColor("Desktop.background"));
    }

    /**
     * ����һ����
     * @param p JPanel Ŀ�����
     * @param index int ˳���
     * @param name String ����


         /**
      * ����һ����
      * @param p JPanel Ŀ�����
      * @param index int ˳���
      * @param name String ����
      * @return JGroupContainer
      */
     private JGroupContainer insertGroup(JPanel p, int index, String name,
                                         Color bg) {
         JGroupContainer group = new JGroupContainer(name, bg);
         p.add(group);
         return group;
     }

    /**
     * ɾ��һ����
     * @param index int ˳���
     */
    public void removeGroup(int index) {
        JGroupContainer c = (JGroupContainer) groupList.get(index);
        c.getParent().remove(c);
        c.getTitleButton().removeActionListener(al);
    }

    /**
     * ɾ��һ����
     * @param name String ����
     */
    public void removeGroup(String name) {
        for (int i = getGroupCount() - 1; i >= 0; i--) {
            if (getGroupName(i).equals(name)) {
                this.removeGroup(i);
            }
        }
    }

    /**
     * ��������
     * @param index int ˳���
     * @param name String ����
     */
    public void setGroupName(int index, String name) {
        this.getGroup(index).setName(name);
    }

    /**
     * ȡ������
     * @param groupIndex int ˳���
     * @return String ����
     */
    public String getGroupName(int groupIndex) {
        return getGroup(groupIndex).getName();
    }

    /**
     * ȡ��ȫ������
     * @return String[]
     */
    public String[] getGroupNames() {
        String sResult[] = new String[getGroupCount()];
        for (int i = 0; i < getGroupCount(); i++) {
            sResult[i] = getGroupName(i);
        }
        return sResult;
    }

    /**
     * ȡ�õ�ǰ�������
     * @return int
     */
    public int getGroupCount() {
        return groupList.size();
    }

    /**
     * ��������ӳ�Ա���
     * @param groupIndex int ���˳���
     * @param member Component ��Ա���
     */
    public void addMember(int groupIndex, Component member) {
        getGroup(groupIndex).addMember(getGroup(groupIndex).getMemberCount(),
                                       member);
    }

    /**
     * �����в����Ա���
     * @param groupIndex int ���˳���
     * @param memberIndex int �����˳���
     * @param member Component ��Ա���
     */
    public void insertMember(int groupIndex, int memberIndex, Component member) {
        getGroup(groupIndex).addMember(memberIndex, member);
    }

    /**
     * �������Ƴ���Ա���
     * @param groupIndex int
     * @param memberIndex int
     */
    public void removeMember(int groupIndex, int memberIndex) {
        getGroup(groupIndex).removeMember(memberIndex);
    }

    /**
     * ȡ�ó�Ա���
     * @param groupIndex int ���˳���
     * @param memberIndex int ��Ա�����˳���
     * @return Component ��Ա���
     */
    public Component getMember(int groupIndex, int memberIndex) {
        return getGroup(groupIndex).getMember(memberIndex);
    }

    /**
     * ȡ��ȫ����Ա���
     * @param groupIndex int ���˳���
     * @return Component[] ȫ����Ա���
     */
    public Component[] getMembers(int groupIndex) {
        return getGroup(groupIndex).getMembers();
    }

    /**
     * ȡ�ó�Ա���������
     * @param groupIndex int ���˳���
     * @return int ����
     */
    public int getMemberCount(int groupIndex) {
        return getGroup(groupIndex).getMemberCount();
    }

    /**
     * ȡ����
     * @param index int ���˳���
     * @return JGroupContainer ��
     */
    protected JGroupContainer getGroup(int index) {
        return (JGroupContainer) groupList.get(index);
    }

    /**
     * ��д��addImpl����,��ֹ����JGroupPane��������
     * @param comp Component
     * @param constraints Object
     * @param index int
     */
    protected void addImpl(Component comp, Object constraints, int index) {
        if (forbidFlag) {
            if (! (comp instanceof JGroupContainer)) {
                throw new UnsupportedOperationException(
                    "JGroupPane can't add component!");
            }
        }
        else {
            super.addImpl(comp, constraints, index);
        }
    }

    /**
     * <p>Title: OpenSwing</p>
     * <p>Description: ����岼�ֹ�����</p>
     * <p>Copyright: Copyright (c) 2004</p>
     * <p>Company: </p>
     * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
     * @version 1.0
     */
    class GroupLayout
        implements LayoutManager, java.io.Serializable {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		int vgap = 0;
        int hgap = 0;
        public GroupLayout() {
        }

        public GroupLayout(int hg, int vg) {
            this.hgap = hg;
            this.vgap = vg;
        }

        public void addLayoutComponent(String name, Component comp) {
        }

        public void removeLayoutComponent(Component comp) {
        }

        public Dimension preferredLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                Insets insets = parent.getInsets();
                int ncomponents = parent.getComponentCount();
                int w = 0;
                int h = 0;
                for (int i = 0; i < ncomponents; i++) {
                    Component comp = parent.getComponent(i);
                    Dimension d = comp.getPreferredSize();
                    if (w < d.width) {
                        w = d.width;
                    }
                    h += d.height + vgap;
                }
                return new Dimension(insets.left + insets.right + w + 2 * hgap,
                                     insets.top + insets.bottom + h + 2 * vgap);
            }
        }

        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        public void layoutContainer(Container parent) {
            synchronized (parent.getTreeLock()) {
                Insets insets = parent.getInsets();
                int ncomponents = parent.getComponentCount();
                if (ncomponents == 0) {
                    return;
                }
                int y = insets.top + vgap;
                for (int c = 0; c < ncomponents; c++) {
                    int h = parent.getComponent(c).getPreferredSize().height;
                    parent.getComponent(c).setBounds(
                        insets.left + hgap,
                        y,
                        parent.getWidth() - insets.left - insets.right -
                        2 * hgap, h);
                    y += h + vgap;
                }
            }
        }

        public String toString() {
            return getClass().getName();
        }
    }

    /**
     * <p>Title: OpenSwing</p>
     * <p>Description: ��</p>
     * <p>Copyright: Copyright (c) 2004</p>
     * <p>Company: </p>
     * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
     * @version 1.0
     */
    class JGroupContainer
        extends JPanel {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JButton bttGroupTitle = new JButton();
        private JPanel pMembers = new JPanel();
        private JScrollPane sp;
        public JGroupContainer() {
            this("");
        }

        public JGroupContainer(String name) {
            this(name, UIManager.getColor("Desktop.background"));
        }

        /**
         * @param name String  ����
         * @param background Color ��Ա���������屳��ɫ
         */
        public JGroupContainer(String name, Color background) {
            bttGroupTitle.setText(name);
            bttGroupTitle.setFocusable(false);
            pMembers.setLayout(new GroupLayout(5, 5));
            this.setLayout(new BorderLayout());
            this.add(bttGroupTitle, BorderLayout.NORTH);

            pMembers.setBackground(background);

            Color thumbColor = UIManager.getColor("ScrollBar.thumb");
            Color trackColor = UIManager.getColor("ScrollBar.track");
            Color trackHighlightColor = UIManager.getColor(
                "ScrollBar.trackHighlight");

            UIManager.put("ScrollBar.thumb", background);
            UIManager.put("ScrollBar.track", background);
            UIManager.put("ScrollBar.trackHighlight", background);
            sp = new JScrollPane(pMembers);
            sp.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            this.add(sp, BorderLayout.CENTER);
            collapse();
            UIManager.put("ScrollBar.thumb", thumbColor);
            UIManager.put("ScrollBar.track", trackColor);
            UIManager.put("ScrollBar.trackHighlight", trackHighlightColor);

        }

        /**
         * ���ü��
         * @param hgap int ����
         * @param vgap int �����
         */
        public void setMemberGap(int hgap, int vgap) {
            pMembers.setLayout(new GroupLayout(hgap, vgap));
        }

        /**
         * ȡ����ı��ⰴť
         * @return JButton
         */
        public JButton getTitleButton() {
            return bttGroupTitle;
        }

        /**
         * ȡ����ĳ�Ա������
         * @return JPanel
         */
        public JPanel getMembersContainer() {
            return pMembers;
        }

        /**
         * ������
         */
        public void collapse() {
            sp.setVisible(false);
            this.revalidate();
        }

        /**
         * չ����
         */
        public void expand() {
            sp.setVisible(true);
            this.revalidate();
        }

        /**
         * ��������
         * @param name String ����
         */
        public void setName(String name) {
            bttGroupTitle.setText(name);
        }

        /**
         * ȡ������
         * @return String
         */
        public String getName() {
            return bttGroupTitle.getText();
        }

        /**
         * ���һ����Ա���
         * @param index int ˳���
         * @param c Component ��Ա���
         */
        public void addMember(int index, Component c) {
            pMembers.add(c, index);
            pMembers.doLayout();
        }

        /**
         * ɾ��һ����Ա���
         * @param index int ˳���
         */
        public void removeMember(int index) {
            pMembers.remove(index);
            pMembers.doLayout();
        }

        /**
         * ȡ��һ����Ա���
         * @param index int ˳���
         * @return Component ��Ա���
         */
        public Component getMember(int index) {
            return pMembers.getComponent(index);
        }

        /**
         * ȡ��ȫ����Ա���
         * @return Component[] ��Ա���
         */
        public Component[] getMembers() {
            Component coms[] = new Component[getMemberCount()];
            for (int i = 0; i < coms.length; i++) {
                coms[i] = pMembers.getComponent(i);
            }
            return coms;
        }

        /**
         * ȡ�ó�Ա�������
         * @return int ����
         */
        public int getMemberCount() {
            return pMembers.getComponentCount();
        }

        /**
         * ��д��toString����
         * @return String
         */
        public String toString() {
            return getName();
        }
    }

    /**
         /**
      * ���Գ���
      * @param args String[]
      */
     public static void main(String[] args){
         JFrame frame = new JFrame("JGroupPanel Demo");
         frame.getContentPane().setLayout(new BorderLayout());
         frame.getContentPane().add(new JGroupPanel(), BorderLayout.CENTER);
         frame.setSize(150, 600);
         Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
         frame.setLocation(d.width - frame.getSize().width - 10, 10);
         frame.setVisible(true);
     }
}
