package com.wh.control.requirement;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.control.EditorEnvironment;
import com.wh.dialog.editor.ModelflowSelectDialog;
import com.wh.dialog.editor.ModelflowSelectDialog.Result;
import com.wh.form.IMainControl;
import com.wh.system.tools.JsonHelp;
import com.wh.system.tools.Tools;

public class ViewControl {
	public static Color DEPEND_COLOR = Color.YELLOW;
	public static Color VERSION_DEPEND_COLOR = Color.green;

	public enum RequirementState {
		rsClose, rsNoClose, rsAll
	}

	enum FormType {
		dtDepend, dtVersionDepend, dtNormal
	}

	FormType fType = FormType.dtNormal;

	HashMap<String, JSONObject> requirementMap = new HashMap<>();

	ListSelectionListener tableListSelectionListener = new ListSelectionListener() {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting())
				return;

			if (!tableControl.isSelectColorModel())
				setSelectColors();
		}
	};

	protected boolean allowDepend(int row, String field, boolean allowSameVersion) {
		JSONObject rowdata = tableControl.getRow(row);

		if (row == colorSelectRow)
			return false;

		JSONObject selectRow = tableControl.getRow(colorSelectRow);
		String id = tableControl.getRowId(selectRow);
		JSONArray rows = null;
		try {
			rows = rowdata.getJSONArray(field);
		} catch (Exception e) {
		}
		if (rows != null) {
			if (!allowSameVersion) {
				if (rowdata.getString(RequirementDefines.field_version)
						.equals(selectRow.getString(RequirementDefines.field_version))) {
					EditorEnvironment.showMessage("此需求与当前选定需求版本一致，不能设置！");
					return false;
				}
			}
			for (Object object : rows) {
				if (object.equals(id)) {
					EditorEnvironment.showMessage("此需求已经依赖当前选定需求，不能循环依赖！");
					return false;
				}
			}
		}

		return true;
	}

	protected void setSelectColors() {
		int row = tableControl.getSelectRow();
		switch (fType) {
		case dtDepend:
			if (!allowDepend(row, RequirementDefines.field_depend, true))
				return;
			tableControl.setColor(row, DEPEND_COLOR);
			tableControl.table.updateUI();
			break;
		case dtVersionDepend:
			if (!allowDepend(row, RequirementDefines.field_depend_version, false))
				return;

			tableControl.setColor(row, VERSION_DEPEND_COLOR);
			tableControl.table.updateUI();
			break;
		default:
			tableControl.setDependColors(row);
			loadRow(true);

		}
	}

	public void setEvents() {
		tableControl.table.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (tableControl.isSelectColorModel())
					setSelectColors();
			}
		});

		tableControl.table.getSelectionModel().addListSelectionListener(tableListSelectionListener);
		
		tableControl.usedView.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				tableControl.isEdit = true;
			}
		});
		
		tableControl.useTime.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				tableControl.isEdit = true;
				
			}
		});
		
		tableControl.planEndTime.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				tableControl.isEdit = true;
				
			}
		});
	}

	private int colorSelectRow = -1;

	public TableControl tableControl = new TableControl();
	public TreeControl treeControl = new TreeControl();
	public File requirementFile;

	public ViewControl() {
		tableControl.treeControl = treeControl;
		treeControl.tableControl = tableControl;
	}

	public TreeSelectionListener treeSelectionListener = new TreeSelectionListener() {

		@Override
		public void valueChanged(TreeSelectionEvent e) {
			if (e.getNewLeadSelectionPath() != null) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getNewLeadSelectionPath()
						.getLastPathComponent();
				selectTable(node);
			}
		}
	};

	public interface IEditing {
		void onEdit(boolean isEdit);
	}

	public IEditing onEditing = null;

	protected void getRequirementMap(boolean reset) {
		if (requirementMap.size() > 0 && !reset)
			return;

		requirementMap.clear();
		for (Object obj : tableControl.getData()) {
			JSONObject row = (JSONObject) obj;
			if (row.has(RequirementDefines.field_id))
				requirementMap.put(row.getString(RequirementDefines.field_id), row);
		}
	}

	public void fireEditing(boolean isEdit) {
		tableControl.isEdit = isEdit;
		if (onEditing != null)
			onEditing.onEdit(isEdit);
	}

	public void cancelSelectColor() {
		try {
			setColorEnd(oldButton, fType, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	String oldTitle;
	JButton oldButton;

	protected void setColorEnd(JButton button, FormType ft, String key) throws Exception {
		switch (fType) {
		case dtNormal: {
			colorSelectRow = tableControl.getSelectRow();
			if (colorSelectRow == -1) {
				EditorEnvironment.showMessage("请先选择一个需求！");
				return;
			}

			if (isPublished()) {
				throw new Exception("当前版本已经发布，不能修改！");
			}

			switch (ft) {
			case dtDepend:
				tableControl.setColors(RequirementDefines.field_depend, Color.GREEN, colorSelectRow, true);
				break;
			case dtVersionDepend:
				tableControl.setColors(RequirementDefines.field_depend_version, Color.YELLOW, colorSelectRow, true);
				break;
			default:
				break;
			}
			tableControl.setSelectColorModel(colorSelectRow, Color.ORANGE);

			oldTitle = button.getText();
			oldButton = button;

			button.setText("完成");
			fType = ft;
			return;
		}
		default: {
			if (ft != fType)
				return;
			if (key != null) {
				if (tableControl.getColorRows().size() == 0) {
					tableControl.setValue(new JSONArray(), colorSelectRow, key);
				} else {
					JSONArray data = new JSONArray();
					for (Integer row : tableControl.getColorRows()) {
						JSONObject rowData = tableControl.getRow(row);
						String id = rowData.getString(RequirementDefines.field_id);
						data.put(id);
					}
					tableControl.setValue(data, colorSelectRow, key);
				}
			}

			button.setText(oldTitle);

			fType = FormType.dtNormal;

			tableControl.tableToJson();

			tableControl.setDrawColorModel();

			tableControl.table.setRowSelectionInterval(colorSelectRow, colorSelectRow);
			fireEditing(true);
			return;
		}
		}

	}

	public boolean isPublished() {
		JSONObject row = tableControl.getRow(tableControl.getSelectRow());
		if (row != null && row.has(RequirementDefines.field_version))
			return RequirementController.isPublish(row.getString(RequirementDefines.field_version));
		else
			return false;
	}

	public boolean hasMainVersion() {
		String version = tableControl.getInputMainVersion();
		if (version == null || version.isEmpty())
			return false;
		else
			return true;
	}

	public boolean isMainVersionPublished() {
		if (!hasMainVersion())
			return false;
		else
			return RequirementController.isPublish(tableControl.getInputMainVersion());
	}

	public void publish() throws Exception {
		String curVersion = tableControl.getInputMainVersion();
		if (curVersion == null || curVersion.isEmpty()) {
			throw new Exception("请输入当前版本号！");
		}

		save();
		JSONObject data = RequirementController.getRequirementPublishes();
		data.put(curVersion, curVersion);
		RequirementController.setRequirementPublishes(data);
	}

	public void unPublish() throws Exception {
		String curVersion = tableControl.getInputMainVersion();
		if (curVersion == null || curVersion.isEmpty()) {
			throw new Exception("请输入当前版本号！");
		}

		save();
		JSONObject data = RequirementController.getRequirementPublishes();
		data.put(curVersion, curVersion);
		RequirementController.setRequirementPublishes(data);
	}

	public void saveMainVersion() {
		String curVersion = tableControl.getInputMainVersion();
		if (curVersion == null || curVersion.isEmpty()) {
			return;
		}

		RequirementController.setRequirementMainVersion(curVersion);
	}

	public void save() throws Exception {
		saveRow(null);
		JSONArray saveData;
		HashMap<String, Object> versions = RequirementController.getRequirementVersionMap();
		if (requirementFile.exists()) {
			saveData = new JSONArray();
			JSONArray data = ((JSONObject) JsonHelp.parseJson(requirementFile, null))
					.getJSONArray(RequirementDefines.KEY_DATA);
			HashMap<String, JSONObject> dataMap = new HashMap<>();

			for (Object object : data) {
				JSONObject row = (JSONObject) object;
				dataMap.put(row.getString(RequirementDefines.field_id), row);
			}

			JSONObject publishVersions = RequirementController.getRequirementPublishes();
			for (Object object : tableControl.getData()) {
				JSONObject row = (JSONObject) object;
				String version = row.getString(RequirementDefines.field_version);
				if (!versions.containsKey(version)) {
					RequirementController.setRequirementVersion(new String[] { version });
					versions.put(version, version);
				}
				boolean needDeleteCurVersion = publishVersions.has(version);
				if (needDeleteCurVersion) {
					saveData.put(dataMap.get(row.getString(RequirementDefines.field_id)));
				} else
					saveData.put(row);
			}

			tableControl.setData(saveData);
		} else
			saveData = tableControl.getData();

		save(requirementFile, saveData);

		getRequirementMap(true);

		EditorEnvironment.lockFile(requirementFile);
		
		fireEditing(false);
	}

	public void saveVersion(String version) throws Exception {
		RequirementController.setRequirementMainVersion(version);
	}

	protected void save(File file, JSONArray data) throws Exception {
		save(file, data, false);
	}

	protected void save(File file, JSONArray data, boolean needLoad) throws Exception {
		RequirementController.saveRequirement(file, data, needLoad);
	}

	public void load(String version, boolean used) {
		load(version, RequirementState.rsAll, used);
	}

	public void load(String version, RequirementState requirementState, boolean used) {
		JSONArray data;
		try {
			data = RequirementController.loadRequirement(version, requirementState, used);
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showException(e);
			return;
		}
		JSONObject loadInfo = new JSONObject();
		loadInfo.put(RequirementDefines.KEY_DATA, data);
		load(loadInfo);

		tableControl.setClasses(RequirementController.getRequirementTypeNames());
		tableControl.setMainVersion(version);

	}

	public void load(File file) throws Exception {
		if (file != null)
			requirementFile = file;
		else
			file = requirementFile;

		if (!file.exists()) {
			tableControl.setData(new JSONArray());
			treeControl.newTreeModel();
			return;
		}
		try {
			load(RequirementController.loadRequirement(file));
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showException(e);
			return;
		}
	}

	public void export(String version) {
		export(version, null, tableControl.getData());
	}

	public void export(String version, String typeName, JSONArray data) {
		File saveFile = Tools.selectSaveFile(null, null, "导出数据【" + version + "】", "excel文件=xlsx");
		if (saveFile == null)
			return;

		File mapTemplateFile = EditorEnvironment.getTemplateFile(EditorEnvironment.Requirement_Dir_Name,
				EditorEnvironment.Excel_Export_Map_Template_Name);
		File dataTemplateFile = EditorEnvironment.getTemplateFile(EditorEnvironment.Requirement_Dir_Name,
				EditorEnvironment.Excel_Export_Data_Template_Name);
		try {
			RequirementController.requirementExportToExcel(mapTemplateFile, dataTemplateFile, saveFile, typeName, data);
			Desktop.getDesktop().open(saveFile);
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showException(e);
		}
	}

	public void load(JSONObject data) {
		try {
			tableControl.setData(data.getJSONArray(RequirementDefines.KEY_DATA), tableControl.isView, false);

			tableControl.setMainVersion(RequirementController.getRequirementMainVersion());

			treeControl.jsonToTree();
			fireEditing(false);
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showException(e);
			return;
		}
	}

	public void selectTable(DefaultMutableTreeNode node) {
		JSONObject data = treeControl.getTreeNodeValue(node);
		tableControl.select(data, tableListSelectionListener);
	}

	public void resetTree() {
		treeControl.refresh();
	}

	public void selectDependColors() {
		switch (fType) {
		case dtNormal:
			tableControl.clearColor();
			tableControl.setColor(-1, null);
			loadRow(true);
			break;
		case dtDepend:
			tableControl.setColor(tableControl.getSelectRow(), DEPEND_COLOR);
			break;
		case dtVersionDepend:
			tableControl.setColor(tableControl.getSelectRow(), VERSION_DEPEND_COLOR);
			break;
		}

	}

	public void setVersionFilter(Object version) throws Exception {
		tableControl.setFilter(RequirementDefines.field_version, version);
	}

	public void saveRow(Integer rowIndex) throws Exception {
		if (rowIndex == null)
			rowIndex = tableControl.getSelectRow();
		
		if (rowIndex == -1)
			return;

		JSONObject row = tableControl.getRow(rowIndex);

		if (isPublished()) {
			throw new Exception("当前版本已经发布，不能修改！");
		}

		getRequirementMap(false);

		String id = tableControl.getInputId();

		if (!id.equals(row.getString(RequirementDefines.field_id))) {
			if (requirementMap.containsKey(id)) {
				throw new Exception("id[" + id + "]已经存在，不能保存，请修改后重试！");
			}
		}
		
		tableControl.saveRow(rowIndex);

		getRequirementMap(true);
		
		if (onEditing != null)
			onEditing.onEdit(true);
	}

	public void saveDynamicInfo() throws Exception {
		tableControl.saveDynamicInfo();
		fireEditing(true);
		tableControl.isEdit = false;
	}

	public void saveDynamicInfos() throws Exception {
		HashMap<File, JSONArray> datasets = new HashMap<>();
		for (Object obj : tableControl.getData()) {
			JSONObject row = (JSONObject) obj;
			if (!row.has(RequirementDefines.field_class))
				continue;

			String classType = row.getString(RequirementDefines.field_class);
			File file = RequirementController.getRequirementFile(classType);
			JSONArray dataset;
			if (datasets.containsKey(file))
				dataset = datasets.get(file);
			else {
				dataset = new JSONArray();
				datasets.put(file, dataset);
			}

			dataset.put(row);
		}

		for (File file : datasets.keySet()) {
			if (!EditorEnvironment.lockFile(file)) {
				throw new IOException("锁定文件【" + file + "】失败，其他用户正在使用！");
			}
		}

		for (File file : datasets.keySet()) {
			save(file, datasets.get(file), true);
		}

		for (File file : datasets.keySet()) {
			EditorEnvironment.unlockFile(file);
		}

		fireEditing(false);
	}

	public void loadRow(boolean needPrompt) {
		if (tableControl.isEdit && needPrompt) {
			tableControl.isEdit = false;
			if (EditorEnvironment.showConfirmDialog("当前项目已经修改，是否保存？",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
				try {
					if (tableControl.lastRow != -1)
						saveRow(tableControl.lastRow);
					else
						saveRow(null);
					
				} catch (Exception e) {
					e.printStackTrace();
					EditorEnvironment.showException(e);
				}
		}
		tableControl.isEdit = false;
		tableControl.loadRow();
	}

	public void addRequirement() {
		String version = tableControl.getInputMainVersion();
		if (version == null || version.isEmpty()) {
			EditorEnvironment.showMessage("主版本号不能为空，请输入后再试！");
			tableControl.setInputMainVersionFocus();
			return;
		}
		tableControl.add();
	}

	public void removeRequirement() {
		tableControl.isEdit = false;
		JSONObject row = tableControl.remove();
		if (row != null) {
			requirementMap.remove(tableControl.getRowId(row));
			fireEditing(true);
			tableControl.isEdit = false;
		}
	}

	public void setModelNode(IMainControl mainControl) throws Exception {
		int row = tableControl.getSelectRow();
		if (row == -1) {
			EditorEnvironment.showMessage("请先选择一个需求！");
			return;
		}

		if (isPublished()) {
			throw new Exception("当前版本已经发布，不能修改！");
		}

		Result result = ModelflowSelectDialog.showDialog(mainControl, null, null);
		if (result == null)
			return;

		tableControl.setValue(result.id, row, RequirementDefines.field_model);
		fireEditing(true);

	}

	public void openModelRelation(IMainControl mainControl) throws Exception {
		int row = tableControl.getSelectRow();
		if (row == -1) {
			EditorEnvironment.showMessage("请先选择一个需求！");
			return;
		}

		String model_id = (String) tableControl.getValue(row, RequirementDefines.field_model);
		if (model_id == null || model_id.isEmpty()) {
			EditorEnvironment.showMessage("此需求未映射到模块节点！");
			return;
		}

		File file = EditorEnvironment.getModelRelationFileFromNodeID(model_id);
		if (file == null || !file.exists()) {
			EditorEnvironment.showMessage("此需求映射的模块节点并不存在！");
			return;
		}

		mainControl.openModelflowRelation(file.getName(), model_id);
	}

	public void setDepend(JButton button) throws Exception {
		setColorEnd(button, FormType.dtDepend, RequirementDefines.field_depend);
	}

	public void setVersionDepend(JButton button) throws Exception {
		setColorEnd(button, FormType.dtVersionDepend, RequirementDefines.field_depend_version);
	}

	public void selectDependColor() {
		tableControl.setDependColors(tableControl.getSelectRow());
	}
}
