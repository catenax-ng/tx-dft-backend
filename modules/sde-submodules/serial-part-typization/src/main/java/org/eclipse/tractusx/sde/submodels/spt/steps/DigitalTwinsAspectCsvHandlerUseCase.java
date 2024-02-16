/********************************************************************************
 * Copyright (c) 2022 Critical TechWorks GmbH
 * Copyright (c) 2022 BMW GmbH
 * Copyright (c) 2022, 2024 T-Systems International GmbH
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.submodels.spt.steps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerDigitalTwinUseCaseException;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.CreateSubModelRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellDescriptorRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellLookupRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellDescriptorResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelListResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelResponse;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DigitalTwinsFacilitator;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DigitalTwinsUtility;
import org.eclipse.tractusx.sde.submodels.spt.model.Aspect;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class DigitalTwinsAspectCsvHandlerUseCase extends Step {

	private final DigitalTwinsFacilitator digitalTwinsFacilitator;

	private final DigitalTwinsUtility digitalTwinsUtility;

	@SneakyThrows
	public Aspect run(Aspect aspect, PolicyModel policy) throws CsvHandlerDigitalTwinUseCaseException {
		try {
			return doRun(aspect, policy);
		} catch (Exception e) {
			throw new CsvHandlerUseCaseException(aspect.getRowNumber(), ": DigitalTwins: " + e.getMessage());
		}
	}

	@SneakyThrows
	private Aspect doRun(Aspect aspect, PolicyModel policy) throws CsvHandlerDigitalTwinUseCaseException {
		ShellLookupRequest shellLookupRequest = digitalTwinsUtility.getShellLookupRequest(getSpecificAssetIds(aspect));
		List<String> shellIds = digitalTwinsFacilitator.shellLookup(shellLookupRequest);

		String shellId;
		
		ShellDescriptorRequest aasDescriptorRequest = digitalTwinsUtility.getShellDescriptorRequest(
				aspect.getNameAtManufacturer(), aspect.getManufacturerPartId(), aspect.getUuid(),
				getSpecificAssetIds(aspect), policy);

		if (shellIds.isEmpty()) {
			logDebug(String.format("No shell id for '%s'", shellLookupRequest.toJsonString()));
			ShellDescriptorResponse result = digitalTwinsFacilitator.createShellDescriptor(aasDescriptorRequest);
			shellId = result.getIdentification();
			logDebug(String.format("Shell created with id '%s'", shellId));
		} else if (shellIds.size() == 1) {
			logDebug(String.format("Shell id found for '%s'", shellLookupRequest.toJsonString()));
			shellId = shellIds.stream().findFirst().orElse(null);
			logDebug(String.format("Shell id '%s'", shellId));
		} else {
			throw new CsvHandlerDigitalTwinUseCaseException(
					String.format("Multiple ids found on aspect %s", shellLookupRequest.toJsonString()));
		}

		aspect.setShellId(shellId);
		SubModelListResponse subModelResponse = digitalTwinsFacilitator.getSubModels(shellId);
		SubModelResponse foundSubmodel = null;
		if (subModelResponse != null) {
			foundSubmodel = subModelResponse.getResult().stream()
					.filter(x -> getIdShortOfModel().equals(x.getIdShort())).findFirst().orElse(null);
			if (foundSubmodel != null)
				aspect.setSubModelId(foundSubmodel.getId());
		}

		if (subModelResponse == null || foundSubmodel == null) {
			logDebug(String.format("No submodels for '%s'", shellId));
			
			CreateSubModelRequest createSubModelRequest = digitalTwinsUtility
					.getCreateSubModelRequest(aspect.getShellId(), getsemanticIdOfModel(), getIdShortOfModel());
			
			digitalTwinsFacilitator.updateShellDetails(shellId, aasDescriptorRequest, createSubModelRequest);
			
			aspect.setSubModelId(createSubModelRequest.getId());
			
		} else {
			//There is no need to send submodel because of nothing to change in it so sending null of it
			digitalTwinsFacilitator.updateShellDetails(shellId, aasDescriptorRequest, null);
			aspect.setUpdated(CommonConstants.UPDATED_Y);
			logDebug("Complete Digital Twins Update Update Digital Twins");
		}

		return aspect;
	}

	private Map<String, String> getSpecificAssetIds(Aspect aspect) {
		Map<String, String> specificIdentifiers = new HashMap<>();
		specificIdentifiers.put(CommonConstants.PART_INSTANCE_ID, aspect.getPartInstanceId());
		specificIdentifiers.put(CommonConstants.MANUFACTURER_PART_ID, aspect.getManufacturerPartId());
		specificIdentifiers.put(CommonConstants.MANUFACTURER_ID, digitalTwinsUtility.getManufacturerId());
		if (aspect.hasOptionalIdentifier()) {
			specificIdentifiers.put(aspect.getOptionalIdentifierKey(), aspect.getOptionalIdentifierValue());
		}

		return specificIdentifiers;
	}

}