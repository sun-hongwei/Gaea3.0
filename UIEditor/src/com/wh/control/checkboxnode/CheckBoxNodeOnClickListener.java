package com.wh.control.checkboxnode;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import com.wh.control.checkboxnode.CheckBoxNode.ISelection;

public class CheckBoxNodeOnClickListener extends MouseAdapter {
	public CheckBoxNodeOnClickListener(ISelection onSelection) {
		super();
		this.onSelection = onSelection;
		// TODO Auto-generated constructor stub
	}

	ISelection onSelection;

	@Override
    public void mouseReleased(MouseEvent event) {
        JTree tree = (JTree)event.getSource();
        if (tree.getSelectionPath() == null)
        	return;
        
        CheckBoxNode checkBoxNode = (CheckBoxNode)tree.getSelectionPath().getLastPathComponent();
		boolean isSelected = !checkBoxNode.isSelected(); 
		checkBoxNode.setSelected(isSelected); 
		((DefaultTreeModel)tree.getModel()).nodeStructureChanged(checkBoxNode); 
		if (onSelection != null){
		  	onSelection.onSelected(checkBoxNode);
		}
    }

	@Override
	public void mouseClicked(MouseEvent event) {
	}
}