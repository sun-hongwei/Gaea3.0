package com.wh.draws.control;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.json.JSONException;
import org.json.JSONObject;

import com.wh.draws.DrawCanvas;
import com.wh.draws.DrawNode;
import com.wh.draws.UINode;
import com.wh.draws.DrawCanvas.ChangeType;
import com.wh.draws.DrawCanvas.ICreateNodeSerializable;
import com.wh.draws.drawinfo.DrawInfo;
import com.wh.draws.drawinfo.DrawInfoDefines;
import com.wh.draws.drawinfo.ReportInfo;
import com.wh.draws.drawinfo.ReportInfo.CellInfo;

public class ActionCommandManager {

	public enum CommandInfoType {
		ctRemove, ctDeselected, ctUpdateID, ctAdd, ctMove, ctResize, ctUpdateAttr, ctMoveAndResize, ctLink, ctRemoveLink, ctNone
	}

	class CommandInfo {
		public String newid;
		public CommandInfoType type = CommandInfoType.ctNone;
		public List<JSONObject> data = new ArrayList<>();
		public ICreateNodeSerializable createDataSerializable;
	}

	Stack<CommandInfo> commands = new Stack<>();
	List<CommandInfo> prepareCommands = new ArrayList<>();
	Point start;
	boolean isPrepare = false;
	DrawCanvas canvas;

	boolean isPoping = false;

	public ActionCommandManager(DrawCanvas canvas) {
		this.canvas = canvas;
	}

	public void startPush(Point start, List<DrawNode> nodes, CommandInfoType type) {
		this.start = new Point(start);
		isPrepare = true;

		prepareCommands.clear();

		if (nodes != null) {
			pushCommand(nodes, type);
		}
	}

	public boolean end(Point endP) {
		try {
			if (prepareCommands.size() == 0)
				return false;

			if (!isPrepare)
				return false;

			isPrepare = false;

			HashMap<CommandInfoType, CommandInfo> realNodes = new HashMap<>();
			for (int i = prepareCommands.size() - 1; i >= 0; i--) {
				CommandInfo info = prepareCommands.get(i);
				realNodes.put(info.type, info);
			}

			boolean b = false;
			if (endP != null && start != null) {
				if (Math.abs(endP.x - start.x) > 10 || Math.abs(endP.y - start.y) > 10) {
					b = true;
				}
			}

			for (CommandInfo commandInfo : realNodes.values()) {
				switch (commandInfo.type) {
				case ctAdd:
				case ctDeselected:
				case ctRemove:
					commands.add(commandInfo);
					break;
				case ctMove:
				case ctResize:
					if (b) {
						commands.add(commandInfo);
					}
				default:
					if (commandInfo.type == CommandInfoType.ctUpdateAttr) {
						for (JSONObject data : commandInfo.data) {
							if (data.has("data")) {
								JSONObject uiInfo = data.getJSONObject("data");
								if (uiInfo.has("type") && uiInfo.getString("type").compareTo(DrawInfoDefines.Report_Name) == 0) {
									if (b) {
										commands.add(commandInfo);
										break;
									}
								}
							}
						}
					}
					break;
				}

			}
			return false;
		} finally {
			prepareCommands.clear();
		}
	}

