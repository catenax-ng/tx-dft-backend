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

package org.eclipse.tractusx.sde.edc.util;

import java.util.List;

import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DDTRUrlCacheUtility {

	private final EDCAssetLookUp edcAssetLookUp;

	@Cacheable(value = "bpn-ddtr", key = "#bpnNumber")
	public List<QueryDataOfferModel> getDDTRUrl(String bpnNumber) {
		return edcAssetLookUp.getEDCAssetsByType(bpnNumber, "data.core.digitalTwinRegistry");
	}

	@CacheEvict(value = "bpn-ddtr", key = "#bpnNumber")
	public void removeDDTRUrlCache(String bpnNumber) {
		log.info("Cleared '" + bpnNumber + "' bpn-ddtr cache");
	}

	@CacheEvict(value = "bpn-ddtr", allEntries = true)
	public void cleareDDTRUrlAllCache() {
		log.info("Cleared All bpn-ddtr cache");
	}

}