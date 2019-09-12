package com.wh.system.tools;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.sunking.swing.JDirChooser;
import com.wh.control.EditorEnvironment;
import com.wh.dialog.selector.FileSelector;
import com.wh.dialog.selector.FileSelector.DialogType;
import com.wh.dialog.selector.FileSelector.Ext;
import com.wh.dialog.selector.FileSelector.FileInfo;

public abstract class Tools {

	public static class LastFile {
		public File file;
	}

	public static LastFile lastOpenFile = new LastFile();
	public static LastFile lastOpenDir = new LastFile();
	public static LastFile lastSaveFile = new LastFile();
	public static LastFile lastSaveDir = new LastFile();

	protected static String getInitDir(String initDir, File lastDir) {
		if (initDir == null || initDir.isEmpty())
			if (lastDir != null)
				return lastDir.getAbsolutePath();
		return initDir;
	}

	public static String Now() {
		DateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dFormat.format(new Date());
	}

	public static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

	static File[] selectFileDialog(int type, String title, String msg, Component frame, String initDir,
			String initValue, String ext, boolean onlyDir, boolean multipleSelect, LastFile lastFile) {
		File[] files = openSelectFileDialog(type, title, msg, frame, getInitDir(initDir, lastFile.file), initValue, ext,
				onlyDir, multipleSelect);
		if (files != null && files.length > 0) {
			lastFile.file = files[0].isDirectory() ? files[0] : files[0].getParentFile();
		}
		return files;

	}

	public static File[] selectOpenAudioFiles(Component frame, String initDir, String initValue) {
		return selectOpenFiles(frame, initDir, initValue, AudioUtils.getExts());
	}

	public static File[] selectOpenImageFiles(Component frame, String initDir, String initValue) {
		return selectOpenFiles(frame, initDir, initValue, ImageUtils.getExts());
	}

	public static File selectOpenAudioFile(Component frame, String initDir, String initValue) {
		return selectOpenFile(frame, initDir, initValue, AudioUtils.getExts());
	}

	public static File selectOpenImageFile(Component frame, String initDir, String initValue) {
		return selectOpenFile(frame, initDir, initValue, ImageUtils.getExts());
	}

	public static File selectSaveImageFile(Component frame, String initDir, String initValue) {
		return selectSaveFile(frame, initDir, initValue, ImageUtils.getExts());
	}

	public static File[] selectOpenFiles(Component frame, String initDir, String initValue, String ext) {
		File[] files = openSelectFileDialog(FileDialog.LOAD, null, null, frame, getInitDir(initDir, lastOpenFile.file), initValue, ext,
				false, true);
		return files;
	}

	public static File selectOpenFile(Component frame, String initDir, String initValue, String ext) {
		return selectFileDialog(FileDialog.LOAD, frame, initDir, initValue, ext, false, lastOpenFile);
	}

	public static File selectOpenDir(Component frame, String title, String msg, String initDir) {
		return selectFileDialog(FileDialog.LOAD, title, msg, frame, initDir, null, null, true, lastOpenDir);
	}

	public static File selectSaveFile(Component frame, String initDir, String initValue, String ext) {
		return selectFileDialog(FileDialog.SAVE, frame, initDir, initValue, ext, false, lastSaveFile);
	}

	public static File selectSaveDir(Component frame, String title, String msg, String initDir) {
		return selectFileDialog(FileDialog.SAVE, title, msg, frame, initDir, null, null, true, lastSaveDir);
	}

	static File selectFileDialog(int type, Component frame, String initDir, String initValue, String ext,
			final boolean onlyDir, LastFile lastFile) {
		return selectFileDialog(type, null, null, frame, initDir, initValue, ext, onlyDir, lastFile);
	}

	static File selectFileDialog(int type, String title, String msg, Component frame, String initDir, String initValue,
			String ext, final boolean onlyDir, LastFile lastFile) {
		File[] files = selectFileDialog(type, title, msg, frame, initDir, initValue, ext, onlyDir, false, lastFile);
		if (files == null)
			return null;
		else
			return files[0];
	}

	static void setupUI(JFileChooser chooser) {

	}

