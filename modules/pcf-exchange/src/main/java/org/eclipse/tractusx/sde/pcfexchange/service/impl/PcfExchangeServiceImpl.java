/********************************************************************************
 * Copyright (c) 2023, 2024 T-Systems International GmbH
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.sde.pcfexchange.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.eclipse.tractusx.sde.common.model.PagingResponse;
import org.eclipse.tractusx.sde.edc.model.request.ConsumerRequest;
import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.eclipse.tractusx.sde.edc.util.EDCAssetUrlCacheService;
import org.eclipse.tractusx.sde.pcfexchange.entity.PcfResponseEntity;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFRequestStatusEnum;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFTypeEnum;
import org.eclipse.tractusx.sde.pcfexchange.repository.PcfReqsponseRepository;
import org.eclipse.tractusx.sde.pcfexchange.request.PcfRequestModel;
import org.eclipse.tractusx.sde.pcfexchange.service.IPCFExchangeService;
import org.eclipse.tractusx.sde.submodels.pcf.service.PcfService;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PcfExchangeServiceImpl implements IPCFExchangeService {

	private final PCFRepositoryService pcfRepositoryService;
	private final PcfReqsponseRepository pcfReqsponseRepository;
	private final EDCAssetUrlCacheService edcAssetUrlCacheService;
	private final PcfService pcfService;
	private final ProxyRequestInterface proxyRequestInterface;

	@SneakyThrows
	@Override
	public String requestForPcfDataExistingOffer(String productId, ConsumerRequest consumerRequest) {

		StringBuilder sb = new StringBuilder();
		consumerRequest.getOffers().stream().forEach(offer -> {

			String requestId = UUID.randomUUID().toString();
			String providerBPNNumber = offer.getConnectorId();

			String message = "Please provide PCF value for " + productId;

			pcfRepositoryService.savePcfRequestData(requestId, productId, providerBPNNumber, message,
					PCFTypeEnum.CONSUMER, PCFRequestStatusEnum.SENDING_REQUEST, "");

			QueryDataOfferModel queryDataOfferModel = QueryDataOfferModel.builder().assetId(offer.getAssetId())
					.offerId(offer.getOfferId()).policyId(offer.getPolicyId()).connectorId(providerBPNNumber)
					.connectorOfferUrl(offer.getConnectorOfferUrl())
					.policy(PolicyModel.builder().usagePolicies(consumerRequest.getUsagePolicies()).build()).build();

			proxyRequestInterface.requestToProviderForPCFValue(productId, sb, requestId, message, queryDataOfferModel, false);
		});

		return sb.toString();

	}

	@Override
	public Object requestForPcfNotExistDataOffer(PcfRequestModel pcfRequestModel) {
		StringBuilder sb = new StringBuilder();
		String requestId = UUID.randomUUID().toString();
		try {
			pcfRepositoryService.savePcfRequestData(requestId, pcfRequestModel.getProductId(),
					pcfRequestModel.getBpnNumber(), pcfRequestModel.getMessage(), PCFTypeEnum.CONSUMER,
					PCFRequestStatusEnum.SENDING_REQUEST, "");

			// 1 fetch EDC connectors and DTR Assets from EDC connectors
			List<QueryDataOfferModel> pcfExchangeUrlOffers = edcAssetUrlCacheService
					.getPCFExchangeUrlFromTwin(pcfRequestModel.getBpnNumber());

			// 2 request for PCF value for non existing sub model and send notification to
			// call provider for data
			pcfExchangeUrlOffers.parallelStream().forEach(dtOffer -> proxyRequestInterface.requestToProviderForPCFValue(
					pcfRequestModel.getProductId(), sb, requestId, pcfRequestModel.getMessage(), dtOffer, true));

		} catch (FeignException e) {
			log.error("FeignRequest requestForPcfNotExistDataOffer:" + e.request());
			String errorMsg = "Unable to request to data provider because: "
					+ (StringUtils.isBlank(e.contentUTF8()) ? e.getMessage() : e.contentUTF8());
			log.error("FeignException requestForPcfNotExistDataOffer: " + errorMsg);
		}

		if (sb.isEmpty())
			throw new ValidationException("Not requested to provider for '" + pcfRequestModel.getProductId()
					+ "' because there is no PCF exchange endpoint found");

		return sb.toString();
	}

	@SneakyThrows
	@Override
	public String actionOnPcfRequestAndSendNotificationToConsumer(PcfRequestModel pcfRequestModel) {
		String remark = "";
		try {

			JsonObject calculatedPCFValue = pcfService
					.readCreatedTwinsDetailsByProductId(pcfRequestModel.getProductId()).get("json").getAsJsonObject();

			
			PCFRequestStatusEnum status = pcfRepositoryService.identifyRunningStatus(pcfRequestModel.getRequestId(),
					pcfRequestModel.getStatus());

			// push api call
			Runnable runnable = () -> proxyRequestInterface.sendNotificationToConsumer(status,
					calculatedPCFValue, pcfRequestModel.getProductId(), pcfRequestModel.getBpnNumber(),
					pcfRequestModel.getRequestId());

			new Thread(runnable).start();

			remark = "PCF push request accepted for '" + pcfRequestModel.getProductId()
					+ "' and asynchronously pushing notification to consumer";

		} catch (NoDataFoundException e) {
			remark = "Unable to take action on PCF request becasue PCF calculated value does not exist, please upload PCF value for "
					+ pcfRequestModel.getProductId() + " in systems using Manual/Recurring Upload";
			pcfRepositoryService.savePcfRequestData(pcfRequestModel.getRequestId(), pcfRequestModel.getProductId(),
					pcfRequestModel.getBpnNumber(), pcfRequestModel.getMessage(), PCFTypeEnum.PROVIDER,
					PCFRequestStatusEnum.FAILED, remark);
			log.error(remark);
			throw new ValidationException(e.getMessage());
		} catch (Exception e) {
			pcfRepositoryService.savePcfStatus(pcfRequestModel.getRequestId(), PCFRequestStatusEnum.FAILED);
			throw new ServiceException(e.getMessage());
		}
		return remark;
	}

	@Override
	public PcfRequestModel savePcfRequestData(String requestId, String productId, String bpnNumber, String message) {
		PCFRequestStatusEnum status = PCFRequestStatusEnum.REQUESTED;
		String remark = "";
		try {
			pcfService.readCreatedTwinsDetailsByProductId(productId).get("json").getAsJsonObject();
		} catch (NoDataFoundException e) {
			String msg = "The PCF calculated value does not exist in system, please upload PCF value for '" + productId
					+ "' in systems using Manual/Recurring Upload";
			log.warn(msg);
			remark = msg;
			status = PCFRequestStatusEnum.PENDING_DATA_FROM_PROVIDER;
		}
		return pcfRepositoryService.savePcfRequestData(requestId, productId, bpnNumber, message, PCFTypeEnum.PROVIDER,
				status, remark);
	}

	@Override
	public void recievedPCFData(String productId, String bpnNumber, String requestId, String message,
			JsonNode pcfData) {

		PCFRequestStatusEnum status = PCFRequestStatusEnum.FAILED;
		try {
			status = PCFRequestStatusEnum.valueOf(message);
		} catch (Exception e) {
			log.error("Unable to find PCF value status " + e.getMessage());
		}

		PcfResponseEntity entity = PcfResponseEntity.builder().pcfData(pcfData).requestId(requestId)
				.responseId(UUID.randomUUID().toString()).lastUpdatedTime(Instant.now().getEpochSecond()).build();

		pcfReqsponseRepository.save(entity);

		if (PCFRequestStatusEnum.APPROVED.equals(status) || PCFRequestStatusEnum.PUSHING_DATA.equals(status)
				|| PCFRequestStatusEnum.PUSHING_UPDATED_DATA.equals(status)) {
			status = PCFRequestStatusEnum.RECEIVED;
		}

		pcfRepositoryService.savePcfStatus(requestId, status);

	}

	@Override
	public PagingResponse getPcfData(PCFRequestStatusEnum status, PCFTypeEnum type, Integer page, Integer pageSize) {
		List<PCFRequestStatusEnum> statusLs = null;
		if (status != null)
			statusLs = List.of(status);
		return pcfRepositoryService.getPcfData(statusLs, type, page, pageSize);
	}

	@Override
	public PcfResponseEntity viewForPcfDataOffer(String requestId) {
		Optional<PcfResponseEntity> findById = pcfReqsponseRepository
				.findFirstByRequestIdOrderByLastUpdatedTimeDesc(requestId);
		if (!findById.isPresent())
			throw new NoDataFoundException("No data found uuid " + requestId);
		return findById.get();
	}

}
