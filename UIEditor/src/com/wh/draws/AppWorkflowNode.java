package com.wh.draws;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.control.EditorEnvironment;
import com.wh.draws.DrawCanvas.ICreateNodeSerializable;
import com.wh.system.tools.JsonHelp;
import com.wh.system.tools.TextStreamHelp;

public abstract class AppWorkflowNode extends DrawNode {

	protected Color bgColor = new Color(46, 46, 46);
	protected Color fontColor = new Color(250, 250, 250);
	
	public static final String BEGIN_NAME = "开始";
	public static final String END_NAME = "结束";
	public static final String ADAPTER_JS_NAME = "适配器(前端)";
	public static final String ADAPTER_PHP_NAME = "适配器(后端)";
	public static final String JS_NAME = "前端脚本";
	public static final String PHP_NAME = "服务端脚本";
	public static final String SUB_NAME = "子流程";
	public static final String JSFILE_NAME = "脚本文件";
	
	public static final String[] names = {BEGIN_NAME, END_NAME, JS_NAME, PHP_NAME, SUB_NAME, JSFILE_NAME};
	
	public static final AppWorkflowNode getInstance(AppWorkflowCanvas canvas, String name){
		switch(name){
		case BEGIN_NAME:
			return new BeginNode(canvas);
		case END_NAME:
			return new EndNode(canvas);
		case JS_NAME:
			return new JSCommandNode(canvas);
		case PHP_NAME:
			return new PhpCommandNode(canvas);
		case SUB_NAME:
			return new ChildAppWorkflowNode(canvas);
		case JSFILE_NAME:
			return new JSFileNode(canvas);
		}
		
		return null;
	}
	
	public AppWorkflowNode(DrawCanvas canvas) {
		super(canvas);
		
	}

	protected Color getColor() {
		return bgColor;
	}
	
	protected String getHeader(){
		return null;
	}
	
	protected Color getFontColor() {
		return fontColor;
	}
	
	protected void drawNode(Graphics g){
		Rectangle rect = super.getRect();
		
		String title = getHeader();
		int height = 40;
		int titleHeight = 30;
		if (title == null || title.isEmpty()){
			titleHeight = 0;
		}
		
		if (rect.height < (height + titleHeight))
			rect.height = height + titleHeight;
		else
			height = rect.height - titleHeight;
		
		int oldHeight = rect.height;

		Color color = getColor();
		g.setColor(color);				
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
		g.setColor(bgColor);
		g.drawRect(rect.x, rect.y, rect.width, rect.height);
		
		rect.height = height;
		drawText(g, font, getFontColor(), rect, new StringBuilder(this.title == null ? "" : this.title));
		if (titleHeight > 0 && title != null && !title.isEmpty()){
			Rectangle titleRect = new Rectangle(rect.x, rect.y + height, rect.width, titleHeight);
			g.setColor(bgColor);
			drawLine(g, titleRect.getLocation(), titleRect.width - 2);
			drawText(g, font, getFontColor(), titleRect, new StringBuilder(title == null ? "" : title));
		}
		rect.height = oldHeight;
	}
	
	public String toString(){
		String string = name;
		if (title != null && !title.isEmpty())
			string = name + "[" + title + "]";
		return string;
	}
	
	public static class BeginNode extends AppWorkflowNode{

		public BeginNode(DrawCanvas canvas) {
			super(canvas);
		}
		
	}

	public static class EndNode extends AppWorkflowNode{

		public EndNode(DrawCanvas canvas) {
			super(canvas);
		}
		
	}


	public static class ChildAppWorkflowNode extends AppWorkflowNode{

		public ChildAppWorkflowNode(DrawCanvas canvas) {
			super(canvas);
		}

		protected Color getColor() {
			return new Color(0, 74, 123);
		}
		
	}

	public static abstract class CommandNode extends AppWorkflowNode{

		public String memo;
		public String command;
		public String params;

		protected abstract File getSavePath() throws Exception;
		
		protected abstract String getExt();
		
		protected abstract String getText();
		
		protected String replaceText(String text) throws Exception{
			return text;
		}
		
