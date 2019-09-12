package com.wh.system.tools;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by imac on 2016/10/10 0010.
 */

public class BytesHelp {
	static final int BUFFER_SIZE = 8192;

    public static void copyStream(InputStream inputStream, OutputStream outputStream) throws Exception{
        byte[] buffer = new byte[8196];
        int len;
        while ((len = inputStream.read(buffer)) != -1){
            outputStream.write(buffer, 0, len);
        }
        outputStream.flush();
    }

    public static byte[] loadFile(File file, String charsetName) throws IOException{
    	FileInputStream stream = new FileInputStream(file);
    	byte[] data = loadStream(stream, charsetName);
    	stream.close();
    	return data;
    }
    
    public static byte[] loadStream(InputStream stream, String charsetName) throws IOException{
    	InputStreamReader reader = new InputStreamReader(stream, charsetName);
    	int readLen = 0;
    	
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	OutputStreamWriter writer = new OutputStreamWriter(outputStream, charsetName);
    	char[] buffer = new char[BUFFER_SIZE];
    	while (stream.available() > 0 && (readLen = reader.read(buffer, 0, buffer.length)) != -1){
    		writer.write(buffer, 0, readLen);
    	}
    	
    	byte[] data = outputStream.toByteArray();
    	writer.close();;
    	return data;
    }
    
	public static void saveFile(byte[] data, File file) throws Exception{
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		saveStream(data, fileOutputStream);
		fileOutputStream.close();
	}
	
	public static void saveStream(byte[] data, OutputStream outputStream)
			throws Exception {
		outputStream.write(data, 0, data.length);
	}

	public static void saveStream(byte[] data, DataOutputStream outputStream)
			throws Exception {
		outputStream.writeInt(data.length);
		outputStream.write(data, 0, data.length);
	}

	public static void saveStream(File f, DataOutputStream outputStream)
			throws Exception {
		FileInputStream inputStream = new FileInputStream(f);
		outputStream.writeInt(inputStream.available());
		byte[] buffer = new byte[BUFFER_SIZE];
		while (inputStream.available() > 0) {
			int len = inputStream.read(buffer, 0,
					Math.min(inputStream.available(), buffer.length));
			outputStream.write(buffer, 0, len);
		}
		inputStream.close();
	}

	public static byte[] loadStream(DataInputStream inputStream)
			throws Exception {
		int len = inputStream.readInt();
		byte[] resultBuffer = new byte[len];

		int index = 0;
		while (len > 0) {
			int readLen = inputStream.read(resultBuffer, index,
					Math.min(len, BUFFER_SIZE));
			len -= readLen;
			index += readLen;
		}
		return resultBuffer;
	}

	public static byte[] loadFile(File f) throws Exception {
		if (!f.exists())
			return null;

		FileInputStream inputStream = new FileInputStream(f);
		int len = inputStream.available();
		byte[] resultBuffer = new byte[len];

		int index = 0;
		while (len > 0) {
			int readLen = inputStream.read(resultBuffer, index,
					Math.min(len, BUFFER_SIZE));
			len -= readLen;
			index += readLen;
		}
		inputStream.close();
		return resultBuffer;
	}

}
