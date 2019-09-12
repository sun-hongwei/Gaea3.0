package com.wh.draws;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.wh.control.EditorEnvironment;
import com.wh.draws.AppWorkflowNode.BeginNode;
import com.wh.draws.AppWorkflowNode.CommandNode;
import com.wh.draws.AppWorkflowNode.EndNode;
import com.wh.draws.AppWorkflowNode.JSCommandNode;
import com.wh.draws.AppWorkflowNode.JSDefaultFileNode;
import com.wh.draws.AppWorkflowNode.JSFileNode;

public class AppWorkflowCanvas extends DrawCanvas {

	private static final long serialVersionUID = 1L;

	public JSDefaultFileNode jsDefaultFileNode;
	
	protected void updateCanvasSize(Rectangle oldUseRect) {
		if (useRect.width == 0 || useRect.height == 0){
			useRect = oldUseRect;
		}
		
	}
	
	protected void changePasteNode(DrawNode node) throws IOException {
		super.changePasteNode(node);
		if (node instanceof UINode){
			UINode uiNode = (UINode)node;
			if (uiNode.info != null){
				uiNode.info.id = UUID.randomUUID().toString();
			}
		}

	}

	protected boolean allowPaste(DrawNode node){
		return node instanceof AppWorkflowNode;
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

	public static void publish(String name, String memo, String params, List<String> appnames){
		try {
			JSDefaultFileNode jsDefaultFileNode = new JSDefaultFileNode(null);
			jsDefaultFileNode.command = name;
			jsDefaultFileNode.memo = memo;
			jsDefaultFileNode.params = params;
			HashMap<String, JSFileNode> jsfiles = new HashMap<>();
			for (String appwname : appnames) {
				AppWorkflowCanvas appWorkflowCanvas = new AppWorkflowCanvas();
				appWorkflowCanvas.setFile(EditorEnvironment.getProjectFile(EditorEnvironment.AppWorkflow_Dir_Name, EditorEnvironment.getAppWorkflow_FileName(appwname)));
				appWorkflowCanvas.load(null, null);
				jsfiles.putAll(appWorkflowCanvas.publish(jsDefaultFileNode));
			}
			
			for (JSFileNode node : jsfiles.values()) {
				if (!node.createFile()){
					throw new Exception("脚本文件：" + node.filename + "发布失败！");
				}
			}
			
			if (!jsDefaultFileNode.createFile()){
				throw new Exception("当前节点：" + jsDefaultFileNode.command + "发布失败！");
			}
			
			EditorEnvironment.showMessage("发布成功完成！");
		} catch (Exception e1) {
			e1.printStackTrace();
			EditorEnvironment.showException(e1);
		}
	}
	
	public HashMap<String, JSFileNode> publish(JSDefaultFileNode defaultRoot) throws Exception{
	
		if (defaultRoot == null){
			throw new Exception("未发现缺省前台文件组件！");
		}
		
		defaultRoot.canvas = this;
		
		HashMap<String, JSFileNode> jsfiles = new HashMap<>();
		
		for (DrawNode tmp : nodes.values()) {
			if (!(tmp instanceof JSFileNode))
				continue;
			
			JSFileNode jsNode = (JSFileNode)tmp;
			jsfiles.put(jsNode.filename.trim().toLowerCase(), jsNode);
		}

		for (DrawNode tmp : nodes.values()) {
			if (!(tmp instanceof AppWorkflowNode.CommandNode))
				continue;
			
			CommandNode node = (CommandNode)tmp;
			if (node instanceof JSCommandNode){
				JSFileNode jsNode = (JSFileNode)nodeConnectToPrevType(node, JSFileNode.class);
				if (jsNode == null){
					defaultRoot.addCommand(node);
				}else
					jsNode.addCommand(node);
				
				continue;
			}
			
			if (node instanceof JSFileNode)
				continue;
			
			if (!node.createFile()){
				setSelected(node);
				throw new IOException("节点：" + node.name + "发布失败！");
			}
		}
	
		return jsfiles;
	}
	
	public AppWorkflowNode add(String title, Class<? extends AppWorkflowNode> c, Object userData, IDataSerializable dataSerializable){
		AppWorkflowNode node = (AppWorkflowNode)add(null, title, new Rectangle(0, 0, 100, 100), userData, new IDataSerializable() {
			
			@Override
			public String save(Object userData) {
				if (dataSerializable == null)
					return null;
				
				return dataSerializable.save(userData);
			}
			
			@Override
			public DrawNode newDrawNode(Object userdata) {
				try {
					AppWorkflowNode node = c.getConstructor(DrawCanvas.class).newInstance(AppWorkflowCanvas.this);
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

		if (node instanceof BeginNode || node instanceof EndNode){
			Rectangle rect = node.getRect();
			rect.height = 30;
		}
		return node;
	}
	
}
