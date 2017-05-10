
package org.mule.modules.zipkinlogger.model;

import java.util.ArrayList;
import java.util.List;

public class LoggerTags {

    private List<BinaryAnnotation> binaryAnnotations = new ArrayList<BinaryAnnotation>();

    public LoggerTags(List<BinaryAnnotation> arrayList) {
    	this.binaryAnnotations = arrayList;
	}

	public LoggerTags() {
	}

	public List<BinaryAnnotation> getBinaryAnnotations() {
        return binaryAnnotations;
    }

    public void setBinaryAnnotations(List<BinaryAnnotation> binaryAnnotations) {
        this.binaryAnnotations = binaryAnnotations;
    }
    
    public void addTag(String key, String value) {
    	
    	BinaryAnnotation ann = new BinaryAnnotation();
    	ann.setKey(key);
    	ann.setValue(value);
    	
    	binaryAnnotations.add(ann);
    }

}
