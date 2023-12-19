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

package org.eclipse.tractusx.sde.submodels.apr.steps;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tractusx.sde.common.configuration.properties.EDCConfigurationProperties;
import org.eclipse.tractusx.sde.common.configuration.properties.PortalConfigurationProperties;
import org.eclipse.tractusx.sde.edc.catalog.EDCCatalogV2Facilator;
import org.eclipse.tractusx.sde.edc.catalog.model.Dataset;
import org.eclipse.tractusx.sde.edc.catalog.model.OfferCatalog;
import org.eclipse.tractusx.sde.edc.consumer.EDCConsumerV2Facilator;
import org.eclipse.tractusx.sde.edc.core.model.Constraint;
import org.eclipse.tractusx.sde.edc.core.model.ConstraintOperator;
import org.eclipse.tractusx.sde.edc.core.proxy.EDCClient;
import org.eclipse.tractusx.sde.edc.edr.model.EDRCachedByIdResponse;
import org.eclipse.tractusx.sde.portal.impl.PortalFacilator;
import org.eclipse.tractusx.sde.portal.model.PortalClient;
import org.eclipse.tractusx.sde.portal.response.ConnectorInfo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import feign.FeignException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DDTRUrlCacheUtility {

	private final PortalFacilator portalFacilator;

	private final EDCCatalogV2Facilator edcCatalogV2Facilator;

	private final EDCConsumerV2Facilator edcConsumerV2Facilator;
	
	private final EDCConfigurationProperties edcConfigurationProperties;
	
	private final PortalConfigurationProperties portalConfigurationProperties;
	
	@SneakyThrows
	@PostConstruct
	public void init() {
		
		EDCClient edcClient =EDCClient.builder()
				.edcHost(new URI(edcConfigurationProperties.getConsumerHost()))
				.edcApiHeaderKey(edcConfigurationProperties.getConsumerApiKey())
				.edcApiHeaderValue(edcConfigurationProperties.getConsumerApiValue())
				.edcDataManagementPath(edcConfigurationProperties.getConsumerManagementPath())
				.build();
		
		PortalClient portalClient = PortalClient.builder()
				.portalHost(new URI(portalConfigurationProperties.getHostname()))
				.portalTokenUrl(new URI(portalConfigurationProperties.getAuthenticationUrl()))
				.clientId(portalConfigurationProperties.getClientId())
				.clientSecret(portalConfigurationProperties.getClientSecret()).build();
		
		edcCatalogV2Facilator.setEDCClient(edcClient);
		
		edcConsumerV2Facilator.init(edcClient, portalClient);
	}

	@Cacheable(value = "bpn-ddtr", key = "#bpnNumber")
	public List<OfferCatalog> getDDTRUrl(String bpnNumber) {

		List<ConnectorInfo> connectorInfos = portalFacilator.fetchConnectorInfo(List.of(bpnNumber));

		List<OfferCatalog> offers = new ArrayList<>();

		Constraint constraint = Constraint.builder().leftOperand("type").operator(ConstraintOperator.EQ)
				.rightOperand("data.core.digitalTwinRegistry").build();

		connectorInfos.stream().forEach(
				connectorInfo -> connectorInfo.getConnectorEndpoint().parallelStream().distinct().forEach(connector -> {
					try {
						OfferCatalog queryDataOfferModel = edcCatalogV2Facilator.requestCatalog(connector,
								List.of(constraint), 0, 100);
						log.info("For Connector " + connector + ", found asset :"
								+ queryDataOfferModel.getDataSetOffers().size());
						offers.add(queryDataOfferModel);
					} catch (Exception e) {
						log.error("Error while looking EDC catalog for digitaltwin registry url, " + connector
								+ ", Exception :" + e.getMessage());
					}
				}));

		return offers;
	}

	@SneakyThrows
	public EDRCachedByIdResponse verifyAndGetToken(Dataset dataset, String connectorEndpointUrl, String connectorId) {
		try {
			return edcConsumerV2Facilator.subcribeAndInititateTransferWaitGetEDRAuthToken(dataset, connectorEndpointUrl,
					connectorId);
		} catch (FeignException e) {
			String errorMsg = "Unable to look up offer because: " + e.contentUTF8();
			log.error("FeignException : " + errorMsg);
		} catch (Exception e) {
			String errorMsg = "Unable to look up offer because: " + e.getMessage();
			log.error("Exception : " + errorMsg);
		}
		return null;
	}

	@CacheEvict(value = "bpn-ddtr", key = "#bpnNumber")
	public void removeDDTRUrlCache(String bpnNumber) {
		log.info("Cleared '" + bpnNumber + "' bpn-ddtr cache");
	}

	@CacheEvict(value = "bpn-ddtr", allEntries = true)
	public void cleareDDTRUrlAllCache() {
		log.info("Cleared All bpn-ddtr cache");
	}

}