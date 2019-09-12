package com.wh.draws;

import java.awt.Font;
import java.awt.Graphics;
import java.util.Collection;

public class ReportCanvas extends DrawCanvas {

	private static final long serialVersionUID = 1L;

	protected boolean allowPaste(DrawNode node){
		return node instanceof UINode;
	}
	
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

}
