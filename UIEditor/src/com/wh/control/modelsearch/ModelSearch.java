package com.wh.control.modelsearch;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.wh.control.EditorEnvironment;

public class ModelSearch<T extends Component> implements IModelSearch<T> {
	@SuppressWarnings("rawtypes")
	static final List<ModelSearch> modelSearchs = new ArrayList<>();

	String findKey;
	T findControl;
	Object value;
	int index;
	Integer col;

	List<Object> selects = new ArrayList<>();

	public ModelSearch(T control) {
		findControl = control;
	}

	@SuppressWarnings("rawtypes")
	protected void selectView(Object value) {
		if (findControl instanceof JTree) {
			TreeNode node = (TreeNode) value;
			JTree tree = (JTree) findControl;
			DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
			TreePath path = new TreePath(model.getPathToRoot(node));
			tree.setSelectionPath(path);
			tree.scrollPathToVisible(path);
		} else if (findControl instanceof JList) {
			JList list = (JList) findControl;
			list.setSelectedValue(value, true);
		} else if (findControl instanceof JTable) {
			int row = (int) value;
			JTable table = (JTable) findControl;
			row = table.convertRowIndexToView(row);
			table.setRowSelectionInterval(row, row);
		}
	}

	protected void treeToSelects() {
		selects.clear();
		if (!(findControl instanceof JTree)) {
			return;
		}

		JTree tree = (JTree) findControl;
		initSelects(tree, null);
	}

	protected void initSelects(JTree tree, TreeNode root) {
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		if (model == null)
			return;

		if (root == null)
			root = (TreeNode) model.getRoot();

		if (root == null) {
			return;
		}

		selects.add(root);
		for (int j = 0; j < root.getChildCount(); j++) {
			initSelects(tree, root.getChildAt(j));
		}

	}

	@SuppressWarnings("unchecked")
	protected void listToSelects() {
		selects.clear();
		if (!(findControl instanceof JList)) {
			return;
		}

		JList<T> list = (JList<T>) findControl;

		for (int i = 0; i < list.getModel().getSize(); i++) {
			selects.add(list.getModel().getElementAt(i));
		}

	}

	protected void tableToSelects() {
		selects.clear();
		if (!(findControl instanceof JTable)) {
			return;
		}

		JTable table = (JTable) findControl;

		DefaultTableModel model = (DefaultTableModel) table.getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			selects.add(model.getValueAt(i, col));
		}
	}

	protected boolean check(Object item, int index) {
		if (item == null)
			return false;

		String text = item.toString();
		if (findKey == null || findKey.isEmpty() || text == null || text.isEmpty())
			return false;

		if (text.toLowerCase().indexOf(findKey) != -1) {
			this.value = item;
			this.index = index;
			selectView(item);
			return true;
		} else {
			return false;
		}
	}

	protected void realSearch(String findKey, boolean prior) {
		if (prior) {
			int start = index - 1;
			if (start < 0)
				return;
			for (int i = start; i >= 0; i--) {
				if (check(selects.get(i), i))
					return;
			}
		} else {
			int start = index + 1;
			if (start >= selects.size())
				return;
			for (int i = start; i < selects.size(); i++) {
				if (check(selects.get(i), i))
					return;
			}

		}
	}

	protected void search(String findKey, Integer col, boolean prior) {
		reset();
		this.findKey = findKey.toLowerCase();
		if (col != null)
			this.col = col;
		else {
			if (findControl instanceof JTable) {
				JTable table = (JTable)findControl;
				if (table.getSelectedColumn() == -1) {
					EditorEnvironment.showMessage("请先选中一列后重试！");
					return;
				}else {
					this.col = table.convertColumnIndexToModel(table.getSelectedColumn());
				}
			}
		}
		if (findControl instanceof JList) {
			listToSelects();
		} else if (findControl instanceof JTree) {
			treeToSelects();
		} else if (findControl instanceof JTable) {
			tableToSelects();
		} else {
			return;
		}

		realSearch(findKey, prior);
	}

	@Override
	public void search(String findKey) {
		search(findKey, null);
	}

	@Override
	public void search(String findKey, Integer col) {
		search(findKey, null, false);
	}

	@Override
	public void prior() {
		realSearch(findKey, true);
	}

	@Override
	public void next() {
		realSearch(findKey, false);
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public void reset() {
		findKey = null;
		index = -1;
		value = null;
		col = 0;
	}

}
