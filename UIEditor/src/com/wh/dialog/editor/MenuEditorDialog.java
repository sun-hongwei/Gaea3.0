package com.wh.dialog.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.control.EditorEnvironment;
import com.wh.dialog.editor.JsonTreeDataEditor.ChangeObjectType;
import com.wh.dialog.editor.JsonTreeDataEditor.ChangeType;
import com.wh.dialog.editor.JsonTreeDataEditor.IChange;
import com.wh.dialog.editor.JsonTreeDataEditor.TreeItemInfo;
import com.wh.dialog.editor.ModelflowSelectDialog.Result;
import com.wh.form.IMainControl;
import com.wh.system.tools.JsonHelp;
import java.awt.Font;
public class MenuEditorDialog extends JDialog{
	private static final long serialVersionUID = 4251405285974410412L;
	private JsonTreeDataEditor jsonTreeDataEditor;
	private JMenuBar menuBar;

	/**
	 * Create the dialog.
	 */
	
	IMainControl mainControl;
	boolean isEdit = false;
	
	protected void onClose() {
		if (isEdit){
			int ret = EditorEnvironment.showConfirmDialog("数据已经修改，是否退出", "退出", JOptionPane.YES_NO_CANCEL_OPTION);
			switch(ret){
			case JOptionPane.YES_OPTION:
				try {
					save();
					dispose();
				} catch (Exception e) {
					e.printStackTrace();
					EditorEnvironment.showException(e);
				}
				break;
			case JOptionPane.NO_OPTION:
				dispose();
				break;
			}
		}else
			dispose();
	}
	
