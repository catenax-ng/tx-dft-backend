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
package org.eclipse.tractusx.sde.submodels.slbap.steps;

import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.submodels.pap.entity.PartAsPlannedEntity;
import org.eclipse.tractusx.sde.submodels.pap.repository.PartAsPlannedRepository;
import org.eclipse.tractusx.sde.submodels.slbap.model.SingleLevelBoMAsPlanned;
import org.springframework.stereotype.Component;

import lombok.SneakyThrows;

@Component
public class SingleLevelBoMASPlannedUUIDUrnUUID extends Step {

	private final PartAsPlannedRepository repository;


	public SingleLevelBoMASPlannedUUIDUrnUUID(PartAsPlannedRepository repository) {
		this.repository = repository;
	}
	
	@SneakyThrows
	public SingleLevelBoMAsPlanned run(SingleLevelBoMAsPlanned input, String processId) {
		if (input.getParentUuid() == null || input.getParentUuid().isBlank()) {
			String parentUuid = getUuidIfPartAsPlannedAspectExists(input.getRowNumber(), input.getParentManufacturerPartId());
			input.setParentUuid(parentUuid);
		}

		if (input.getChildUuid() == null || input.getChildUuid().isBlank()) {
			String childUuid = getUuidIfPartAsPlannedAspectExists(input.getRowNumber(), input.getChildManufacturerPartId());
			input.setChildUuid(childUuid);
		}

		return input;
	}

	@SneakyThrows
	private String getUuidIfPartAsPlannedAspectExists(int rowNumber, String manufacturerPartId) {
		PartAsPlannedEntity entity = repository.findByManufacturerPartId(manufacturerPartId);

		if (entity == null) {
			throw new CsvHandlerUseCaseException(rowNumber, String.format(
					"Missing parent PartAsPlanned Aspect for the given Identifier: ManufactorerPartId: %s ",
					manufacturerPartId));
		}
		return entity.getUuid();
	}

}
