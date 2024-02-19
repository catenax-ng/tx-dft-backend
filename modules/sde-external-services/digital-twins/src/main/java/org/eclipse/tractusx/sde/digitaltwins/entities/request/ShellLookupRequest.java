/********************************************************************************
 * Copyright (c) 2022 BMW GmbH
 * Copyright (c) 2022, 2024 T-Systems International GmbH
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.digitaltwins.entities.request;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tractusx.sde.digitaltwins.entities.common.LocalIdentifier;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

public class ShellLookupRequest {

    @JsonProperty
    private final List<LocalIdentifier> assetIds;

    public ShellLookupRequest() {
        this.assetIds = new ArrayList<>();
    }

    public void addLocalIdentifier(String key, String vale) {
        assetIds.add(LocalIdentifier
                .builder()
                .key(key)
                .value(vale)
                .build()
        );
    }
    
    public List<LocalIdentifier> getAssetIds() {
		return assetIds;
	}

    @SneakyThrows
    public String toJsonString() {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(assetIds);
    }
}