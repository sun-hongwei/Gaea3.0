package com.wh.system.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by Administrator on 2014/12/12.
 */
public class ZipManager {
	String zipFileString;

	public ZipManager(String zipFileString) {
		this.zipFileString = zipFileString;
	}

	static String ChangeExt(String filename, String newext) {
		for (int i = filename.length() - 1; i >= 0; i--) {
			if (filename.charAt(i) == '.') {
				filename = filename.substring(0, i + 1);
				return filename + newext;
			}

		}
		return filename + "." + newext;
	}

	/**
	 * DeCompress the ZIP to the path
	 *
	 * @param outPathString
	 *            path to be unZIP
	 * @throws Exception
	 */
	public List<File> UnZipFolder(String outPathString) throws Exception {
		return UnZipFolder(new FileInputStream(zipFileString), outPathString,
				null);
	}

	public static List<File> UnZipFolder(InputStream stream,
			String outPathString, String extName) throws Exception {
		List<File> files = new ArrayList<File>();
		File path = new File(outPathString);
		if (!path.exists())
			if (!path.mkdirs())
				return null;
		ZipInputStream inZip = new ZipInputStream(stream);
		ZipEntry zipEntry;
		String szName = "";
		while ((zipEntry = inZip.getNextEntry()) != null) {
			szName = zipEntry.getName();
			if (zipEntry.isDirectory()) {
				// get the folder name of the widget
				szName = szName.substring(0, szName.length() - 1);
				File folder = new File(outPathString + File.separator + szName);
				folder.mkdirs();
			} else {

				if (extName != null && !extName.isEmpty())
					szName = ChangeExt(szName, extName);

				File file = new File(outPathString + File.separator + szName);
				file.createNewFile();
				// get the output stream of the file
				FileOutputStream out = new FileOutputStream(file);
				int len;
				byte[] buffer = new byte[1024];
				// read (len) bytes into buffer
				while ((len = inZip.read(buffer)) != -1) {
					// write (len) byte from buffer at the position 0
					out.write(buffer, 0, len);
					out.flush();
				}
				out.close();
				files.add(file);
			}
		}
		inZip.close();
		return files;
	}

	public static HashMap<String, ByteBuffer> UnZipFolder(InputStream stream)
			throws Exception {
		HashMap<String, ByteBuffer> datas = new HashMap<String, ByteBuffer>();

		ZipInputStream inZip = new ZipInputStream(stream);
		ZipEntry zipEntry;
		while ((zipEntry = inZip.getNextEntry()) != null) {
			String szName = zipEntry.getName();
			if (zipEntry.isDirectory()) {
				continue;
			} else {
				// get the output stream of the file
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				int len;
				byte[] buffer = new byte[1024];
				// read (len) bytes into buffer
				while ((len = inZip.read(buffer)) != -1) {
					// write (len) byte from buffer at the position 0
					out.write(buffer, 0, len);
					out.flush();
				}
				ByteBuffer byteBuffer = ByteBuffer.allocate(out.size());
				byteBuffer.put(out.toByteArray());
				byteBuffer.flip();
				datas.put(szName, byteBuffer);
				out.close();
			}
		}
		inZip.close();
		return datas;
	}

