package com.wh.control.tree.drag;

import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

public class TreeDrag extends JTree implements Autoscroll {

	private static final long serialVersionUID = 7090187854586693642L;

	TreeDragSource ds;

	TreeDropMoveTarget dt;

	private int margin = 12;

	public TreeDrag() {
		super();
		// If we only support move operations...
		// ds = new TreeDragSource(tree, DnDConstants.ACTION_MOVE);
		ds = new TreeDragSource(this, DnDConstants.ACTION_COPY_OR_MOVE);
		dt = new TreeDropMoveTarget(this);
		setDragEnabled(true);
	}

	public void autoscroll(Point p) {
		int realrow = getRowForLocation(p.x, p.y);
		Rectangle outer = getBounds();
		realrow = (p.y + outer.y <= margin ? realrow < 1 ? 0 : realrow - 1
				: realrow < getRowCount() - 1 ? realrow + 1 : realrow);
		scrollRowToVisible(realrow);
	}

	public Insets getAutoscrollInsets() {
		Rectangle outer = getBounds();
		Rectangle inner = getParent().getBounds();
		return new Insets(inner.y - outer.y + margin, inner.x - outer.x + margin,
				outer.height - inner.height - inner.y + outer.y + margin,
				outer.width - inner.width - inner.x + outer.x + margin);
	}

	public interface IOnDrag{
		void onDragEnd(DefaultMutableTreeNode newParent, 
				DefaultMutableTreeNode oldParent, int newIndex, int oldIndex, DefaultMutableTreeNode node);
	}
	
	List<IOnDrag> onDrags = new ArrayList<>();
	
	public void setModel(TreeModel model){
		super.setModel(model);
	}
	
	public void addOnDragListener(IOnDrag onDrag){
		removeOnDragListener(onDrag);
		onDrags.add(onDrag);
	}

	public void removeOnDragListener(IOnDrag onDrag){
		onDrags.remove(onDrag);
	}
	// // Use this method if you want to see the boundaries of the
	// // autoscroll active region
	//
	// public void paintComponent(Graphics g) {
	// super.paintComponent(g);
	// Rectangle outer = getBounds();
	// Rectangle inner = getParent().getBounds();
	// g.setColor(Color.red);
	// g.drawRect(-outer.x + 12, -outer.y + 12, inner.width - 24, inner.height -
	// 24);
	// }
	//
}


