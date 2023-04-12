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

package org.eclipse.tractusx.sde.edc.services;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.entities.UsagePolicies;
import org.eclipse.tractusx.sde.common.enums.PolicyAccessEnum;
import org.eclipse.tractusx.sde.common.enums.UsagePolicyEnum;
import org.eclipse.tractusx.sde.edc.api.ContractOfferCatalogApi;
import org.eclipse.tractusx.sde.edc.constants.EDCAssetConstant;
import org.eclipse.tractusx.sde.edc.entities.database.ContractNegotiationInfoEntity;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ConstraintRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyConstraintBuilderService;
import org.eclipse.tractusx.sde.edc.facilitator.AbstractEDCStepsHelper;
import org.eclipse.tractusx.sde.edc.facilitator.ContractNegotiateManagementHelper;
import org.eclipse.tractusx.sde.edc.gateways.database.ContractNegotiationInfoRepository;
import org.eclipse.tractusx.sde.edc.model.asset.Asset;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractNegotiationDto;
import org.eclipse.tractusx.sde.edc.model.contractoffers.ContractOffer;
import org.eclipse.tractusx.sde.edc.model.contractoffers.ContractOffersCatalogResponse;
import org.eclipse.tractusx.sde.edc.model.policies.PolicyDefinition;
import org.eclipse.tractusx.sde.edc.model.request.ConsumerRequest;
import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.eclipse.tractusx.sde.edc.util.UtilityFunctions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ConsumerControlPanelService extends AbstractEDCStepsHelper {

	private final String edcDataUri;
	private final ContractOfferCatalogApi contractOfferCatalogApiProxy;
	private final ContractNegotiateManagementHelper contractNegotiateManagement;

	private ContractNegotiationInfoRepository contractNegotiationInfoRepository;
	private PolicyConstraintBuilderService policyConstraintBuilderService;

	@Autowired
	public ConsumerControlPanelService(@Value("${edc.consumer.datauri}") String edcDataUri,
			ContractOfferCatalogApi contractOfferCatalogApiProxy,
			ContractNegotiateManagementHelper contractNegotiateManagement,
			ContractNegotiationInfoRepository contractNegotiationInfoRepository,
			PolicyConstraintBuilderService policyConstraintBuilderService) {
		this.edcDataUri = edcDataUri;
		this.contractOfferCatalogApiProxy = contractOfferCatalogApiProxy;
		this.contractNegotiateManagement = contractNegotiateManagement;
		this.contractNegotiationInfoRepository = contractNegotiationInfoRepository;
		this.policyConstraintBuilderService = policyConstraintBuilderService;


	}

	public List<QueryDataOfferModel> queryOnDataOffers(String providerUrl, Integer limit, Integer offset) {
		providerUrl = UtilityFunctions.removeLastSlashOfUrl(providerUrl);

		providerUrl += edcDataUri;

		List<QueryDataOfferModel> queryOfferResponse = new ArrayList<>();

		ContractOffersCatalogResponse contractOfferCatalog = contractOfferCatalogApiProxy
				.getContractOffersCatalog(getAuthHeader(), providerUrl, limit, offset );

		for (ContractOffer contractOffer : contractOfferCatalog.getContractOffers()) {
			Asset asset = contractOffer.getAsset();
			PolicyDefinition policy = contractOffer.getPolicy();

			// Populating usage policies response based on usage policy constraints
			List<UsagePolicies> usagePolicies = new ArrayList<>();
			policy.getPermissions().stream().forEach(permission -> {
				usagePolicies.addAll(UtilityFunctions.getUsagePolicies(permission.getConstraints().stream()));
			});

			UtilityFunctions.addCustomUsagePolicy(policy.getExtensibleProperties(), usagePolicies);
			// Later to be part of access policy
			List<String> bpnNumbers = new ArrayList<>();
			policy.getPermissions().stream().forEach(permission -> {
				permission.getConstraints().stream().forEach(constraint -> {
					if (constraint.getLeftExpression().getValue().equals("BusinessPartnerNumber")) {
						String value = constraint.getRightExpression().getValue().toString();
						bpnNumbers.addAll(Arrays
								.asList(value.trim().substring(value.indexOf("[") + 1, value.indexOf("]")).split(",")));
						return;
					}
				});
			});

			queryOfferResponse.add(QueryDataOfferModel.builder()
					.assetId(getFieldFromAsset(asset, EDCAssetConstant.ASSET_PROP_ID))
					.connectorOfferUrl(
							providerUrl + File.separator + getFieldFromAsset(asset, EDCAssetConstant.ASSET_PROP_ID))
					.offerId(contractOffer.getId()).title(getFieldFromAsset(asset, EDCAssetConstant.ASSET_PROP_NAME))
					.description(getFieldFromAsset(asset, EDCAssetConstant.ASSET_PROP_DESCRIPTION))
					.created(getFieldFromAsset(asset, EDCAssetConstant.ASSET_PROP_CREATED))
					.modified(getFieldFromAsset(asset, EDCAssetConstant.ASSET_PROP_MODIFIED))
					.publisher(getFieldFromAsset(asset, EDCAssetConstant.ASSET_PROP_PUBLISHER))
					.typeOfAccess(!bpnNumbers.isEmpty() ? PolicyAccessEnum.RESTRICTED : PolicyAccessEnum.UNRESTRICTED)
					.version(getFieldFromAsset(asset, EDCAssetConstant.ASSET_PROP_VERSION)).bpnNumbers(bpnNumbers)
					.usagePolicies(usagePolicies)
					.fileName(getFieldFromAsset(asset, EDCAssetConstant.ASSET_PROP_FILENAME))
					.fileContentType(getFieldFromAsset(asset, EDCAssetConstant.ASSET_PROP_CONTENTTYPE))
					.connectorId(contractOfferCatalog.getId()).build());
		}
		return queryOfferResponse;
	}

	private String getFieldFromAsset(Asset asset, String field) {
		return asset.getProperties().getOrDefault(field, "");
	}

	@Async
	public void subscribeDataOffers(ConsumerRequest consumerRequest, String processId) {
		HashMap<String, String> extensibleProperty = new HashMap<>();
		String recipient = UtilityFunctions.removeLastSlashOfUrl(consumerRequest.getProviderUrl());
		AtomicReference<String> negotiateContractId = new AtomicReference<>();
		AtomicReference<ContractNegotiationDto> checkContractNegotiationStatus = new AtomicReference<>();
		var recipientURL = recipient + edcDataUri;
		List<UsagePolicies> policies = consumerRequest.getPolicies();
		UsagePolicies customPolicy = policies.stream().filter(type -> type.getType().equals(UsagePolicyEnum.CUSTOM))
				.findFirst().get();
		if (StringUtils.isNotBlank(customPolicy.getValue())) {
			extensibleProperty.put(customPolicy.getType().name(), customPolicy.getValue());
		}
		List<ConstraintRequest> constraintRequests = policyConstraintBuilderService.getUsagePolicyConstraints(policies);
		consumerRequest.getOffers().parallelStream().forEach((offer) -> {
			try {

				negotiateContractId.set(contractNegotiateManagement.negotiateContract(offer.getOfferId(), recipientURL,
						offer.getAssetId(), constraintRequests, extensibleProperty));
				int retry = 3;
				int counter = 1;

				do {
					Thread.sleep(3000);
					checkContractNegotiationStatus
							.set(contractNegotiateManagement.checkContractNegotiationStatus(negotiateContractId.get()));
					counter++;
				} while (checkContractNegotiationStatus.get() != null
						&& !checkContractNegotiationStatus.get().getState().equals("CONFIRMED")
						&& !checkContractNegotiationStatus.get().getState().equals("DECLINED") && counter <= retry);

			} catch (Exception e) {
				log.error("Exception in subscribeDataOffers" + e.getMessage());
			} finally {
				// Local DB entry
				ContractNegotiationInfoEntity contractNegotiationInfoEntity = ContractNegotiationInfoEntity.builder()
						.processId(processId).connectorId(consumerRequest.getConnectorId()).offerId(offer.getOfferId())
						.contractNegotiationId(negotiateContractId != null ? negotiateContractId.get() : null)
						.status(checkContractNegotiationStatus.get() != null
								? checkContractNegotiationStatus.get().getState()
								: "Failed:Exception")
						.dateTime(LocalDateTime.now()).build();
				contractNegotiationInfoRepository.save(contractNegotiationInfoEntity);
			}
		});

	}

}
