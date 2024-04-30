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

package org.eclipse.tractusx.sde.retrieverl.service;

import java.util.List;

import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.eclipse.tractusx.sde.core.policy.service.PolicyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyProvider {

	private final PolicyService policyService;

	@Value(value = "${manufacturerId}")
	private String bpnNumber;

	ObjectMapper mapper = new ObjectMapper();

	private String defaultPolicy = """
					{
	 				"policy_name" : "default",
	 				"Access": [
	 			  	],
	 				"Usage": [
	 				]
 				}
				""";

	@SneakyThrows
	public PolicyModel saveDefaultPolicy() {
		String defaultPolicyStr = String.format(defaultPolicy, bpnNumber);
		PolicyModel policy = mapper.readValue(defaultPolicyStr, PolicyModel.class);
		return policyService.savePolicy(policy);
	}

	@SneakyThrows
	public PolicyModel getDefaultPolicy() {

		log.info("Applying default policy");

		PolicyModel policyByName = policyService.getPolicyByName("default");

		if (policyByName == null) {
			policyByName = saveDefaultPolicy();
			log.info("Saved default policy");
		}

		return policyByName;
	}

	@SneakyThrows
	public PolicyModel getMatchingPolicyBasedOnFileName(String fileName) {
		List<PolicyModel> matchingList = policyService.findMatchingPolicyBasedOnFileName(fileName);
		if (matchingList.isEmpty())
			throw new ValidationException("No matching policy found");
		else {
			log.info("Applying policy " + matchingList.get(0).getPolicyName());
			return matchingList.get(0);
		}
	}
}