/********************************************************************************
 * Copyright (c) 2022 Critical TechWorks GmbH
 * Copyright (c) 2022 BMW GmbH
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

package org.eclipse.tractusx.sde.submodels.apr.steps;

import java.util.Map;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequest;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequestFactory;
import org.eclipse.tractusx.sde.edc.facilitator.CreateEDCAssetFacilator;
import org.eclipse.tractusx.sde.edc.facilitator.DeleteEDCFacilitator;
import org.eclipse.tractusx.sde.edc.gateways.external.EDCGateway;
import org.eclipse.tractusx.sde.submodels.apr.entity.AspectRelationshipEntity;
import org.eclipse.tractusx.sde.submodels.apr.model.AspectRelationship;
import org.eclipse.tractusx.sde.submodels.apr.service.AspectRelationshipService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EDCAspectRelationshipHandlerUseCase extends Step {

	private final AssetEntryRequestFactory assetFactory;
	private final EDCGateway edcGateway;
	private final CreateEDCAssetFacilator createEDCAssetFacilator;
	private final AspectRelationshipService aspectRelationshipService;
	private final DeleteEDCFacilitator deleteEDCFacilitator;

	@SneakyThrows
	public AspectRelationship run(String submodel, AspectRelationship input, String processId, PolicyModel policy) {
		String shellId = input.getShellId();
		String subModelId = input.getSubModelId();

		try {

			AssetEntryRequest assetEntryRequest = assetFactory.getAssetRequest(submodel,
					getSubmodelShortDescriptionOfModel(), shellId, subModelId, input.getParentUuid());
			if (!edcGateway.assetExistsLookup(assetEntryRequest.getId())) {

				if (CommonConstants.UPDATED_Y.equals(input.getUpdated())
						&& input.getOldSubmodelIdforUpdateCase() != null) {
					deleteIfAnyReferenceExist(input, input.getOldSubmodelIdforUpdateCase());
				}

				edcProcessingforAspectRelationship(assetEntryRequest, input, policy);

			} else {

				deleteEDCFirstForUpdate(submodel, input, processId);
				edcProcessingforAspectRelationship(assetEntryRequest, input, policy);
				input.setUpdated(CommonConstants.UPDATED_Y);
			}

			return input;
		} catch (Exception e) {
			throw new CsvHandlerUseCaseException(input.getRowNumber(), "EDC: " + e.getMessage());
		}
	}

	private void deleteIfAnyReferenceExist(AspectRelationship input, String oldSubModelId) {
		try {
			AspectRelationshipEntity aspectRelationshipEntity = aspectRelationshipService
					.readEntityBySubModelId(oldSubModelId);
			aspectRelationshipService.deleteEDCAsset(aspectRelationshipEntity);
			log.info(
					"Deleted existing EDC asset in update case reference,so EDC end will be single asset for each submodel: "
							+ oldSubModelId);
		} catch (NoDataFoundException e) {
			log.warn("In deleteIfAnyReferenceExist The EDC assetInfo not found in local database for delete, looking EDC connector and going to delete asset");
			deleteEDCFacilitator.findEDCOfferInformation(input.getShellId(), input.getSubModelId());
		} catch (Exception e) {
			log.warn(
					"Trying to delete existing EDC asset in update case reference,so EDC end will be single asset for each submodel: "
							+ e.getMessage());
		}

	}

	@SneakyThrows
	private void deleteEDCFirstForUpdate(String submodel, AspectRelationship input, String processId) {
		try {
			AspectRelationshipEntity aspectRelationshipEntity = aspectRelationshipService
					.readEntity(input.getChildUuid());
			aspectRelationshipService.deleteEDCAsset(aspectRelationshipEntity);
		} catch (NoDataFoundException e) {
			log.warn("In deleteEDCFirstForUpdate The EDC assetInfo not found in local database for delete, looking EDC connector and going to delete asset");
			deleteEDCFacilitator.findEDCOfferInformation(input.getShellId(), input.getSubModelId());
		} catch (Exception e) {
			if (!e.getMessage().contains("404 Not Found")) {
				throw new ServiceException("Exception in EDC delete request process: " + e.getMessage());
			}
		}
	}

	@SneakyThrows
	private void edcProcessingforAspectRelationship(AssetEntryRequest assetEntryRequest, AspectRelationship input, PolicyModel policy) {

		Map<String, String> createEDCAsset = createEDCAssetFacilator.createEDCAsset(assetEntryRequest, policy);

		// EDC transaction information for DB
		input.setAssetId(assetEntryRequest.getId());
		input.setAccessPolicyId(createEDCAsset.get("accessPolicyId"));
		input.setUsagePolicyId(createEDCAsset.get("usagePolicyId"));
		input.setContractDefinationId(createEDCAsset.get("contractDefinitionId"));
	}
}
