package com.scrippsnetworks.wcm.taglib;
import java.util.ArrayList;
import java.util.List;
public class ListBean implements java.io.Serializable {

	private List<Object> list = new ArrayList<Object>();
	private String child;
	public void setChild(String object) {
		list.add(object);
	}

	public List<Object> getList() {
		return list;
	}
}
