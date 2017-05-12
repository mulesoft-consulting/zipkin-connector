package org.mule.modules.zipkinlogger.datasense;

import java.util.ArrayList;
import java.util.List;

import org.mule.api.annotations.MetaDataKeyRetriever;
import org.mule.api.annotations.MetaDataOutputRetriever;
import org.mule.api.annotations.MetaDataRetriever;
import org.mule.api.annotations.components.MetaDataCategory;
import org.mule.common.metadata.DefaultMetaData;
import org.mule.common.metadata.DefaultMetaDataKey;
import org.mule.common.metadata.MetaData;
import org.mule.common.metadata.MetaDataKey;
import org.mule.common.metadata.MetaDataModel;
import org.mule.common.metadata.builder.DefaultMetaDataBuilder;
import org.mule.modules.zipkinlogger.model.LoggerData;
import org.mule.modules.zipkinlogger.model.TraceData;

@MetaDataCategory
public class DefaultCategory {

	@MetaDataKeyRetriever
	public List<MetaDataKey> getKeys() throws Exception {
		List<MetaDataKey> dropdownKeys = new ArrayList<MetaDataKey>();

		dropdownKeys.add(new DefaultMetaDataKey("join_id", "Join Parent Span"));
		dropdownKeys.add(new DefaultMetaDataKey("standalone_id", "Standalone Span"));
		return dropdownKeys;
	}

	@MetaDataRetriever
	public MetaData getPayloadModel(MetaDataKey entityKey) throws Exception {
		MetaDataModel standaloneModel = new DefaultMetaDataBuilder().createPojo(LoggerData.class).build();
		return new DefaultMetaData(standaloneModel);
	}

	@MetaDataOutputRetriever
	public MetaData getPayloadOutputModel(MetaDataKey entityKey) throws Exception {
		MetaDataModel standaloneModel = new DefaultMetaDataBuilder().createPojo(TraceData.class).build();
		return new DefaultMetaData(standaloneModel);
	}

}
