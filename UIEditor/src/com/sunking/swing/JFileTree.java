package com.sunking.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.wh.control.EditorEnvironment;

/**
 * <p>Title: OpenSwing</p>
 * <p>Description: JFileTree �ļ���</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
 * @version 1.0
 */

public class JFileTree extends JTree implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final FileSystemView fileSystemView =
        FileSystemView.getFileSystemView();
    DefaultTreeModel treeModel;
    /**
     * ֻ���Ŀ¼��Ŀ¼��
     */
    public JFileTree(){
        this(new DirFilter());
    }

    /**
     * ָ�����������ļ���
     * @param filter FileFilter ָ��������
     */
    public JFileTree(java.io.FileFilter filter){
        FileNode root = new FileNode(fileSystemView.getRoots()[0], filter);
        treeModel = new DefaultTreeModel(root);
        root.explore();
        treeModel.nodeStructureChanged(root);
        this.setModel(treeModel);
        addTreeExpansionListener(new JFileTreeExpandsionListener());
        setCellRenderer(new JFileTreeCellRenderer());
    }

    /**
     * ȡ�õ�ǰѡ��Ľڵ�
     * @return FileNode
     */
    public FileNode getSelectFileNode(){
        TreePath path = getSelectionPath();
        if(path == null || path.getLastPathComponent() == null){
            return null;
        }
        return(FileNode)path.getLastPathComponent();
    }

    /**
     * ���õ�ǰѡ��Ľڵ�
     * @param f FileNode
     * @throws Exception
     */
    public void setSelectFileNode(FileNode f) throws Exception{
        this.setSelectFile(f.getFile());
    }

    /**
     * ȡ�õ�ǰѡ����ļ���Ŀ¼
     * @return File
     */
    public File getSelectFile(){
        FileNode node = getSelectFileNode();
        return node == null ? null : node.getFile();
    }

    /**
     * ���õ�ǰѡ����ļ���Ŀ¼
     * @param f File
     */
    public void setSelectFile(File f){
        FileNode node;
		try {
			node = this.expandFile(f);
	        TreePath path = new TreePath(((DefaultTreeModel)getModel()).getPathToRoot(node));
	        this.scrollPathToVisible(path);
	        this.setSelectionPath(path);
	        this.repaint();
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showException(e);
		}
    }

    /**
     * չ��ָ�����ļ���Ŀ¼
     * @param f File
     * @return FileNode
     * @throws Exception
     */
    public FileNode expandFile(File f) throws Exception{
        if(!f.exists()){
            throw new java.io.FileNotFoundException(f.getAbsolutePath());
        }
        Vector<File> vTemp = new Vector<File>();
        File fTemp = f;
        while(fTemp != null){
            vTemp.add(fTemp);
            fTemp = fileSystemView.getParentDirectory(fTemp);
        }

        FileNode nParent = (FileNode)treeModel.getRoot();
        for(int i = vTemp.size() - 1; i >= 0; i--){
            fTemp = (File)vTemp.get(i);
            nParent.explore();
            for(int j = 0; j < nParent.getChildCount(); j++){
                FileNode nChild = (FileNode)nParent.getChildAt(j);
                if(nChild.getFile().equals(fTemp)){
                    nParent = nChild;
                }
            }
        }
        return nParent;
    }

    /**
     *
     * <p>Title: OpenSwing</p>
     *
     * <p>Description: �ļ���������</p>
     *
     * <p>Copyright: Copyright (c) 2004</p>
     *
     * <p>Company: </p>
     *
     * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
     * @version 1.0
     */
    class JFileTreeCellRenderer extends DefaultTreeCellRenderer{
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded,
            boolean leaf, int row,
            boolean hasFocus){
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
                                               row,
                                               hasFocus);
            try{
                closedIcon = fileSystemView.getSystemIcon(((FileNode)value).
                    getFile());
                openIcon = closedIcon;
                setIcon(closedIcon);
            } catch(Exception ex){
            }
            return this;
        }
    }

    /**
     *
     * <p>Title: OpenSwing</p>
     *
     * <p>Description: �ļ���չ���¼�������</p>
     *
     * <p>Copyright: Copyright (c) 2004</p>
     *
     * <p>Company: </p>
     *
     * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
     * @version 1.0
     */
    class JFileTreeExpandsionListener implements TreeExpansionListener{
        public JFileTreeExpandsionListener(){}

        public void treeExpanded(TreeExpansionEvent event){
            TreePath path = event.getPath();
            if(path == null || path.getLastPathComponent() == null)
                return;
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            FileNode node = (FileNode)path.getLastPathComponent();
            node.explore();
            JTree tree = (JTree)event.getSource();
            DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
            treeModel.nodeStructureChanged(node);
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

        public void treeCollapsed(TreeExpansionEvent event){}
    }

    /**
     *
     * <p>Title: OpenSwing</p>
     *
     * <p>Description:�ļ��ڵ� </p>
     *
     * <p>Copyright: Copyright (c) 2004</p>
     *
     * <p>Company: </p>
     *
     * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
     * @version 1.0
     */
    public static class FileNode extends DefaultMutableTreeNode{
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private boolean explored = false;
        private java.io.FileFilter filter = null;

        public FileNode(File file, java.io.FileFilter filter){
            if(filter == null){
                this.filter = new DirFilter();
            } else{
                this.filter = filter;
            }
            setUserObject(file);
        }

        public boolean getAllowsChildren(){
            return isDirectory();
        }

        public boolean isDirectory(){
            return!isLeaf();
        }

        public boolean isLeaf(){
            return getFile().isFile();
        }

        public File getFile(){
            return(File)getUserObject();
        }

        public boolean isExplored(){
            return explored;
        }

        public void setExplored(boolean b){
            explored = b;
        }

        public String toString(){
            if(getFile() instanceof File)
                return fileSystemView.getSystemDisplayName((File)getFile());
            else
                return getFile().toString();
        }

        /**
         * չ���ڵ�
         */
        public void explore(){
            if(!explored){
                explored = true;
                File file = getFile();
                //�������ʹ�� file.listFiles(filter) ��BUG
                File[] children = file.listFiles();
                if(children == null || children.length == 0){
                    return;
                }
                //���˺�����,ѡ����������Ŀ¼, �ټ����������ļ�
                ArrayList<File> listDir = new ArrayList<File>();
                ArrayList<File> listFile = new ArrayList<File>();
                for(int i = 0; i < children.length; ++i){
                    File f = children[i];
                    if(filter.accept(f)){
                        if(f.isDirectory()){
                            listDir.add(f);
                        }else{
                            listFile.add(f);
                        }
                    }
                }
                Collections.sort(listDir);
                Collections.sort(listFile);
                for(int i = 0; i < listDir.size(); i++){
                    add(new FileNode((File)listDir.get(i),filter));
                }
                for(int i = 0; i < listFile.size(); i++){
                    add(new FileNode((File)listFile.get(i),filter));
                }
            }
        }
    }
    /**
     *
     * <p>Title: OpenSwing</p>
     *
     * <p>Description:Ŀ¼������ </p>
     *
     * <p>Copyright: Copyright (c) 2004</p>
     *
     * <p>Company: </p>
     *
     * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
     * @version 1.0
     */
    public static class DirFilter implements java.io.FileFilter{
         public boolean accept(File pathname){
             return pathname.isDirectory();
         }
    }
    /**
     *
     * <p>Title: OpenSwing</p>
     *
     * <p>Description: �����ļ�������</p>
     *
     * <p>Copyright: Copyright (c) 2004</p>
     *
     * <p>Company: </p>
     *
     * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
     * @version 1.0
     */
    public static class AllFileFilter implements java.io.FileFilter{
         public boolean accept(File pathname){
             return true;
         }
    }
    /**
     *
     * <p>Title: OpenSwing</p>
     *
     * <p>Description:��չ�������� </p>
     *
     * <p>Copyright: Copyright (c) 2004</p>
     *
     * <p>Company: </p>
     *
     * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
     * @version 1.0
     */
    public static class ExtensionFilter implements java.io.FileFilter{
        String extensions[];
        public ExtensionFilter(String extensions[]){
            this.extensions = new String[extensions.length];
            for(int i = 0; i < extensions.length; i++){
                this.extensions[i] = extensions[i].toLowerCase();
            }
        }

        public boolean accept(File pathname){
            if(pathname.isDirectory()){
                return true;
            }
            String name = pathname.getName().toLowerCase();
            for(int i = 0; i < extensions.length; i++){
                if(name.endsWith(this.extensions[i])){
                    return true;
                }
            }
            return false;
        }
    }

    public static void main(String[] args){
        JFrame frame = OpenSwingUtil.createDemoFrame("JFileTree Demo");
        frame.setSize(600, 400);
        JFileTree tree = new JFileTree();
        Container pContent = frame.getContentPane();
        pContent.setLayout(new BorderLayout());
        JPanel pNorth = new JPanel(new GridLayout(1,3));
        JPanel pCenter = new JPanel(new GridLayout(1,3));
        pNorth.add(new JLabel("Directory only"));
        pNorth.add(new JLabel("All file"));
        pNorth.add(new JLabel("Directory and *.doc,*.txt file"));
        pCenter.add(new JScrollPane(tree));
        pCenter.add(new JScrollPane(new JFileTree(new AllFileFilter())));
        pCenter.add(new JScrollPane(new JFileTree(
            new ExtensionFilter(new String[]{"doc", "txt"}))));
        pContent.add(pNorth, BorderLayout.NORTH);
        pContent.add(pCenter, BorderLayout.CENTER);
        SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
		        try{
		            tree.setSelectFile(new File("X:/JAVA/UIEditor/src/lib"));
		        } catch(Exception ex){
		            ex.printStackTrace();
		        }
			}
		});
        frame.setVisible(true);
    }
}
