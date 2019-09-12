package com.wh.system.tools;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JScrollBar;

public class EventHelp {
	
	public static void setGlobalMouseWheel(Component target, JScrollBar scrollBar){
		EventHelp.addGlobalMouseWheelEvent(new MouseWheelListener() {
			
			protected boolean checkIsContentScrollbar(Component view) {
				if (view == target)
					return true;
				else{
					if (view.getParent() != null)
						return checkIsContentScrollbar(view.getParent());
					else
						return false;
				}
			}
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.getSource() instanceof Component){
					Component view = (Component)e.getSource();
					if (view == target){
						return;
					}
					
					if (checkIsContentScrollbar(view)){
						int y = e.getUnitsToScroll() * scrollBar.getUnitIncrement();
						scrollBar.setValue(scrollBar.getValue() + y);
					}
				}
			}
		});
	
	}
	
	public static void addGlobalMouseWheelEvent(MouseWheelListener listener) {
		Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
			
			@Override
			public void eventDispatched(AWTEvent event) {
				if (event.getClass() == MouseWheelEvent.class)
					listener.mouseWheelMoved((MouseWheelEvent)event);
			}
		}, AWTEvent.MOUSE_WHEEL_EVENT_MASK);
	}
}
