package com.wh.draws.control;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.draws.DrawCanvas;
import com.wh.draws.DrawNode;
import com.wh.draws.UINode;
import com.wh.draws.drawinfo.DivInfo;
import com.wh.draws.drawinfo.DivInfo.DivType;

public class StackTreeManager {

	public static final int Min_Index = 0;
	public static final int Max_Index = 1;

	public HashMap<String, StatckTreeElement> roots = new HashMap<>();
	public HashMap<String, StatckTreeElement> elements = new HashMap<>();

	public DrawCanvas canvas;

	public StackTreeManager(DrawCanvas canvas) {
		this.canvas = canvas;
	}

	public boolean inPath(String elementid, String checkid) {
		List<String> ids = new ArrayList<>();
		ids.add(checkid);
		return inPath(elementid, ids);
	}

	public boolean isVisiable(DrawNode parent, DrawNode node) {
		if (parent == null)
			return canvas.getClipRect().intersects(node.getRect());

		if (!isVisiable(parent.id))
			return false;

		if (!parent.getRect().intersects(node.getRect()))
			return false;

		return isVisiable(getRoot(parent.id), node);
	}

	public boolean isVisiable(String id) {
		DrawNode node = canvas.getNode(id);
		return isVisiable(node);
	}

	public boolean isVisiable(DrawNode node) {
		if (node == null)
			return false;
		DrawNode parent = getRoot(node.id);
		return isVisiable(parent, node);
	}

	public boolean inPath(String elementid, Collection<String> checkids) {
		if (!elements.containsKey(elementid))
			return false;

		HashMap<String, String> keys = new HashMap<>();
		for (String id : checkids) {
			keys.put(id, id);
		}

		List<String> parents = new ArrayList<>();
		getParents(elementid, parents);
		for (String pid : parents) {
			if (keys.containsKey(pid))
				return true;
		}

		return false;
	}

	public int[] getMinAndMaxOrder(String id, List<DrawNode> result) {
		TreeMap<Integer, List<DrawNode>> orders = new TreeMap<>();
		HashMap<String, Integer> indexs = new HashMap<>();
		int[] mms = getMinAndMaxOrder(id, orders, indexs);
		if (orders.size() > 0) {
			result.addAll(orders.get(orders.firstKey()));
		}
		return mms;
	}

	public int[] getMinAndMaxOrder(String id) {
		TreeMap<Integer, List<DrawNode>> result = new TreeMap<>();
		HashMap<String, Integer> indexs = new HashMap<>();
		return getMinAndMaxOrder(id, result, indexs);
	}

	public int getParentZOrder(String id) {
		List<String> parents = new ArrayList<>();
		getParents(id, parents);
		if (parents.size() > 0) {
			return canvas.nodes.get(parents.get(0)).zOrder;
		} else
			return canvas.nodes.get(id).zOrder;
	}

