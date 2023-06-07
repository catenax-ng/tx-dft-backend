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
package org.eclipse.tractusx.sde.bpndiscovery.handler;

import java.util.List;

import org.eclipse.tractusx.sde.bpndiscovery.api.IBpndiscoveryExternalServiceApi;
import org.eclipse.tractusx.sde.bpndiscovery.model.request.BpnDiscoveryRequest;
import org.eclipse.tractusx.sde.bpndiscovery.model.request.BpnDiscoverySearchRequest;
import org.eclipse.tractusx.sde.bpndiscovery.model.response.BpnDiscoveryBatchResponse;
import org.eclipse.tractusx.sde.bpndiscovery.model.response.BpnDiscoveryResponse;
import org.eclipse.tractusx.sde.bpndiscovery.model.response.BpnDiscoverySearchResponse;
import org.eclipse.tractusx.sde.bpndiscovery.utils.BpnDiscoveryAuthToken;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class BpnDiscoveryProxyService {
	
	private final IBpndiscoveryExternalServiceApi bpndiscoveryExternalServiceApi;

	private final BpnDiscoveryAuthToken bpnDiscoveryAuthToken;
	
	
	@SneakyThrows
	public BpnDiscoveryResponse bpnDiscoveryData(BpnDiscoveryRequest bpnDiscoveryRequest) {
		return bpndiscoveryExternalServiceApi.bpnDiscoveryDataByKey(bpnDiscoveryRequest, bpnDiscoveryAuthToken.getToken());
	}
	
	@SneakyThrows
	public  BpnDiscoverySearchResponse bpnDiscoverySearchData(BpnDiscoverySearchRequest bpnDiscoverySearchRequest) {
		return bpndiscoveryExternalServiceApi.bpnDiscoverySearchData(bpnDiscoverySearchRequest, bpnDiscoveryAuthToken.getToken());
	}
	
	@SneakyThrows
	public List<BpnDiscoveryBatchResponse> bpnDiscoveryBatchData(List<BpnDiscoveryRequest> bpnDiscoveryKeyList) {
		return bpndiscoveryExternalServiceApi.bpnDiscoveryBatchDataByList(bpnDiscoveryKeyList, bpnDiscoveryAuthToken.getToken());
	}
	
	@SneakyThrows
	public  void deleteBpnDiscoveryData(String resourceId) {
		bpndiscoveryExternalServiceApi.deleteBpnDiscoveryData(resourceId, bpnDiscoveryAuthToken.getToken());
	}

}
