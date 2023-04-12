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

package org.eclipse.tractusx.sde.core.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;

import org.eclipse.tractusx.sde.core.processreport.ProcessReportUseCase;
import org.eclipse.tractusx.sde.core.processreport.model.ProcessFailureDetails;
import org.eclipse.tractusx.sde.core.processreport.model.ProcessReport;
import org.eclipse.tractusx.sde.core.processreport.model.ProcessReportPageResponse;
import org.eclipse.tractusx.sde.core.service.SubmodelCsvService;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("processing-report")
public class ProcessReportController {

	private final ProcessReportUseCase processReportUseCase;

	private final SubmodelCsvService submodelCsvService;

	public ProcessReportController(ProcessReportUseCase processReportUseCase, SubmodelCsvService submodelCsvService) {
		this.processReportUseCase = processReportUseCase;
		this.submodelCsvService = submodelCsvService;
	}

	@GetMapping(produces = APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission('','provider_view_history')")
	public ResponseEntity<ProcessReportPageResponse> getProcessingReportsByDateDesc(@Param("page") Integer page,
			@Param("pageSize") Integer pageSize) {
		page = page == null ? 0 : page;
		pageSize = pageSize == null ? 10 : pageSize;

		return ok().body(processReportUseCase.listAllProcessReports(page, pageSize));
	}

	@GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission('','provider_view_history')")
	public ResponseEntity<ProcessReport> getProcessReportById(@PathVariable("id") String id) {
		ProcessReport processReportById = processReportUseCase.getProcessReportById(id);
		if (processReportById == null) {
			return notFound().build();
		}
		return ok().body(processReportById);
	}

	@GetMapping(value = "/failure-details/{id}", produces = APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission('','provider_view_history')")
	public ResponseEntity<List<ProcessFailureDetails>> getProcessFailureDetailsReportById(
			@PathVariable("id") String id) {
		List<ProcessFailureDetails> processDetails = processReportUseCase.getProcessFailureDetailsReportById(id);
		return ok().body(processDetails);
	}

	@GetMapping(value = "{submodel}/success-details/{id}", produces = APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission('','provider_view_history')")
	public ResponseEntity<List<List<String>>> getProcessSuccessDetailsReportById(@PathVariable("id") String processId,
			@PathVariable("submodel") String submodel) {
		List<List<String>> processDetails = submodelCsvService.findAllSubmodelCsvHistory(submodel, processId);
		return ok().body(processDetails);
	}
}