	public DrawNode getRoot(String id) {
		if (!elements.containsKey(id))
			return null;

		String parentid = null;
		String linkid = elements.get(id).parentid;
		while (linkid != null && !linkid.isEmpty()) {
			parentid = linkid;
			linkid = elements.get(linkid).parentid;
		}
		return canvas.nodes.get(parentid);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void addNodeAndAllParentsToHashmap(HashMap hashMap, String id) {
		List<String> parents = new ArrayList<>();
		getParents(id, parents);
		for (String pid : parents) {
			hashMap.put(pid, id);
		}
		hashMap.put(id, id);
	}

	public DrawNode NodeOfPoint(Point pt) {
		TreeMap<Integer, HashMap<String, String>> sortNodes = new TreeMap<>();
		for (StatckTreeElement treeNode : new ArrayList<>(elements.values())) {
			DrawNode node = canvas.nodes.get(treeNode.id);
			if (!isVisiable(node.id))
				continue;

			if (node.isPoint(pt)) {
				HashMap<String, String> hashMap;
				int z = getParentZOrder(node.id);
				if (sortNodes.containsKey(z)) {
					hashMap = sortNodes.get(z);
				} else {
					hashMap = new HashMap<>();
					sortNodes.put(z, hashMap);
				}
				if (hashMap.containsKey(node.id))
					continue;

				addNodeAndAllParentsToHashmap(hashMap, node.id);
			}
		}
		if (sortNodes.size() == 0)
			return null;
		else {
			int max = -999;
			DrawNode result = null;
			for (String id : sortNodes.get(sortNodes.lastKey()).values()) {
				DrawNode node = canvas.getNode(id);
				if (node.zOrder > max) {
					max = node.zOrder;
					result = node;
				}
			}
			return result;
		}
	}

	int[] getMinAndMaxOrder(String id, TreeMap<Integer, List<DrawNode>> result, HashMap<String, Integer> indexs) {
		if (!elements.containsKey(id))
			throw new NullPointerException("未发现id：" + id + "的层叠节点！");

		DrawNode cur = canvas.nodes.get(id);
		DrawNode parent = getParent(id);
		int min = Integer.MAX_VALUE;

		String parentid = cur.id;
		if (parent == null)
			min = canvas.getZOrderMinAndMax()[0];
		else
			parentid = parent.id;

		int max = 0;

		getSortedChilds(parentid, result, indexs);

		if (result.size() > 0) {
			boolean needMin = parent != null;
			for (DrawNode node : result.get(result.firstKey())) {
				if (needMin) {
					if (node.zOrder < min)
						min = node.zOrder;
				}

				if (node.zOrder > max)
					max = node.zOrder;
			}
		}

		return new int[] { min, max };
	}

	public void getSortedChilds(String id, TreeMap<Integer, List<DrawNode>> result,
			HashMap<String, Integer> indexs) {
		if (!elements.containsKey(id)) {
			return;
		}

		StatckTreeElement drawTree = elements.get(id);
		int index = result.size();
		List<DrawNode> childs = new ArrayList<>();
		result.put(index, childs);
		for (String childid : drawTree.childs.values()) {
			childs.add(canvas.nodes.get(childid));
			indexs.put(childid, index);
		}
		for (String childid : drawTree.childs.values()) {
			getSortedChilds(childid, result, indexs);
		}
	}

	/**
	 * 获取指定id的node的所有子控件
	 * @param id 要获取的节点id
	 * @param result 所有子控件列表
	 */
	public void getChilds(String id, List<StatckTreeElement> result) {
		if (!elements.containsKey(id)) {
			return;
		}

		StatckTreeElement drawTree = elements.get(id);
		for (String childid : new ArrayList<>(drawTree.childs.values())) {
			if (canvas.nodes.containsKey(childid)) {
				result.add(elements.get(childid));
				getChilds(childid, result);
			} else {
				elements.remove(childid);
				roots.remove(childid);
			}
		}
	}

	public boolean bringToTop(DrawNode node) {
		if (elements.containsKey(node.id) && !roots.containsKey(node.id)) {
			int[] minAndMax = getMinAndMaxOrder(node.id);
			node.zOrder = minAndMax[Max_Index] + 1;
			return true;
		}
		return false;
	}

	public boolean sendToBack(DrawNode node) {
		if (elements.containsKey(node.id) && !roots.containsKey(node.id)) {
			List<DrawNode> result = new ArrayList<>();
			int[] minAndMax = getMinAndMaxOrder(node.id, result);
			node.zOrder = minAndMax[Min_Index] - 1;
			if (node.zOrder < 0) {
				int fixOrder = Math.abs(node.zOrder) + 1;
				for (DrawNode n : result) {
					n.zOrder += fixOrder;
				}
				node.zOrder = 0;
			}
			return true;
		}
		return false;
	}

	public JSONObject toJson() throws JSONException {
		JSONObject json = new JSONObject();
		JSONArray nodedatas = new JSONArray();
		for (StatckTreeElement treeNode : elements.values()) {
			JSONObject data = treeNode.toJson();
			nodedatas.put(data);
		}

		json.put("nodes", nodedatas);

		JSONArray rootDatas = new JSONArray();
		for (String id : roots.keySet()) {
			rootDatas.put(id);
		}
		json.put("roots", rootDatas);
		return json;
	}

	public void fromJson(JSONObject json) throws JSONException {
		JSONArray nodes = json.getJSONArray("nodes");
		this.elements.clear();
		this.roots.clear();

		for (int i = 0; i < nodes.length(); i++) {
			JSONObject treeData = nodes.getJSONObject(i);
			StatckTreeElement drawTree = new StatckTreeElement(canvas);
			drawTree.fromJson(treeData);
			this.elements.put(drawTree.id, drawTree);
		}

		List<DrawNode> drawNodes = new ArrayList<>();
		JSONArray rootdatas = json.getJSONArray("roots");
		for (int i = 0; i < rootdatas.length(); i++) {
			String id = rootdatas.getString(i);
			roots.put(id, this.elements.get(id));
			drawNodes.add(canvas.nodes.get(id));
		}

		// SwingUtilities.invokeLater(new Runnable() {
		//
		// @Override
		// public void run() {
		// resetTree(drawNodes);
		// }
		// });
	}

	public DrawNode getParent(String id) {
		DrawNode cur = canvas.getNode(id);
		if (cur == null) {
			throw new NullPointerException("未找到此节点！");
		}

		DrawNode parentNode = getTop(cur);
		if (parentNode != null)
			return parentNode;

		for (DrawNode node : canvas.nodes.values()) {
			if (node.isParent(cur)) {
				if (parentNode == null) {
					if (node.isDrawTreeRoot()) {
						parentNode = node;
					}
				} else {
					if (parentNode.zOrder < node.zOrder) {
						parentNode = node;
					}

				}
			}
		}

		return parentNode;
	}

	protected void getParents(String id, List<String> parents) {
		DrawNode parent = getParent(id);
		if (parent == null)
			return;

		parents.add(0, parent.id);
		getParents(parent.id, parents);
	}

	public void resetTree(Collection<DrawNode> nodes) {
		resetTree(nodes, false);
	}
	
	public void resetTree(Collection<DrawNode> nodes, boolean autoAdd) {
		HashMap<String, String> checkNodes = new HashMap<>();
		HashMap<String, String> treeNodes = new HashMap<>();
		for (DrawNode node : nodes) {
			StatckTreeElement olDrawTree = null;
			String oldParentID = "";
			if (this.elements.containsKey(node.id)) {
				olDrawTree = this.elements.get(node.id);
				oldParentID = olDrawTree.parentid == null ? "" : olDrawTree.parentid;
			}

			StatckTreeElement treeNode = autoAdd ? add(node.id) : elements.get(node.id);
			if (treeNode == null) {
				if (olDrawTree != null) {
					if (olDrawTree.childs.size() == 0) {
						remove(node.id);
						continue;
					} else {
						if (olDrawTree.parentid != null && !olDrawTree.parentid.isEmpty()) {
							StatckTreeElement parent = this.elements.get(olDrawTree.parentid);
							parent.childs.remove(olDrawTree.id);
							olDrawTree.parentid = null;
						}
						roots.put(olDrawTree.id, olDrawTree);
						treeNode = olDrawTree;
					}
				} else {
					continue;
				}
			} else {
				String curParentID = treeNode.parentid == null ? "" : treeNode.parentid;
				if (oldParentID.compareTo(curParentID) != 0 && !oldParentID.isEmpty()) {
					StatckTreeElement parent = this.elements.get(oldParentID);
					parent.childs.remove(olDrawTree.id);
				}

				if (curParentID != null && !curParentID.isEmpty()) {
					if (roots.containsKey(treeNode.id)) {
						roots.remove(treeNode.id);
					}
				}
			}

			checkNodes.put(treeNode.id, treeNode.id);
			treeNodes.put(treeNode.id, treeNode.id);
		}

		resetTree(treeNodes, checkNodes, false);
	}

	void resetTree(HashMap<String, String> treeNodes, HashMap<String, String> checkNodes, boolean needRelocation) {
		for (String treeid : treeNodes.values()) {
			StatckTreeElement treeNode = this.elements.get(treeid);
			if (needRelocation) {
				DrawNode drawNode = canvas.getNode(treeNode.id);
				DrawNode parentNode = canvas.getNode(treeNode.parentid);
				StatckTreeElement parentTreeNode = this.elements.get(treeNode.parentid);
				Rectangle r = parentNode.getRect();
				int x = r.x, y = r.y;

				if (!checkNodes.containsKey(treeNode.id)) {
					x = r.x + (drawNode.getRect().x - parentTreeNode.location.x);
					y = r.y + (drawNode.getRect().y - parentTreeNode.location.y);
					Rectangle rectangle = new Rectangle(x, y, drawNode.getRect().width, drawNode.getRect().height);
					drawNode.setRect(rectangle);
				}
			}

			if (treeNode.childs.size() > 0) {
				for (String id : new ArrayList<>(treeNode.childs.keySet())) {
					StatckTreeElement node = this.elements.get(id);
					if (node == null || node.parentid == null || node.parentid.isEmpty())
						remove(id);
				}

				treeNode = this.elements.get(treeNode.id);
				if (treeNode != null) {
					resetTree(treeNode.childs, checkNodes, true);
				}
			}
		}

		for (String id : treeNodes.values()) {
			DrawNode drawNode = canvas.getNode(id);
			StatckTreeElement treeNode = this.elements.get(id);
			treeNode.location = drawNode.getRect().getLocation();
		}
		canvas.repaint();
	}

	public void getTopForRoot(DrawNode root, DrawNode cur, TreeMap<Integer, DrawNode> nodes) {
		if (root.isParent(cur)) {
			nodes.put(-1, root);
		}
		TreeMap<Integer, List<DrawNode>> result = new TreeMap<>();
		HashMap<String, Integer> indexs = new HashMap<>();
		getSortedChilds(root.id, result, indexs);
		for (String id : indexs.keySet()) {
			DrawNode node = canvas.nodes.get(id);
			if (node == null)
				continue;
			if (node.isParent(cur)) {
				int key = indexs.get(id);
				if (!nodes.containsKey(key)) {
					nodes.put(key, node);
				} else {
					if (nodes.get(key).zOrder < node.zOrder) {
						nodes.put(key, node);
					}
				}
			}
		}
	}

	public DrawNode getTop(DrawNode cur) {
		TreeMap<Integer, DrawNode> nodes = new TreeMap<>();
		for (String rootid : new ArrayList<>(roots.keySet())) {
			DrawNode rootNode = canvas.nodes.get(rootid);
			getTopForRoot(rootNode, cur, nodes);
		}

		if (nodes.size() == 0)
			return null;
		else {
			return nodes.get(nodes.lastKey());
		}
	}

	StatckTreeElement add(String nodeid) {
		List<String> parents = new ArrayList<>();
		getParents(nodeid, parents);
		if (parents.size() == 0) {
			return null;
		}

		for (int i = parents.size() - 1; i >= 0; i--) {
			String id = parents.get(i);
			DrawNode drawNode = canvas.nodes.get(id);
			
			if (drawNode instanceof UINode){
				UINode uiNode = (UINode)drawNode;
				if (uiNode.getDrawInfo() instanceof DivInfo){
					DivInfo divInfo = (DivInfo)uiNode.getDrawInfo();
					if (divInfo.divType != DivType.dtDiv){
						parents.remove(i);
					}
				}
			}
		}
		
		parents.add(nodeid);

		String pid = null;
		for (int i = 0; i < parents.size(); i++) {
			String id = parents.get(i);
			StatckTreeElement node;
			if (!elements.containsKey(id)) {
				node = new StatckTreeElement(canvas, id, pid);
				if (i == 0) {
					roots.put(node.id, node);
				}
				elements.put(node.id, node);
			} else {
				node = elements.get(id);
			}
			node.parentid = pid;
			if (i > 0) {
				elements.get(pid).childs.put(node.id, node.id);
			}

			pid = id;
		}
		StatckTreeElement drawTree = elements.get(nodeid);
		return drawTree;
	}

	public void clear() {
		roots.clear();
		elements.clear();
	}

	public void removeTree(String id, boolean removeChild) {
		removeTree(id, removeChild, true);
	}
	
	public void removeTree(String id, boolean removeChild, boolean recursive) {
		if (!elements.containsKey(id))
			return;

		if (roots.containsKey(id))
			roots.remove(id);

		StatckTreeElement node = elements.remove(id);
		if (node.parentid != null && !node.parentid.isEmpty()) {
			if (elements.containsKey(node.parentid)) {
				elements.get(node.parentid).childs.remove(node.id);
			}
		}

		node.parentid = null;

		if (removeChild) {
			for (String childid : node.childs.keySet()) {
				removeTree(childid, recursive ? true : false, recursive);
			}
			node.childs.clear();
		} else {
			if (node.childs.size() > 0) {
				roots.put(id, node);
				elements.put(id, node);
			}
		}
	}

	public void remove(String id) {
		removeTree(id, false);
	}

	public void checkNodes() {
		List<StatckTreeElement> nodes = new ArrayList<>(elements.values());
		for (StatckTreeElement node : nodes) {
			if (!canvas.nodes.containsKey(node.id))
				removeTree(node.id, false);
			else
				for (String id : node.childs.keySet()) {
					if (!canvas.nodes.containsKey(id))
						removeTree(node.id, false);
				}
		}
	}

}

