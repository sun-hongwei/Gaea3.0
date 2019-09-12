package com.wh.dialog.selector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.sunking.swing.JFileTree;
import com.wh.control.EditorEnvironment;
import com.wh.control.JImage;
import com.wh.system.tools.FileHelp;
import com.wh.system.tools.ImageUtils;

import net.coobird.thumbnailator.Thumbnails;
import sun.awt.shell.ShellFolder;

@SuppressWarnings("restriction")
public class FileSelector extends JDialog {
	private static final long serialVersionUID = 1L;
	private JTextField addr;
	private JTextField filename;
	private JComboBox<Ext> extComboBox;
	private JFileTree dirTree;
	private JList<FileInfo> fileList;

	public static class Ext {
		public String name;
		public List<String> exts = new ArrayList<>();

		public Ext(String name, String ext) {
			this.name = name;
			int index = ext.indexOf(".");
			if (index != -1) {
				ext = ext.substring(index + 1);
			}
			this.exts.add(ext.trim().toLowerCase());
		}

		public Ext(String name, List<String> exts) {
			this.name = name;
			for (String ext : exts) {
				this.exts.add(ext.trim().toLowerCase());
			}
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public static class FileInfo {
		public File file;

		@Override
		public String toString() {
			return file.getName();
		}

		public FileInfo(File file) {
			this.file = file;
		}
	}

	public enum DialogType {
		dtOPen, dtSave
	}

	public DialogType dt = DialogType.dtOPen;

	List<Ext> exts = new ArrayList<>();

	boolean isok = false;

	boolean mulitSelect = false;
	private JButton iconButton;
	private JButton listButton;
	private JTable table;
	private JScrollPane listScrollPane;
	private JScrollPane tableScrollPane;
	private JPanel mainPanel;

	public List<FileInfo> getResult() {
		switch (dt) {
		case dtOPen:
			if (listScrollPane.isVisible()) {
				if (fileList.getSelectedValuesList() != null)
					return fileList.getSelectedValuesList();
			} else {
				if (table.getSelectedRows() != null && table.getSelectedRows().length > 0) {
					List<FileInfo> result = new ArrayList<>();
					for (int rowIndex : table.getSelectedRows()) {
						result.add((FileInfo) table.getValueAt(rowIndex, 0));
					}
					return result;
				}
			}
		case dtSave:
			List<FileInfo> infos = new ArrayList<>();
			File file = new File(dirTree.getSelectFile(), filename.getText());
			String ext = FileHelp.GetExt(file.getName());
			if (ext == null || ext.isEmpty()) {
				Ext extInfo = (Ext) extComboBox.getSelectedItem();
				if (extInfo != null) {
					for (String extName : extInfo.exts) {
						if (extName != null && !extName.isEmpty()) {
							file = new File(file.getAbsolutePath() + "." + extName);
							break;
						}
					}
				}
			}
			infos.add(new FileInfo(file));
			return infos;
		}

		return new ArrayList<>();
	}

	/**
	 * 获取小图标
	 * 
	 * @param f
	 * @return
	 */
	@SuppressWarnings("unused")
	private static Icon getSmallIcon(File f) {
		if (f != null && f.exists()) {
			FileSystemView fsv = FileSystemView.getFileSystemView();
			return fsv.getSystemIcon(f);
		}
		return null;
	}

	/**
	 * 获取大图标
	 * 
	 * @param f 要获取的文件
	 * @return
	 */
	private static Icon getBigIcon(File f) {
		if (f != null && f.exists()) {
			String ext = FileHelp.GetExt(f.getName());
			if (ext != null && !ext.isEmpty()) {
				ext = ext.trim();
				if (ext.compareToIgnoreCase("png") == 0 || ext.compareToIgnoreCase("jpg") == 0
						|| ext.compareToIgnoreCase("jpeg") == 0 || ext.compareToIgnoreCase("bmp") == 0
						|| ext.compareToIgnoreCase("gif") == 0) {

					try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();) {
						Thumbnails.of(f).size(28, 28).toOutputStream(outputStream);
						BufferedImage source = ImageUtils.loadImage(outputStream.toByteArray());
						return new ImageIcon(source);
					} catch (IOException e) {
						e.printStackTrace();
						return null;
					}
				}
			}
			try {
				ShellFolder sf = ShellFolder.getShellFolder(f);
				Image source = sf.getIcon(true);
				return new ImageIcon(source);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	static class IconListCellRender extends JLabel implements ListCellRenderer<FileInfo> {

		private static final long serialVersionUID = 1L;

		Icon icon;

		@Override
		public Component getListCellRendererComponent(JList<? extends FileInfo> list, FileInfo value, int index,
				boolean isSelected, boolean cellHasFocus) {
			setSize(new Dimension(80, 80));
			if (isSelected)
				setBackground(Color.yellow);
			icon = getBigIcon(value.file);
			setIcon(icon);
			setText(value.toString());
			setToolTipText(value.toString());
			setHorizontalTextPosition(SwingConstants.CENTER);
			setVerticalTextPosition(SwingConstants.BOTTOM);
			setHorizontalAlignment(SwingConstants.CENTER);
			setVerticalAlignment(SwingConstants.CENTER);

			return this;
		}

	}

	protected void resetFileList() {
		mainPanel.remove(listScrollPane);
		mainPanel.remove(tableScrollPane);
		listScrollPane.setVisible(false);
		tableScrollPane.setVisible(false);
	}

	static class IconColumnCellRender extends JImage implements TableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			setIcon(getBigIcon(((FileInfo) value).file));
			setHorizontalAlignment(JLabel.CENTER);
			setVerticalAlignment(JLabel.CENTER);
			return this;
		}

	}

	public void setFileListTableModel() {
		resetFileList();
		tableScrollPane.setVisible(true);
		mainPanel.add(tableScrollPane, BorderLayout.CENTER);
		initFileListView();

		setTableSelected(getSelectFileInfo());
	}

	public void setFileListIconModel() {
		resetFileList();
		listScrollPane.setVisible(true);
		mainPanel.add(listScrollPane, BorderLayout.CENTER);

		initFileListView();

		setListSelected(getSelectFileInfo());
	}

	public void initExt() {
		if (exts.size() == 0) {
			exts.add(new Ext("所有文件", "*"));
		}

		extComboBox.setModel(new DefaultComboBoxModel<>());
		for (Ext ext : exts) {
			extComboBox.addItem(ext);
		}

	}

	public void initFileListView() {
		if (dirTree.getSelectFile() == null)
			return;

		File selectPath = dirTree.getSelectFile();
		if (listScrollPane.isVisible()) {
			initFileList(selectPath);
		} else {
			initFileTable(selectPath);
		}

		mainPanel.updateUI();
	}

	interface ITraveFile {
		void onFile(FileInfo fileInfo);
	}

	protected void traveFiles(File dir, ITraveFile onTraveFile) {
		HashMap<String, String> exts = new HashMap<>();
		for (Ext ext : this.exts) {
			for (String e : ext.exts) {
				exts.put(e, e);
			}
		}

		File[] files = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				boolean b = !file.isDirectory() && !(file.getName().equals(".") || file.getName().equals(".."));
				if (b) {

					String fileExt = file.getName();
					if (fileExt == null)
						fileExt = "";
					else
						fileExt = FileHelp.GetExt(fileExt);

					if (fileExt != null)
						fileExt = fileExt.trim().toLowerCase();

					if (exts.containsKey("*"))
						return true;

					return fileExt == null ? false : exts.containsKey(fileExt);
				} else
					return false;
			}
		});