	public MenuEditorDialog(IMainControl mainControl) {
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e) {
				onClose();
			}
		});
		setType(Type.UTILITY);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setTitle("主菜单编辑");
		setIconImage(Toolkit.getDefaultToolkit().getImage(MenuEditorDialog.class.getResource("/image/browser.png")));
		this.mainControl = mainControl;
		setBounds(100, 100, 1054, 725);
		getContentPane().setLayout(new BorderLayout());
		
		JPanel buttonPane = new JPanel();
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		buttonPane.setLayout(new BorderLayout(0, 0));
		jsonTreeDataEditor = new JsonTreeDataEditor(new IChange() {
			
			@Override
			public void onChange(Object data) {
				isEdit = true;
			}
		});
		jsonTreeDataEditor.table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		jsonTreeDataEditor.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jsonTreeDataEditor.table.setAutoCreateRowSorter(true);
		jsonTreeDataEditor.onDataNotify = new JsonTreeDataEditor.IDataNotify() {
			
			@Override
			public void onChange(ChangeObjectType ot, ChangeType ct) {
				isEdit = true;
			}
		};
		
		getContentPane().add(jsonTreeDataEditor, BorderLayout.CENTER);
		JPanel panel = new JPanel();
		buttonPane.add(panel, BorderLayout.CENTER);
		JButton button = new JButton("刷新菜单");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					refreshMenu();
				} catch (JSONException e1) {
					e1.printStackTrace();
					EditorEnvironment.showMessage(null, "刷新失败！", "刷新", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		panel.add(button);
		
		JButton button_1 = new JButton("关联节点");
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if (jsonTreeDataEditor.tree.getSelectionPath() == null || jsonTreeDataEditor.tree.getSelectionPath().getLastPathComponent() == null){
					EditorEnvironment.showMessage(null, "请先选择一个节点", "提示", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				
				DefaultMutableTreeNode node= (DefaultMutableTreeNode) jsonTreeDataEditor.tree.getSelectionPath().getLastPathComponent();
				
				if (!node.isLeaf()){
					EditorEnvironment.showMessage(null, "只有无子节点的节点才可以关联！", "提示", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				
				TreeItemInfo info = (TreeItemInfo)node.getUserObject();
				
				try {
					String name = null;
					if (info.data.has(JsonTreeDataEditor.jumpid))
						name = info.data.getString(JsonTreeDataEditor.jumpid);
					Result result = ModelflowSelectDialog.showDialog(MenuEditorDialog.this.mainControl, null, name);
					if (result == null){
						return;
					}
					info.data.put(JsonTreeDataEditor.jumpid, result.name);
					info.data.put(JsonTreeDataEditor.name, result.title);
					jsonTreeDataEditor.refreshGrid();
					isEdit = true;
					jsonTreeDataEditor.tree.updateUI();
				} catch (JSONException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
			}
		});
		panel.add(button_1);
		
		JButton button_2 = new JButton("切换类型");
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (jsonTreeDataEditor.tree.getSelectionPath() == null || jsonTreeDataEditor.tree.getSelectionPath().getLastPathComponent() == null){
					EditorEnvironment.showMessage(null, "请先选择一个节点", "提示", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				
				DefaultMutableTreeNode node= (DefaultMutableTreeNode) jsonTreeDataEditor.tree.getSelectionPath().getLastPathComponent();
				
				if (!node.isLeaf()){
					EditorEnvironment.showMessage(null, "只有叶子节点才可以关联！", "提示", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				
				TreeItemInfo info = (TreeItemInfo)node.getUserObject();
				try {
					if (info.data.has("type") && info.data.getString("type").compareTo("separator") == 0){
						info.data.remove("type");
						info.data.put(JsonTreeDataEditor.name, "请输入新的名称");
					}else{
						info.data.put("type", "separator");
						info.data.put(JsonTreeDataEditor.name, "-");
					}
					isEdit = true;
					jsonTreeDataEditor.tree.updateUI();
				} catch (JSONException ex) {
					ex.printStackTrace();
				}

			}
		});
		panel.add(button_2);
				
		menuBar = new JMenuBar();
		menuBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setJMenuBar(menuBar);
		
		setLocationRelativeTo(null);
		load();
	}

	public static File getMainMenuFile(){
		return EditorEnvironment.getProjectFile(EditorEnvironment.Menu_Dir_Path, EditorEnvironment.getMenu_FileName(EditorEnvironment.Main_Menu_FileName));
	}
	
	File menuFile = null;
	public File getFile(){
		if (menuFile != null)
			return menuFile;
		else
			return getMainMenuFile();
	}
	
	public void setFile(File file){
		menuFile = file;
	}
	
	public void refresh(){
		File file = getFile();
		if (file.exists()){
			JSONArray obj;
			try {
				obj = (JSONArray) JsonHelp.parseJson(file, null);
				jsonTreeDataEditor.setValue(obj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected Component addMenu(JSONObject data, JMenuItem parentNode, TreeMap<String, List<JSONObject>> all) throws JSONException{
		Component node = null;
		String text = null;
		if (data.has(JsonTreeDataEditor.name) && !data.getString(JsonTreeDataEditor.name).isEmpty()){
			text = data.getString(JsonTreeDataEditor.name);
		}else if (data.has(JsonTreeDataEditor.type)){
			if (data.getString(JsonTreeDataEditor.type).compareTo("separator") == 0)
				text = "-";
		}
		
		if (text.compareTo("-") == 0){
			if (parentNode != null){
				((JMenu)parentNode).addSeparator();
			}
		}else{			
			String id = data.getString(JsonTreeDataEditor.id);
			String pid = null;
			if (data.has(JsonTreeDataEditor.parent))
				pid = data.getString(JsonTreeDataEditor.parent);
			if (all.containsKey(id) || pid == null || pid.isEmpty())
				node = new JMenu(text);
			else
				node = new JMenuItem(text);
			
			if (parentNode == null){
				menuBar.add((JMenu) node);
			}else{
				parentNode.add(node);
			}
		}
		
		if (!data.has(JsonTreeDataEditor.id))
			data.put(JsonTreeDataEditor.id, UUID.randomUUID().toString());
		nodes.put(data.getString(JsonTreeDataEditor.id), node);

		return node;
	}
	
	public static List<String> getMenuRoot(JSONArray data, String pidkey, String textkey) throws JSONException{
		List<String> result = new ArrayList<>();
		
		for (int i = 0; i < data.length(); i++) {
			JSONObject value = data.getJSONObject(i);
			if (!value.has(pidkey) || value.getString(pidkey).isEmpty())
				result.add(value.getString(textkey));			
		}
		return result;
	}
	
	HashMap<String, Component> nodes = new HashMap<>();
	protected void jsonToMenu(JSONArray valueObj) throws JSONException {
		TreeMap<String, List<JSONObject>> values = new TreeMap<>();
		for (int i = 0; i < valueObj.length(); i++) {
			JSONObject value = valueObj.getJSONObject(i);
			String key = null;
			if (value.has(JsonTreeDataEditor.parent))
				key = value.getString(JsonTreeDataEditor.parent);
			if (key == null || key.isEmpty()){
				key = "";
			}
			
			List<JSONObject> list;
			if (!values.containsKey(key)){
				list = new ArrayList<>();
				values.put(key, list);
			}else
				list = values.get(key);
			
			list.add(value);
		}
		
		JMenuItem parentNode = null;
		List<String> keys = new ArrayList<>(values.keySet());
		List<String> nokeys = new ArrayList<>();
		while (keys.size() > 0) {
			String pid = keys.remove(0);
			if (pid.isEmpty()){
				parentNode = null;
			}else{
				parentNode = (JMenuItem) nodes.get(pid);
				if (parentNode == null){
					nokeys.add(pid);
					continue;
				}else{
					keys.addAll(nokeys);
					nokeys.clear();
				}
			}
			List<JSONObject> datas = values.get(pid);
			jsonToMenu(pid, datas, parentNode, values);
		}

	}
	
	protected void jsonToMenu(String pid, List<JSONObject> datas, JMenuItem parentNode, TreeMap<String, List<JSONObject>> all) throws JSONException {
		for (int i = 0; i < datas.size(); i++) {
			JSONObject subInfo = datas.get(i);
			addMenu(subInfo, parentNode, all);
		}
		
	}
	
	public void refreshMenu() throws JSONException{
		menuBar.removeAll();
		JSONArray data = jsonTreeDataEditor.getResult();
		if (data == null){
			return;
		}
		
		jsonToMenu(data);
		menuBar.updateUI();
	}
	
	public void save() throws Exception {
		if (!isEdit)
			return;
		
		JSONArray value = jsonTreeDataEditor.getResult();
		if (value == null)
			return;
		
		JsonHelp.saveJson(getFile(), value, null);
		isEdit = false;
		EditorEnvironment.lockFile(getFile());
	}

	public void load() {
		refresh();
		try {
			refreshMenu();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		isEdit = false;
	}

	public static void showDialog(IMainControl mainControl, File file){
		if (file.exists())
			if (!EditorEnvironment.lockFile(file)){
				EditorEnvironment.showMessage("文件【" + file.getAbsolutePath() + "】已经被其他用户锁定，请稍后再试！");
				return;
			}
		
		MenuEditorDialog dialog = new MenuEditorDialog(mainControl);
		if (file != null)
			dialog.setFile(file);
		dialog.setModal(true);
		dialog.setVisible(true);

		if (file != null)
			EditorEnvironment.unlockFile(file);
	}
	
}
