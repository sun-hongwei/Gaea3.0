package com.wh.control.modelsearch;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.table.TableModel;
import javax.swing.tree.TreeModel;

import com.wh.control.CheckBoxList;
import com.wh.control.EditorEnvironment;
import com.wh.control.tree.drag.TreeDrag;

public class ModelSearchView {
	@SuppressWarnings("rawtypes")
	protected static void setKeyPressEvent(JComponent component, IModelSearch modelSearch) {
		component.addKeyListener(new KeyAdapter() {
			boolean isPress = false;

			@Override
			public void keyReleased(KeyEvent e) {
				if (isPress) {
					isPress = false;
					switch (e.getKeyCode()) {
					case KeyEvent.VK_F3:
						modelSearch.prior();
						break;

					case KeyEvent.VK_F4:
						modelSearch.next();
						break;
					case KeyEvent.VK_ENTER:
						if (component instanceof JTree) {
						} else if (component instanceof JList) {
						} else if (component instanceof JTable) {
						} else {
							return;
						}

						String input = EditorEnvironment.showInputDialog("请输入要搜索的字符");

						if (input == null || input.isEmpty())
							return;

						modelSearch.search(input);
						break;
					default:
						break;
					}
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.isAltDown() || e.isControlDown() || e.isShiftDown())
					return;

				isPress = true;
			}
		});
	}

	public static class TreeDragModelSearchView extends TreeDrag {

		private static final long serialVersionUID = 1L;

		public IModelSearch<JTree> modelSearch;

		public TreeDragModelSearchView() {
			super();
			modelSearch = new ModelSearch<JTree>(this);
			setKeyPressEvent(this, modelSearch);
		}
	}

	public static class TreeModelSearchView extends JTree {

		private static final long serialVersionUID = 1L;

		public IModelSearch<JTree> modelSearch;

		public TreeModelSearchView() {
			super();
			modelSearch = new ModelSearch<JTree>(this);
			setKeyPressEvent(this, modelSearch);
		}
		
		public TreeModelSearchView(TreeModel model) {
			super(model);
			modelSearch = new ModelSearch<JTree>(this);
			setKeyPressEvent(this, modelSearch);			
		}
	}

	public static class TableModelSearchView extends JTable {

		private static final long serialVersionUID = 1L;

		public IModelSearch<JTable> modelSearch;

		public TableModelSearchView() {
			super();
			modelSearch = new ModelSearch<>(this);
			setKeyPressEvent(this, modelSearch);
		}
		public TableModelSearchView(TableModel model) {
			super(model);
			modelSearch = new ModelSearch<>(this);
			setKeyPressEvent(this, modelSearch);
		}
	}

	public static class CheckBoxListModelSearchView<T> extends CheckBoxList<T> {

		private static final long serialVersionUID = 1L;

		public IModelSearch<CheckBoxList<T>> modelSearch;

		public CheckBoxListModelSearchView() {
			super();
			modelSearch = new ModelSearch<CheckBoxList<T>>(this);
			setKeyPressEvent(this, modelSearch);
		}
	}

	public static class ListModelSearchView<T> extends JList<T> {

		private static final long serialVersionUID = 1L;

		public IModelSearch<JList<T>> modelSearch;

		public ListModelSearchView() {
			super();
			modelSearch = new ModelSearch<JList<T>>(this);
			setKeyPressEvent(this, modelSearch);
		}
		public ListModelSearchView(ListModel<T> model) {
			super(model);
			modelSearch = new ModelSearch<JList<T>>(this);
			setKeyPressEvent(this, modelSearch);
		}
	}
}
