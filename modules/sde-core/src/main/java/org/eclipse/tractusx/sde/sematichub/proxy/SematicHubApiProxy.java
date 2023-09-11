package org.eclipse.tractusx.sde.sematichub.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.JsonNode;

@FeignClient(value = "SematicHubApiProxy", url = "${semantics.backend.hostname}", configuration = SematicHubApiProxyConfiguration.class)
public interface SematicHubApiProxy {

	@GetMapping(value = "/models")
	public JsonNode getAllSematicModels(@RequestParam("status") String status,
			@RequestParam("pageSize") Integer pageSize, @RequestParam("page") Integer page);
	
	@GetMapping(value = "/models/{urn}/json-schema")
	public JsonNode getJsonSchemaOfSubmodel(@PathVariable("urn") String urn);
	
	@GetMapping(value = "/models/{urn}/example-payload")
	public JsonNode getExampleOfSubmodel(@PathVariable("urn") String urn);

}
