/********************************************************************************
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.core.processreport.mapper;

import java.util.List;

import org.eclipse.tractusx.sde.common.entities.UsagePolicies;
import org.eclipse.tractusx.sde.core.processreport.entity.ConsumerDownloadHistoryEntity;
import org.eclipse.tractusx.sde.core.processreport.model.ConsumerDownloadHistory;
import org.eclipse.tractusx.sde.edc.model.request.Offer;
import org.mapstruct.Mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

@Mapper(componentModel = "spring")
public interface ConsumerDownloadHistoryMapper {

	ObjectMapper mapper = new ObjectMapper();

	ConsumerDownloadHistory mapFrom(ConsumerDownloadHistoryEntity entity);

	@SneakyThrows
	default ConsumerDownloadHistory mapFromCustom(ConsumerDownloadHistoryEntity entity) {
		ConsumerDownloadHistory mapFrom = mapFrom(entity);
		
		if (entity.getOffers() != null)
			mapFrom.setOffers(mapper.readValue(entity.getOffers(), new TypeReference<List<Offer>>() {
			}));
		if (entity.getPolicies() != null)
			mapFrom.setPolicies(mapper.readValue(entity.getPolicies(), new TypeReference<List<UsagePolicies>>() {
			}));
			
		return mapFrom;
	}
}
