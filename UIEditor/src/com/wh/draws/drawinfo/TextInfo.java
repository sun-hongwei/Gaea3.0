package com.wh.draws.drawinfo;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import com.wh.draws.DrawNode;
import com.wh.draws.UINode;
import com.wh.form.Defines;
import com.wh.system.tools.ImageUtils;

public class TextInfo extends DrawInfo {

	protected boolean needImage = true;

	enum DrawWay {
		dwLeft, dwRight
	}

	protected TextInfo.DrawWay drawWay = DrawWay.dwRight;

	public String typeName() {
		return DrawInfoDefines.TextBox_Name;
	}

	public TextInfo(UINode node) {
		super(node);
		width = "150px";
		height = "24px";
	}

	BufferedImage icon;

	protected String getButtonImageFileName() {
		return "line.png";
	}

	protected BufferedImage getImage() {
		if (icon == null) {
			try {
				icon = ImageUtils.loadImage(
						new File(Defines.Java_Dir_Icon_Resource.getAbsolutePath(), getButtonImageFileName()));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return icon;
	}

	protected String getDisplayText() {
		if (value == null)
			return "";
		return value.toString();
	}

	protected int getTextHeight(Graphics g, String text) {
		return DrawNode.getTextHeight(g, getFont(), text);
	}

	protected int drawItem(Graphics g, int x, int y, int width, int height, boolean autoHeight, boolean needCenter,
			String text) {
		BufferedImage image = getImage();
		if (image == null)
			return -1;

		final int div = 5;

		int textLeft = x + div;
		int textWidth = width - div * 2;

		int imageHeight = needImage ? Math.min(height, image.getHeight()) : 0;
		int imageWidth = needImage ? (int) (imageHeight * ((float) image.getWidth() / image.getHeight())) : 0;
		if (needImage) {
			int imageTop = needCenter ? y + (height - imageHeight) / 2 : y;
			textWidth = width - imageWidth - div * 3;
			int imageLeft = 0;
			switch (drawWay) {
			case dwLeft:
				imageLeft = x + div;
				textLeft = imageLeft + imageWidth + div;
				g.drawImage(image, imageLeft, imageTop, null);
				break;
			case dwRight:
				imageLeft = x + width - imageWidth - div;
				textLeft = x + div;
				g.drawImage(image, imageLeft, imageTop, null);
				break;
			}
		}

		int textHeight = DrawNode.drawLineText(g, getFont(), textColor, textLeft, y, textWidth,
				autoHeight ? -1 : height, text, needCenter);
		y += Math.max(textHeight, imageHeight) + div;
		return y;
	}

	public void drawNode(Graphics g, Rectangle rect) {
		drawItem(g, rect.x, rect.y, rect.width, rect.height, false, true, getDisplayText());
	}

	public JSONObject toJson(boolean needAll) throws JSONException {
		JSONObject json = super.toJson(needAll);
		json.put("align", needAll && align == null ? "center" : align);
		json.put("readonly", readonly);
		json.put("vtype", needAll && vtype == null ? "" : vtype);
		return json;
	}

	public void fromJson(JSONObject json) throws JSONException {
		super.fromJson(json);
		if (json.has("align"))
			align = json.getString("align");
		else
			align = "center";

		if (json.has("readonly"))
			readonly = json.getBoolean("readonly");
		else
			readonly = false;

		if (json.has("vtype"))
			vtype = json.getString("vtype");
		else
			vtype = null;
	}

	public String align = "center";
	public boolean readonly = false;
	public String vtype;

}