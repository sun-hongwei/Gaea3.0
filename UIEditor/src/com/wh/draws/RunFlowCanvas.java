package com.wh.draws;

import java.util.HashMap;

import com.wh.draws.FlowNode.ANDNode;
import com.wh.draws.FlowNode.BeginNode;
import com.wh.draws.FlowNode.EndNode;
import com.wh.draws.FlowNode.ProcessNode;
import com.wh.draws.FlowNode.StateNode;
import com.wh.draws.FlowNode.SwitchNode;
import com.wh.draws.FlowNode.XORNode;

public class RunFlowCanvas extends FlowCanvas {

	@SuppressWarnings("rawtypes")
	private static final Class[][] AllowTypes = new Class[][] { 
		{ BeginNode.class, StateNode.class },
		{ StateNode.class, ProcessNode.class }, 
		{ StateNode.class, SwitchNode.class },
		{ StateNode.class, EndNode.class }, 
		{ SwitchNode.class, StateNode.class },
		{ SwitchNode.class, ProcessNode.class }, 
		{ ProcessNode.class, StateNode.class }
	};

	private static final long serialVersionUID = 1L;

	protected boolean allowPaste(DrawNode node) {
		return node instanceof FlowNode;
	}

	static HashMap<String, Class<FlowNode>[]> allows = new HashMap<>();

	protected static String getAllowHashKey(Class<? extends FlowNode> start, Class<? extends FlowNode> end) {
		return start.getName() + end.getName();
	}

	protected static Class<? extends FlowNode> convertClass(FlowNode node) {
		if (node instanceof SwitchNode) {
			return SwitchNode.class;
		}else if (node instanceof ProcessNode)
			return ProcessNode.class;
		else {
			return node.getClass();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static boolean allowLinked(DrawNode start, DrawNode end) {
		Class<? extends FlowNode> startClass = convertClass((FlowNode) start);
		Class<? extends FlowNode> endClass = convertClass((FlowNode) end);

		String key = getAllowHashKey(startClass, endClass);
		boolean b = allows.containsKey(key);
		if (!b) {
			key = getAllowHashKey((Class<? extends FlowNode>) startClass.getSuperclass(), endClass);
			b = allows.containsKey(key);
		}
		if (!b) {
			key = getAllowHashKey(startClass, (Class<? extends FlowNode>) endClass.getSuperclass());
			b = allows.containsKey(key);
		}

		return b;
	}

	@SuppressWarnings("unchecked")
	public RunFlowCanvas() {
		super();
		for (int i = 0; i < AllowTypes.length; i++) {
			Class<FlowNode>[] value = AllowTypes[i];
			String key = getAllowHashKey(value[0], value[1]);
			allows.put(key, value);
		}
	}

	protected boolean checkStateToSwitchLinked(StateNode start, SwitchNode end) {
		if (end instanceof ANDNode){
			if (end.getPrevs().size() > 0 && end.getNexts().size() > 1)
				return false;		
		}else if (end instanceof XORNode){
//			if (end.getNexts().size() > 1)
//				return false;		
		}
		return true;
	}
	
	protected boolean checkSwitchToStateLinked(SwitchNode start, StateNode end) {
		if (start instanceof ANDNode){
			if (end.getPrevs().size() > 1 && end.getNexts().size() > 0)
				return false;		
		}else if (start instanceof XORNode){
			return false;		
		}
		
		return true;
	}
	
	protected boolean checkSwitchToProcessLinked(SwitchNode start, ProcessNode end) {
		if (start instanceof XORNode){
			if (start.getNexts().size() > 0)
				return false;		
		}
		
		
		return true;
	}
	
	protected boolean linkingDrawNode(DrawNode start, DrawNode end) {
		boolean b = allowLinked(start, end);
		if (b) {
			if (start instanceof BeginNode){
				return start.getNexts().size() == 0;
			}
			
//			if (end instanceof EndNode){
//				return end.getPrevs().size() == 0;
//			}
			
			for (String id : start.getNexts()) {
				if (!(getNode(id) instanceof BeginNode) && getNode(id).getClass() != end.getClass())
					return false;
			}
			for (String id : end.getPrevs()) {
				FlowNode node = (FlowNode) getNode(id);
				if (node instanceof BeginNode)
					continue;
				
				if (node.getClass() != start.getClass()){
					return false;
				}
			}
			if (!(start instanceof XORNode) && end instanceof StateNode) {
				if (end.prevs.size() > 0 && !(getNode(end.prevs.get(0)) instanceof BeginNode)) {
					return false;
				}
			} else if (start instanceof StateNode) {
				return start.getNexts().size() == 0;
			} else if (start instanceof SwitchNode) {
				if (start.getPrevs().size() > 1){
					return start.getNexts().size() == 0;
				}
				
			} else if (start instanceof StateNode && end instanceof SwitchNode) {
				return checkStateToSwitchLinked((StateNode)start, (SwitchNode)end);
			} else if (start instanceof SwitchNode && end instanceof StateNode) {
				return checkSwitchToStateLinked((SwitchNode)start, (StateNode)end);
			} else if (start instanceof SwitchNode && end instanceof ProcessNode) {
				return checkSwitchToProcessLinked((SwitchNode)start, (ProcessNode)end);
			} else if (start instanceof StateNode && end instanceof ProcessNode) {
				if (end.prevs.size() > 0)
					return false;
			} else if (start instanceof SwitchNode && end instanceof ProcessNode) {
				if (start.prevs.size() > 0) {
					return false;
				}
			}
		}

		return b;
	}

}
