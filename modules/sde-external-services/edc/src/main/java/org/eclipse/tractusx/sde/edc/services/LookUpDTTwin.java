/********************************************************************************
 * Copyright (c) 2024 T-Systems International GmbH
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.KeyValuePair;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.MultiLanguage;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellLookupRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellDescriptorResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellDescriptorResponseList;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellLookupResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelResponse;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DigitalTwinsUtility;
import org.eclipse.tractusx.sde.digitaltwins.gateways.external.EDCDigitalTwinProxyForLookUp;
import org.eclipse.tractusx.sde.edc.model.edr.EDRCachedByIdResponse;
import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import feign.FeignException;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LookUpDTTwin {

	private final EDCDigitalTwinProxyForLookUp eDCDigitalTwinProxyForLookUp;

	private final DigitalTwinsUtility digitalTwinsUtility;

	private final CatalogResponseBuilder catalogResponseBuilder;

	@Value(value = "${digital-twins.managed.thirdparty:false}")
	private boolean managedThirdParty;

	@Value(value = "${manufacturerId}")
	private String manufacturerId;

	String filterExpressionTemplate = """
			"filterExpression": [
				    {
				        "operandLeft": "https://w3id.org/edc/v0.0.1/ns/id",
				        "operator": "=",
				        "operandRight": "%s"
				    }
				]
			""";

	@SneakyThrows
	public List<QueryDataOfferModel> lookUpTwin(EDRCachedByIdResponse edrToken, QueryDataOfferModel dtOffer,
			String manufacturerPartId, String bpnNumber, String submodel, Integer offset, Integer limit) {

		String endpoint = edrToken.getEndpoint();
		String dtOfferUrl = dtOffer.getConnectorOfferUrl();
		Map<String, String> header = new HashMap<>();
		header.put(edrToken.getAuthKey(), edrToken.getAuthCode());
		submodel = StringUtils.isBlank(submodel) ? "" : submodel;

		if (StringUtils.isNotBlank(bpnNumber))
			header.put("Edc-Bpn", bpnNumber);

		if (StringUtils.isBlank(manufacturerPartId)) {
			return lookUpAllShellForBPN(submodel, endpoint, dtOfferUrl, header, offset, limit);
		} else {
			return lookUpTwinBasedOnBPNAndManufacturerPartId(manufacturerPartId, bpnNumber, submodel, endpoint,
					dtOfferUrl, header);
		}
	}

	private List<QueryDataOfferModel> lookUpTwinBasedOnBPNAndManufacturerPartId(String manufacturerPartId,
			String bpnNumber, String submodel, String endpoint, String dtOfferUrl, Map<String, String> header) {
		ShellLookupRequest shellLookupRequest = getShellLookupRequest(manufacturerPartId, bpnNumber, submodel);
		try {

			String assetIds = digitalTwinsUtility.encodeAssetIdsObject(shellLookupRequest.toJsonString());

			ShellLookupResponse shellLookup = eDCDigitalTwinProxyForLookUp.shellLookup(new URI(endpoint), assetIds,
					header);

			return getSubmodelDetails(shellLookupRequest, endpoint, header, dtOfferUrl, shellLookup.getResult(),
					submodel);

		} catch (FeignException e) {
			String errorMsg = "Unable to lookUpTwinBasedOnManufacturerPartId " + dtOfferUrl + ", "
					+ shellLookupRequest.toJsonString() + " because: " + e.contentUTF8();
			log.error("FeignException : " + errorMsg);
		} catch (Exception e) {
			String errorMsg = "Unable to lookUpTwinBasedOnManufacturerPartId " + dtOfferUrl + ", "
					+ shellLookupRequest.toJsonString() + "because: " + e.getMessage();
			log.error("Exception : " + errorMsg);
		}
		return Collections.emptyList();
	}

	private List<QueryDataOfferModel> lookUpAllShellForBPN(String submodel, String endpoint, String dtOfferUrl,
			Map<String, String> header, Integer offset, Integer limit) {
		List<QueryDataOfferModel> queryOnDataOffers = new ArrayList<>();
		try {

			ShellDescriptorResponseList allShell = eDCDigitalTwinProxyForLookUp.getAllShell(new URI(endpoint), offset,
					limit, header);
			for (ShellDescriptorResponse shellDescriptorResponse : allShell.getResult())
				preapreSubmodelResult(submodel, queryOnDataOffers, shellDescriptorResponse);

		} catch (FeignException e) {
			String errorMsg = "Unable to lookUpAllShellForBPN " + dtOfferUrl + ", " + endpoint + ", because: "
					+ e.contentUTF8();
			log.error("FeignException : " + errorMsg);
		} catch (Exception e) {
			String errorMsg = "Unable to lookUpAllShellForBPN " + dtOfferUrl + ", " + endpoint + ", because: "
					+ e.getMessage();
			log.error("Exception : " + errorMsg);
		}
		return queryOnDataOffers;
	}

	@SneakyThrows
	private List<QueryDataOfferModel> getSubmodelDetails(ShellLookupRequest shellLookupRequest, String endpoint,
			Map<String, String> header, String dtOfferUrl, List<String> shellIds, String submodel) {
		List<QueryDataOfferModel> queryOnDataOffers = new ArrayList<>();

		for (String shellId : shellIds) {
			ShellDescriptorResponse shellDescriptorResponse = eDCDigitalTwinProxyForLookUp.getShellDescriptorByShellId(
					new URI(endpoint), digitalTwinsUtility.encodeShellIdBase64Utf8(shellId), header);
			preapreSubmodelResult(submodel, queryOnDataOffers, shellDescriptorResponse);
		}
		return queryOnDataOffers;
	}

	private void preapreSubmodelResult(String submodel, List<QueryDataOfferModel> queryOnDataOffers,
			ShellDescriptorResponse shellDescriptorResponse) {

		String manufacturerPartId = getSpecificKeyFromList(shellDescriptorResponse, "manufacturerPartId");

		String manufacturerBPNId = getSpecificKeyFromList(shellDescriptorResponse, "manufacturerId");

		if (StringUtils.isNotBlank(shellDescriptorResponse.getIdShort()))
			for (SubModelResponse subModelResponse : shellDescriptorResponse.getSubmodelDescriptors()) {

				String sematicId = subModelResponse.getSemanticId().getKeys().get(0).getValue();

				buildQdmOffer(submodel, queryOnDataOffers, shellDescriptorResponse, manufacturerPartId,
						manufacturerBPNId, subModelResponse, sematicId);
			}
	}

	private void buildQdmOffer(String submodel, List<QueryDataOfferModel> queryOnDataOffers,
			ShellDescriptorResponse shellDescriptorResponse, String manufacturerPartId, String manufacturerBPNId,
			SubModelResponse subModelResponse, String sematicId) {
		
		if (!subModelResponse.getIdShort().isEmpty() && sematicId.toLowerCase().contains(submodel.toLowerCase())
				&& subModelResponse.getEndpoints() != null) {

			String subprotocolBody = subModelResponse.getEndpoints().get(0).getProtocolInformation()
					.getSubprotocolBody();

			String[] edcInfo = subprotocolBody.split(";");
			String[] assetInfo = edcInfo[0].split("=");
			String[] connectorInfo = edcInfo[1].split("=");

			QueryDataOfferModel edcOffer = getEDCOffer(assetInfo[1], connectorInfo[1]);

			if (edcOffer != null) {
				Optional<String> descriptionOptional = subModelResponse.getDescription().stream()
						.filter(e -> e.getLanguage().contains("en")).map(MultiLanguage::getText).findFirst();

				String idShort = subModelResponse.getIdShort();

				String description = descriptionOptional.isPresent() ? descriptionOptional.get()
						: edcOffer.getDescription();

				String type = edcOffer.getType();

				if (sematicId != null && sematicId.toLowerCase().contains("pcf"))
					type = "data.pcf.exchangeEndpoint";

				QueryDataOfferModel qdm = QueryDataOfferModel.builder().connectorId(edcOffer.getConnectorId())
						.publisher(manufacturerBPNId).manufacturerPartId(manufacturerPartId)
						.connectorOfferUrl(connectorInfo[1]).offerId(edcOffer.getOfferId())
						.assetId(assetInfo[1]).type(type)
						.title(idShort + "_" + shellDescriptorResponse.getIdShort())
						.created(edcOffer.getCreated()).description(description).policy(edcOffer.getPolicy())
						.build();

				queryOnDataOffers.add(qdm);
			}
		}
	}

	public QueryDataOfferModel getEDCOffer(String assetId, String connectorOfferUrl) {
		String filterExpression = String.format(filterExpressionTemplate, assetId);
		List<QueryDataOfferModel> queryOnDataOffers = catalogResponseBuilder.queryOnDataOffers(connectorOfferUrl, 0, 10,
				filterExpression);
		Optional<QueryDataOfferModel> findFirst = queryOnDataOffers.stream().findFirst();
		return findFirst.isPresent() ? findFirst.get() : null;
	}

	private String getSpecificKeyFromList(ShellDescriptorResponse shellDescriptorResponse, String key) {
		Optional<String> findFirst = shellDescriptorResponse.getSpecificAssetIds().stream()
				.filter(e -> e.getName().equals(key)).map(KeyValuePair::getValue).findFirst();
		return findFirst.isPresent() ? findFirst.get() : "";
	}

	private ShellLookupRequest getShellLookupRequest(String manufacturerPartId, String bpnNumber, String submodel) {

		ShellLookupRequest shellLookupRequest = new ShellLookupRequest();
		getSpecificAssetIds(manufacturerPartId, bpnNumber, submodel).entrySet().stream()
				.forEach(entry -> shellLookupRequest.addLocalIdentifier(entry.getKey(), entry.getValue()));

		return shellLookupRequest;
	}

	private Map<String, String> getSpecificAssetIds(String manufacturerPartId, String bpnNumber, String submodel) {
		Map<String, String> specificIdentifiers = new HashMap<>();

		if (StringUtils.isNotBlank(manufacturerPartId))
			specificIdentifiers.put(CommonConstants.MANUFACTURER_PART_ID, manufacturerPartId);

		if (StringUtils.isNotBlank(bpnNumber))
			specificIdentifiers.put(CommonConstants.MANUFACTURER_ID, bpnNumber);

		if (StringUtils.isNotBlank(submodel) && "pcf".equals(submodel))
			specificIdentifiers.put(CommonConstants.ASSET_LIFECYCLE_PHASE, CommonConstants.AS_PLANNED);

		return specificIdentifiers;
	}
}
