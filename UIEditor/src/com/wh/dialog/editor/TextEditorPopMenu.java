package com.wh.dialog.editor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.TextAction;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public class TextEditorPopMenu {
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	public static void add(JTextPane editor){
		JPopupMenu menu = new JPopupMenu();
		addPopup(editor, menu);
		
		editor.resetKeyboardActions();
		
		JMenuItem menuItem = new JMenuItem(new DefaultEditorKit.CutAction());
		menuItem.setText("剪切");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
		menu.add(menuItem);
		
		menuItem = new JMenuItem(new DefaultEditorKit.CopyAction());
		menuItem.setText("复制");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
		menu.add(menuItem);
		
		menuItem = new JMenuItem(new DefaultEditorKit.PasteAction());
		menuItem.setText("粘贴");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
		menu.add(menuItem);
		
		menu.addSeparator();
		
		final UndoAction undoAction = new UndoAction();
		final RedoAction redoAction = new RedoAction();
		UndoHandler undoHandler = new UndoHandler(undoAction, redoAction);
		
		undoAction.setIUpdate(undoHandler);
		redoAction.setIUpdate(undoHandler);
		
		editor.registerKeyboardAction(undoAction, KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
		editor.registerKeyboardAction(redoAction, KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		menuItem = new JMenuItem(undoAction);
		menuItem.setText("撤销");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
		menu.add(menuItem);

		menuItem = new JMenuItem(redoAction);
		menuItem.setText("重做");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
		menu.add(menuItem);

		menu.addSeparator();
		
		menuItem = new JMenuItem(new SelectAllAction(editor));
		menuItem.setText("全选");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK));
		menu.add(menuItem);
	
		editor.getDocument().addUndoableEditListener(undoHandler);
		undoHandler.resetUndoManager();
	}
	
	public interface IUpdate{
		public void update();
		public UndoManager getUndoManager();
	}
	
	public static class UndoHandler implements UndoableEditListener, IUpdate{
		public UndoManager undoManager = new UndoManager();
		public UndoAction undoAction;
		public RedoAction redoAction;
		
		public UndoHandler(UndoAction undoAction, RedoAction redoAction){
			this.undoAction = undoAction;
			this.redoAction = redoAction;
		}
		
		public void undoableEditHappened(UndoableEditEvent e) {
			undoManager.addEdit(e.getEdit());
			update();
		}
		
		public void update(){
			undoAction.update();
			redoAction.update();
		}
		
		public void resetUndoManager(){ 
			undoManager.discardAllEdits();
			update();
		}

		@Override
		public UndoManager getUndoManager() {
			return undoManager;
		}
	}

	public static class UndoAction extends TextAction 
	{ 
		private static final long serialVersionUID = 1L;

		IUpdate iUpdate;
		public UndoAction() 
		{
			super("Undo");
			setEnabled(false);
		}
		
		public void setIUpdate(IUpdate iUpdate){
			this.iUpdate = iUpdate;
		}
		
		public void actionPerformed(ActionEvent e) 
		{ 
			try{ 
				iUpdate.getUndoManager().undo(); 
			} 
			catch (CannotUndoException ex){
				ex.printStackTrace();
			} 
			iUpdate.update();
		}

		protected void update(){ 
			if(iUpdate.getUndoManager().canUndo()){ 
			    setEnabled(true); 
			    //putValue(Action.NAME, "撤销"); 
			}else{ 
			    setEnabled(false); 
			    //putValue(Action.NAME, "撤销"); 
			}
		} 
		 
	}

	public static class SelectAllAction extends TextAction 
	{ 
		private static final long serialVersionUID = 1L;

		private JTextPane editor;
		public SelectAllAction(JTextPane editor) 
		{
			super("SelectAll");
			this.editor = editor;
		}
		
		public void actionPerformed(ActionEvent e) 
		{
			editor.selectAll();
		}

	}

	public static class RedoAction extends TextAction{ 
		private static final long serialVersionUID = 1L;

		IUpdate iUpdate;
		public RedoAction() 
		{
			super("Redo"); 
			setEnabled(false); 
		}

		public void setIUpdate(IUpdate iUpdate){
			this.iUpdate = iUpdate;
		}
		
		public void actionPerformed(ActionEvent e){ 
			try 
			{ 
				iUpdate.getUndoManager().redo(); 
			}catch (CannotRedoException ex){ 
				ex.printStackTrace();
			} 
			iUpdate.update(); 
		}
		
		protected void update(){ 
			if(iUpdate.getUndoManager().canRedo()){ 
				setEnabled(true);
				//putValue(Action.NAME, "重做"); 
			}else{
				setEnabled(false);
				//putValue(Action.NAME, "重做"); 
			} 
		} 
	} 
}