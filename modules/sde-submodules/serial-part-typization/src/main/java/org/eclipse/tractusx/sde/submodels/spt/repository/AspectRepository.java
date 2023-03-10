/********************************************************************************
 * Copyright (c) 2022 Critical TechWorks GmbH
 * Copyright (c) 2022 BMW GmbH
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

package org.eclipse.tractusx.sde.submodels.spt.repository;

import java.util.List;

import org.eclipse.tractusx.sde.common.enums.OptionalIdentifierKeyEnum;
import org.eclipse.tractusx.sde.submodels.spt.entity.AspectEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface AspectRepository extends CrudRepository<AspectEntity, String> {

	AspectEntity findByPartInstanceIdAndManufacturerPartIdAndOptionalIdentifierKeyAndOptionalIdentifierValue(
			String partInstanceId, String manufacturerPartId, OptionalIdentifierKeyEnum optionalIdentifierKey,
			String optionalIdentifierValue);

	AspectEntity findByPartInstanceIdAndManufacturerPartIdAndOptionalIdentifierKeyIsNullAndOptionalIdentifierValueIsNull(
			String partInstanceId, String manufacturerId);

	default AspectEntity findByIdentifiers(String partInstanceId, String manufacturerPartId,
			String optionalIdentifierKey, String optionalIdentifierValue) {
		return findByPartInstanceIdAndManufacturerPartIdAndOptionalIdentifierKeyAndOptionalIdentifierValue(
				partInstanceId, manufacturerPartId,
				optionalIdentifierKey == null ? null : OptionalIdentifierKeyEnum.valueOf(optionalIdentifierKey.toUpperCase()),
				optionalIdentifierValue);
	}

	default AspectEntity findByIdentifiers(String partInstanceId, String manufacturerId) {
		return findByPartInstanceIdAndManufacturerPartIdAndOptionalIdentifierKeyIsNullAndOptionalIdentifierValueIsNull(
				partInstanceId, manufacturerId);
	}

	AspectEntity findByUuid(String uuid);

	List<AspectEntity> findByProcessId(String processId);


	@Query("select count(ae) from AspectEntity ae where ae.updated = ?1 and ae.processId = ?2")
	long countByUpdatedAndProcessId(String updated, String processId);
	

}