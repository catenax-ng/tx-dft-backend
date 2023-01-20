/********************************************************************************
 * Copyright (c) 2022 Critical TechWorks GmbH
 * Copyright (c) 2022 BMW GmbH
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.submodels.apr.steps;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerDigitalTwinUseCaseException;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.common.utils.UUIdGenerator;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.Endpoint;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.GlobalAssetId;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.KeyValuePair;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.ProtocolInformation;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.SemanticId;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.CreateSubModelRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellDescriptorRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellLookupRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellDescriptorResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellLookupResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelListResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelResponse;
import org.eclipse.tractusx.sde.digitaltwins.gateways.external.DigitalTwinGateway;
import org.eclipse.tractusx.sde.submodels.apr.model.AspectRelationship;
import org.eclipse.tractusx.sde.submodels.spt.entity.AspectEntity;
import org.eclipse.tractusx.sde.submodels.spt.mapper.AspectMapper;
import org.eclipse.tractusx.sde.submodels.spt.model.Aspect;
import org.eclipse.tractusx.sde.submodels.spt.repository.AspectRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.SneakyThrows;

@Service
public class DigitalTwinsAspectRelationShipCsvHandlerUseCase extends Step {

	private static final String PART_INSTANCE_ID = "partInstanceId";
	private static final String MANUFACTURER_PART_ID = "manufacturerPartId";
	private static final String MANUFACTURER_ID = "manufacturerId";
	private static final String HTTP = "HTTP";
	private static final String HTTPS = "HTTPS";
	private static final String SEMANTIC_ID = "urn:bamm:io.catenax.assembly_part_relationship:1.1.0#AssemblyPartRelationship";
	private static final String ID_SHORT = "assemblyPartRelationship";
	private static final String ENDPOINT_PROTOCOL_VERSION = "1.0";

	private final DigitalTwinGateway gateway;
	private final AspectRepository aspectRepository;
	private final AspectMapper aspectMapper;

	@Value(value = "${manufacturerId}")
	private String manufacturerId;
	@Value(value = "${edc.hostname}")
	private String edcEndpoint;

	public DigitalTwinsAspectRelationShipCsvHandlerUseCase(DigitalTwinGateway gateway,
			AspectRepository aspectRepository, AspectMapper aspectMapper) {
		this.gateway = gateway;
		this.aspectRepository = aspectRepository;
		this.aspectMapper = aspectMapper;
	}

	@SneakyThrows
	public AspectRelationship run(AspectRelationship aspectRelationShip) throws CsvHandlerDigitalTwinUseCaseException {
		try {
			return doRun(aspectRelationShip);
		} catch (Exception e) {
			throw new ServiceException(aspectRelationShip.getRowNumber() + ": DigitalTwins: " + e.getMessage());
		}
	}

	private AspectRelationship doRun(AspectRelationship aspectRelationShip)
			throws CsvHandlerUseCaseException, CsvHandlerDigitalTwinUseCaseException {
		ShellLookupRequest shellLookupRequest = getShellLookupRequest(aspectRelationShip);
		ShellLookupResponse shellIds = gateway.shellLookup(shellLookupRequest);

		String shellId;

		if (shellIds.isEmpty()) {
			shellId = createShellDescriptor(aspectRelationShip, shellLookupRequest);
		} else if (shellIds.size() == 1) {
			logDebug(String.format("Shell id found for '%s'", shellLookupRequest.toJsonString()));
			shellId = shellIds.stream().findFirst().orElse(null);
			logDebug(String.format("Shell id '%s'", shellId));
		} else {
			throw new CsvHandlerDigitalTwinUseCaseException(
					String.format("Multiple id's found on childAspect %s", shellLookupRequest.toJsonString()));
		}

		aspectRelationShip.setShellId(shellId);
		SubModelListResponse subModelResponse = gateway.getSubModels(shellId);
		SubModelResponse foundSubmodel = null;
		if (subModelResponse != null) {
			foundSubmodel = subModelResponse.stream().filter(x -> ID_SHORT.equals(x.getIdShort())).findFirst()
					.orElse(null);
			if (foundSubmodel != null)
				aspectRelationShip.setSubModelId(foundSubmodel.getIdentification());
		}

		if (subModelResponse == null || foundSubmodel == null) {
			logDebug(String.format("No submodels for '%s'", shellId));
			CreateSubModelRequest createSubModelRequest = getCreateSubModelRequest(aspectRelationShip);
			gateway.createSubModel(shellId, createSubModelRequest);
			aspectRelationShip.setSubModelId(createSubModelRequest.getIdentification());
		} else {
			aspectRelationShip.setUpdated(CommonConstants.UPDATED_Y);
			logDebug("Complete Digital Twins Update Update Digital Twins");

		}
		return aspectRelationShip;
	}

	private String createShellDescriptor(AspectRelationship aspectRelationShip, ShellLookupRequest shellLookupRequest)
			throws CsvHandlerUseCaseException {
		String shellId;
		logDebug(String.format("No shell id for '%s'", shellLookupRequest.toJsonString()));
		AspectEntity aspectEntity = null;
		if (aspectRelationShip.hasOptionalParentIdentifier()) {
			aspectEntity = aspectRepository.findByIdentifiers(aspectRelationShip.getParentPartInstanceId(),
					aspectRelationShip.getParentManufacturerPartId(),
					aspectRelationShip.getParentOptionalIdentifierKey(),
					aspectRelationShip.getParentOptionalIdentifierValue());
		} else {
			aspectEntity = aspectRepository.findByIdentifiers(aspectRelationShip.getParentPartInstanceId(),
					aspectRelationShip.getParentManufacturerPartId());
		}

		if (aspectEntity == null) {
			throw new CsvHandlerUseCaseException(aspectRelationShip.getRowNumber(), "No parent aspect found");
		}

		ShellDescriptorRequest aasDescriptorRequest = getShellDescriptorRequest(aspectMapper.mapFrom(aspectEntity));
		ShellDescriptorResponse result = gateway.createShellDescriptor(aasDescriptorRequest);
		shellId = result.getIdentification();
		logDebug(String.format("Shell created with id '%s'", shellId));

		return shellId;
	}

	private ShellLookupRequest getShellLookupRequest(AspectRelationship aspectRelationShip) {
		ShellLookupRequest shellLookupRequest = new ShellLookupRequest();
		shellLookupRequest.addLocalIdentifier(PART_INSTANCE_ID, aspectRelationShip.getParentPartInstanceId());
		shellLookupRequest.addLocalIdentifier(MANUFACTURER_PART_ID, aspectRelationShip.getParentManufacturerPartId());
		shellLookupRequest.addLocalIdentifier(MANUFACTURER_ID, manufacturerId);

		if (aspectRelationShip.hasOptionalParentIdentifier()) {
			shellLookupRequest.addLocalIdentifier(aspectRelationShip.getParentOptionalIdentifierKey(),
					aspectRelationShip.getParentOptionalIdentifierValue());
		}

		return shellLookupRequest;
	}

	@SneakyThrows
	private CreateSubModelRequest getCreateSubModelRequest(AspectRelationship aspectRelationShip) {
		ArrayList<String> value = new ArrayList<>();
		value.add(SEMANTIC_ID);
		String identification = UUIdGenerator.getUrnUuid();
		SemanticId semanticId = new SemanticId(value);

		List<Endpoint> endpoints = new ArrayList<>();
		endpoints.add(Endpoint.builder().endpointInterface(HTTP)
				.protocolInformation(ProtocolInformation.builder()
						.endpointAddress(String.format(String.format("%s%s/%s-%s%s", edcEndpoint,
								manufacturerId, aspectRelationShip.getShellId(), identification,
								"/submodel?content=value&extent=WithBLOBValue")))
						.endpointProtocol(HTTPS).endpointProtocolVersion(ENDPOINT_PROTOCOL_VERSION).build())
				.build());

		return CreateSubModelRequest.builder().idShort(ID_SHORT).identification(identification).semanticId(semanticId)
				.endpoints(endpoints).build();
	}

	private ShellDescriptorRequest getShellDescriptorRequest(Aspect aspect) {
		ArrayList<KeyValuePair> specificIdentifiers = new ArrayList<>();
		setSpecifiers(specificIdentifiers, aspect);
		List<String> values = new ArrayList<>();
		values.add(aspect.getUuid());
		GlobalAssetId globalIdentifier = new GlobalAssetId(values);

		return ShellDescriptorRequest.builder()
				.idShort(String.format("%s_%s_%s", aspect.getNameAtManufacturer(), manufacturerId,
						aspect.getManufacturerPartId()))
				.globalAssetId(globalIdentifier).specificAssetIds(specificIdentifiers)
				.identification(UUIdGenerator.getUrnUuid()).build();
	}

	private void setSpecifiers(final ArrayList<KeyValuePair> specificIdentifiers, Aspect aspect) {
		specificIdentifiers.add(new KeyValuePair(PART_INSTANCE_ID, aspect.getPartInstanceId()));
		specificIdentifiers.add(new KeyValuePair(MANUFACTURER_PART_ID, aspect.getManufacturerPartId()));
		specificIdentifiers.add(new KeyValuePair(MANUFACTURER_ID, manufacturerId));
		if (aspect.hasOptionalIdentifier()) {
			specificIdentifiers
					.add(new KeyValuePair(aspect.getOptionalIdentifierKey(), aspect.getOptionalIdentifierValue()));
		}
	}

}