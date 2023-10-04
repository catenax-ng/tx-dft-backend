/********************************************************************************
 * Copyright (c) 2022, 2023 T-Systems International GmbH
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.tractusx.sde.core.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.eclipse.tractusx.sde.common.mapper.SubmodelMapper;
import org.eclipse.tractusx.sde.common.model.Submodel;
import org.eclipse.tractusx.sde.core.registry.SubmodelRegistration;
import org.eclipse.tractusx.sde.core.registry.UsecaseRegistration;
import org.eclipse.tractusx.sde.core.submodel.entity.SubmodelEntity;
import org.eclipse.tractusx.sde.core.submodel.entity.SubmodelSematicModelMapper;
import org.eclipse.tractusx.sde.core.submodel.repository.SubmodelRepository;
import org.eclipse.tractusx.sde.sematichub.proxy.SematichubModel;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SubmodelService {

	private final SubmodelRegistration submodelRegistration;

	private final UsecaseRegistration usecaseRegistry;

	private final SubmodelMapper submodelMapper;

	private final SubmodelSematicModelMapper submodelSematicModelMapper;

	private final SubmodelRepository submodelRepository;

	public List<Map<String, String>> findAllSubmodels(List<String> usecases) {

		Set<String> neededSubmodelList = usecaseRegistry.neededSubmodelList(usecases);

		List<Map<String, String>> ls = new ArrayList<>();

		submodelRegistration.getModels().forEach(obj -> {
			if (neededSubmodelList.contains(obj.getId()) || usecases == null || usecases.isEmpty()) {
				Map<String, String> sbBuild = new LinkedHashMap<>();
				sbBuild.put("id", obj.getId());
				sbBuild.put("name", obj.getName());
				sbBuild.put("version", obj.getVersion());
				sbBuild.put("semanticId", obj.getSemanticId());
				ls.add(sbBuild);
			}
		});
		return ls;
	}

	public List<Map<Object, Object>> getAllSubmodelswithDetails(List<String> usecases) {

		Set<String> neededSubmodelList = usecaseRegistry.neededSubmodelList(usecases);

		List<Map<Object, Object>> ls = new ArrayList<>();

		submodelRegistration.getModels().forEach(obj -> {
			if (neededSubmodelList.contains(obj.getId()) || usecases == null || usecases.isEmpty()) {
				ls.add(submodelMapper.jsonPojoToMap(obj.getSchema()));
			}
		});
		return ls;
	}

	public Map<Object, Object> findSubmodelByName(String submodelName) {
		return readValue(submodelName).map(e -> submodelMapper.jsonPojoToMap(e.getSchema()))
				.orElseThrow(() -> new NoDataFoundException("No data found for " + submodelName));
	}

	private Optional<Submodel> readValue(String submodelName) {
		return submodelRegistration.getModels().stream()
				.filter(obj -> obj.getId().equalsIgnoreCase(submodelName.toLowerCase())).findFirst();

	}

	public Submodel findSubmodelByNameAsSubmdelObject(String submodelName) {
		return readValue(submodelName)
				.orElseThrow(() -> new ValidationException(submodelName + " submodel is not supported"));
	}

	public List<Submodel> getAllSubmodels() {
		return submodelRegistration.getModels();
	}

	public List<Map<Object, Object>> readSubmodels() {

		List<SubmodelEntity> findAll = submodelRepository.findAll();
		return findAll.stream().map(entity -> {
			return submodelMapper.jsonPojoToMap(mapFromEntityToJson(entity));
		}).toList();
	}

	@PostConstruct
	public void loadSubmodelsFromDatabase() {

		List<SubmodelEntity> findAll = submodelRepository.findAll();
		findAll.stream().forEach(entity -> {
			JsonObject mapFromEntityToJson = mapFromEntityToJson(entity);
			submodelRegistration.register(mapFromEntityToJson);
		});
	}

	private JsonObject mapFromEntityToJson(SubmodelEntity entity) {
		JsonObject entitytoPojo = submodelSematicModelMapper.entitytoPojo(entity);
		JsonObject schemaObject = new JsonObject();
		schemaObject.addProperty("$schema", "https://json-schema.org/draft/2019-09/schema");
		schemaObject.addProperty("$id", "http://example.com/example.json");
		schemaObject.addProperty("type", "array");
		schemaObject.addProperty("id", entity.getId());
		schemaObject.addProperty("idShort", getValue(entitytoPojo, "idShort"));
		schemaObject.addProperty("version", getValue(entitytoPojo, "version"));
		schemaObject.addProperty("semantic_id", getValue(entitytoPojo, "semanticId"));
		schemaObject.addProperty("title", getValue(entitytoPojo, "title"));
		schemaObject.addProperty("description", getValue(entitytoPojo, "description"));
		schemaObject.addProperty("shortDescription", getValue(entitytoPojo, "shortDescription"));

		JsonObject items = new JsonObject();
		items.addProperty("type", "object");
		items.add("required", entitytoPojo.get("required"));
		items.add("properties", prepareProperties(entitytoPojo));
		schemaObject.add("items", items);
		schemaObject.add("examples", entitytoPojo.get("examples"));
		return schemaObject;
	}

	private String getValue(JsonObject entitytoPojo, String prop) {
		return entitytoPojo.get(prop) == null ? "" : entitytoPojo.get(prop).getAsString();
	}

	private JsonObject prepareProperties(JsonObject entitytoPojo) {
		JsonObject jPropObject = new JsonObject();
		JsonArray asJsonArray = entitytoPojo.get("properties").getAsJsonArray();
		asJsonArray.forEach(element -> {
			JsonObject jsonElement = element.getAsJsonObject().get("schemadetails").getAsJsonObject();
			jsonElement.addProperty("title", element.getAsJsonObject().get("csvFieldName").getAsString());
			jPropObject.add(element.getAsJsonObject().get("schemaFieldName").getAsString(), jsonElement);
		});
		return jPropObject;
	}

	public Map<Object, Object> saveSubmodel(SematichubModel sematichubModel) {
		sematichubModel.setId(UUID.randomUUID().toString());
		SubmodelEntity savedEntity = submodelRepository.save(submodelSematicModelMapper.mapFrom(sematichubModel));
		JsonObject mapFromEntityToJson = mapFromEntityToJson(savedEntity);
		submodelRegistration.register(mapFromEntityToJson);
		return submodelMapper.jsonPojoToMap(mapFromEntityToJson);
	}

}
