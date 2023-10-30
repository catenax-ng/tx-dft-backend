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

package org.eclipse.tractusx.sde.core.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.sde.common.ConfigurationProvider;
import org.eclipse.tractusx.sde.common.utils.TryUtils;
import org.eclipse.tractusx.sde.common.validators.SpringValidator;
import org.eclipse.tractusx.sde.sftp.service.RetrieverScheduler;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@PreAuthorize("hasPermission('','auto_config_management')")
@RequiredArgsConstructor
public class AutoUploadAgentConfigController {


	private final ApplicationContext context;
	private final SpringValidator springValidator;
	private final RetrieverScheduler retrieverScheduler;

	private final ObjectMapper mapper = new ObjectMapper();


	@PostMapping("/fire")
	public Map<String, String> fire() {
		return Map.of("msg", retrieverScheduler.fire());
	}

	@GetMapping("/config/{type}")
	public Object getConfig(@PathVariable("type") String type) {
		return TryUtils.tryExec(
				() -> (ConfigurationProvider<?>) context.getBean(type.toLowerCase()),
				TryUtils::IGNORE
		).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Configuration not found")
		).getConfiguration();
	}

	@PutMapping("/config/{type}")
	public void updateScheduler(@PathVariable("type") String type, @RequestBody JsonNode config) {
		@SuppressWarnings("unchecked") var cp = (ConfigurationProvider<Object>) TryUtils.tryExec(
				() ->  (ConfigurationProvider<?>) context.getBean(type.toLowerCase()),
				TryUtils::IGNORE
		).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Configuration not found"));
		Class<?> aClass = cp.getConfigClass();
		var configObj = mapper.convertValue(config, aClass);
		cp.saveConfig(springValidator.validate(configObj));
	}
}