	protected boolean processPopReportNode(DrawNode node) {
		for (DrawNode drawNode : canvas.getNodes()) {
			if (drawNode instanceof UINode) {
				DrawInfo drawInfo = ((UINode) drawNode).getDrawInfo();
				if (drawInfo instanceof ReportInfo) {
					ReportInfo reportInfo = (ReportInfo) drawInfo;
					for (CellInfo cellInfo : reportInfo.getCells().values()) {
						if (cellInfo.editor != null && cellInfo.editor.id.equals(node.id)) {
							cellInfo.editor = (UINode) node;
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}

	public void popCommand() {
		if (commands.isEmpty())
			return;

		isPoping = true;
		try {
			CommandInfo info = commands.pop();

			HashMap<String, DrawNode> selectMaps = new HashMap<>();

			canvas.clearSelect(false);

			switch (info.type) {
			case ctNone:
				break;
			case ctAdd:
				for (JSONObject json : info.data) {
					DrawNode node = DrawNode.fromJson(canvas, json, info.createDataSerializable);
					DrawNode oldNode = canvas.getNode(node.id);
					canvas.remove(oldNode, false, false, true);

					node.popCommanded(oldNode, info.type);
				}
				break;
			case ctDeselected: {
				for (JSONObject json : info.data) {
					DrawNode node = DrawNode.fromJson(canvas, json, info.createDataSerializable);
					if (canvas.getNode(node.id) != null && !selectMaps.containsKey(node.id))
						selectMaps.put(node.id, canvas.getNode(node.id));

					node.pasted();
				}

				canvas.setSelects(selectMaps.values(), true, false);
				break;
			}
			case ctRemove:
			case ctResize:
			case ctMove:
			case ctLink:
			case ctRemoveLink:
			case ctUpdateAttr:
				for (JSONObject json : info.data) {
					DrawNode node = DrawNode.fromJson(canvas, json, info.createDataSerializable);
					DrawNode existNode = canvas.getNode(node.id);
					if (existNode == null) {
						if (node instanceof UINode)
							if (!processPopReportNode(node)){
								canvas.addNode(node, false, false);
							}
					} else {
						canvas.remove(existNode, false, false, false);
						canvas.addNode(node, false, false);
						selectMaps.put(node.id, node);
						canvas.fixNode(node);
					}
					node.popCommanded(existNode, info.type);
				}

				canvas.setSelects(selectMaps.values(), true, false);
				break;
			case ctUpdateID:
				if (info.data.size() > 0) {
					canvas.clearSelect(false);
					DrawNode node = DrawNode.fromJson(canvas, info.data.get(0), info.createDataSerializable);
					canvas.changeNodeId(node, info.newid);
					node.pasted();
				}
				break;
			default:
				break;
			}

			canvas.repaint();

			canvas.fireChange(canvas.getSelecteds(), ChangeType.ctBackspacing);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			isPoping = false;
		}
	}

	public void pushCommand(DrawNode[] nodes, CommandInfoType type) {
		pushCommand(Arrays.asList(nodes), type);
	}

	public void pushCommand(DrawNode node, CommandInfoType type) {
		List<DrawNode> nodes = new ArrayList<>();
		nodes.add(node);
		pushCommand(nodes, type);
	}

	public void pushCommand(JSONObject value, CommandInfoType type) {
		pushCommand(new JSONObject[] { value }, type);
	}

	public void pushCommand(JSONObject value, String newid, CommandInfoType type) {
		pushCommand(new JSONObject[] { value }, newid, type);
	}

	public void pushCommand(JSONObject[] values, CommandInfoType type) {
		pushCommand(values, null, type);
	}

	public void pushCommand(List<DrawNode> nodes, CommandInfoType type) {
		if (nodes == null || nodes.size() == 0)
			return;
		JSONObject[] values = new JSONObject[nodes.size()];
		int index = 0;
		for (DrawNode node : nodes) {
			try {
				values[index++] = node.toJson();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		pushCommand(values, type);
	}

	public void pushCommand(JSONObject[] values, String newid, CommandInfoType type) {

		if (isPoping)
			return;

		CommandInfo info = new CommandInfo();
		info.type = type;
		info.newid = newid;
		info.data.addAll(Arrays.asList(values));
		info.createDataSerializable = canvas.getLastCreateUserDataSerializable();
		if (isPrepare)
			prepareCommands.add(info);
		else
			commands.push(info);
	}

	public void reset() {
		commands.clear();
		prepareCommands.clear();
		isPrepare = false;
		start = null;
	}
}
