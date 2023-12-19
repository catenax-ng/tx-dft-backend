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

package org.eclipse.tractusx.sde.core.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import java.net.URI;
import java.util.Map;

import org.eclipse.tractusx.sde.common.configuration.properties.EDCConfigurationProperties;
import org.eclipse.tractusx.sde.core.model.RequestForType;
import org.eclipse.tractusx.sde.core.service.ContractNegotiateServiceHandler;
import org.eclipse.tractusx.sde.edc.core.proxy.EDCClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RestController
@RequestMapping("contract-agreements")
@RequiredArgsConstructor
public class ContractManagementController {

	private final ContractNegotiateServiceHandler contractProviderNegotiateManagement;
	private final ContractNegotiateServiceHandler contractConsumerNegotiateManagement;

	private final EDCConfigurationProperties edcConfigurationProperties;

	private EDCClient edcProviderClient = null;
	private EDCClient edcConsumerClient = null;

	@SneakyThrows
	@PostConstruct
	public void init() {

		this.edcProviderClient = EDCClient.builder().edcHost(new URI(edcConfigurationProperties.getConsumerHost()))
				.edcApiHeaderKey(edcConfigurationProperties.getConsumerApiKey())
				.edcApiHeaderValue(edcConfigurationProperties.getConsumerApiValue())
				.edcDataManagementPath(edcConfigurationProperties.getConsumerManagementPath()).build();

		this.edcConsumerClient = EDCClient.builder().edcHost(new URI(edcConfigurationProperties.getConsumerHost()))
				.edcApiHeaderKey(edcConfigurationProperties.getConsumerApiKey())
				.edcApiHeaderValue(edcConfigurationProperties.getConsumerApiValue())
				.edcDataManagementPath(edcConfigurationProperties.getConsumerManagementPath()).build();
		
		contractProviderNegotiateManagement.init(this.edcProviderClient);
		contractConsumerNegotiateManagement.init(this.edcConsumerClient);

	}

	@GetMapping(value = "/provider", produces = APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission('','provider_view_contract_agreement')")
	public ResponseEntity<Object> contractAgreementsProvider(
			@RequestParam(value = "maxLimit", required = false) Integer limit,
			@RequestParam(value = "offset", required = false) Integer offset) {
		if (limit == null) {
			limit = 10;
		}
		if (offset == null) {
			offset = 0;
		}
		Map<String, Object> res = contractProviderNegotiateManagement.getAllContractOffers(RequestForType.PROVIDER,
				limit, offset);
		return ok().body(res);
	}

	@GetMapping(value = "/consumer", produces = APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission('','consumer_view_contract_agreement')")
	public ResponseEntity<Object> contractAgreementsConsumer(
			@RequestParam(value = "maxLimit", required = false) Integer limit,
			@RequestParam(value = "offset", required = false) Integer offset) {
		if (limit == null) {
			limit = 10;
		}
		if (offset == null) {
			offset = 0;
		}
		Map<String, Object> res = contractConsumerNegotiateManagement.getAllContractOffers(RequestForType.CONSUMER,
				limit, offset);
		return ok().body(res);
	}

	@PostMapping(value = "/provider/{negotiationId}/terminate", produces = APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission('','provider_delete_contract_agreement')")
	public ResponseEntity<Object> declineContractProvider(@PathVariable("negotiationId") String negotiationId) {
		contractProviderNegotiateManagement.terminateContract(RequestForType.PROVIDER, negotiationId);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@GetMapping(value = "/consumer/{negotiationId}/terminate", produces = APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission('','consumer_delete_contract_agreement')")
	public ResponseEntity<Object> declineContractConsumer(@PathVariable("negotiationId") String negotiationId) {
		contractConsumerNegotiateManagement.terminateContract(RequestForType.CONSUMER, negotiationId);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

}
