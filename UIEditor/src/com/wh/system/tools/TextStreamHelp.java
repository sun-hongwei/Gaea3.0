package com.wh.system.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class TextStreamHelp {
	public static final String Default_Charset = "utf8";

	protected static String getCharsetName(String charset) {
		charset = (charset == null || charset.isEmpty()) ? Default_Charset : charset;
		return charset;
	}

	public static void saveToFile(File file, String text) throws IOException{
		saveToFile(file, text, null);
	}
	
	public static void saveToFile(File file, String text, String charsetName) throws IOException{
		OutputStreamWriter document = new OutputStreamWriter(new FileOutputStream(file), getCharsetName(charsetName));
		document.write(text);
		document.close();
	}

	public static String loadFromFile(File file) throws IOException{
		return loadFromFile(file, null);
	}
	
	public static String loadFromFile(File file, String charsetName) throws IOException{
		FileInputStream stream = new FileInputStream(file);
		InputStreamReader document = new InputStreamReader(stream, getCharsetName(charsetName));
		char[] data= new char[stream.available()];
		int index = 0, readLen = 0;
		while (stream.available() > 0 && (readLen = document.read(data, index, data.length)) != -1){
			index += readLen;
		}
		document.close();
		return new String(data);
	}
}
