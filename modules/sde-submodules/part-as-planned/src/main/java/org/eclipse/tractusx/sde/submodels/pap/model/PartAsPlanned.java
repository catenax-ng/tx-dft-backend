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
package org.eclipse.tractusx.sde.submodels.pap.model;

import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.common.entities.UsagePolicies;
import org.eclipse.tractusx.sde.common.enums.UsagePolicyEnum;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PartAsPlanned {

    @JsonProperty(value ="shell_id")
	private String shellId;
    
	private String subModelId;
	
	@JsonProperty(value ="row_number")
	private Integer rowNumber;
	
	@JsonProperty(value ="uuid")
	private String uuid;
	
	@JsonProperty(value ="process_id")
	private String processId;
	
	@JsonProperty(value ="contract_defination_id")
	private String contractDefinationId;
	
	@JsonProperty(value ="usage_policy_id")
	private String usagePolicyId;
	
	@JsonProperty(value ="asset_id")
	private String assetId;
	
	@JsonProperty(value ="access_policy_id")
	private String accessPolicyId;
	
	@JsonProperty(value = "deleted")
	private String deleted;

	@JsonProperty(value ="bpn_numbers")
	private List<String> bpnNumbers;
	
	@JsonProperty(value ="type_of_access")
	private String typeOfAccess;
	
	@JsonProperty(value ="usage_policies")
	private Map<UsagePolicyEnum, UsagePolicies> usagePolicies;

	@JsonProperty(value ="manufacturer_part_id")
	private String manufacturerPartId;
	
	@JsonProperty(value ="customer_part_id")
	private String customerPartId;
	
    @JsonProperty(value ="classification")
	private String classification;
	
	@JsonProperty(value ="name_at_manufacturer")
	private String nameAtManufacturer;
	
	@JsonProperty(value ="valid_from")
    private String validFrom;
	
	@JsonProperty(value ="valid_to")
    private String validTo;
	
	@JsonProperty(value = "updated")
	private String updated;
}
