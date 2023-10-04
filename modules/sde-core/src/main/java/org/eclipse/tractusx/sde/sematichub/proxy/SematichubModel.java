package org.eclipse.tractusx.sde.sematichub.proxy;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SematichubModel {

	private String id;
	private String title;
	private String version;
	private String semanticId;
	private String submodelIdShort;
	private String shortDescription;
	private String description;
	private String globalAssetId;
	private JsonNode examples;
	private JsonNode examplePayload;
	private JsonNode required;
	private JsonNode digitalTwin;
	private JsonNode properties;
	private JsonNode specificAssetIds;
	private List<String> shellIdShortId;
	private List<String> usecases;

}
