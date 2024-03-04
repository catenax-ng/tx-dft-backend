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
package org.eclipse.tractusx.sde.controllers;

import static org.eclipse.tractusx.sde.common.enums.PolicyTypeIdEnum.ACCESS;
import static org.eclipse.tractusx.sde.common.enums.PolicyTypeIdEnum.USAGE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.EnablePostgreSQL;
import org.eclipse.tractusx.sde.common.entities.Policies;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.core.policy.entity.PolicyEntity;
import org.eclipse.tractusx.sde.core.policy.repository.PolicyRepository;
import org.eclipse.tractusx.sde.core.policy.service.PolicyService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WithMockUser(username = "Admin", authorities = { "Admin" })
@EnablePostgreSQL
class PolicyControllerTest {


    @Autowired
    private MockMvc mvc;

    @Autowired
    private PolicyRepository policyRepository;
    
    @Autowired
    private PolicyService policyService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void init() {
        policyRepository.deleteAll();
    }

    @Test
    void testSaveAndRetrievePolicy() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .post("/policy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getPolicy("new_policy"))))
                .andExpect(status().isOk());

        Assertions.assertEquals(1, policyRepository.findAll().size());
        List<PolicyEntity> entities = policyRepository.findAll();
        String uuid = entities.get(0).getUuid();

        mvc.perform(MockMvcRequestBuilders
                        .get("/policy/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void testUniquePolicies() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .post("/policy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getPolicy("new_policy"))))
                .andExpect(status().isOk());

        Assertions.assertEquals(1, policyRepository.findAll().size());

        mvc.perform(MockMvcRequestBuilders
                        .post("/policy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getPolicy("new_policy"))))
                .andExpect(status().isBadRequest());

        Assertions.assertEquals(1, policyRepository.findAll().size());

    }
    
    @Test
    void findMatchingPolicyBasedOnFileName() throws Exception {
    	
    	String fileName = "Mysubmodel_new_policy.csv";
    	mvc.perform(MockMvcRequestBuilders
                .post("/policy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getPolicy("new_policy"))))
        .andExpect(status().isOk());
    	
    	List<PolicyModel> findMatchingPolicyBasedOnFileName = policyService.findMatchingPolicyBasedOnFileName(fileName);
        Assertions.assertEquals(1, findMatchingPolicyBasedOnFileName.size());
        Assertions.assertEquals("new_policy", findMatchingPolicyBasedOnFileName.get(0).getPolicyName());

    }


    private PolicyModel getPolicy(String policyName) {
       
    	List<Policies> accessPolicies = List.of(Policies.builder()
        		.technicalKey("BusinessPartnerNumber")
        		.value(List.of("BPNL00000005PROV", "BPNL00000005PROW", "BPNL00000005PROB"))
        		.build(),Policies.builder()
        		.technicalKey("Membership")
        		.value(List.of("active"))
        		.build());
        
    	List<Policies> usagePolicies = List.of(Policies.builder()
        		.technicalKey("Membership")
        		.value(List.of("active"))
        		.build());
              
        return PolicyModel.builder()
        		.policyName(policyName)
        		.policies(Map.of(ACCESS.getPolicyTypeValue(), accessPolicies, USAGE.getPolicyTypeValue(), usagePolicies))
        		.build();
    }
}