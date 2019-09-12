package com.wh.control.modelsearch;

import java.awt.Component;

public interface IModelSearch<T extends Component> {

	void reset();

	void search(String findKey);

	void search(String findKey, Integer col);

	void prior();

	void next();

	Object getValue();

}