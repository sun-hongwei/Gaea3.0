package com.wh.control;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.control.EditorEnvironment.RegionName;
import com.wh.control.ExportToWord.LevelManager.LevelType;
import com.wh.dialog.WaitDialog;
import com.wh.dialog.WaitDialog.IProcess;
import com.wh.draws.AppWorkflowCanvas;
import com.wh.draws.AppWorkflowNode;
import com.wh.draws.AppWorkflowNode.JSCommandNode;
import com.wh.draws.DrawCanvas;
import com.wh.draws.DrawNode;
import com.wh.draws.FlowCanvas;
import com.wh.draws.UICanvas;
import com.wh.draws.UINode;
import com.wh.draws.WorkflowCanvas;
import com.wh.draws.WorkflowNode;
import com.wh.draws.WorkflowNode.BeginNode;
import com.wh.draws.WorkflowNode.ChildWorkflowNode;
import com.wh.draws.WorkflowNode.EndNode;
import com.wh.draws.drawinfo.DrawInfo;
import com.wh.draws.drawinfo.SubUIInfo;
import com.wh.form.SceneBuilder;
import com.wh.form.SceneBuilder.NodeType;
import com.wh.form.SceneBuilder.SceneInfo;
import com.wh.system.tools.JsonHelp;
import com.wh.system.tools.WordDocument;
import com.wh.system.tools.WordDocument.HeaderInfo;
import com.wh.system.tools.WordDocument.Info;

public class ExportToWord {

	public static final Font subTitleFont = new Font("微软雅黑", 0, 24);
	
