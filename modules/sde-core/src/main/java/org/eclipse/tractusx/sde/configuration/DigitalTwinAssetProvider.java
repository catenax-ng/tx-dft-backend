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

package org.eclipse.tractusx.sde.configuration;

import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.common.mapper.EDCAssetEntryRequestFactory;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.EDCHandlerStep;
import org.eclipse.tractusx.sde.common.utils.UUIdGenerator;
import org.eclipse.tractusx.sde.core.properties.SdeCommonProperties;
import org.eclipse.tractusx.sde.edc.core.model.Constraint;
import org.eclipse.tractusx.sde.edc.core.model.ConstraintOperator;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("default")
public class DigitalTwinAssetProvider {

	private final EDCHandlerStep edcHandlerStep;
	private final EDCAssetEntryRequestFactory edcAssetEntryRequestFactory;
	private final SdeCommonProperties sdeCommonProperties;

	@PostConstruct
	@SneakyThrows
	public void init() {

		String assetId = UUIdGenerator.getUuid();

		Constraint criteriaConstraintType = Constraint.builder()
				.leftOperand("type")
				.operator(ConstraintOperator.EQ)
				.rightOperand("data.core.digitalTwinRegistry")
				.build();

		Constraint criteriaConstraint = Constraint.builder()
				.leftOperand("registry")
				.operator(ConstraintOperator.EQ)
				.rightOperand(sdeCommonProperties.getDigitalTwinRegistry())
				.build();

		Map<String, Object> assetProps = edcAssetEntryRequestFactory.getAssetProperties(assetId, "DigitalTwinAsset");

		Map<String, Object> dataAddressProps = edcAssetEntryRequestFactory
				.getDataAddressProperties(sdeCommonProperties.getDigitalTwinRegistry());

		if (sdeCommonProperties.isDDTRManagedThirdparty()) {
			dataAddressProps.put("baseUrl", sdeCommonProperties.getDigitalTwinRegistry());
			dataAddressProps.put("oauth2:scope", sdeCommonProperties.getDigitalTwinAuthenticationScope());
			dataAddressProps.put("oauth2:clientSecret", sdeCommonProperties.getDigitalTwinClientSecret());
			dataAddressProps.remove("oauth2:clientSecretKey");
		} else {
			dataAddressProps.put("baseUrl",
					sdeCommonProperties.getDigitalTwinRegistry() + sdeCommonProperties.getDigitalTwinRegistryURI());
		}

		JsonNode allAssets = edcHandlerStep.getAllAssets(0, 100, List.of(criteriaConstraintType, criteriaConstraint));

		if (allAssets == null || (allAssets.isArray() && allAssets.isEmpty())) {
			Map<String, String> createEDCAsset = edcHandlerStep.createAsset(List.of(), Map.of(), assetProps,
					dataAddressProps);
			log.info("Digital twin asset creates :" + createEDCAsset.toString());
		} else {
			log.info("Digital twin asset exists in edc connector, so ignoring asset creation");
		}
	}
}
