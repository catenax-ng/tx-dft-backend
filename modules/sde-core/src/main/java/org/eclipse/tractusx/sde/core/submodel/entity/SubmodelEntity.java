package org.eclipse.tractusx.sde.core.submodel.entity;

import java.util.List;

import org.eclipse.tractusx.sde.core.utils.ListToStringConverter;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "submodel_tbl")
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Cacheable(value = false)
public class SubmodelEntity {

	@Id
	private String id;
	private String title;
	private String version;
	private String semanticId;
	private String submodelIdShort;
	private String shortDescription;
	
	@Lob
	private String description;
	
	private String globalAssetId;

	@Column(columnDefinition = "TEXT")
	@Convert(converter = JsonNodeToStringConverter.class)
	private JsonNode examples;

	@Column(columnDefinition = "TEXT")
	@Convert(converter = JsonNodeToStringConverter.class)
	private JsonNode examplePayload;

	@Column(columnDefinition = "TEXT")
	@Convert(converter = JsonNodeToStringConverter.class)
	private JsonNode required;

	@Column(columnDefinition = "TEXT")
	@Convert(converter = JsonNodeToStringConverter.class)
	private JsonNode digitalTwin;

	@Column(columnDefinition = "TEXT")
	@Convert(converter = JsonNodeToStringConverter.class)
	private JsonNode properties;

	@Column(columnDefinition = "TEXT")
	@Convert(converter = JsonNodeToStringConverter.class)
	private JsonNode specificAssetIds;

	@Column(columnDefinition = "TEXT")
	@Convert(converter = ListToStringConverter.class)
	private List<String> shellIdShortId;

	@Column(columnDefinition = "TEXT")
	@Convert(converter = ListToStringConverter.class)
	private List<String> usecases;

}