	protected static void exportUI(LevelManager levelManager, WordDocument document, String title, File uiFile, HashMap<String, DrawNode> uiKeyWorkflowNodes, 
			boolean exportUI, boolean exportAppflow) throws Exception{
		UICanvas canvas = new UICanvas();
		canvas.setFile(uiFile);
		canvas.load(null, null);
		
		List<DrawNode> nodes = canvas.getNodes();
		if (nodes == null || nodes.size() == 0)
			return;
		
		JSONArray data = null;
		if (exportAppflow){
			if (canvas.appWorkflowid != null && !canvas.appWorkflowid.isEmpty()){
				data = (JSONArray) JsonHelp.parseJson(canvas.appWorkflowid);
				if (data.length() == 0)
					data = null;
			}
		}
		if (exportUI || data != null && title != null && !title.isEmpty()){
			document.addParagraph(levelManager.getPrefix(LevelType.ltUI, 1) + " " + title);
			String memo = canvas.getPageConfig().memo;
			if (memo != null && !memo.isEmpty()){
				document.addNewLine();
				document.addParagraph(memo);
			}
			
			document.addNewLine();
			if (exportUI)
				addImage(document, canvas);
		}

		if (data != null){
			for (int i = 0; i < data.length(); i++) {
				String name = data.getString(i);
				String filename = EditorEnvironment.getAppWorkflow_FileName(name);
				File file = EditorEnvironment.getProjectFile(EditorEnvironment.AppWorkflow_Dir_Name, filename);
				AppWorkflowCanvas appCanvas = new AppWorkflowCanvas();
				appCanvas.setFile(file);
				appCanvas.load(null, null);
//					appCanvas.fixNodesInPage();
				
				if (appCanvas.nodes.size() == 0)
					continue;
				
				String text = appCanvas.getPageConfig().title;
				text = text == null || text.isEmpty() ? name : text;
				document.addParagraph(levelManager.getPrefix(LevelType.ltUI, 1) + " " +  text);
				String memo = appCanvas.getPageConfig().memo;
				if (memo != null && !memo.isEmpty()){
					document.addNewLine();
					document.addParagraph(memo);
				}
				addImage(document, appCanvas);

				for (DrawNode drawNode : appCanvas.nodes.values()) {
					AppWorkflowNode node = (AppWorkflowNode)drawNode;
					if (node instanceof AppWorkflowNode.BeginNode || node instanceof AppWorkflowNode.EndNode)
						continue;
					
					String command = "";
					memo = "";
					List<JSONObject> params = new ArrayList<>();
					if (node instanceof JSCommandNode){
						command = ((JSCommandNode)node).command;
						memo = ((JSCommandNode)node).memo;
						String tmp = ((JSCommandNode)node).params;
						if (tmp != null && !tmp.isEmpty()){
							try {
								JSONArray json = (JSONArray) JsonHelp.parseJson(tmp);
								for (int j = 0; j < json.length(); j++) {
									JSONObject paramJson = json.getJSONObject(j);
									params.add(paramJson);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
							
						}
					}
					
					if (command != null && !command.isEmpty()){
						document.addParagraph(levelManager.getPrefix(LevelType.ltUI, 2) + " " +  command);
						if (memo != null && !memo.isEmpty()){
							document.addParagraph(memo);
							document.addNewLine();
						}
						document.addParagraph("参数说明");
						if (params.size() == 0){
							document.addParagraph("无");
						}else{
							for (JSONObject jsonObject : params) {
								String id = jsonObject.getString("id");
								String typename = jsonObject.getString("type");
								String idvalue = "";
								String idmemo = "";							
								if (jsonObject.has("value"))
									idvalue = jsonObject.getString("value");
								if (jsonObject.has("memo"))
									idmemo = jsonObject.getString("memo");							
								document.addParagraph("字段名称：" + id);
								if (idmemo != null && !idmemo.isEmpty()){
									document.addParagraph("说明：" + idmemo);
								}
								document.addParagraph("字段类型：" + typename);
								document.addParagraph("默认值：" + idvalue);
								document.addNewLine();
							}
						}
					}
				}
			}
		}
		
		for (DrawNode tmp: nodes) {
			UINode node = (UINode)tmp;
			if (node.getDrawInfo() instanceof SubUIInfo){
				SubUIInfo info = (SubUIInfo)node.getDrawInfo();
				if (info.uiInfo != null && !info.uiInfo.isEmpty()){
					WorkflowNode wNode = null;
					if (uiKeyWorkflowNodes != null){
						if (!uiKeyWorkflowNodes.containsKey(info.uiInfo)){
							wNode = EditorEnvironment.getModelNodeFromUI(info.uiInfo);
							uiKeyWorkflowNodes.put(info.uiInfo, wNode == null ? node : wNode);
						}
					}
				}
			}
		}
		
		
	}

	public static void exportUI(LevelManager levelManager, WordDocument document, String title, DrawNode workflowNode,  
			HashMap<String, DrawNode> uiKeyWorkflowNodes, boolean exportUI, boolean exportAppflow) throws Exception{
		
		File uiFile = EditorEnvironment.getUIFile(workflowNode.id, false);
		exportUI(levelManager, document, title, uiFile, uiKeyWorkflowNodes, exportUI, exportAppflow);
	}
	
	protected static void addImage(WordDocument document, DrawCanvas canvas) throws Exception {
		BufferedImage image = canvas.saveToImage();
		if (image == null)
			return;
		
		int width = image.getWidth();
		int height = image.getHeight();
		float div = (float)width / height;
		int maxWidth = 1000;
		int maxHeight = 1000;
		while (width > maxWidth || height > maxHeight){
			if (width > height){
				if (width > maxWidth){
					width -= 10;
					height = (int) (width / div);
				}else{
					height -= 10;
					width = (int) (height * div);
				}
			}else{
				if (height > maxHeight){
					height -= 10;
					width = (int) (height * div);
				}else{
					width -= 10;
					height = (int) (width / div);
				}
			}
		}
		
		document.addImage(image, canvas.getPageConfig().name, new Dimension(400, 400));
		document.addSpacePage();
	}

	public static void exportUI(LevelManager levelManager, WordDocument document, File workflowRelationFile, HashMap<String, DrawNode> uiKeysWorkflowNodes, 
			WorkflowNode selectNode, boolean exportUI, boolean exportAppflow) throws Exception{
		
		if (workflowRelationFile == null)
			return;

		DrawCanvas canvas = new WorkflowCanvas();
		canvas.setFile(workflowRelationFile);
		canvas.load(new EditorEnvironment.WorkflowDeserializable(), null);

		boolean hasSelectNode = false;
		if (selectNode != null){
			for (DrawNode tmp : canvas.nodes.values()) {
				if (tmp.id.compareTo(selectNode.id) == 0){
					hasSelectNode = true;
					break;
				}
			}			
		}
		
		for (DrawNode tmp : canvas.nodes.values()) {
			WorkflowNode node = (WorkflowNode)tmp;
			if (node instanceof BeginNode){
				continue;
			}
			if (node instanceof EndNode){
				continue;
			}
			
			if (selectNode != null){
				if (hasSelectNode){
					if (node.id.compareTo(selectNode.id) != 0)
						continue;
				}else{
					if (!(node instanceof ChildWorkflowNode))
						continue;
				}
			}
			if (node  instanceof ChildWorkflowNode){
				File subFile = EditorEnvironment.getChildModelRelationFile(node.id, false);
				exportUI(levelManager, document, subFile, uiKeysWorkflowNodes, selectNode, exportUI, exportAppflow);
				continue;
			}
			
			if (node instanceof WorkflowNode){
				exportUI(levelManager, document, node.title, node, uiKeysWorkflowNodes, exportUI, exportAppflow);
			}
		}
		
	}
	
	public static void exportScene(LevelManager levelManager, 
			WordDocument document, boolean exportWorkflow, int level, SceneInfo info) throws Exception{
		
		if (info.nodeType != NodeType.ntDirNode){
			List<String> columns = new ArrayList<>();
			columns.add("进入条件");
			columns.add("操作过程");
			columns.add("不变式");
			columns.add("输出");
			columns.add("备注");
			columns.add("完成时间");
			columns.add("命名空间");
			
			document.addParagraph(info.data.get(SceneBuilder.title));
			if (exportWorkflow && info.data.containsKey(SceneBuilder.flow)){
				File flowFile = EditorEnvironment.getFlowRelationFile(info.data.get(SceneBuilder.flow));
				if (flowFile != null && flowFile.exists()){
					FlowCanvas flowCanvas = new FlowCanvas();
					flowCanvas.setFile(flowFile);
					flowCanvas.load(null, null);				
					if (flowCanvas != null && flowCanvas.nodes.size() > 0){
						document.addParagraph("关联业务流程图");
						document.addNewLine();
						addImage(document, flowCanvas);
					} 
				}
			}
			
			HashMap<String, Object> row = new HashMap<>();
			row.put("进入条件", info.data.get(SceneBuilder.input));
			row.put("操作过程", info.data.get(SceneBuilder.step));
			row.put("不变式", info.data.get(SceneBuilder.invariance));
			row.put("输出", info.data.get(SceneBuilder.output));
			row.put("备注", info.data.get(SceneBuilder.memo));
			row.put("完成时间", info.data.get(SceneBuilder.endtime));
			row.put("命名空间", info.data.get(SceneBuilder.scenens));
			document.addTable(columns, row, new float[]{0.15F, 0.3F, 0.1F, 0.1F, 0.15F, 0.1F, 0.1F}, true);
			document.addNewLine();
		}else
			document.addParagraph(levelManager.getPrefix(LevelType.ltSceneDir, level) + " " + info.data.get(SceneBuilder.title));

		if (info.nodeType == SceneBuilder.NodeType.ntDirNode){
			for (SceneInfo child : info.childs) {
				exportScene(levelManager, document, exportWorkflow, level + 1, child);
			}
		}
	}
	
	public static void exportScenes(LevelManager levelManager, 
			WordDocument document, boolean exportWorkflow, int level, SceneInfo info) throws Exception{
		document.addParagraph(levelManager.getPrefix(LevelType.ltModel, level) + " 场景");
		for (SceneInfo child : info.childs) {
			exportScene(levelManager, document, exportWorkflow, level + 1, child);
		}
	}
	
	public static void exportModel(LevelManager levelManager, WordDocument document, String title, File workflowRelationFile, HashMap<String, DrawNode> uiKeysWorkflowNodes,
			WorkflowNode selectNode, boolean exportModelflow, boolean exportModelNode, boolean exportWorkflow, boolean exportScene, int level) throws Exception{
		if (workflowRelationFile == null)
			return;

		DrawCanvas canvas = new WorkflowCanvas();
		canvas.setFile(workflowRelationFile);
		canvas.load(new EditorEnvironment.WorkflowDeserializable(), null);

		if (canvas.nodes.size() == 0)
			return;
		
		if (exportModelflow){
			document.addParagraph(levelManager.getPrefix(LevelType.ltModel, level) + " " + title);
			String memo = canvas.getPageConfig().memo;
			if (memo != null && !memo.isEmpty()){
				document.addNewLine();
				document.addParagraph(memo);
			}
			if (exportModelflow)
				addImage(document, canvas);
		}

		List<ChildWorkflowNode> childWorkflowNodes = new ArrayList<>();
		List<WorkflowNode> nodes = new ArrayList<>();

		boolean hasSelectNode = false;
		for (DrawNode tmp : canvas.nodes.values()) {
			WorkflowNode node = (WorkflowNode)tmp;
			if (node instanceof BeginNode){
				continue;
			}
			if (node instanceof EndNode){
				continue;
			}
			
			if (selectNode != null && !hasSelectNode){
				hasSelectNode = selectNode.id.compareTo(node.id) == 0;
			}
			
			if (node  instanceof ChildWorkflowNode){
				childWorkflowNodes.add((ChildWorkflowNode)node);
			}			
			
			if (node instanceof WorkflowNode){
				nodes.add((WorkflowNode)node);
			}
		}
		
		if (selectNode == null || (selectNode != null && hasSelectNode)){
			for (WorkflowNode node : nodes) {
				if (selectNode != null && node.id.compareTo(selectNode.id) != 0)
					continue;
				
				SceneInfo info = null;
				if (exportScene){
					info = SceneBuilder.parseJson(node.id);
				}
	
				if (info != null){
					exportScenes(levelManager, document, exportWorkflow, level, info);
				}
			}
		}
		
		if (selectNode == null || (selectNode != null && selectNode instanceof ChildWorkflowNode)){
			for (ChildWorkflowNode node : childWorkflowNodes) {
				if (node  instanceof ChildWorkflowNode){
					WorkflowNode curNode = selectNode;
					if (selectNode != null){
						if (node.id.compareTo(selectNode.id) != 0)
							continue;
						curNode = null;
					}
					File subFile = EditorEnvironment.getChildModelRelationFile(node.id, false);
					exportModel(levelManager, document, node.title, subFile, uiKeysWorkflowNodes, curNode, exportModelflow, exportModelNode, exportWorkflow, exportScene, level + 1);
				}			
			}
		}
	}
	
	static class LevelManager{
		boolean exportModelflow, exportModelNode, exportWorkflow, exportScene, exportUI, exportAppflow, exportRunflow;
		public enum LevelType{
			ltModel, ltUI, ltSceneDir, ltSceneItem
		}
		
		public LevelManager(boolean exportModelflow, boolean exportModelNode, boolean exportWorkflow, boolean exportScene, boolean exportUI, boolean exportAppflow, boolean exportRunflow){
			this.exportModelflow = exportModelflow;
			this.exportModelNode = exportModelNode;
			this.exportWorkflow = exportWorkflow;
			this.exportScene = exportScene;
			this.exportUI = exportUI;
			this.exportAppflow = exportAppflow;
			this.exportRunflow = exportRunflow;
		}
		
		protected String splitChar = ".";
		
		HashMap<LevelType, HashMap<Integer, Integer>> subs = new HashMap<>();
		
		protected String addPrefix(int prefix, LevelType lt, int subLevel) {
			HashMap<Integer, Integer> sub = null;
			if (subs.containsKey(lt)){
				sub = subs.get(lt);
			}else{
				sub = new HashMap<>();
				subs.put(lt, sub);
			}
			
			boolean needAdd = true;
			for(int i = 0; i < subLevel + 1; i++){
				if (!sub.containsKey(i)){
					sub.put(i, 1);
					needAdd = false;
				}
			}
			
			if (needAdd){
				int index = sub.get(subLevel);
				sub.put(subLevel, index + 1);
			}
			String text = String.valueOf(prefix);
			for (int i = 0; i < subLevel + 1; i++) {
				text += splitChar + String.valueOf(sub.get(i));
			}
			
			return text;
		}

		public String getPrefix(LevelType lt, int subLevel){
			
			int prefix = 0;
			while (true){
				if (exportModelflow)
					prefix++;
				if (lt == LevelType.ltModel)
					break;

				if (exportUI)
					prefix++;
				if (lt == LevelType.ltUI)
					break;
				
				if (exportAppflow)
					prefix++;
				if (lt == LevelType.ltSceneDir){
					break;
				}
				
				if (exportAppflow)
					prefix++;
				if (lt == LevelType.ltSceneItem){
					break;
				}
				break;
			}
			
			if (prefix == 0)
				prefix = 1;
			
			if (subLevel < 0)
				return String.valueOf(prefix);
			
			return addPrefix(prefix, lt, subLevel);
			
		}
	}
	
	public static void syncExport(String workflowRelationName, String title, HeaderInfo[] headers, HeaderInfo[] footers, File saveFile, 
			WorkflowNode selectNode, 
			boolean exportModelflow, boolean exportModelNode, boolean exportRunflow, boolean exportWorkflow, 
			boolean exportScene, boolean exportUI, boolean exportAppflow, boolean exportNavUI,
			Font headerFont, Font footerFont) throws Exception {
		
		LevelManager levelManager = new LevelManager(exportModelflow, exportModelNode, exportWorkflow, exportScene, exportUI, exportAppflow, exportRunflow);
		
		WordDocument document = new WordDocument();
		document.setFile(saveFile);
		Info info = document.addTitle(title);
		document.setFont(info, new Font("微软雅黑", 0, 28));
		document.setAlign(info, ParagraphAlignment.CENTER);
		
		if (headers != null)
			document.addHeader(headers, headerFont);
		
		if (footers != null)
			document.addFooter(footers, footerFont);
		
		HashMap<String, DrawNode> uiKeysWorkflowNodes = new HashMap<>();
		File mainWorkflowRelationFile = EditorEnvironment.getProjectFile(EditorEnvironment.Workflow_Dir_Name, 
				EditorEnvironment.getRelationFileName(EditorEnvironment.Main_Workflow_Relation_FileName));
		
		if (workflowRelationName != null && !workflowRelationName.isEmpty() && EditorEnvironment.Main_Workflow_Relation_FileName.compareToIgnoreCase(workflowRelationName) != 0){
			mainWorkflowRelationFile = EditorEnvironment.getProjectFile(EditorEnvironment.Workflow_Dir_Name, 
					EditorEnvironment.getNodeFileName(workflowRelationName));
		}
		
		if (mainWorkflowRelationFile == null || !mainWorkflowRelationFile.exists()){
			EditorEnvironment.showMessage(null, "未发现名称：" + workflowRelationName + "的模块关系文件，请检查后重试！", "导出", JOptionPane.WARNING_MESSAGE);
			document.close();
			return;
		}
		
		boolean hasModelflow = exportModelflow || exportModelNode; 
		if (hasModelflow){
			document.addParagraph(levelManager.getPrefix(LevelType.ltModel, -1) + " 模块设计");
			document.addNewLine();
		}
		exportModel(levelManager, document, "模块关系图", mainWorkflowRelationFile, uiKeysWorkflowNodes, selectNode, exportModelflow, exportModelNode, exportWorkflow, exportScene, 0);
		
		if (exportUI){
			if (hasModelflow)
				document.addNewLine();
			
			document.addParagraph(levelManager.getPrefix(LevelType.ltUI, -1) + " 界面设计");
			document.addNewLine();
			document.addParagraph(levelManager.getPrefix(LevelType.ltUI, 0) + " 业务界面");
			document.addNewLine();
		}
		exportUI(levelManager, document, mainWorkflowRelationFile, uiKeysWorkflowNodes, selectNode, exportUI, exportAppflow);
		for (String uiid : uiKeysWorkflowNodes.keySet()) {
			DrawNode node = (DrawNode)uiKeysWorkflowNodes.get(uiid);
			if (node instanceof UINode){
				File file = EditorEnvironment.getUIFileForUIID(uiid);
				DrawInfo drawInfo = ((UINode)node).getDrawInfo();
				String caption = drawInfo.title;
				exportUI(levelManager, document, caption, file, uiKeysWorkflowNodes, exportUI, exportAppflow);
			}
		}

		if (exportUI && exportNavUI){
			document.addParagraph(levelManager.getPrefix(LevelType.ltUI, 0) + " 导航界面");
			document.addNewLine();
			exportUI(levelManager, document, "页头", EditorEnvironment.getFrameModelNode(RegionName.rnTop), uiKeysWorkflowNodes, exportUI, exportAppflow);
			exportUI(levelManager, document, "页脚", EditorEnvironment.getFrameModelNode(RegionName.rnBottom), uiKeysWorkflowNodes, exportUI, exportAppflow);
			exportUI(levelManager, document, "左部", EditorEnvironment.getFrameModelNode(RegionName.rnLeft), uiKeysWorkflowNodes, exportUI, exportAppflow);
			exportUI(levelManager, document, "右部", EditorEnvironment.getFrameModelNode(RegionName.rnRight), uiKeysWorkflowNodes, exportUI, exportAppflow);
		}
		
		document.save();
	}

	public static void asyncExport(String workflowRelationName, String title, HeaderInfo[] headers, HeaderInfo[] footers, 
			File saveFile, WorkflowNode selectNode, 
			boolean exportModelflow, boolean exportModelNode, boolean exportRunflow, boolean exportWorkflow, 
			boolean exportScene, boolean exportUI, boolean exportAppflow, boolean exportNavUI,
			Font headerFont, Font footerFont, final boolean needOpenDoc){
		
		WaitDialog.Show("导出Word文档", "正在导出，请等待。。。", new IProcess() {
			
			@Override
			public boolean doProc(WaitDialog waitDialog) {
				try {
					syncExport(workflowRelationName, title, headers, footers, saveFile, selectNode, 
							exportModelflow, exportModelNode, exportRunflow, exportWorkflow, exportScene, exportUI, exportAppflow, exportNavUI,
							headerFont, footerFont);
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					EditorEnvironment.showException(e);
					return false;
				}
			}
			
			@Override
			public void closed(boolean isok) {
				if (isok && needOpenDoc){
					EditorEnvironment.showMessage("成功导出文档，关闭自动打开文档！");
					try {
						Desktop.getDesktop().open(saveFile);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}, null);
	}
}
