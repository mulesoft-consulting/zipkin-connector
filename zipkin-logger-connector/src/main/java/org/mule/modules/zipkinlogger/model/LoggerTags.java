
package org.mule.modules.zipkinlogger.model;

import java.util.ArrayList;
import java.util.List;

public class LoggerTags {

    private List<BinaryAnnotation> binaryAnnotations = new ArrayList<BinaryAnnotation>();

    public LoggerTags(ArrayList<BinaryAnnotation> arrayList) {
    	this.binaryAnnotations = arrayList;
	}

	public List<BinaryAnnotation> getBinaryAnnotations() {
        return binaryAnnotations;
    }

    public void setBinaryAnnotations(List<BinaryAnnotation> binaryAnnotations) {
        this.binaryAnnotations = binaryAnnotations;
    }

}
