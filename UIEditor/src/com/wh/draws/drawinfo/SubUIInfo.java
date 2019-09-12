package com.wh.draws.drawinfo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.wh.control.EditorEnvironment;
import com.wh.draws.DrawNode;
import com.wh.draws.UICanvas;
import com.wh.draws.UINode;
import com.wh.draws.DrawCanvas.ICreateNodeSerializable;
import com.wh.draws.DrawCanvas.IDataSerializable;
import com.wh.draws.DrawCanvas.IInitPage;
import com.wh.draws.DrawCanvas.PageConfig;

public class SubUIInfo extends DrawInfo{
		public static HashMap<String, UICanvas> subInfos = new HashMap<>();
		
		public String typeName(){
			return DrawInfoDefines.SubUI_Name;
		}
		public SubUIInfo(UINode node) {
			super(node);
			needFrame = false;
			width = "320px";
			height = "180px";
		}
		
		public void drawNode(Graphics g, Rectangle rect){
			Color old = g.getColor();
			if (border){
				g.setColor(Color.darkGray);
				g.drawRect(rect.x, rect.y, rect.width, rect.height);
			}
			
			if (uiInfo != null && !uiInfo.isEmpty()){
				if (!subInfos.containsKey(uiInfo)){
					UICanvas uiCanvas = new UICanvas();
					try {
						File f = EditorEnvironment.getUIFileForUIID(uiInfo);
						uiCanvas.setFile(f);
						uiCanvas.load(new ICreateNodeSerializable() {
							
							@Override
							public DrawNode newDrawNode(JSONObject json) {
								return new	UINode(uiCanvas);
							}
							
							@Override
							public IDataSerializable getUserDataSerializable(DrawNode node) {
								// TODO Auto-generated method stub
								return null;
							}
						}, new IInitPage() {
							
							@Override
							public void onPage(PageConfig pageConfig) {
								pageConfig.setPageSizeMode();
							}
						});
						subInfos.put(uiInfo, uiCanvas);
					} catch (Exception e) {
						e.printStackTrace();
						EditorEnvironment.showException(e);
						return;
					}
				}
				UICanvas uiCanvas = subInfos.get(uiInfo);
				BufferedImage image = uiCanvas.saveToImage();
				int width = Math.min(image.getWidth(), rect.width);
				int height = Math.min(image.getHeight(), rect.height);
				g.drawImage(image, rect.x, rect.y, width, height,  null);
//				try {
//					ImageUtils.saveImage(image, new File("x:\\a.png"));
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
			}
			g.setColor(old);
		}
		
		public JSONObject toJson(boolean needAll) throws JSONException{
			JSONObject json = super.toJson(needAll);
			json.put("border", border);
			json.put("showScrollbar", showScrollbar);
			json.put("uiInfo", needAll && uiInfo == null ? "" : uiInfo);
			json.put("transparent", transparent);
			return json;
		}
		
		public void fromJson(JSONObject json) throws JSONException{
			super.fromJson(json);
			border = json.getBoolean("border");
			if (json.has("showScrollbar"))
				showScrollbar = json.getBoolean("showScrollbar");
			else
				showScrollbar = false;
			
			if (json.has("uiInfo"))
				uiInfo = json.getString("uiInfo");
			else
				uiInfo = null;
			
			if (json.has("transparent"))
				transparent = json.getBoolean("transparent");
			else
				transparent = true;
		}

		public boolean showScrollbar = false;
		public boolean border = true;
		public String uiInfo;
		public boolean transparent = true;

	}