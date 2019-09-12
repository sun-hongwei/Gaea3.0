package com.wh.form;

import java.io.File;

import javax.swing.JInternalFrame;

import com.wh.draws.DrawNode;
import com.wh.draws.FlowNode.ChildFlowNode;
import com.wh.draws.UINode;
import com.wh.form.MainForm.IControl;

public interface IMainControl {

	public enum FormType{
		ftWorkflow, ftUI, ftFlow, ftRun, ftCSS, ftAppWorkflow, ftScene, ftReport, ftSystemRegion, ftRequirement, ftRequirementVersion
	}

	void setTitle(String title);

	void openSceneDesign(DrawNode node);

	void openRunWorkflow(File runFlowFile);

	/**
	 * 打开模块编辑器
	 * @param workflowRelationName 模块关系图名称，格式id.whn
	 * @param selectNodeId 选定的模块id，可以为null
	 */
	void openModelflowRelation(String workflowRelationName, String selectNodeId);
	
	void openFrameNodeEditor();
	
	boolean openModelflowRelation(DrawNode node, String[] selectNodeIds);

	void openCodeflowRelation(String name, String uiid, String workflowid);

	void openUIBuilder(File file, String controlId) throws Exception;
	
	void openUIBuilder(String uiid, String controlId) throws Exception;

	void openWorkflowRelation(String title, String name);
	
	void openSubWorkflowRelation(ChildFlowNode node);
	
	void openRequirementBuilder(File file);

	//void openFlowRelation(ISubForm iSubForm, DrawNode node, boolean checkFront);
	
	void updateUIButtonTitle(ChildForm form, String title);

	ChildForm openFrame(Class<? extends ChildForm> formClass, boolean needNew, Object... args);

	void toFront(ChildForm form);

	void selectForm(ChildForm form);

	JInternalFrame[] getForms();
	
	ChildForm getFront();
	
	void openReportEditor();
	void openReportEditor(UIBuilder uiBuilder, UINode node);
	
	void switchSubForm(ChildForm parent, Class<? extends ChildForm> subFormClass, IControl iControl, Object param)
			throws Exception;

	void initRequirementVersionMenu();

	Integer getSelectDPI();
}