	/**
	 * Compress file and folder
	 *
	 * @param srcFileString
	 *            file or folder to be Compress
	 * @throws Exception
	 */
	public void ZipFolder(String srcFileString) throws Exception {
		// create ZIP
		ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(
				zipFileString));
		// create the file
		File file = new File(srcFileString);
		// compress
		ZipFiles(file + File.separator, "", outZip);
		// finish and close
		outZip.finish();
		outZip.close();
	}

	/**
	 * compress files
	 *
	 * @param folderString
	 * @param fileString
	 * @param zipOutputSteam
	 * @throws Exception
	 */
	private static void ZipFiles(String folderString, String fileString,
			ZipOutputStream zipOutputSteam) throws Exception {
		if (zipOutputSteam == null)
			return;
		File file = new File(folderString + fileString);
		if (file.isFile()) {
			ZipEntry zipEntry = new ZipEntry(fileString);
			FileInputStream inputStream = new FileInputStream(file);
			try {
				zipOutputSteam.putNextEntry(zipEntry);
				int len;
				byte[] buffer = new byte[4096];
				while ((len = inputStream.read(buffer)) != -1) {
					zipOutputSteam.write(buffer, 0, len);
				}
				zipOutputSteam.closeEntry();
			} finally {
				inputStream.close();
			}
		} else {
			// folder
			String fileList[] = file.list();
			// no child file and compress
			if (fileList.length <= 0) {
				ZipEntry zipEntry = new ZipEntry(fileString + File.separator);
				zipOutputSteam.putNextEntry(zipEntry);
				zipOutputSteam.closeEntry();
			}
			// child files and recursion
			for (int i = 0; i < fileList.length; i++) {
				ZipFiles(folderString, fileString + File.separator
						+ fileList[i], zipOutputSteam);
			}// end of for
		}
	}

	/**
	 * return the InputStream of file in the ZIP
	 *
	 * @param fileString
	 *            name of file in the ZIP
	 * @return InputStream
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	public InputStream UpZip(String fileString) throws Exception {
		ZipFile zipFile = new ZipFile(zipFileString);
		ZipEntry zipEntry = zipFile.getEntry(fileString);
		return zipFile.getInputStream(zipEntry);
	}

	/**
	 * return files list(file and folder) in the ZIP
	 *
	 * @param bContainFolder
	 *            contain folder or not
	 * @param bContainFile
	 *            contain file or not
	 * @return
	 * @throws Exception
	 */
	public List<File> GetFileList(boolean bContainFolder, boolean bContainFile)
			throws Exception {
		List<File> fileList = new ArrayList<File>();
		ZipInputStream inZip = new ZipInputStream(new FileInputStream(
				zipFileString));
		ZipEntry zipEntry;
		String szName = "";
		while ((zipEntry = inZip.getNextEntry()) != null) {
			szName = zipEntry.getName();
			if (zipEntry.isDirectory() && bContainFolder) {
				// get the folder name of the widget
				szName = szName.substring(0, szName.length() - 1);
				File folder = new File(szName);
				fileList.add(folder);
			} else {
				if (bContainFile) {
					File file = new File(szName);
					fileList.add(file);
				}
			}
		}
		inZip.close();
		return fileList;
	}

	public static void unzipFile(InputStream zipstream, String filename,
			OutputStream outputStream) throws Exception {
		ZipInputStream in = new ZipInputStream(zipstream);
		ZipEntry entry = in.getNextEntry();
		while (entry != null) {
			if (!entry.isDirectory()
					&& entry.getName().compareToIgnoreCase(filename) == 0) {
				int len = 0;
				byte[] buffer = new byte[8196];
				while ((len = in.read(buffer)) != -1) {
					outputStream.write(buffer, 0, len);
				}
				outputStream.close();
				break;
			}
			// 璇诲彇涓嬩竴涓猌ipEntry
			entry = in.getNextEntry();
		}
		in.close();
	}

	public static List<String> GetNames(InputStream zipstream,
			boolean containdir) {
		try {
			List<String> names = new ArrayList<String>();

			ZipInputStream in = new ZipInputStream(zipstream);
			// 鑾峰彇ZipInputStream涓殑ZipEntry鏉＄洰锛屼竴涓獄ip鏂囦欢涓彲鑳藉寘鍚涓猌ipEntry锛�
			// 褰揼etNextEntry鏂规硶鐨勮繑鍥炲�间负null锛屽垯浠ｈ〃ZipInputStream涓病鏈変笅涓�涓猌ipEntry锛�
			// 杈撳叆娴佽鍙栧畬鎴愶紱
			ZipEntry entry = in.getNextEntry();
			while (entry != null) {

				// 鍒涘缓浠ip鍖呮枃浠跺悕涓虹洰褰曞悕鐨勬牴鐩綍
				if (entry.isDirectory() && containdir) {
					String name = entry.getName();
					name = name.substring(0, name.length() - 1);
					names.add(name);
				} else {
					names.add(entry.getName());
				}
				// 璇诲彇涓嬩竴涓猌ipEntry
				entry = in.getNextEntry();
			}
			in.close();
			return names;
		} catch (Exception e) {
			return null;
		}
	}
}
