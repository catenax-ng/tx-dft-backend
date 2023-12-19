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

package org.eclipse.tractusx.sde.core.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.core.model.ContractAgreementResponse;
import org.eclipse.tractusx.sde.core.model.RequestForType;
import org.eclipse.tractusx.sde.edc.core.mapper.ObjectAsJsonString;
import org.eclipse.tractusx.sde.edc.core.model.Constraint;
import org.eclipse.tractusx.sde.edc.core.model.ConstraintOperator;
import org.eclipse.tractusx.sde.edc.core.model.LogicalConstraint;
import org.eclipse.tractusx.sde.edc.core.proxy.EDCClient;
import org.eclipse.tractusx.sde.edc.core.utility.JsonNodePolicyObjectMapper;
import org.eclipse.tractusx.sde.edc.negotiations.EDCNegotiationsV2Facilator;
import org.eclipse.tractusx.sde.edc.negotiations.model.ContractAgreement;
import org.eclipse.tractusx.sde.edc.negotiations.model.ContractNegotiation;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class ContractNegotiateServiceHandler {

	private final EDCNegotiationsV2Facilator edcNegotiationsV2Facilator;

	private EDCClient edcClient;

	@SneakyThrows
	public void init(EDCClient edcClient) {
		this.edcClient = edcClient;
		edcNegotiationsV2Facilator.setEDCClient(edcClient);
	}

	public Map<String, Object> getAllContractOffers(RequestForType type, Integer limit, Integer offset) {
		List<ContractAgreementResponse> contractAgreementResponses = new ArrayList<>();
		Constraint constraint = Constraint.builder()
				.leftOperand("type")
				.operator(ConstraintOperator.EQ)
				.rightOperand(type.name()).build();

		List<ContractNegotiation> queryNegotiations = edcNegotiationsV2Facilator.queryNegotiations(List.of(constraint),
				offset, limit);
		
		queryNegotiations.stream().forEach(contract->{
			ContractAgreementResponse agreementResponse = null;
				if (StringUtils.isNotBlank(contract.getContractAgreementId())
						&& (contract.getState().equals("FINALIZED")
								|| ("DECLINED".equalsIgnoreCase(contract.getErrorDetail())
										&& contract.getState().equals("TERMINATED")))) {
					agreementResponse = getAgreementByNegotiation(contract.getId());
					
				} else {
					agreementResponse = ContractAgreementResponse.builder()
							.contractAgreementId(StringUtils.EMPTY)
							.organizationName(StringUtils.EMPTY)
							.title(StringUtils.EMPTY)
							.contractAgreementInfo(null)
							.build();
				}
				
				agreementResponse.setNegotiationId(contract.getId());
				agreementResponse.setCounterPartyAddress(contract.getCounterPartyAddress());
				agreementResponse.setDateCreated(contract.getCreatedAt());
				agreementResponse.setType(contract.getType());
				agreementResponse.setState(contract.getState());
				agreementResponse.setErrorDetail(contract.getErrorDetail());
				contractAgreementResponses.add(agreementResponse);
		});
		
		Map<String, Object> res = new HashMap<>();
		res.put("connector", edcClient.getEdcHost());
		res.put("contracts", contractAgreementResponses);

		return res;
	}

	public void terminateContract(RequestForType type, String negotiationId) {
		edcNegotiationsV2Facilator.terminateNegotiation(negotiationId, negotiationId);
	}

	
	public ContractAgreementResponse getAgreementByNegotiation(String negotiationId) {
		ContractAgreementResponse agreementResponse =null;
		ContractAgreement agreement = edcNegotiationsV2Facilator.getAgreementForNegotiation(negotiationId);
		
		if (agreement != null) {
			
			LogicalConstraint mapPolicy = JsonNodePolicyObjectMapper.mapPolicy(
					ObjectAsJsonString.toJsonNode(agreement.getPolicy()), "/odrl:permission/odrl:constraint");

			agreement.setPolicy(mapPolicy);

			agreementResponse = ContractAgreementResponse.builder()
					.contractAgreementId(agreement.getId())
					.organizationName(StringUtils.EMPTY)
					.title(StringUtils.EMPTY)
					.contractAgreementInfo(agreement)
					.build();

		}
		return agreementResponse;
	}

}