		public File getFile() throws Exception{
			File path = getSavePath();
			if (path == null){
				throw new IOException(name + "节点未设置文件保存路径！");
			}
			
			if (!path.exists())
				if (!path.mkdirs()){
					throw new IOException(name + "节点建立目录：" + path.getAbsolutePath() + "失败！");
				}
			if (command == null || command.isEmpty()){
				throw new IOException(name + "节点未设置command！");
			}

			String ext = getExt();
			
			File file = new File(path, command + "." + ext);
			return file;
		}
		
		public String createText() throws Exception{
			String text = getText().trim();
			
			text = text.replace("%command%", (command == null ? "" : command));
			text = text.replace("%memo%", memo == null ? "" : memo);
			String paramsText = "参数列表\n";
			if (params != null && !params.isEmpty()){
				JSONArray paramJson = (JSONArray) JsonHelp.parseJson(params);
				for (int i = 0; i < paramJson.length(); i++) {
					JSONObject param = paramJson.getJSONObject(i);
					String id = param.getString("id");
					String type = param.getString("type");
					Object value = param.has("value") ? param.get("value") : null;
					String memo = "";
					if (param.has("memo"))
						memo = param.getString("memo");
					
					if (value != null ){
						if (type.compareToIgnoreCase("string") == 0 || type.compareToIgnoreCase("datetime") == 0){
							value = "'" + value.toString() + "'";
						}
					}
					paramsText += id + ":" + memo + "，[" + type + "，缺省值：" + (value == null ? "无" : value.toString()) + "]\n";
				}
			}
			text = text.replace("%params%", paramsText);
			text = replaceText(text);
			return text;
		}

