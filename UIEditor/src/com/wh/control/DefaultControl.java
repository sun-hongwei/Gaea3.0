package com.wh.control;

import java.util.HashMap;

public class DefaultControl implements IControl {
	HashMap<String, Object> tags = new HashMap<String, Object>();

	@Override
	public Object getTag(String key) {
		if (tags.containsKey(key))
			return tags.get(key);
		else
			return null;
	}

	@Override
	public void setTag(String key, Object value) {
		tags.put(key, value);
	}

}
