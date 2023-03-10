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

package org.eclipse.tractusx.sde.submodels.apr;

import java.util.List;

import org.eclipse.tractusx.sde.common.entities.csv.RowData;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerDigitalTwinUseCaseException;
import org.eclipse.tractusx.sde.common.submodel.executor.SubmodelExecutor;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.CsvParse;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.JsonRecordFormating;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.JsonRecordValidate;
import org.eclipse.tractusx.sde.submodels.apr.mapper.AspectRelationshipMapper;
import org.eclipse.tractusx.sde.submodels.apr.model.AspectRelationship;
import org.eclipse.tractusx.sde.submodels.apr.service.AspectRelationshipService;
import org.eclipse.tractusx.sde.submodels.apr.steps.DigitalTwinsAspectRelationShipCsvHandlerUseCase;
import org.eclipse.tractusx.sde.submodels.apr.steps.EDCAspectRelationshipHandlerUseCase;
import org.eclipse.tractusx.sde.submodels.apr.steps.StoreAspectRelationshipCsvHandlerUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@Component
@AllArgsConstructor
public class AssemblyPartRelationshipExecutor extends SubmodelExecutor {

	@Autowired
	private final CsvParse csvParseStep;

	private final JsonRecordValidate jsonRecordValidate;
	
	private final JsonRecordFormating jsonRecordformater;

	private final DigitalTwinsAspectRelationShipCsvHandlerUseCase digitalTwinsAspectRelationShipCsvHandlerUseCase;

	private final EDCAspectRelationshipHandlerUseCase eDCAspectRelationshipHandlerUseCase;

	private final StoreAspectRelationshipCsvHandlerUseCase storeAspectRelationshipCsvHandlerUseCase;

	private final AspectRelationshipMapper aspectRelationshipMapper;

	private final AspectRelationshipService aspectRelationshipService;

	@SneakyThrows
	public void executeCsvRecord(RowData rowData, ObjectNode jsonObject, String processId) {

		csvParseStep.init(getSubmodelSchema());
		csvParseStep.run(rowData, jsonObject, processId);

		nextSteps(rowData.position(), jsonObject, processId);

	}

	@SneakyThrows
	public void executeJsonRecord(Integer rowIndex, ObjectNode jsonObject, String processId) {

		jsonRecordformater.init(getSubmodelSchema());
		jsonRecordformater.run(rowIndex, jsonObject, processId);
		
		nextSteps(rowIndex, jsonObject, processId);

	}

	private void nextSteps(Integer rowIndex, ObjectNode jsonObject, String processId)
			throws CsvHandlerDigitalTwinUseCaseException {

		AspectRelationship aspectRelationship = aspectRelationshipMapper.mapFrom(jsonObject);

		jsonRecordValidate.init(getSubmodelSchema());
		jsonRecordValidate.run(rowIndex, jsonObject);

		digitalTwinsAspectRelationShipCsvHandlerUseCase.init(getSubmodelSchema());
		digitalTwinsAspectRelationShipCsvHandlerUseCase.run(aspectRelationship);

		eDCAspectRelationshipHandlerUseCase.init(getSubmodelSchema());
		eDCAspectRelationshipHandlerUseCase.run(getNameOfModel(), aspectRelationship, processId);

		storeAspectRelationshipCsvHandlerUseCase.run(aspectRelationship);
	}

	@Override
	public void executeDeleteRecord(JsonObject jsonObject, String delProcessId, String refProcessId) {
		aspectRelationshipService.deleteAllDataBySequence(jsonObject);
	}

	@Override
	public List<JsonObject> readCreatedTwinsforDelete(String refProcessId) {
		return aspectRelationshipService.readCreatedTwinsforDelete(refProcessId);
	}

	@Override
	public JsonObject readCreatedTwinsDetails(String uuid) {
		return aspectRelationshipService.readCreatedTwinsDetails(uuid);
	}

	@Override
	public int getUpdatedRecordCount(String processId) {
		return aspectRelationshipService.getUpdatedData(processId);
	}

}
