package com.wh.form;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.plaf.basic.BasicSplitPaneDivider;

public abstract class ChildForm extends JInternalFrame {
	protected boolean isEdit = false;
	private static final long serialVersionUID = 1L;

	class ImplAWTEventListener implements AWTEventListener {
		public boolean hasFocus(Component c) {
			if (c instanceof Container && (c instanceof JScrollBar || c instanceof JScrollPane || c instanceof JViewport || c instanceof JToolBar || c instanceof BasicSplitPaneDivider || 
					c instanceof JPanel || c instanceof JSplitPane || c instanceof JRootPane || c instanceof JLayeredPane || 
					c instanceof Window || c instanceof JInternalFrame || c instanceof Dialog)){
				Container container = (Container)c;
				for (int i = 0; i < container.getComponentCount(); i++) {
					boolean b = hasFocus(container.getComponent(i));
					if (b)
						return true;
				}
				return false;
			}
			
			return  c.hasFocus();
		}
		
		@Override
		public void eventDispatched(AWTEvent event) {
			if (event.getClass() == KeyEvent.class) {
				
				boolean b = mainControl.getFront() == null
						|| mainControl.getFront() != ChildForm.this;
						
				if (b)
					return;

				if (!ChildForm.this.hasFocus())
					if (!hasFocus(ChildForm.this))
						return;
				
				KeyEvent keyEvent = (KeyEvent) event;
				if (keyEvent.getID() == KeyEvent.KEY_PRESSED) {
					keyPressed(keyEvent);
				} else if (keyEvent.getID() == KeyEvent.KEY_RELEASED) {
					keyReleased(keyEvent);
				}
			}
		}
	}

	protected void keyPressed(KeyEvent event) {
	}

	protected void keyReleased(KeyEvent event) {
	}

	public JFrame getFrame() {
		Container parent = this;
		while (parent != null) {
			if (parent instanceof JFrame)
				return (JFrame) parent;
			parent = parent.getParent();
		}

		return null;
	}

	protected boolean needPrompt() {
		return isEdit;
	}

	protected boolean allowQuit() {
		return true;
	}

	protected IMainControl mainControl;

	public ChildForm(IMainControl mainControl) {
		super();
		this.mainControl = mainControl;
		Toolkit.getDefaultToolkit().addAWTEventListener(awtEventListener, AWTEvent.KEY_EVENT_MASK);
	}

	ImplAWTEventListener awtEventListener = new ImplAWTEventListener();

	public void dispose() {
		Toolkit.getDefaultToolkit().removeAWTEventListener(awtEventListener);
	}
}