		public boolean createFile() throws Exception {
			File file = getFile();
			if (file == null)
				return false;
			
			if (file.exists()){
				if (EditorEnvironment.showConfirmDialog(name + "节点设置的文件【" + file.getAbsolutePath() + "】已经存在，是否覆盖？", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return false;
			}
			
			String text = createText();
			
			TextStreamHelp.saveToFile(file, text);
			
			return true;
		}
		
		public void fromJson(JSONObject data, ICreateNodeSerializable createUserDataSerializable) throws JSONException{
			super.fromJson(data, createUserDataSerializable);
			if (data.has("memo"))
				memo = data.getString("memo");
			if (data.has("command"))
				command = data.getString("command");
			if (data.has("params"))
				params = data.getString("params");
		}
		
		public JSONObject toJson() throws JSONException{
			JSONObject data = super.toJson();
    		data.put("command", command == null ? "" : command);
    		data.put("memo", memo == null ? "" : memo);
    		data.put("params", params == null ? "" : params);
	    	return data;
		}
		
		public CommandNode(DrawCanvas canvas) {
			super(canvas);
		}

		public String toString(){
			String string = super.toString() + "(" + command + ")";
			return string;
		}
		
	}

	public static class PhpNode extends CommandNode{
		public PhpNode(DrawCanvas canvas) {
			super(canvas);
		}

		protected String getPhpFunctoin() throws IOException{
			return TextStreamHelp.loadFromFile(EditorEnvironment.getEditorSourcePath(EditorEnvironment.Codes_Dir_Name, "php.value.function.template"));
		}
		
		protected String replaceText(String text) throws Exception{
			if (params != null && !params.isEmpty()){
				JSONArray paramJson = (JSONArray) JsonHelp.parseJson(params);
				for (int i = 0; i < paramJson.length(); i++) {
					JSONObject param = paramJson.getJSONObject(i);
					String id = param.getString("id");
					String type = param.getString("type");
					Object value = param.has("value") ? param.get("value") : null;
					String memo = "";
					if (param.has("memo"))
						memo = param.getString("memo");
					
					if (value != null && value.toString().compareToIgnoreCase("null") != 0)
						if (type.compareToIgnoreCase("string") == 0 || type.compareToIgnoreCase("datetime") == 0){
							value = "'" + value.toString() + "'";
						}
					String funText = getPhpFunctoin();
					funText = funText.replace("%command%", command);
					funText = funText.replace("%id%", id);
					funText = funText.replace("%type%", type);
					funText = funText.replace("%value%", value.toString());
					funText = funText.replace("%memo%", "功能：获取" + id + "的值.\n值的类型：" + type + ".\n缺省值：" + (value == null ? "null" : value.toString()) + "。\n说明：" + memo + "\n");
					text += "\n" + funText;
				}
			}
			
			return text + "\n?>";
		}
		
		@Override
		protected Color getColor() {
			return new Color(232, 139, 12);
		}
		
		@Override
		protected File getSavePath() throws Exception {
			return EditorEnvironment.getPublishWebFile(EditorEnvironment.Service_Dir_Path, EditorEnvironment.User_PHP_Dir_Path);
		}

		@Override
		protected String getExt() {
			return "php";
		}

		@Override
		protected String getText() {
			try {
				return TextStreamHelp.loadFromFile(EditorEnvironment.getEditorSourcePath(EditorEnvironment.Codes_Dir_Name, "php.template"));
			} catch (IOException e) {
				EditorEnvironment.showException(e);
				return null;
			}
		}

	}

	public static class PhpCommandNode extends PhpNode{

		public PhpCommandNode(DrawCanvas canvas) {
			super(canvas);
		}
		
		protected String getHeader(){
			return "PHP任务";
		}
		
	}

	public static class JSDefaultFileNode extends CommandNode{
		JSFileNode fileNode = new JSFileNode(null);
		public JSDefaultFileNode(DrawCanvas canvas) {
			super(canvas);
		}
		
		@Override
		protected String getText() {
			try {
				return TextStreamHelp.loadFromFile(EditorEnvironment.getEditorSourcePath(EditorEnvironment.Codes_Dir_Name, "js.default.template"));
			} catch (IOException e) {
				EditorEnvironment.showException(e);
				return null;
			}
		}

		@Override
		protected File getSavePath() throws Exception {
			return EditorEnvironment.getProjectJSPath();
		}

		@Override
		protected String getExt() {
			return "js";
		}

		@Override
		protected String replaceText(String text) throws Exception{
			fileNode.filename = command;
			fileNode.canvas = canvas;
			String funText = fileNode.getText();
			funText = fileNode.replaceText(funText);
			return text.replace("%object%", funText);
			
		}

		public void addCommand(CommandNode node) {
			fileNode.addCommand(node);
		}
	}
	
	public static class JSFileNode extends CommandNode{
		public JSFileNode(DrawCanvas canvas) {
			super(canvas);
		}

		List<CommandNode> commands = new ArrayList<CommandNode>();
		public void addCommand(CommandNode node){
			commands.add(node);
		}
		
		public void clearCommand(){
			commands.clear();
		}
		
		@Override
		protected String getHeader(){
			return "JS库";
		}
		
		@Override
		public File getFile() throws Exception{
			File path = getSavePath();
			if (path == null){
				throw new IOException(name + "节点未设置文件保存路径！");
			}
			
			if (!path.exists())
				if (!path.mkdirs()){
					throw new IOException(name + "节点建立目录：" + path.getAbsolutePath() + "失败！");
				}
			
			if (filename == null || filename.isEmpty()){
				throw new IOException(name + "节点未设置filename！");
			}

			String ext = getExt();
			
			File file = new File(path, filename + "." + ext);
			return file;
		}
		
		@Override
		protected File getSavePath() throws Exception {
			return EditorEnvironment.getProjectJSPath();
		}

		@Override
		protected String getExt() {
			return "js";
		}

		protected String replaceText(String text) throws Exception{
			text = text.replace("%memo%", memo == null ? "" : memo);
			text = text.replace("%objname%", filename);
			String funTexts = "";
			for (CommandNode node : commands) {
				String funText = node.getText();
				if (funText == null || funText.isEmpty())
					continue;
				
				funTexts += funText + "\n";
			
			}
			return text.replace("%functions%", funTexts);
		}
		
		@Override
		protected String getText() {
			try {
				return TextStreamHelp.loadFromFile(EditorEnvironment.getEditorSourcePath(EditorEnvironment.Codes_Dir_Name, "js.template"));
			} catch (IOException e) {
				EditorEnvironment.showException(e);
				return null;
			}
		}

		public void fromJson(JSONObject data, ICreateNodeSerializable createUserDataSerializable) throws JSONException{
			super.fromJson(data, createUserDataSerializable);
			if (data.has("filename"))
				filename = data.getString("filename");
			if (data.has("needCreate"))
				needCreate = data.getBoolean("needCreate");
		}
		
		public JSONObject toJson() throws JSONException{
			JSONObject data = super.toJson();
			data.remove("command");
			data.remove("params");
    		data.put("filename", filename == null ? "" : filename);
    		data.put("needCreate", needCreate);
	    	return data;
		}
		
		public String filename;
		public boolean needCreate = false;
	}

	public abstract static class JSFunctionNode extends CommandNode{
		
		public JSFunctionNode(DrawCanvas canvas) {
			super(canvas);
		}

		@Override
		protected Color getColor(){
			return new Color(8, 131, 85);
		}
		
		@Override
		protected String getExt() {
			return "js";
		}

		@Override
		protected String getText() {
			try {
				if (command == null || command.isEmpty())
					return "";
				
				String text = TextStreamHelp.loadFromFile(EditorEnvironment.getEditorSourcePath(EditorEnvironment.Codes_Dir_Name, "jsfunction.template"));
				text = text.replace("%memo%", memo == null ? "" : memo);
				text = text.replace("%name%", command);
				String funText = "";
				String memos = "";
				if (params != null && !params.isEmpty()){
					JSONArray paramJson = (JSONArray) JsonHelp.parseJson(params);
					for (int i = 0; i < paramJson.length(); i++) {
						JSONObject param = paramJson.getJSONObject(i);
						String id = param.getString("id");
						String type = param.getString("type");
						Object value = param.get("value");
						String memo = "";
						if (param.has("memo"))
							memo = param.getString("memo");
						
						if (!memo.isEmpty()){
							memos += id + ":" + memo + "\n";
						}
						if (type.compareToIgnoreCase("string") == 0 || type.compareToIgnoreCase("datetime") == 0 && value.toString().compareTo("null") != 0){
							value = "'" + value.toString() + "'";
						}

						String tmp = id + ((value == null || (value instanceof String && ((String)value).isEmpty())) ? "" : ("=" + value.toString()));
						if (funText.isEmpty())
							funText = tmp;
						else
							funText += "," + tmp;
						
					}
					
				}
				text = text.replace("%paramsmemo%", memos);
				text = text.replace("%paramdefines%", funText);
				text = text.replace("%code%", code == null ? "" : code);
				return text;
			} catch (Exception e) {
				EditorEnvironment.showException(e);
				return null;
			}
		}
		
		public void fromJson(JSONObject data, ICreateNodeSerializable createUserDataSerializable) throws JSONException{
			super.fromJson(data, createUserDataSerializable);
			if (data.has("code"))
				code = data.getString("code");
		}
		
		public JSONObject toJson() throws JSONException{
			JSONObject data = super.toJson();
    		data.put("code", code);
	    	return data;
		}
		
		public String code;

	}

	public static class JSCommandNode extends JSFunctionNode{

		public JSCommandNode(DrawCanvas canvas) {
			super(canvas);
		}
		
		@Override
		protected String getHeader(){
			return "JS函数";
		}
		
		@Override
		public boolean createFile() throws Exception {
			throw new Exception("脚本对象不支持此方法！");
		}
		
		@Override
		protected File getSavePath() throws Exception {
			 if (prevs.size() > 0){
				 for (String id : prevs) {
					if (canvas.getNode(id) instanceof JSFileNode){
						JSFileNode fileNode = (JSFileNode)canvas.getNode(id);
						return fileNode.getSavePath();
					}
				}
			 }
			 JSDefaultFileNode node = new JSDefaultFileNode(canvas);
			 return node.getSavePath();
		}

	}

}
