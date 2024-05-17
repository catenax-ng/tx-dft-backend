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

package org.eclipse.tractusx.sde.portal.handler;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.sde.portal.api.IPartnerPoolExternalServiceApi;
import org.eclipse.tractusx.sde.portal.model.LegalEntityData;
import org.eclipse.tractusx.sde.portal.model.response.LegalEntityResponse;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class PartnerPoolService {

	private final IPartnerPoolExternalServiceApi partnerPoolExternalServiceApi;
	
	@SneakyThrows
	public List<LegalEntityResponse> fetchLegalEntitiesData(String searchText, Integer page, Integer size) {
		LegalEntityData legalEntity = partnerPoolExternalServiceApi.fetchLegalEntityData(searchText, page, size);
		return Optional
				.ofNullable(legalEntity.getContent()
						.stream()
						.map(companyData -> LegalEntityResponse.builder()
						.bpn(companyData.getBpnl()).name(companyData.getLegalName()).build())
						.toList())
				.orElse(Collections.emptyList());  
		
	}


}
