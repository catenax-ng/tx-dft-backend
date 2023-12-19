/********************************************************************************
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

package org.eclipse.tractusx.sde.common.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tractusx.sde.common.constants.EDCAssetConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class EDCAssetEntryRequestFactory {

	private static final String ASSET_PROP_CONTENT_TYPE = "application/json";
	private static final String ASSET_PROP_TYPE = "data.core.digitalTwin.submodel";
	private static final String ASSET_PROP_VERSION = "1.0.0";
	private static final String NAME = "Backend Data Service - AAS Server";
	private static final String TYPE = "HttpData";
	private static final String DATE_FORMATTER = "dd/MM/yyyy HH:mm:ss";
	private static final String ASSET_PROP_POLICYID = "use-eu";

	@Value(value = "${dft.apiKeyHeader}")
	private String apiKeyHeader;

	@Value(value = "${dft.apiKey}")
	private String apiKey;

	@Value(value = "${dft.hostname}")
	private String dftHostname;

	@Value(value = "${manufacturerId}")
	private String manufacturerId;

	@Value(value = "${edc.hostname}")
	private String edcEndpoint;

	@Value(value = "${spring.security.oauth2.resourceserver.jwt.issuer-uri}/protocol/openid-connect/token")
	private String idpIssuerTokenURL;

	@Value(value = "${digital-twins.authentication.clientId}")
	private String clientId;

	public Map<String, Object> getAssetProperties(String assetId, String assetName) {

		HashMap<String, Object> assetProperties = new HashMap<>();
		LocalDateTime d = LocalDateTime.now();
		String date = d.format(DateTimeFormatter.ofPattern(DATE_FORMATTER));
		assetProperties.put(EDCAssetConstant.ASSET_PROP_ID, assetId);
		assetProperties.put(EDCAssetConstant.ASSET_PROP_NAME, assetName);
		assetProperties.put(EDCAssetConstant.ASSET_PROP_TYPE, ASSET_PROP_TYPE);
		assetProperties.put(EDCAssetConstant.ASSET_PROP_POLICYID, ASSET_PROP_POLICYID);
		assetProperties.put(EDCAssetConstant.ASSET_PROP_CONTENTTYPE, ASSET_PROP_CONTENT_TYPE);
		assetProperties.put(EDCAssetConstant.ASSET_PROP_DESCRIPTION, assetName);
		assetProperties.put(EDCAssetConstant.ASSET_PROP_VERSION, ASSET_PROP_VERSION);
		assetProperties.put(EDCAssetConstant.ASSET_PROP_CREATED, date);
		assetProperties.put(EDCAssetConstant.ASSET_PROP_MODIFIED, date);
		return assetProperties;
	}

	public Map<String, Object> getDataAddressProperties(String endpoint) {
		HashMap<String, Object> dataAddressProperties = new HashMap<>();
		dataAddressProperties.put("type", TYPE);
		dataAddressProperties.put("baseUrl", endpoint);
		dataAddressProperties.put("oauth2:tokenUrl", idpIssuerTokenURL);
		dataAddressProperties.put("oauth2:clientId", clientId);
		dataAddressProperties.put("oauth2:clientSecretKey", "client-secret");
		dataAddressProperties.put("proxyMethod", "true");
		dataAddressProperties.put("proxyBody", "true");
		dataAddressProperties.put("proxyPath", "true");
		dataAddressProperties.put("proxyQueryParams", "true");
		dataAddressProperties.put("contentType", ASSET_PROP_CONTENT_TYPE);
		return dataAddressProperties;
	}

}