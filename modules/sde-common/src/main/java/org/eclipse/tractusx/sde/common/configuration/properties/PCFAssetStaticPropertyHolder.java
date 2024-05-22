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
package org.eclipse.tractusx.sde.common.configuration.properties;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class PCFAssetStaticPropertyHolder {

	@Value(value = "${digital-twin.pcf.sematicid:}")
	public String sematicId;

	@Value(value = "${digital-twin.pcf.sematicid:}")
	public String sematicIdPart;

	private String pcfExchangeAssetId;

	public String getSematicId() {
		if (StringUtils.isAllBlank(sematicId))
			sematicId = "urn:samm:io.catenax.pcf:6.0.0#Pcf";
		return sematicId;
	}

	public void setSematicId(String sematicId) {
		this.sematicId = sematicId;
	}

	public String getSematicIdPart() {
		if (StringUtils.isAllBlank(sematicIdPart))
			sematicIdPart = "urn:samm:io.catenax.pcf";
		return sematicIdPart;
	}

	public void setSematicIdPart(String sematicIdPart) {
		this.sematicIdPart = sematicIdPart;
	}

	public String getPcfExchangeAssetId() {
		return pcfExchangeAssetId;
	}

	public void setPcfExchangeAssetId(String pcfExchangeAssetId) {
		this.pcfExchangeAssetId = pcfExchangeAssetId;
	}

}
