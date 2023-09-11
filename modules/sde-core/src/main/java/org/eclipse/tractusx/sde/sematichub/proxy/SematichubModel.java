package org.eclipse.tractusx.sde.sematichub.proxy;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SematichubModel {
	
	private String submodel;
	private String title;
	private String version;
	private String submodelIdShort;
	private String shortDescription;
	private String description;
	private String examplePayload;
	private String digitalTwin;
	private List<String> csvFields;
	private List<String> globalAssetId;
	private List<String> localIdentifier;
	private List<String> specificAssetIds;
	private List<String> shellIdShortId;
	

}
