package com.wh.control;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import com.wh.system.tools.Tools;

public class ControlSearchHelp implements KeyListener{
	HashMap<String, List<Integer>> datas = new HashMap<>();
	String key;
	int index, dataindex;
	List<Integer> cur;
	JComboBox<?> comboBox; 
	JTree tree;
	
	@Override
	public void keyTyped(KeyEvent e) {
		
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		switch(e.getKeyCode()){
		case KeyEvent.VK_ESCAPE:
			clear();
			updateUI();
			break;
		case KeyEvent.VK_F3:
			next();
			break;
		case KeyEvent.VK_F4:
			prev();
			break;
		case KeyEvent.VK_F5:
			init();
			break;
		case KeyEvent.VK_DELETE:
		case KeyEvent.VK_BACK_SPACE:
			if (key.length() > 0){
				key = key.substring(0, key.length() - 1);
				find(key);
			}
			break;
		default:
			int code = e.getKeyCode();
			if ((code >= KeyEvent.VK_0 && code <= KeyEvent.VK_9)
					|| (code >= KeyEvent.VK_A && code <= KeyEvent.VK_Z)
					|| code == KeyEvent.VK_SPACE
					|| code == KeyEvent.VK_OPEN_BRACKET
					|| code == KeyEvent.VK_CLOSE_BRACKET
					|| code == KeyEvent.VK_UNDERSCORE
					|| code == KeyEvent.VK_LEFT_PARENTHESIS
					|| code == KeyEvent.VK_RIGHT_PARENTHESIS
					|| code == KeyEvent.VK_PERIOD
					){				
				String kChar = Character.toString((char)e.getKeyCode());
				key += kChar;
				find(key);
			}
			break;
		}
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
	}

	public void clear(){
		index = -1;
		dataindex = -1;
		key = "";
		cur = null;
	}
	
	public void reset(){
		clear();
		datas.clear();
	}
	
	public ControlSearchHelp(JTree tree){
		this.tree = tree;
		initTree();
		tree.addKeyListener(this);
	}
	
	public ControlSearchHelp(JComboBox<?> comboBox){
		this.comboBox = comboBox;
		initComboBox();
		comboBox.addKeyListener(this);
	}
	
	public void init(){
		if (tree != null)
			initTree();
		else if (comboBox != null)
			initComboBox();
	}
	
	private void initComboBox(){
		reset();
		for (int i = 0; i < comboBox.getItemCount(); i++) {
			
			String value = comboBox.getItemAt(i).toString();
			String jp = Tools.toJP(value);
			List<Integer> values;
			if (!datas.containsKey(jp)){
				values = new ArrayList<>();
				datas.put(jp, values);
			}else
				values = datas.get(jp);
			
			values.add(i);
		}
	}
	
	private void initTree(){
		reset();
		for (int i = 0; i < tree.getRowCount(); i++) {
			if (((DefaultMutableTreeNode)tree.getPathForRow(i).getLastPathComponent()).getUserObject() == null)
				continue;
			
			String value = ((DefaultMutableTreeNode)tree.getPathForRow(i).getLastPathComponent()).getUserObject().toString();
			String jp = Tools.toJP(value);
			List<Integer> values;
			if (!datas.containsKey(jp)){
				values = new ArrayList<>();
				datas.put(jp, values);
			}else
				values = datas.get(jp);
			
			values.add(i);
		}
	}
	
	public void find(String key){
		clear();
		cur = new ArrayList<>();
		this.key = key.toLowerCase();
		if (datas.containsKey(key)){
			cur = datas.get(key);
		}else{
			for (String dkey : datas.keySet()) {
				if (dkey.toLowerCase().indexOf(this.key) != -1){
					cur.addAll(datas.get(dkey));
				}
			}
		}
		
		if (cur.size() == 0)
			cur = null;
		
		next();
	}
	
	public void prev(){
		if (cur != null && index > 0)
			index--;
		
		updateUI();
	}
	
	public void next(){
		if (cur != null && index < cur.size() - 1)
			index++;
		
		updateUI();
	}
	
	protected void updateUI(int index) {
		if (comboBox != null){
			if (index >= 0 && index < comboBox.getItemCount())
				comboBox.setSelectedIndex(index);
		}else if (tree != null){
			if (index >= 0 && index < tree.getRowCount())
				tree.setSelectionRow(index);
		}
	}
	
	public void updateUI(){
		if (cur == null || index == -1){
			updateUI(0);
		}else
			updateUI(cur.get(index));
	}
}