	// ext格式：类型说明1=ext1;ext2,类型说明2=ext3;ext4
	static File[] openSelectFileDialog(int type, String title, String msg, Component frame, String initDir,
			String initValue, String exts, final boolean onlyDir, boolean multipleSelect) {

		if (onlyDir) {
			if (title == null || title.isEmpty())
				title = "选择目录";

			if (msg == null || msg.isEmpty())
				msg = "请选择一个目录";

			JDirChooser chooser = new JDirChooser();
			if (title != null)
				chooser.setTitle(title);
			if (msg != null)
				chooser.setMsg(msg);

			if (initDir != null && !initDir.isEmpty()) {
				try {
					chooser.setSelectFile(new File(initDir));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			chooser.setModal(true);
			chooser.setVisible(true);

			File dir = chooser.getSelectFile();

			chooser.dispose();

			if (dir == null)
				return null;
			return new File[] { dir };
		}

		if (exts != null && exts.isEmpty())
			exts = null;

		DialogType dt = DialogType.dtOPen;
		switch (type) {
		case FileDialog.SAVE:
			dt = DialogType.dtSave;
			break;
		}

		String firstExt = null;
		List<Ext> extInfos = new ArrayList<>();
		if (exts != null) {
			for (String ext : exts.split(",")) {
				if (ext.isEmpty())
					continue;
				if (ext.compareTo("*") != 0) {
					String name = ext;
					String[] filterExts = null;
					String[] tmps = ext.split("=");
					if (tmps.length == 2) {
						name = tmps[0];
						filterExts = tmps[1].split(";");
						Ext extInfo = new Ext(name, Arrays.asList(filterExts));
						extInfos.add(extInfo);
						if (firstExt == null || firstExt.isEmpty()) {
							for (String extName : extInfo.exts) {
								if (!extName.equals("*")) {
									firstExt = extName;
									break;
								}
							}
						}
					}

				} else {
					extInfos.add(new Ext("所有文件", "*"));
				}
			}
		}

		File selectFile = null;
		File dir;
		if (initDir != null && !initDir.isEmpty()) {
			dir = new File(initDir);
		} else
			dir = EditorEnvironment.getProjectBasePath();
		if (!dir.exists())
			if (!dir.mkdirs())
				return null;

		if (initValue != null && !initValue.isEmpty()){
			int index = initValue.indexOf("."); 
			if (index != -1)
				initValue = initValue.substring(0, index);
			selectFile = new File(dir, initValue + (firstExt == null || firstExt.isEmpty() ? "" : "." + firstExt));
		}else if (dir != null)
			selectFile = dir;
			
		List<FileInfo> result = FileSelector.show(dir, selectFile, title, dt, multipleSelect,
				extInfos.toArray(new Ext[extInfos.size()]));

		if (result.size() == 0)
			return null;

		if (!multipleSelect) {
			return new File[] { result.get(0).file };
		} else {
			List<File> resultFiles = new ArrayList<>();
			for (FileInfo info : result) {
				resultFiles.add(info.file);
			}
			return resultFiles.toArray(new File[resultFiles.size()]);
		}
	}

	public static void showErrorMessage(JComponent component, Exception e) {
		if (e == null) {
			EditorEnvironment.showMessage(component, "未知的错误！", "错误", JOptionPane.ERROR_MESSAGE);
			return;
		}
		String msg = e.getMessage();
		if (msg == null || msg.isEmpty())
			msg = e.getClass().getName();
		e.printStackTrace();
		EditorEnvironment.showMessage(component, msg, "错误", JOptionPane.ERROR_MESSAGE);
	}

	public static void showMaxFrameNotMaxButton(Window frame) {
		frame.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentMoved(ComponentEvent e) {
				if (frame.isActive() && frame.isVisible()) {
					frame.removeComponentListener(this);
					frame.setLocationRelativeTo(null);
					frame.addComponentListener(this);
				}
			}
		});

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration());
		frame.setSize(screen.width, screen.height - insets.bottom);
		frame.setLocationRelativeTo(null);
		if (frame instanceof JFrame)
			frame.setVisible(true);
	}

	public static void showMaxFrame(Window frame) {
		frame.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentMoved(ComponentEvent e) {
				if (frame.isActive() && frame.isVisible()) {
				}
			}
		});

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration());
		frame.setSize(screen.width, screen.height - insets.bottom);
		frame.setLocationRelativeTo(null);
		if (frame instanceof JFrame) {
			frame.setVisible(true);
			((Frame) frame).setExtendedState(JFrame.MAXIMIZED_BOTH);
		}
	}

	public static String getExceptionMsg(Throwable e) {
		if (e.getMessage() != null)
			return e.getMessage();

		return e.getClass().getName();
	}

	public static float roundFloatTo(float value, int decimalLength) {
		return Float.parseFloat(roundTo(value, decimalLength));
	}

	public static String roundTo(float value, int decimalLength) {
		String zeros = "";
		for (int i = 0; i < decimalLength; i++) {
			zeros += "0";
		}
		return new java.text.DecimalFormat("#." + zeros).format(value);
	}

	public static String toJP(String c) {
		char[] chars = c.toCharArray();
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < chars.length; i++) {
			sb.append(getJP(chars[i]));
		}
		return sb.toString().toUpperCase();
	}

	public static String getJP(char c) {
		byte[] array = new byte[2];
		try {
			array = String.valueOf(c).getBytes("gbk");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if (array.length < 2)
			return String.valueOf(c);

		int i = (short) (array[0] - '\0' + 256) * 256 + ((short) (array[1] - '\0' + 256));
		if (i < 0xB0A1)
			return String.valueOf(c);
		if (i < 0xB0C5)
			return "a";
		if (i < 0xB2C1)
			return "b";
		if (i < 0xB4EE)
			return "c";
		if (i < 0xB6EA)
			return "d";
		if (i < 0xB7A2)
			return "e";
		if (i < 0xB8C1)
			return "f";
		if (i < 0xB9FE)
			return "g";
		if (i < 0xBBF7)
			return "h";
		if (i < 0xBFA6)
			return "j";
		if (i < 0xC0AC)
			return "k";
		if (i < 0xC2E8)
			return "l";
		if (i < 0xC4C3)
			return "m";
		if (i < 0xC5B6)
			return "n";
		if (i < 0xC5BE)
			return "o";
		if (i < 0xC6DA)
			return "p";
		if (i < 0xC8BB)
			return "q";
		if (i < 0xC8F6)
			return "r";
		if (i < 0xCBFA)
			return "s";
		if (i < 0xCDDA)
			return "t";
		if (i < 0xCEF4)
			return "w";
		if (i < 0xD1B9)
			return "x";
		if (i < 0xD4D1)
			return "y";
		if (i < 0xD7FA)
			return "z";
		return String.valueOf(c);
	}
}
