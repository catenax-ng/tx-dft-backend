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
package org.eclipse.tractusx.sde.core.processreport.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "consumer_download_history")
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Cacheable(value = false)
public class ConsumerDownloadHistoryEntity {
	@Id
	@Column(name = "process_id")
	private String processId;
	@Column(name = "provider_url")
	private String providerUrl;
	@Column(name = "connector_id")
	private String connectorId;
	@Column(name = "number_of_items")
	private Integer numberOfItems;
	@Column(name = "download_failed")
	private Integer downloadFailed;
	@Column(name = "download_successed")
	private Integer downloadSuccessed;
	@Column(name = "status")
	private String status;
	@Column(name = "start_date")
	private LocalDateTime startDate;
	@Column(name = "end_date")
	private LocalDateTime endDate;
	
	@Lob
	@Column(name = "offers")
	private String offers;
	@Lob
	private String policies;
	private String referenceProcessId;

}