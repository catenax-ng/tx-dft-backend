/*
 * Copyright 2022 CatenaX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.catenax.dft.gateways.external;

import com.catenax.dft.entities.edc.request.asset.AssetEntryRequest;
import com.catenax.dft.entities.edc.request.contractDefinition.ContractDefinitionRequest;
import com.catenax.dft.entities.edc.request.policies.PolicyDefinitionRequest;
import com.catenax.dft.gateways.exceptions.EDCGatewayException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
public abstract class EDCGatewayImpl implements EDCGateway {

    protected boolean checkIfAssetExists(String url, String apiKey, String apiValue) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(apiKey, apiValue);

        try {
            restTemplate.getForEntity(url, Object.class, headers);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw e;
        }
        return true;
    }

    protected void postAsset(String url, String apiKey, String apiValue, AssetEntryRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(apiKey, apiValue);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AssetEntryRequest> entity = new HttpEntity<>(request, headers);
        try {
            restTemplate.postForEntity(url, entity, String.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new EDCGatewayException("Asset already exists");
            }
            throw new EDCGatewayException(e.getStatusCode().toString());
        }
    }

    protected void postPolicyDefinition(String url, String apiKey, String apiValue,PolicyDefinitionRequest request ) {
        final String policyResource = "/policies";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(apiKey, apiValue);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PolicyDefinitionRequest> entity = new HttpEntity<>(request, headers);
        try {
            restTemplate.postForEntity(url+ policyResource, entity, String.class);
        } catch (HttpClientErrorException e) {
            throw new EDCGatewayException(e.getStatusCode().toString());
        }
    }

    protected void postContractDefinition(String url, String apiKey, String apiValue,ContractDefinitionRequest request) {
        final String contractDefinitionResource = "/contractdefinitions";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(apiKey, apiValue);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ContractDefinitionRequest> entity = new HttpEntity<>(request, headers);
        try {
            restTemplate.postForEntity(url  + contractDefinitionResource, entity, String.class);
        } catch (HttpClientErrorException e) {
            throw new EDCGatewayException(e.getStatusCode().toString());
        }
    }
}
