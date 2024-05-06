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
package org.eclipse.tractusx.sde.digitaltwins.gateways.external;

import java.util.List;

import org.eclipse.tractusx.sde.digitaltwins.entities.response.CreateAccessRuleRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.Items;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ReadUpdateAccessRuleRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ReadUpdateAccessRuleResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "DigitalTwinsFeignClient", url = "${digital-twins.hostname:default}", configuration = DigitalTwinsFeignClientConfiguration.class)
public interface IAccessRuleManagementApi {
	
	@GetMapping(path = "${digital-twins.registry.uri:/api/v3.0}/access-controls/rules")
	ResponseEntity<List<Items>> getAccessControlsRules(@RequestHeader("Edc-Bpn") String edcBpn);

	@PostMapping(path = "${digital-twins.registry.uri:/api/v3.0}/access-controls/rules")
	ResponseEntity<Void> createAccessControlsRules(@RequestHeader("Edc-Bpn") String edcBpn, @RequestBody CreateAccessRuleRequest request);
	
	@GetMapping(path = "${digital-twins.registry.uri:/api/v3.0}/access-controls/rules/{ruleId}")
	ResponseEntity<ReadUpdateAccessRuleResponse> createAccessControlsRules(@RequestHeader("Edc-Bpn") String edcBpn, @PathVariable("ruleId") String ruleId);
	
	@PutMapping(path = "${digital-twins.registry.uri:/api/v3.0}/access-controls/rules/{ruleId}")
	ResponseEntity<ReadUpdateAccessRuleResponse> createAccessControlsRules(@RequestHeader("Edc-Bpn") String edcBpn,@PathVariable("ruleId") String ruleId, @RequestBody ReadUpdateAccessRuleRequest request);

	@DeleteMapping(path = "${digital-twins.registry.uri:/api/v3.0}/access-controls/rules/{ruleId}")
	ResponseEntity<Void> deleteAccessControlsRules(@RequestHeader("Edc-Bpn") String edcBpn, @PathVariable("ruleId") String ruleId);



}
