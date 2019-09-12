package com.wh.control.checkboxnode;

import javax.swing.tree.DefaultMutableTreeNode;

public class CheckBoxNode extends DefaultMutableTreeNode {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3947805063884134823L;
	protected boolean isSelected;

	public boolean autoCheckChilds = false;
	public CheckBoxNode() {
		this(null);
	}

	public CheckBoxNode(Object userObject) {
		this(userObject, true, false);
	}

	public CheckBoxNode(Object userObject, boolean allowsChildren,
			boolean isSelected) {
		super(userObject, allowsChildren);
		this.isSelected = isSelected;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public interface ISelection {
		public void onSelected(CheckBoxNode selectNode);
	}

	public void setSelected(boolean _isSelected) {
		setSelected(_isSelected, null);
	}

	public void setSingleSelected(boolean _isSelected) {
		this.isSelected = _isSelected;
	}

	public void setSelected(boolean _isSelected, ISelection iSelection) {
		this.isSelected = _isSelected;

		if (autoCheckChilds){
		
			if (_isSelected) {
				if (children != null) {
					for (Object obj : children) {
						CheckBoxNode node = (CheckBoxNode) obj;
						if (_isSelected != node.isSelected())
							node.setSelected(_isSelected, iSelection);
					}
				}
			} else {
				if (children != null) {
					int index = 0;
					for (; index < children.size(); ++index) {
						CheckBoxNode childNode = (CheckBoxNode) children.get(index);
						if (!childNode.isSelected())
							break;
					}
					if (index == children.size()) {
						for (int i = 0; i < children.size(); ++i) {
							CheckBoxNode node = (CheckBoxNode) children.get(i);
							if (node.isSelected() != _isSelected)
								node.setSelected(_isSelected, iSelection);
						}
					}
				}
	
				CheckBoxNode pNode = (CheckBoxNode) parent;
				if (pNode != null && pNode.isSelected() != _isSelected)
					pNode.setSelected(_isSelected, iSelection);
			}
		}
		if (iSelection != null)
			iSelection.onSelected(this);
	}
}