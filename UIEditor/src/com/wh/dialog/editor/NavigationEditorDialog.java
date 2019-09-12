package com.wh.dialog.editor;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.json.JSONArray;
import org.json.JSONException;

import com.wh.control.EditorEnvironment;
import com.wh.dialog.editor.JsonTreeDataEditor.ChangeObjectType;
import com.wh.dialog.editor.JsonTreeDataEditor.ChangeType;
import com.wh.dialog.editor.JsonTreeDataEditor.IChange;
import com.wh.dialog.editor.JsonTreeDataEditor.TreeItemInfo;
import com.wh.dialog.editor.ModelflowSelectDialog.Result;
import com.wh.form.IMainControl;
import com.wh.system.tools.JsonHelp;
public class NavigationEditorDialog extends JDialog{
	private static final long serialVersionUID = 4251405285974410412L;
	private JsonTreeDataEditor jsonTreeDataEditor;

	/**
	 * Create the dialog.
	 */
	
	boolean isEdit = false;
	IMainControl mainControl;
	public NavigationEditorDialog(IMainControl mainControl) {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("导航树编辑");
		setIconImage(Toolkit.getDefaultToolkit().getImage(NavigationEditorDialog.class.getResource("/image/browser.png")));
		this.mainControl = mainControl;
		setBounds(100, 100, 1070, 725);
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
					Result result = ModelflowSelectDialog.showDialog(NavigationEditorDialog.this.mainControl, null, name);
					if (result == null){
						return;
					}

					info.data.put(JsonTreeDataEditor.jumpid, result.name);
					info.data.put(JsonTreeDataEditor.name, result.title);
					isEdit = true;
					jsonTreeDataEditor.tree.updateUI();
				} catch (JSONException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
			}
		});
		panel.add(button_1);
		
		JSeparator separator = new JSeparator();
		panel.add(separator);
		
		JButton button = new JButton("保存");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		panel.add(button);
		
		JButton button_2 = new JButton("装载");
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				load();
			}
		});
		panel.add(button_2);
		setLocationRelativeTo(null);
		
		load();
	}

	File mainTreeFile = null;
	public File getFile(){
		if (mainTreeFile != null)
			return mainTreeFile;
		else
			return EditorEnvironment.getMainNavTreeFile();
	}
	
	public void setFile(File file){
		mainTreeFile = file;
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
		}else
			jsonTreeDataEditor.setValue(null);
	}

	public void save() {
		if (!isEdit)
			return;
		
		JSONArray value = jsonTreeDataEditor.getResult();
		if (value == null)
			return;
		
		try {
			JsonHelp.saveJson(getFile(), value, null);
			isEdit = false;
			EditorEnvironment.lockFile(mainTreeFile);
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showMessage(null, "保存失败！", "保存", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void load() {
		refresh();
	}

	public static void showDialog(IMainControl mainControl, File file){
		if (file.exists())
			if (!EditorEnvironment.lockFile(file)){
				EditorEnvironment.showMessage("文件【" + file.getAbsolutePath() + "】已经被其他用户锁定，请稍后再试！");
				return;
			}
		
		NavigationEditorDialog dialog = new NavigationEditorDialog(mainControl);
		if (file != null)
			dialog.setFile(file);
		dialog.setModal(true);
		dialog.setVisible(true);

		if (file != null)
			EditorEnvironment.unlockFile(file);

	}
	
}
