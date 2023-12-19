/********************************************************************************
 * Copyright (c) 2023 T-Systems International GmbH
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.submodels.pcf.steps;

import java.util.Map;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.EDCHandlerStep;
import org.eclipse.tractusx.sde.submodels.pcf.entity.PcfEntity;
import org.eclipse.tractusx.sde.submodels.pcf.model.PcfAspect;
import org.eclipse.tractusx.sde.submodels.pcf.service.PcfService;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class EDCPcfHandlerUseCase extends Step {

	private final EDCHandlerStep edcHandlerStep;
	private final PcfService aspectService;

	@SneakyThrows
	public PcfAspect run(String submodel, PcfAspect input, String processId) {
		String shellId = input.getShellIdforPcf();
		String subModelId = input.getSubModelIdforPcf();

		try {

			String assetId = shellId + "-" + subModelId;
			JsonNode asset = edcHandlerStep.getAsset(assetId);
			if (asset == null) {

				edcProcessingforAspect(submodel, shellId, subModelId, input);

			} else {

				deleteEDCFirstForUpdate(submodel, input, processId);
				edcProcessingforAspect(submodel, shellId, subModelId, input);
				input.setUpdatedforPcf(CommonConstants.UPDATED_Y);
			}

			return input;
		} catch (Exception e) {
			throw new CsvHandlerUseCaseException(input.getRowNumberforPcf(), "EDC: " + e.getMessage());
		}
	}

	@SneakyThrows
	private void deleteEDCFirstForUpdate(String submodel, PcfAspect input, String processId) {
		try {
			PcfEntity entity = aspectService.readEntity(input.getId());
			aspectService.deleteEDCAsset(entity);
		} catch (Exception e) {
			if (!e.getMessage().contains("404 Not Found")) {
				throw new ServiceException("Unable to delete EDC offer for update: " + e.getMessage());
			}
		}
	}

	@SneakyThrows
	private void edcProcessingforAspect(String submodel, String shellId, String subModelId, PcfAspect input) {

		Map<String, String> createEDCOffer = edcHandlerStep.createEDCOffer(submodel, shellId, subModelId,
				input.getUuid(), input.getBpnNumbersforPcf(), input.getUsagePoliciesforPcf());

		// EDC transaction information for DB
		input.setAssetIdforPcf(createEDCOffer.get("assetId"));
		input.setAccessPolicyIdforPcf(createEDCOffer.get("accessPolicyId"));
		input.setUsagePolicyIdforPcf(createEDCOffer.get("usagePolicyId"));
		input.setContractDefinationIdforPcf(createEDCOffer.get("contractDefinitionId"));
	}

}