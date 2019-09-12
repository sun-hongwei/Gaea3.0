package com.wh.control;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.json.JSONObject;

public class RunFlowFile {
	File file;
	public String name;
	public String memo;
	public JSONObject data;
	
	public File getFile(){
		return file;
	}
	
	public void setFile(File file){
		this.file = file;
	}
	
	public void load() throws Exception{
		if (!file.exists())
			return;
		
		try(DataInputStream inputStream = new DataInputStream(new FileInputStream(file));) {
			name = inputStream.readUTF();
			memo = inputStream.readUTF();
			data = new JSONObject(inputStream.readUTF());
		} catch (Exception e) {
			data = null;
			memo = null;
			data = new JSONObject();
			e.printStackTrace();
			throw e;
		}
	}
	
	public void save() throws Exception{
		try(DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(file))){
			outputStream.writeUTF(name == null ? "" : name);
			outputStream.writeUTF(memo == null ? "" : memo);
			outputStream.writeUTF(data == null || data.isEmpty() ? "{}" : data.toString());
			outputStream.flush();
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}

