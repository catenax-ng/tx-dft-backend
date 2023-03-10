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
package org.eclipse.tractusx.sde.submodels.slbap.repository;

import java.util.List;

import org.eclipse.tractusx.sde.submodels.slbap.entity.SingleLevelBoMAsPlannedEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface SingleLevelBoMAsPlannedRepository extends CrudRepository<SingleLevelBoMAsPlannedEntity, String> {

	List<SingleLevelBoMAsPlannedEntity> findByProcessId(String processId);
	
    List<SingleLevelBoMAsPlannedEntity> findByParentCatenaXId(String parentCatenaXId);
    
	@Query("select count(ae) from SingleLevelBoMAsPlannedEntity ae where ae.updated = ?1 and ae.processId = ?2")
	long countByUpdatedAndProcessId(String updated, String processId);
	
	SingleLevelBoMAsPlannedEntity findByChildCatenaXId(String uuid);

}
