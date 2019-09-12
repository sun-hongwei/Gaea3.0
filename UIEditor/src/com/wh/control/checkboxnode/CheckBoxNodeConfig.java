package com.wh.control.checkboxnode;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import com.wh.control.checkboxnode.CheckBoxNode.ISelection;
import com.wh.control.checkboxnode.CheckBoxNodeRender.IGetIcon;

public class CheckBoxNodeConfig extends MouseAdapter{
	ISelection onSelection;
	protected CheckBoxNodeConfig(ISelection onSelection){
		this.onSelection = onSelection;
	}
	
	public static void config(JTree tree, ISelection onSelection){
		config(tree, onSelection, null);
	}
	
	public static void config(JTree tree, ISelection onSelection, IGetIcon onIcon){
	
		tree.setCellRenderer(new CheckBoxNodeRender(onIcon));
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addMouseListener(new CheckBoxNodeConfig(onSelection));
	}
	
	@Override
    public void mouseReleased(MouseEvent e) {
        treeMouseClicked(e);
	}
	
	@Override
    public void mouseClicked(MouseEvent e)
    {
    }

    private void treeMouseClicked(MouseEvent event)
    {
        JTree tree = (JTree)event.getSource();
//        int x = event.getX();
//        int y = event.getY();
//        int row = tree.getRowForLocation(x, y);
//        TreePath path = tree.getPathForRow(row);
//        if(path != null) 
//        { 
//            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent(); 
//            if(node != null && node instanceof CheckBoxNode) 
//            { 
//            	CheckBoxNode checkBoxNode = (CheckBoxNode)node;
//                boolean isSelected = !checkBoxNode.isSelected(); 
//                checkBoxNode.setSelected(isSelected); 
//                ((DefaultTreeModel)tree.getModel()).nodeStructureChanged(node); 
//                if (onSelection != null){
//                	onSelection.onSelected(checkBoxNode);
//                }
//            } 
//        }
        if (tree.getSelectionPath() == null)
        	return;
        
        if (tree.getMaxSelectionRow() == -1)
        	return;
        
        Rectangle rectangle = tree.getRowBounds(tree.getMaxSelectionRow());
        rectangle.width = tree.getWidth();
        rectangle.x = 0;
        if (!rectangle.contains(event.getX(), event.getY()))
        	return;
        
        CheckBoxNode checkBoxNode = (CheckBoxNode)tree.getSelectionPath().getLastPathComponent();
		boolean isSelected = !checkBoxNode.isSelected(); 
		checkBoxNode.setSelected(isSelected); 
		((DefaultTreeModel)tree.getModel()).nodeStructureChanged(checkBoxNode); 
		if (onSelection != null){
		  	onSelection.onSelected(checkBoxNode);
		}
        
    }
}
