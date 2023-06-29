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
package org.eclipse.tractusx.sde.digitaltwins.facilitator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.utils.UUIdGenerator;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.Endpoint;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.GlobalAssetId;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.KeyValuePair;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.ProtocolInformation;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.SemanticId;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.CreateSubModelRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellDescriptorRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Getter;
import lombok.SneakyThrows;

@Component
@Getter
public class DigitalTwinsUtility {

	@Value(value = "${manufacturerId}")
	public String manufacturerId;

	@Value(value = "${edc.hostname}")
	public String edcEndpoint;

	ObjectMapper mapper = new ObjectMapper();

	@SneakyThrows
	public ShellDescriptorRequest getShellDescriptorRequest(List<KeyValuePair> specificIdentifiers, Object object) {

		JsonNode jsonNode = mapper.convertValue(object, ObjectNode.class);
		
		List<String> values = new ArrayList<>();
		values.add(getFieldFromJsonNode(jsonNode, "uuid"));
		GlobalAssetId globalIdentifier = new GlobalAssetId(values);

		return ShellDescriptorRequest.builder()
				.idShort(String.format("%s_%s_%s", getFieldFromJsonNode(jsonNode, "name_at_manufacturer"),
						manufacturerId, getFieldFromJsonNode(jsonNode, "manufacturer_part_id")))
				.globalAssetId(globalIdentifier)
				.specificAssetIds(specificIdentifiers)
				.identification(UUIdGenerator.getUrnUuid())
				.build();
	}

	@SneakyThrows
	public CreateSubModelRequest getCreateSubModelRequest(String shellId, String sematicId, String idShortofModel) {
		ArrayList<String> value = new ArrayList<>();
		value.add(sematicId);
		String identification = UUIdGenerator.getUrnUuid();
		SemanticId semanticId = new SemanticId(value);

		List<Endpoint> endpoints = prepareDtEndpoint(shellId, identification);

		return CreateSubModelRequest.builder().idShort(idShortofModel).identification(identification)
				.semanticId(semanticId).endpoints(endpoints).build();
	}
	
	@SneakyThrows
	public CreateSubModelRequest getCreateSubModelRequestForChild(String shellId, String sematicId, String idShortofModel, String identification) {
		ArrayList<String> value = new ArrayList<>();
		value.add(sematicId);
		SemanticId semanticId = new SemanticId(value);

		List<Endpoint> endpoints = prepareDtEndpoint(shellId, identification);

		return CreateSubModelRequest.builder().idShort(idShortofModel).identification(identification)
				.semanticId(semanticId).endpoints(endpoints).build();
	}

	public List<Endpoint> prepareDtEndpoint(String shellId, String submodelIdentification) {
		List<Endpoint> endpoints = new ArrayList<>();
		endpoints.add(Endpoint.builder().endpointInterface(CommonConstants.INTERFACE_EDC)
				.protocolInformation(ProtocolInformation.builder()
						.endpointAddress(edcEndpoint + "/" + encodedUrl(shellId + "-" + submodelIdentification)
								+ CommonConstants.SUBMODEL_CONTEXT_URL)
						.endpointProtocol(CommonConstants.ENDPOINT_PROTOCOL)
						.endpointProtocolVersion(CommonConstants.ENDPOINT_PROTOCOL_VERSION).build())
				.build());
		return endpoints;
	}

	private String encodedUrl(String format) {
		return format.replace(":", "%3A");
	}

	private String getFieldFromJsonNode(JsonNode jnode, String fieldName) {
		if (jnode.get(fieldName) != null)
			return jnode.get(fieldName).asText();
		else
			return "";
	}

}
