package org.eclipse.tractusx.sde.core.controller;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.tractusx.sde.sematichub.proxy.SematicHubApiProxy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SematicHubProxyController {

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

	@PostMapping(value = "/hub/models/json/toflatcsv")
	public Set<String> toflatcsv(@RequestBody String jsonString) {
		Map<String, String> map = new TreeMap<>();
		try {
			addKeys("", new ObjectMapper().readTree(jsonString), map);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map.keySet();
	}

	private void addKeys(String currentPath, JsonNode jsonNode, Map<String, String> map) {
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
}
