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

package org.eclipse.tractusx.sde.pcfexchange.repository;

import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.sde.pcfexchange.entity.PcfRequestEntity;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFRequestStatusEnum;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PcfRequestRepository extends JpaRepository<PcfRequestEntity, String> {
	
	List<PcfRequestEntity> findByProductId(String productId);
	
	Optional<PcfRequestEntity> findByRequestIdAndProductIdAndBpnNumber(String requestId, String productId,
			String bpnNumber);
	
	Page<PcfRequestEntity> findByType(Pageable pageable, PCFTypeEnum type);
	
	Page<PcfRequestEntity> findByTypeAndStatusIn(Pageable pageable, PCFTypeEnum type, List<PCFRequestStatusEnum> status);

	Page<PcfRequestEntity> findByStatusAndTypeLikeOrderByLastUpdatedTimeDesc(PageRequest of, PCFRequestStatusEnum status, PCFTypeEnum type);

}
