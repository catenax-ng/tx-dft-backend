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

package org.eclipse.tractusx.sde.common.entities;

import java.util.List;

import org.eclipse.tractusx.sde.common.validators.UsagePolicyValidation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmodelJsonRequest<T> {

	@JsonProperty(value = "row_data")
	private List<T> rowData;
	
	@JsonProperty(value = "policy_name")
	private String policyName;

	@JsonProperty(value = "type_of_access")
	private String typeOfAccess;
	
	@JsonProperty(value = "bpn_numbers")
	private List<String> bpnNumbers;


	@JsonProperty(value = "usage_policies")
	@UsagePolicyValidation
	private List<UsagePolicies> usagePolicies;

}
