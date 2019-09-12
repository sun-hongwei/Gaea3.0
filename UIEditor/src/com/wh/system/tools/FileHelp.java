package com.wh.system.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public abstract class FileHelp {

	protected interface IGetRoot {
		File getRoot();
	}

	public static String removeExt(String name){
		int index = name.indexOf(".");
		if (index != -1)
			name = name.substring(0, index);
		return name;
	}
	
	static IGetRoot iGetRoot = new IGetRoot() {
		@Override
		public File getRoot() {
			String path = System.getProperty("user.dir");
			return new File(path);
		}
	};

	public static File getPrivatePath() {
		return GetFile("private", getRootPath());
	}

	public static File getRootPath() {
		return iGetRoot.getRoot();
	}

	public static void moveFile(File sourceFile, File destFile)
			throws IOException {
		Path source = Paths.get(sourceFile.getAbsolutePath());
		Path dest = Paths.get(destFile.getAbsolutePath());
		Files.move(source, dest, StandardCopyOption.REPLACE_EXISTING,
				StandardCopyOption.ATOMIC_MOVE);
	}

	public static File GetTempFile(String extname, String dir) {
		try {
			File rootpath = iGetRoot.getRoot();
			File path = new File(rootpath, dir);
			if (!path.exists())
				path.mkdirs();

			File tmpfile = File.createTempFile("~~~", "." + extname, path);
			return tmpfile;

		} catch (IOException e) {
			return null;
		}

	}

	public static String GetTempFileName(String extname, String dir) {
		Random tempFileRandom = new Random();

		try {
			File rootpath = iGetRoot.getRoot();
			File path = new File(rootpath, dir);
			if (!path.exists())
				path.mkdirs();

			File tmpfile = new File(path.getAbsolutePath(), "~~~"
					+ tempFileRandom.nextInt() + "." + extname);
			return tmpfile.getAbsolutePath();

		} catch (Exception e) {
			return null;
		}

	}

	public static File GetTempFile(String dir) {
		return GetTempFile("tmp", dir);
	}

	public static File GetUniqueFile(String filename, String path) {
		File f = GetFile(filename, path);
		if (f.exists()) {
			if (!f.delete()) {
				return null;
			}
		}
		return f;
	}

	public static String ChangeExt(String filename, String newext) {
		for (int i = filename.length() - 1; i >= 0; i--) {
			if (filename.charAt(i) == '.') {
				filename = filename.substring(0, i + 1);
				return filename + newext;
			}

		}
		return filename + "." + newext;
	}

	public static String GetExt(String filename) {
		for (int i = filename.length() - 1; i >= 0; i--) {
			if (filename.charAt(i) == '.') {
				String ext = filename.substring(i + 1);
				return ext;
			}

		}
		return null;
	}

	public static File GetFile(String filename, File path) {
		if (path == null)
			return null;

		File file = new File(path, filename);
		return file;
	}

	public static File GetFile(String filename, String path) {
		File paths = GetPath(path);
		if (paths == null)
			return null;

		File file = new File(paths, filename);
		return file;
	}

	public static File GetPath(String path) {
		try {
			File rootpath = iGetRoot.getRoot();
			File paths = new File(rootpath, path);
			if (!paths.exists())
				if (!paths.mkdirs())
					return null;

			return paths;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static String GetAbsoluteFileName(String name) {
		File root = iGetRoot.getRoot();
		File f = new File(root, name);
		return f.getAbsolutePath();
	}

	public static File createDir(String path) {
		path = GetAbsoluteFileName(path);
		File dir = new File(path);
		dir.mkdir();
		return dir;
	}

	public static File createFile(String fileName) throws IOException {
		File file = new File(fileName);
		File path = file.getParentFile();
		if (path != null && !path.exists())
			if (!path.mkdirs())
				return null;
		file.createNewFile();
		return file;
	}

	public static boolean isFileExist(String fileName) {
		File file = new File(fileName);
		return file.exists();
	}

	public static boolean delFile(String fileName) {
		File file = new File(fileName);
		if (file == null || !file.exists() || file.isDirectory())
			return false;
		file.delete();
		return true;
	}

	public static boolean renameFile(String oldfileName, String newFileName) {
		File oleFile = new File(oldfileName);
		File newFile = new File(newFileName);
		return oleFile.renameTo(newFile);
	}

	public static boolean delDir(File dir) {
		if (dir == null || !dir.exists() || dir.isFile()) {
			return false;
		}
		for (File file : dir.listFiles()) {
			if (file.isFile()) {
				file.delete();
			} else if (file.isDirectory()) {
				delDir(file);
			}
		}
		dir.delete();
		return true;
	}

	public static void copyFileTo(File srcFile, File destFile) throws IOException{
		copyFileTo(srcFile, destFile, false);
	}
	
	public static void copyFileTo(File srcFile, File destFile, boolean isLinked)
			throws IOException {
		if (srcFile.isDirectory() || destFile.isDirectory())
			throw new IOException("源或者目的不是文件！");
		
		if (isLinked){
			Path link = Paths.get(srcFile.getAbsolutePath());
			Path existing = Paths.get(destFile.getAbsolutePath());
			Files.createLink(link, existing);
			return;
		}
		
		FileInputStream fis = new FileInputStream(srcFile);
		FileOutputStream fos = new FileOutputStream(destFile);
		int readLen = 0;
		byte[] buf = new byte[1024];
		while ((readLen = fis.read(buf)) != -1) {
			fos.write(buf, 0, readLen);
		}
		fos.flush();
		fos.close();
		fis.close();
	}

	public static boolean copyFilesTo(File srcDir, File destDir) throws IOException {
		return copyFilesTo(srcDir, destDir, false);
	}
	
	public static boolean copyFilesTo(File srcDir, File destDir, boolean isLinked) throws IOException {
		return copyFilesTo(srcDir, destDir, (String[])null, isLinked);
	}
	
	public static boolean copyFilesTo(File srcDir, File destDir, String[] exts) throws IOException {
		return copyFilesTo(srcDir, destDir, exts, false);
	}
	
	public static boolean copyFilesTo(File srcDir, File destDir, String[] exts, boolean isLinked) throws IOException {
		HashMap<String, String> extHash = new HashMap<>();
		if (exts != null){
			for (String ext : exts) {
				ext = ext.toLowerCase().trim();
				extHash.put(ext, ext);
			}
		}
		return copyFilesTo(srcDir, destDir, extHash, isLinked);
	}

	static boolean copyFilesTo(File srcDir, File destDir, HashMap<String, String> exts, boolean isLinked) throws IOException {
		if (!srcDir.isDirectory())
			return false;
		if (!destDir.exists())
			if (!destDir.mkdirs())
				return false;
		
		File[] srcFiles = srcDir.listFiles();
		for (int i = 0; i < srcFiles.length; i++) {
			if (srcFiles[i].isFile()) {
				if (exts.size() > 0){
					String ext = GetExt(srcFiles[i].getName());
					ext = ext.toLowerCase().trim();
					if (!exts.containsKey(ext))
						continue;
				}
				File destFile = new File(destDir, srcFiles[i].getName());
				copyFileTo(srcFiles[i], destFile, isLinked);
			} else if (srcFiles[i].isDirectory()) {
				File theDestDir = new File(destDir, srcFiles[i].getName());
				if (!copyFilesTo(srcFiles[i], theDestDir, exts, isLinked))
					return false;
			}
		}
		return true;
	}

	public static boolean getFiles(File srcDir, List<File> result) throws IOException {
		if (!srcDir.isDirectory())
			return false;
		
		File[] srcFiles = srcDir.listFiles();
		for (int i = 0; i < srcFiles.length; i++) {
			if (srcFiles[i].isFile()) {
				result.add(new File(srcDir, srcFiles[i].getName()));
			} else if (srcFiles[i].isDirectory()) {
				if (!getFiles(srcFiles[i], result))
					return false;
			}
		}
		return true;
	}

	public static void moveFileTo(File srcFile, File destFile)
			throws IOException {
		copyFileTo(srcFile, destFile);
		String filename = srcFile.getAbsolutePath() + File.separator + srcFile.getName();
		if (!delFile(filename))
			throw new IOException("无法删除文件：" + filename);
	}

	public static boolean moveFilesTo(File srcDir, File destDir)
			throws IOException {
		if (!srcDir.isDirectory() || !destDir.isDirectory()) {
			return false;
		}
		File[] srcDirFiles = srcDir.listFiles();
		for (int i = 0; i < srcDirFiles.length; i++) {
			if (srcDirFiles[i].isFile()) {
				String destfilename = destDir.getPath() + "//"
						+ srcDirFiles[i].getName();
				String srcfilename = srcDir.getPath() + "//"
						+ srcDirFiles[i].getName();
				File oneDestFile = new File(destfilename);
				moveFileTo(srcDirFiles[i], oneDestFile);
				delFile(srcfilename);
			} else if (srcDirFiles[i].isDirectory()) {
				File oneDestFile = new File(destDir.getPath() + "//"
						+ srcDirFiles[i].getName());
				moveFilesTo(srcDirFiles[i], oneDestFile);
				delDir(srcDirFiles[i]);
			}

		}
		return true;
	}

	public static boolean DeleteFile(File file) {
		if (!file.exists()) {
			return false;
		} else {
			if (file.isFile()) {
				file.delete();
				return true;
			} else if (file.isDirectory()) {
				File[] childFile = file.listFiles();
				if (childFile == null || childFile.length == 0) {
					file.delete();
					return true;
				}
				for (File f : childFile) {
					DeleteFile(f);
				}
				file.delete();
				return true;
			} else
				return true;
		}
	}

	public static void go() {
	}
}