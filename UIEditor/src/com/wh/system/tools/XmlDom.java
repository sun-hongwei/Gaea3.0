package com.wh.system.tools;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlDom {
	Document dom;
	private String filename;

	public XmlDom(String filename) {
		this.filename = filename;
	}

	public XmlDom() {
	}

	public void NewDOM() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		dom = factory.newDocumentBuilder().newDocument();
		Element root = dom.createElement("root");
		dom.appendChild(root);
	}

	public void OpenDOM() throws Exception {
		FileInputStream stream = new FileInputStream(filename);
		OpenDOM(stream);
		stream.close();
	}

	public void OpenDOM(InputStream stream) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		dom = builder.parse(stream);
	}

	public void Load() throws Exception {
		File f = new File(filename);
		if (!f.exists()) {
			NewDOM();
		} else {
			OpenDOM();
		}
	}

	public void Load(String xml) throws Exception {
		if (xml == null || xml.isEmpty())
			return;

		ByteArrayInputStream stream = new ByteArrayInputStream(
				xml.getBytes("utf-8"));
		OpenDOM(stream);
	}

	public String toString() {
		try {
			Source source = new DOMSource(dom);
			StringWriter stringWriter = new StringWriter();
			Result result = new StreamResult(stringWriter);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = null;
			transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);
			return stringWriter.getBuffer().toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void Save() throws Exception {
		File file = new File(filename);
		if (file.exists())
			if (!file.delete())
				throw new IOException("not delete file : " + filename);

		DataOutputStream fos = new DataOutputStream(new FileOutputStream(
				filename));
		try {
			Save(fos);
		} catch (Exception e) {
			File f = new File(filename);
			if (f.exists())
				if (!f.delete())
					f.deleteOnExit();

			throw e;
		} finally {
			fos.close();
			fos = null;
		}
	}

	public void Save(OutputStream stream) throws Exception {
		TransformerFactory transfactory = TransformerFactory.newInstance();
		Transformer transformer = transfactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");// 设置输出采用的编码方式
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");// 是否自动添加额外的空白
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");// 是否忽略XML声明

		Source source = new DOMSource(dom);
		Result result = new StreamResult(stream);
		transformer.transform(source, result);
	}

	public List<String> ParsePath(String path) {
		String[] paths = path.split("/");

		ArrayList<String> als = new ArrayList<String>();
		for (int i = 0; i < paths.length; i++) {
			als.add(paths[i]);
		}

		if (als.size() > 0)
			als.remove(0);

		return als;
	}

	public List<Node> GetSubNodes(Node parent) {
		List<Node> results = new ArrayList<Node>();
		NodeList nodes = parent.getChildNodes();
		if (nodes != null){
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
					results.add(nodes.item(i));
			}
		}
		return results;
	}

	public List<Node> GetAttrs(Node parent) {
		List<Node> results = new ArrayList<Node>();
		NamedNodeMap nodes = parent.getAttributes();
		if (nodes != null){
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeType() == Node.ATTRIBUTE_NODE)
					results.add(nodes.item(i));
			}
		}
		return results;
	}

	public Node getRoot(){
		return dom.getDocumentElement();
	}
	
	public List<Node> GetSubNodes(Node parent, String key, boolean isnew) {
		List<Node> results = new ArrayList<Node>();
		List<String> keys = new ArrayList<String>();
		keys.add(key);
		if (GetSubNodes(parent, keys, results, isnew)) {
			return results;
		} else {
			return null;
		}
	}

	public Node GetNode(String path, String attrname, String attrValue) {
		Node parent = GetNode(path, false);
		if (parent == null)
			return null;

		String key = parent.getNodeName();
		parent = parent.getParentNode();

		return GetSubNode(parent, key, attrname, attrValue);
	}

	public Node GetSubNode(Node parent, String key, String attrname,
			String attrValue) {
		List<Node> tmps = GetSubNodes(parent, key, attrname, attrValue);
		if (tmps == null)
			return null;
		else {
			return tmps.size() == 0 ? null : tmps.get(0);
		}
	}

	public List<Node> GetSubNodes(Node parent, String key, String attrname,
			String attrValue) {
		List<Node> tmps = GetSubNodes(parent, key, false);
		if (tmps == null)
			return null;
		else {
			List<Node> results = new ArrayList<Node>();
			for (Node node : tmps) {
				Node attr = node.getAttributes().getNamedItem(attrname);
				if (attr != null
						&& attr.getNodeName().compareToIgnoreCase(attrname) == 0
						&& attr.getNodeValue().compareTo(attrValue) == 0) {
					results.add(node);
				}
			}
			return results;
		}
	}

	public boolean GetSubNodes(Node root, List<String> keys,
			List<Node> results, boolean isnew) {
		NodeList nodes = root.getChildNodes();

		String key = keys.remove(0);

		boolean needReturn = keys.size() == 0;

		boolean isok = false;
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().compareToIgnoreCase(key) == 0) {
				isok = true;
				if (needReturn) {
					results.add(node);
				} else {
					return GetSubNodes(node, keys, results, isnew);
				}
			}
		}

		if (!isok && isnew) {
			Node newnode = dom.createElement(key);
			newnode = root.appendChild(newnode);
			if (needReturn) {
				results.add(newnode);
				return true;
			} else
				return GetSubNodes(newnode, keys, results, isnew);
		} else
			return isok;
	}

	public boolean GetNodes(String key, List<Node> results, boolean isnew) {
		List<String> keys = ParsePath(key);
		keys.remove(0);
		return GetSubNodes(dom.getDocumentElement(), keys, results, isnew);
	}

	public List<Node> GetNodes(String path, boolean isnew) {
		List<Node> nodes = new ArrayList<Node>();
		if (GetNodes(path, nodes, isnew))
			return nodes;
		else {
			return null;
		}
	}

	public Node GetNode(String path, boolean isnew) {
		List<Node> nodes = GetNodes(path, isnew);
		if (nodes != null && nodes.size() > 0)
			return nodes.get(0);
		else
			return null;
	}

	public Node ForceCreateNode(String path) {
		Node node = GetNode(path, true);
		node = node.getParentNode();

		List<String> keys = ParsePath(path);

		Node newnode = dom.createElement(keys.get(keys.size() - 1));
		newnode = node.appendChild(newnode);

		return newnode;
	}

	public Node ForceCreateSubNode(Node parent, String nodeName) {
		Node newnode = dom.createElement(nodeName);
		newnode = parent.appendChild(newnode);
		return newnode;
	}

	public String GetValue(String path, String attrname) {
		Node node = GetNode(path, false);
		return GetValue(node, attrname);
	}

	public String GetValue(Node node, String attrname) {
		if (node == null)
			return null;

		if (attrname == null || attrname.isEmpty()) {
			return getNodeValue(node);
		} else {
			NamedNodeMap values = node.getAttributes();
			Node attrnode = values.getNamedItem(attrname);
			if (attrnode != null)
				return attrnode.getNodeValue();
			else
				return null;
		}
	}

	public void RemoveNode(String path) {
		Node node = GetNode(path, false);
		RemoveNode(node);
	}

	public void RemoveNode(Node node) {
		if (node == null)
			return;

		Node parent = node.getParentNode();
		if (parent != null)
			parent.removeChild(node);
	}

	public void SetValue(String path, String attrname, String value)
			throws Exception {
		Node node = GetNode(path, false);
		if (node == null) {
			node = GetNode(path, true);
		}

		if (value == null) {
			value = "";
		}

		SetValue(node, attrname, value);
	}

	public String getNodeValue(Node node){
		String text = null;
		Node firstChild = node.getFirstChild();
		if (firstChild != null && firstChild.getNodeType() == Node.TEXT_NODE && firstChild.getFirstChild() == null)
			text = firstChild.getNodeValue();
		text = text == null ? "" : text;
    	return text;
	}
	
	public void SetValue(Node node, String attrname, String value)
			throws Exception {
		if (node == null)
			throw new Exception("建立节点失败！");

		attrname = attrname.trim();
		if (attrname.isEmpty())
			node.setNodeValue(value);
		else {
			NamedNodeMap values = node.getAttributes();
			Node attrnode = values.getNamedItem(attrname);
			if (attrnode != null)
				attrnode.setNodeValue(value);
			else {
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element element = (Element)node;
					element.setAttribute(attrname, value);
				}else{
					attrnode = dom.createAttribute(attrname);
					attrnode.setNodeValue(value);
					node.getAttributes().setNamedItem(attrnode);
				}
			}
		}

	}
}