package com.wh.control;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wh.system.tools.FileHelp;
import com.wh.system.tools.TextStreamHelp;

public class ParseApp {
	protected File file;
	public enum AppType{
		atJS, atPhp, atNone
	}
	
	public AppType appType;
	public ParseApp(String appFileName){
		file = new File(appFileName);
		switch (FileHelp.GetExt(appFileName).toLowerCase().trim()) {
		case "js":
			appType = AppType.atJS;
			break;
		case "php":
			appType = AppType.atPhp;
		default:
			appType = AppType.atNone;
			break;
		}
	} 
	
	public List<String> parseJS() throws IOException{
		List<String> commands = new ArrayList<>();
		String text = TextStreamHelp.loadFromFile(file);
		
		Pattern pattern = Pattern.compile("postFrameForm\\(([^\\,]+)\\,([^\\,]+)");
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()){
			String command = matcher.group(2);
			command = command.replaceAll("[\\\",']", "").trim();
			commands.add(command);
		}
		
		return commands;
	}
	
	public List<String> parsePhp() throws IOException{
		List<String> commands = new ArrayList<>();
		String text = TextStreamHelp.loadFromFile(file);
		
		Pattern pattern = Pattern.compile("postFrameForm\\(.+,\\s*([^,]+)");
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()){
			String command = matcher.group(0);
			command = command.replaceAll("[\\\",']", "");
			commands.add(command);
		}
		
		return commands;
		
	}
	
	public List<String> parse() throws IOException{
		switch (appType) {
		case atJS:
			return parseJS();
		case atPhp:
			return parsePhp();
		default:
			return new ArrayList<>();
		}
	}
	
}
