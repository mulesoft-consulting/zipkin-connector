package org.mule.modules.zipkinlogger.model;

import java.util.ArrayList;

public class HierarchicalLoggerTags extends LoggerTags {

	private ParentInfo parentInfo;

	public HierarchicalLoggerTags(ParentInfo parentSpanInfo, ArrayList<BinaryAnnotation> arrayList) {
		super(arrayList);
		parentInfo = parentSpanInfo;
	}

	public HierarchicalLoggerTags() {
	}

	public ParentInfo getParentInfo() {
		return parentInfo;
	}

	public void setParentInfo(ParentInfo parentInfo) {
		this.parentInfo = parentInfo;
	}

}
