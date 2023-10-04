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

package org.eclipse.tractusx.sde.core.registry;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.tractusx.sde.common.extensions.SubmodelExtension;
import org.eclipse.tractusx.sde.common.model.Submodel;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SubmodelRegistration {

	private final List<Submodel> submodelList;


	public SubmodelRegistration() {
		submodelList = new LinkedList<>();
	}

	public void register(SubmodelExtension subomdelService) {
		Submodel submodel = subomdelService.submodel();
		log.info(submodel.toString());
		submodelList.add(submodel);
	}
	
	@SneakyThrows
	public void register(JsonObject schema) {
		Submodel build = Submodel.builder()
				.id(schema.get("id").getAsString())
				.name(schema.get("title").getAsString())
				.version(schema.get("version").getAsString())
				.semanticId(schema.get("semantic_id").getAsString())
				.schema(schema).build();
		log.info(build.toString());
		submodelList.add(build);
	}

	public List<Submodel> getModels() {
		return this.submodelList;
	}

}
