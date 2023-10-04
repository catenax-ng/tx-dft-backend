package org.eclipse.tractusx.sde.core.controller;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.tractusx.sde.sematichub.proxy.SematicHubApiProxy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SematicHubProxyController {

	private static final String PROPERTIES = "properties";
	private static final String REQUIRED = "required";

	private final SematicHubApiProxy sematicHubApiProxy;

	@GetMapping(value = "/hub/models")
	public JsonNode getAllSematicModels(@RequestParam("status") String status,
			@RequestParam("pageSize") Integer pageSize, @RequestParam("page") Integer page) {
		return sematicHubApiProxy.getAllSematicModels(status, pageSize, page);
	}

	@GetMapping(value = "/hub/models/{urn}/json-schema")
	public JsonNode getJsonSchemaOfSubmodel(@PathVariable("urn") String urn) {
		return sematicHubApiProxy.getJsonSchemaOfSubmodel(urn);
	}

	@GetMapping(value = "/hub/models/{urn}/example-payload")
	public JsonNode getExampleOfSubmodel(@PathVariable("urn") String urn) {
		return sematicHubApiProxy.getExampleOfSubmodel(urn);
	}

	@GetMapping(value = "/hub/models/{urn}/toflatcsv")
	public Map<String, Object> toflatcsv(@PathVariable("urn") String urn) {
		SortedMap<String, Object> map = new TreeMap<>();
		Map<String, Object> response = new TreeMap<>();
		Set<String> required = new HashSet<>();
		try {
			JsonNode scehmaNode = sematicHubApiProxy.getJsonSchemaOfSubmodel(urn);
			JsonNode exampleOfSubmodel = sematicHubApiProxy.getExampleOfSubmodel(urn);
			findSchemaNode("", null, scehmaNode, map, required);
			response.put("description", scehmaNode.get("description"));
			response.put(PROPERTIES, map);
			required.retainAll(map.keySet());
			response.put(REQUIRED, required);
			response.put("examples", getCSVExampleObject(map, exampleOfSubmodel));
			response.put("examplePayload", exampleOfSubmodel);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	private Object getCSVExampleObject(SortedMap<String, Object> map, JsonNode exampleOfSubmodel) {
		Map<String, Object> exampleMap = new TreeMap<>();
		map.entrySet().forEach(entry -> {
			String key = entry.getKey();
			String replace = key.replace(".", "/");
			replace = replace.replace("[", "/");
			replace = replace.replace("]", "/");
			replace = replace.replace("//", "/");
			String valueAt = exampleOfSubmodel.at("/" + replace).asText();
			exampleMap.put(key, valueAt);
		});
		return exampleMap;
	}

	public void addKeys(String currentPath, JsonNode jsonNode, Map<String, String> map) {
		if (jsonNode.isObject()) {
			ObjectNode objectNode = (ObjectNode) jsonNode;
			Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();
			String pathPrefix = currentPath.isEmpty() ? "" : currentPath + ".";

			while (iter.hasNext()) {
				Map.Entry<String, JsonNode> entry = iter.next();
				addKeys(pathPrefix + entry.getKey(), entry.getValue(), map);
			}
		} else if (jsonNode.isArray()) {
			ArrayNode arrayNode = (ArrayNode) jsonNode;
			for (int i = 0; i < arrayNode.size(); i++) {
				addKeys(currentPath + "[" + i + "]", arrayNode.get(i), map);
			}
		} else if (jsonNode.isValueNode()) {
			ValueNode valueNode = (ValueNode) jsonNode;
			map.put(currentPath, valueNode.asText());
		}
	}

	private Map<String, Object> findSchemaNode(String key, JsonNode currentNode, JsonNode scehmaNode,
			Map<String, Object> map, Set<String> required) {
		JsonNode properties = null;
		JsonNode requireds = null;

		if (currentNode == null) {
			properties = scehmaNode.get(PROPERTIES);
			requireds = scehmaNode.get(REQUIRED);
		} else {
			properties = currentNode.get(PROPERTIES);
			requireds = currentNode.get(REQUIRED);
		}
		if (requireds != null) {
			String pathPrefix = key.isEmpty() ? "" : key + ".";
			requireds.forEach(element -> required.add(pathPrefix + element.asText()));
		}

		validateAndAddToMap(key, currentNode, scehmaNode, map, required, properties);
		return map;
	}

	private void validateAndAddToMap(String key, JsonNode currentNode, JsonNode scehmaNode, Map<String, Object> map,
			Set<String> required, JsonNode properties) {
		if (properties == null && currentNode != null) {
			JsonNode jsonNode = currentNode.get("type");
			addToMap(key, currentNode, scehmaNode, map, jsonNode, required);
		} else if (properties != null) {
			Set<Entry<String, JsonNode>> propFields = properties.properties();
			propFields.stream().forEach(entry -> {
				String pathPrefix = key.isEmpty() ? "" : key + ".";
				JsonNode entryValue = entry.getValue();
				JsonNode refField = entryValue.get("$ref");
				if (refField != null && !refField.isNull()) {
					String replace = refField.asText().replace("#", "");
					JsonNode refFieldValue = scehmaNode.at(replace);
					findSchemaNode(pathPrefix + entry.getKey(), refFieldValue, scehmaNode, map, required);
				} else
					map.put(pathPrefix + entry.getKey(), entryValue);
			});
		}
	}

	private void addToMap(String key, JsonNode currentNode, JsonNode scehmaNode, Map<String, Object> map,
			JsonNode jsonNode, Set<String> required) {
		if ("array".equalsIgnoreCase(jsonNode.asText())) {
			String pathPrefix = key.isEmpty() ? "" : key + "[0]";
			JsonNode refField = currentNode.at("/items/$ref");
			if (refField.isTextual()) {
				String replace = refField.asText().replace("#", "");
				JsonNode refFieldValue = scehmaNode.at(replace);
				findSchemaNode(pathPrefix, refFieldValue, scehmaNode, map, required);
			} else {
				map.put(key, currentNode);
			}
		} else
			map.put(key, currentNode);
	}
}
