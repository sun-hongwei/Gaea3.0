package com.wh.draws;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Collection;

import com.wh.draws.FlowNode.BeginNode;
import com.wh.draws.FlowNode.ConditionNode;
import com.wh.draws.FlowNode.EndNode;
import com.wh.draws.FlowNode.ProcessNode;
import com.wh.draws.FlowNode.SwitchNode;

public class FlowCanvas extends DrawCanvas{

	private static final long serialVersionUID = -2512110793283328946L;

	protected void paintNodes(Graphics g, Collection<DrawNode> nodes, boolean needCheckViewport){
        for (DrawNode node : nodes) {
        	node.drawLins(g);
		}
        
        for (DrawNode node : nodes) {
        	Font oldfont = g.getFont();
        	
        	g.setFont(node.font);
            
			node.draw(g, needCheckViewport);
			
			g.setFont(oldfont);
		}
	}

	protected boolean allowPaste(DrawNode node){
		return node instanceof FlowNode;
	}
	
	public FlowNode add(String title, Class<? extends FlowNode> c, Object userData, IDataSerializable dataSerializable){
		FlowNode node = (FlowNode)add(null, title, new Rectangle(0, 0, 100, 100), userData, new IDataSerializable() {
			
			@Override
			public String save(Object userData) {
				if (dataSerializable == null)
					return null;
				
				return dataSerializable.save(userData);
			}
			
			@Override
			public DrawNode newDrawNode(Object userdata) {
				try {
					FlowNode node = c.getConstructor(DrawCanvas.class).newInstance(FlowCanvas.this);
					return node;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			
			@Override
			public Object load(String value) {
				if (dataSerializable == null)
					return null;
				
				return dataSerializable.load(value);
			}

			@Override
			public void initDrawNode(DrawNode node) {
				if (dataSerializable != null)
					dataSerializable.initDrawNode(node);
			}
		});
		
		int size = 40;
		Rectangle rect = node.getRect();
		if (node instanceof BeginNode || node instanceof EndNode)
			rect.height = size;
		
		if (node instanceof SwitchNode){
			rect.width = size;
			rect.height = size;
		}

		if (node instanceof ProcessNode || node instanceof ConditionNode){
			rect.width = 100;
			rect.height = size;
		}
		
		return node;
	}

	protected void updateCanvasSize(Rectangle oldUseRect) {
		if (useRect.isEmpty())
			useRect = oldUseRect;
	}

}