		if (files == null)
			return;

		for (File file : files) {
			FileInfo fileInfo = new FileInfo(file);
			onTraveFile.onFile(fileInfo);
		}

	}

	public void initFileList(File dir) {
		fileList.setSelectionMode(
				mulitSelect ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);
		fileList.setModel(new DefaultListModel<>());
		if (dir == null || !dir.exists())
			return;

		DefaultListModel<FileInfo> model = (DefaultListModel<FileInfo>) fileList.getModel();
		traveFiles(dir, new ITraveFile() {

			@Override
			public void onFile(FileInfo fileInfo) {
				model.add(model.getSize(), fileInfo);
			}
		});

	}

	protected String getFileSize(long size) {
		if (size > 1024 * 1024 * 1024) {
			return (size / (1024 * 1024 * 1024)) + "G";
		} else if (size > 1024 * 1024) {
			return (size / (1024 * 1024)) + "M";
		} else if (size > 1024) {
			return (size / (1024)) + "K";
		} else {
			return size + "B";
		}
	}

	protected void setTableColumn(int index, int width, int align, boolean isFit, boolean isIcon) {
		TableColumn column = table.getColumnModel().getColumn(index);
		if (isIcon) {
			column.setCellRenderer(new IconColumnCellRender());
		} else {
			DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
			column.setCellRenderer(defaultTableCellRenderer);
			defaultTableCellRenderer.setHorizontalAlignment(align);
		}
		column.setResizable(true);
		if (isFit)
			column.sizeWidthToFit();
		else
			column.setWidth(width);

	}

	public void initFileTable(File dir) {
		table.setSelectionMode(
				mulitSelect ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);

		mainPanel.add(tableScrollPane, BorderLayout.CENTER);
		DefaultTableModel model = new DefaultTableModel(new Object[][] {}, new String[] { "\u56FE\u6807",
				"\u6587\u4EF6\u540D\u79F0", "\u7C7B\u578B", "\u6587\u4EF6\u5C3A\u5BF8" }) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			boolean[] columnEditables = new boolean[] { false, false, false, false };

			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		};

		table.setModel(model);
		setTableColumn(0, 50, SwingConstants.CENTER, false, true);
		setTableColumn(1, 50, SwingConstants.CENTER, true, false);
		setTableColumn(2, 88, SwingConstants.CENTER, false, false);
		setTableColumn(3, 88, SwingConstants.CENTER, false, false);

		if (dir == null || !dir.exists())
			return;

		HashMap<String, String> exts = new HashMap<>();
		for (Ext ext : this.exts) {
			for (String e : ext.exts) {
				exts.put(e, e);
			}
		}

		traveFiles(dir, new ITraveFile() {

			@Override
			public void onFile(FileInfo fileInfo) {
				File file = fileInfo.file;

				Object[] row = new Object[] { fileInfo, file.getName(), FileHelp.GetExt(file.getName()),
						getFileSize(file.length()) };
				model.addRow(row);
			}
		});

	}

	protected FileInfo getSelectFileInfo() {
		if (listScrollPane.isVisible())
			return fileList.getSelectedValue();
		else {
			if (table.getSelectedRow() == -1)
				return null;
			else
				return (FileInfo) table.getValueAt(table.getSelectedRow(), 0);
		}
	}

	/**
	 * Create the dialog.
	 */
	public FileSelector() {
		setDefaultLookAndFeelDecorated(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(FileSelector.class.getResource("/image/browser.png")));
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setBounds(100, 100, 954, 628);
		getContentPane().setLayout(new BorderLayout());
		JPanel buttonPane = new JPanel();
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		buttonPane.setLayout(new BorderLayout(0, 0));
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		buttonPane.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));
		JLabel label = new JLabel(" 文件名称 ");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel.add(label, BorderLayout.WEST);
		filename = new JTextField();
		filename.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel.add(filename, BorderLayout.CENTER);
		filename.setColumns(10);
		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel.add(panel_1, BorderLayout.EAST);
		label = new JLabel(" 类型 ");
		panel_1.add(label);
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		extComboBox = new JComboBox<Ext>();
		extComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				initFileListView();
			}
		});
		extComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_1.add(extComboBox);
		panel = new JPanel();
		panel.setOpaque(false);
		flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		buttonPane.add(panel, BorderLayout.SOUTH);
		JButton okButton = new JButton("确定");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switch (dt) {
				case dtOPen:
					if (getResult().size() == 0) {
						EditorEnvironment.showMessage("请先选择一个文件！");
						return;
					}
					break;
				case dtSave:
					if (filename.getText() == null || filename.getText().isEmpty()) {
						EditorEnvironment.showMessage("请选择一个文件或者输入文件名称！");
						return;
					}
					break;
				}

				isok = true;
				setVisible(false);
			}
		});
		okButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel.add(okButton);
		okButton.setActionCommand("OK");
		getRootPane().setDefaultButton(okButton);
		JButton cancelButton = new JButton("取消");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel.add(cancelButton);
		cancelButton.setActionCommand("Cancel");
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.3);
		getContentPane().add(splitPane, BorderLayout.CENTER);
		JScrollPane scrollPane = new JScrollPane();
		splitPane.setLeftComponent(scrollPane);
		dirTree = new JFileTree();
		dirTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				if (dirTree.getSelectFile() == null)
					return;

				initFileListView();
			}
		});
		scrollPane.setViewportView(dirTree);
		mainPanel = new JPanel();
		splitPane.setRightComponent(mainPanel);
		mainPanel.setLayout(new BorderLayout(0, 0));
		JPanel toolBar = new JPanel();
		toolBar.setOpaque(false);
		toolBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		mainPanel.add(toolBar, BorderLayout.NORTH);
		toolBar.setLayout(new BorderLayout(0, 0));
		label = new JLabel(" 地址 ");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label, BorderLayout.WEST);
		addr = new JTextField();
		addr.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		addr.setEnabled(true);
		addr.setEditable(false);
		addr.setText("");
		toolBar.add(addr, BorderLayout.CENTER);
		addr.setColumns(10);
		panel = new JPanel();
		toolBar.add(panel, BorderLayout.EAST);
		iconButton = new JButton("");
		iconButton.setMargin(new Insets(0, 0, 0, 0));
		panel.add(iconButton);
		iconButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setFileListIconModel();
			}
		});
		iconButton.setIcon(new ImageIcon(FileSelector.class.getResource("/image/image_24.png")));
		listButton = new JButton("");
		listButton.setMargin(new Insets(0, 0, 0, 0));
		panel.add(listButton);
		listButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setFileListTableModel();
			}
		});
		listButton.setIcon(new ImageIcon(FileSelector.class.getResource("/image/table_green_24.png")));
		listScrollPane = new JScrollPane();
		mainPanel.add(listScrollPane, BorderLayout.CENTER);
		fileList = new JList<FileInfo>();
		fileList.setValueIsAdjusting(true);
		fileList.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		listScrollPane.setViewportView(fileList);
		fileList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		fileList.setVisibleRowCount(-35);
		fileList.setFixedCellHeight(80);
		fileList.setFixedCellWidth(80);
		fileList.setCellRenderer(new IconListCellRender());
		fileList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (fileList.getSelectedValue() == null)
					return;

				addr.setText(fileList.getSelectedValue().file.getAbsolutePath());
				filename.setText(fileList.getSelectedValue().file.getName());
			}
		});

		tableScrollPane = new JScrollPane();
		tableScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tableScrollPane.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		mainPanel.add(tableScrollPane, BorderLayout.CENTER);
		table = new JTable();
		tableScrollPane.setViewportView(table);
		table.setSelectionMode(
				mulitSelect ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);

		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (table.getSelectedRow() == -1)
					return;

				File file = ((FileInfo) table.getValueAt(table.getSelectedRow(), 0)).file;
				addr.setText(file.getAbsolutePath());
				filename.setText(file.getName());
			}
		});

		table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		table.setShowVerticalLines(false);
		table.setShowHorizontalLines(false);
		table.setShowGrid(false);
		table.setRowHeight(48);

		setLocationRelativeTo(null);
	}

	public void setListSelected(FileInfo selected) {
		if (selected != null) {
			if (selected.file.exists()) {
				String filepath = selected.file.getAbsolutePath();
				DefaultListModel<FileInfo> model = (DefaultListModel<FileInfo>) fileList.getModel();
				for (int i = 0; i < model.size(); i++) {
					if (model.getElementAt(i).file.getAbsolutePath().equals(filepath)) {
						fileList.setSelectedIndex(i);
						break;
					}
				}
			}
		}

	}

	public void setTableSelected(FileInfo fileInfo) {
		if (fileInfo != null) {
			if (fileInfo.file.exists()) {
				String filepath = fileInfo.file.getAbsolutePath();
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				for (int i = 0; i < model.getRowCount(); i++) {
					if (((FileInfo) model.getValueAt(i, 0)).file.getAbsoluteFile().equals(filepath)) {
						table.setRowSelectionInterval(i, i);
						break;
					}
				}
			}
		}

	}

	public static List<FileInfo> show(File initPath, File selected, String title, DialogType dt, boolean mulitSelect,
			Ext[] exts) {
		if (selected != null && selected.exists() && !selected.isFile())
			if (initPath == null)
				initPath = null;
			else
				selected = null;

		FileSelector chooser = new FileSelector();
		String defaultTitle = null;
		if (initPath == null || !initPath.exists())
			initPath = EditorEnvironment.getProjectBasePath();
		if (initPath.exists())
			chooser.dirTree.setSelectFile(initPath);
		else
			chooser.dirTree.setSelectFile(EditorEnvironment.getProjectBasePath());

		switch (dt) {
		case dtOPen:
			defaultTitle = "打开文件";
			break;
		case dtSave:
			defaultTitle = "保存文件";
			break;
		}
		chooser.setTitle(title == null || title.isEmpty() ? defaultTitle : title);

		chooser.dt = dt;

		if (mulitSelect) {
			mulitSelect = dt == DialogType.dtOPen;
		}

		chooser.mulitSelect = mulitSelect;

		if (exts != null && exts.length > 0)
			chooser.exts.addAll(Arrays.asList(exts));

		chooser.initExt();

		chooser.setFileListIconModel();

		chooser.iconButton.setSelected(true);

		if (selected == null || !selected.exists()) {

		} else {
			if (selected.isFile()) {
				chooser.setListSelected(new FileInfo(selected));
			}
		}

		if (selected != null) {
			chooser.addr.setText(selected.getAbsolutePath());
			chooser.filename.setText(selected.getName());
		}

		chooser.setModal(true);
		chooser.setVisible(true);

		List<FileInfo> result;
		if (chooser.isok)
			result = chooser.getResult();
		else
			result = new ArrayList<>();

		if (!mulitSelect) {

		}
		chooser.dispose();

		if (dt == DialogType.dtSave && result.size() > 0 && result.get(0).file.exists()) {
			if (EditorEnvironment.showConfirmDialog("文件[" + result.get(0).file.getAbsolutePath() + "]已经存在，是否覆盖？",
					JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
				return new ArrayList<>();
		}
		return result;
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			// JFileChooser chooser = new JFileChooser();
			// chooser.showOpenDialog(null);
			List<FileInfo> result = FileSelector.show(EditorEnvironment.getProjectBasePath(),
					new File("X:\\JAVA\\LinksServer.jar"), null, DialogType.dtOPen, true, null);
			for (FileInfo fileInfo : result) {
				EditorEnvironment.showMessage(fileInfo.file.getAbsolutePath());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
