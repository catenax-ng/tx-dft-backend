/********************************************************************************
 * Copyright (c) 2023, 2024 T-Systems International GmbH
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.submodels.pcf.repository;

import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.sde.submodels.pcf.entity.PcfEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface PcfRepository extends CrudRepository<PcfEntity, String> {

	List<PcfEntity> findByProcessId(String processId);

	@Query("select count(ae) from PcfEntity ae where ae.updated = ?1 and ae.processId = ?2")
	long countByUpdatedAndProcessId(String updated, String processId);

	Optional<PcfEntity> findByProductId(String productId);

}