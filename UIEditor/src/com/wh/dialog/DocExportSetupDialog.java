package com.wh.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sunking.swing.JFontDialog;
import com.wh.control.EditorEnvironment;
import com.wh.system.tools.JsonHelp;
import com.wh.system.tools.Tools;
import com.wh.system.tools.WordDocument.HeaderInfo;
import com.wh.system.tools.WordDocument.PageNumberHeaderInfo;;

public class DocExportSetupDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField content;
	private JRadioButton leftButton;
	private JRadioButton centerButton;
	private JRadioButton rightButton;

	boolean isok = false;
	boolean isFile = true;
	
	private JTextField saveEditor;
	private JList<String> headers;
	private JList<String> footers;
	private JButton headerFont_1;
	private JButton footerFont_1;
	
	protected void deleteItem(KeyEvent e) {
		@SuppressWarnings("unchecked")
		JList<String> list = (JList<String>)e.getSource();
		if (list.getSelectedIndex() == -1)
			return;
		
		if (e.getKeyCode() == KeyEvent.VK_DELETE){
			if (EditorEnvironment.showConfirmDialog("是否删除项目【" + list.getSelectedValue() + "】", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				DefaultListModel<String> model = (DefaultListModel<String>) list.getModel();
				model.remove(list.getSelectedIndex());
				list.updateUI();
			}
		}
	}
	
	protected void selectFont(ActionEvent e) {
		JButton button = (JButton)e.getSource();
		Font result = JFontDialog.showDialog(this, "字体选择", true, button.getFont());
		if (result != null)
			button.setFont(result);
	}
	
	/**
	 * Create the dialog.
	 */
	public DocExportSetupDialog() {
		setTitle("导出设置");
		setIconImage(Toolkit.getDefaultToolkit().getImage(DocExportSetupDialog.class.getResource("/image/browser.png")));
		setBounds(100, 100, 755, 572);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.NORTH);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JLabel label = new JLabel(" 内容 ");
				label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
				panel.add(label, BorderLayout.WEST);
			}
			{
				content = new JTextField();
				content.setFont(new Font("微软雅黑", Font.PLAIN, 12));
				content.setPreferredSize(new Dimension(200, 21));
				content.setMinimumSize(new Dimension(200, 21));
				panel.add(content);
				content.setColumns(10);
			}
			{
				JPanel panel_1 = new JPanel();
				panel.add(panel_1, BorderLayout.EAST);
				panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
				{
					JButton button = new JButton("添加页头");
					button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
					button.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							DefaultListModel<String> model = (DefaultListModel<String>) headers.getModel();
							model.addElement(getAlign() + "," + content.getText());
						}
					});
					{
						JButton button_1 = new JButton("选择图片");
						button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
						button_1.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								File file = Tools.selectOpenImageFile(null, null, "");
								if (file == null || !file.exists())
									return;
								
								content.setText(file.getAbsolutePath());
							}
						});
						panel_1.add(button_1);
					}
					panel_1.add(button);
				}
				{
					JButton button = new JButton("添加页脚");
					button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
					button.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							DefaultListModel<String> model = (DefaultListModel<String>) footers.getModel();
							model.addElement(getAlign() + "," + content.getText());
						}
					});
					panel_1.add(button);
				}
				{
					leftButton = new JRadioButton("居左");
					leftButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
					panel_1.add(leftButton);
				}
				{
					centerButton = new JRadioButton("剧中");
					centerButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
					panel_1.add(centerButton);
					centerButton.setSelected(true);
				}
				{
					rightButton = new JRadioButton("居右");
					rightButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
					panel_1.add(rightButton);
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				buttonPane.setLayout(new BorderLayout(0, 0));
				{
					JPanel panel = new JPanel();
					buttonPane.add(panel, BorderLayout.EAST);
					{
						JButton button = new JButton("保存到");
						button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
						button.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								String text = saveEditor.getText();
								File defaultFile = null;
								if (text != null && !text.isEmpty())
								{
									defaultFile = new File(text);
									if (!defaultFile.isDirectory()){
										if (!defaultFile.getParentFile().exists())
											defaultFile = null;
									}
								}
								
								if (defaultFile == null){
									defaultFile = EditorEnvironment.getMainModelRelationFile().getParentFile();
								}
								
								if (!isFile){
									File file = Tools.selectSaveDir(null, "选择导出路径", "请选择你要保存的目录位置", defaultFile.getAbsolutePath());
									if (file == null)
										return;
									saveEditor.setText(file.getAbsolutePath());
									return;
								}
								
								File file = Tools.selectSaveFile(null, defaultFile.getAbsolutePath(), EditorEnvironment.getCurrentProjectName() + "系统详细设计", "Word文档（2007及以后版本）=docx");
								if (file == null)
									return;
								
								if (file.exists()){
									if (EditorEnvironment.showConfirmDialog("文件：" + file.getAbsolutePath() + "已经存在，是否覆盖？", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
										return;
									
									if (!file.delete()){
										EditorEnvironment.showMessage("不能删除文件：" + file.getAbsolutePath());
										return;
									}
								}
								
								saveEditor.setText(file.getAbsolutePath());
							}
						});
						panel.add(button);
					}
					{
						headerFont_1 = new JButton("页眉字体");
						headerFont_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
						headerFont_1.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								selectFont(e);
							}
						});
						panel.add(headerFont_1);
					}
					{
						footerFont_1 = new JButton("页脚字体");
						footerFont_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
						footerFont_1.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								selectFont(e);
							}
						});
						panel.add(footerFont_1);
					}
					JButton okButton = new JButton("确定");
					okButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
					panel.add(okButton);
					okButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (saveEditor.getText().isEmpty()){
								EditorEnvironment.showMessage("请选择保存文件！");
								return;
							}
							isok = true;
							setVisible(false);
						}
					});
					okButton.setActionCommand("OK");
					getRootPane().setDefaultButton(okButton);
					{
						JButton cancelButton = new JButton("取消");
						cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
						panel.add(cancelButton);
						cancelButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								setVisible(false);
							}
						});
						cancelButton.setActionCommand("Cancel");
					}
				}
			}
			{
				JLabel label = new JLabel("目标 ");
				label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
				buttonPane.add(label, BorderLayout.WEST);
			}
			{
				saveEditor = new JTextField();
				saveEditor.setFont(new Font("微软雅黑", Font.PLAIN, 12));
				saveEditor.setEnabled(false);
				buttonPane.add(saveEditor, BorderLayout.CENTER);
				saveEditor.setColumns(10);
			}
		}
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(leftButton);
		buttonGroup.add(centerButton);
		buttonGroup.add(rightButton);
		{
			JSplitPane splitPane = new JSplitPane();
			splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			contentPanel.add(splitPane, BorderLayout.CENTER);
			{
				JScrollPane scrollPane = new JScrollPane();
				splitPane.setLeftComponent(scrollPane);
				{
					headers = new JList<>();
					headers.addKeyListener(new KeyAdapter() {
						@Override
						public void keyReleased(KeyEvent e) {
							deleteItem(e);
						}
					});
					scrollPane.setViewportView(headers);
				}
			}
			{
				JScrollPane scrollPane = new JScrollPane();
				splitPane.setRightComponent(scrollPane);
				{
					footers = new JList<>();
					footers.addKeyListener(new KeyAdapter() {
						@Override
						public void keyReleased(KeyEvent e) {
							deleteItem(e);
						}
					});
					scrollPane.setViewportView(footers);
				}
			}
		}
		
		headers.setModel(new DefaultListModel<>());
		footers.setModel(new DefaultListModel<>());
		
		setLocationRelativeTo(null);
	}

	protected String getAlign(){
		if (leftButton.isSelected())
			return "left";
		else if (centerButton.isSelected())
			return "center";
		else {
			return "right";
		}
	}
	
	public static class Result{
		public HeaderInfo[] headers;
		public HeaderInfo[] footers;
		
		public File saveFile;
		
		public Font headerFont = new Font("微软雅黑", 0, 9);
		public Font footerFont = new Font("微软雅黑", 0, 9);;
		
		protected File file;
		
		public Result(File file){
			this.file = file;
		}
		
		protected HeaderInfo[] addToList(JSONObject data, String key) throws JSONException {
			HeaderInfo[] headers = null;
			List<HeaderInfo> infos = new ArrayList<>();
			if (data.has(key)){
				JSONArray subs = data.getJSONArray(key);
				for (int i = 0; i < subs.length(); i++) {
					JSONObject headerdata = subs.getJSONObject(i);
					String value = headerdata.getString("obj");
					ParagraphAlignment alignment = HeaderInfo.getAlign(headerdata.getString("align"));
					if (PageNumberHeaderInfo.is(value)){
						infos.add(new PageNumberHeaderInfo(value, alignment));
					}else
						infos.add(new HeaderInfo(value, alignment));
				}
				headers = infos.toArray(new HeaderInfo[infos.size()]);
			}
			return headers;
		}
		
		public void load() throws Exception{
			if (!file.exists())
				return;
			
			JSONObject data = (JSONObject)JsonHelp.parseJson(file, null);
			headers = addToList(data, "header");
			footers = addToList(data, "footer");
			
			if (data.has("headerfontname") && data.has("headerfontstyle") && data.has("headerfontsize"))
				headerFont = new Font(data.getString("headerfontname"), data.getInt("headerfontstyle"), data.getInt("headerfontsize"));
			
			if (data.has("footerfontname") && data.has("footerfontstyle") && data.has("footerfontsize"))
				footerFont = new Font(data.getString("footerfontname"), data.getInt("footerfontstyle"), data.getInt("footerfontsize"));
			
		}
		
		public void save() throws Exception{
			JSONObject data = new JSONObject();
			if (headers != null){
				JSONArray subs = new JSONArray();
				for (HeaderInfo headerInfo : headers) {
					JSONObject headerdata = new JSONObject();
					headerdata.put("obj", headerInfo.object.toString());
					headerdata.put("align", HeaderInfo.getAlign(headerInfo.alignment));
					subs.put(headerdata);
				}
				data.put("header", subs);
				data.put("headerfontname", headerFont.getFontName());
				data.put("headerfontstyle", headerFont.getStyle());
				data.put("headerfontsize", headerFont.getSize());
			}
			
			if (footers != null){
				JSONArray subs = new JSONArray();
				for (HeaderInfo headerInfo : footers) {
					JSONObject headerdata = new JSONObject();
					headerdata.put("obj", headerInfo.object.toString());
					headerdata.put("align", HeaderInfo.getAlign(headerInfo.alignment));
					subs.put(headerdata);
				}
				data.put("footer", subs);
				
				data.put("footerfontname", footerFont.getFontName());
				data.put("footerfontstyle", footerFont.getStyle());
				data.put("footerfontsize", footerFont.getSize());
			}
			
			JsonHelp.saveJson(file, data, null);
		}
	}
	
	protected static HeaderInfo getInfo(String value){
		if (value == null || value.isEmpty())
			return null;
		
		try {
			if (PageNumberHeaderInfo.is(value))
				return HeaderInfo.fromString(value, PageNumberHeaderInfo.class);
			else{
				
				HeaderInfo headerInfo = HeaderInfo.fromString(value, HeaderInfo.class);
				File file = new File((String)headerInfo.object);
				if (file.exists()){
					headerInfo.object = file;
				}
				return headerInfo;
			}
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showMessage(e);
			return null;
		}
	}
	
	protected static void initList(ListModel<?> listModel, HeaderInfo[] headerinfos) {
		List<String> headers = new ArrayList<>();
		if (headerinfos != null)
			for (HeaderInfo info : headerinfos) {
				headers.add(info.toString());
			}
		
		initList(listModel, headers.toArray(new String[headers.size()]));
	}
	
	@SuppressWarnings("unchecked")
	protected static void initList(ListModel<?> listModel, String[] headers) {
		DefaultListModel<String> model = (DefaultListModel<String>)listModel;
		if (headers != null)
			for (String string : headers) {
				model.addElement(string);
			}
	}

	public static Result showDialog(String[] headers, String[] footers, File saveFile, boolean isFile){
		DocExportSetupDialog dialog = new DocExportSetupDialog();
		dialog.isFile = isFile;
		Result result = new Result(EditorEnvironment.getProjectFile(EditorEnvironment.Config_Dir_Path, "export.config"));
		try {
			result.load();
			initList(dialog.headers.getModel(), result.headers);
			initList(dialog.footers.getModel(), result.footers);
			if (isFile){
				if (saveFile == null || saveFile.isDirectory())
					saveFile = EditorEnvironment.getProjectFile(EditorEnvironment.Export_Dir_Name, 
							"export.doc");
			}else{
				if (saveFile.isFile())
					saveFile = saveFile.getParentFile();
			}
			dialog.saveEditor.setText(saveFile.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (footers != null || headers != null){
			initList(dialog.headers.getModel(), headers);
			initList(dialog.footers.getModel(), footers);
		}
		
		dialog.headerFont_1.setFont(result.headerFont);
		dialog.footerFont_1.setFont(result.footerFont);
		
		dialog.setModal(true);
		dialog.setVisible(true);
		
		if (!dialog.isok)
			return null;
		
		DefaultListModel<String> model = (DefaultListModel<String>) dialog.headers.getModel();
		
		result.headers = new HeaderInfo[model.getSize()];
		for (int i = 0; i < model.getSize(); i++) {
			result.headers[i] = getInfo(model.get(i));
		}

		model = (DefaultListModel<String>) dialog.footers.getModel();
		result.footers = new HeaderInfo[model.getSize()];
		for (int i = 0; i < model.getSize(); i++) {
			result.footers[i] = getInfo(model.get(i));
		}
		
		result.headerFont = dialog.headerFont_1.getFont();
		result.footerFont = dialog.footerFont_1.getFont();
		
		File path = new File(dialog.saveEditor.getText());
		result.saveFile = path;
		if (isFile)
			path = path.getParentFile();
		
		if (!path.exists())
			if (!path.mkdirs()){
				EditorEnvironment.showException(new Exception("无法创建目录：" + path.getAbsolutePath()));
				return null;
			}
		
		try {
			result.save();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		dialog.dispose();
		
		return result;
	}
	
}
