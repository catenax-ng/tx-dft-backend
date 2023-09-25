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

package org.eclipse.tractusx.sde.core.policy.repository;

import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.sde.core.policy.entity.PolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PolicyRepository extends JpaRepository<PolicyEntity, String> {

	@Query(value = "select e FROM PolicyEntity e where e.uuid <> :uuid and e.policyName = :policyName")
	List<PolicyEntity> findByIdAndName(String uuid, String policyName);

	Optional<PolicyEntity> findByUuid(String uuid);

	void deleteByUuid(String uuid);

	List<PolicyEntity> findByPolicyNameLike(String policyName);
	
	Optional<PolicyEntity> findByPolicyName(String policyName);
}
