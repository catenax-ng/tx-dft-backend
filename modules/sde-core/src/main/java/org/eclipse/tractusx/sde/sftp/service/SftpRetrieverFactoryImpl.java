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

package org.eclipse.tractusx.sde.sftp.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.tractusx.sde.agent.entity.ConfigEntity;
import org.eclipse.tractusx.sde.agent.model.ConfigType;
import org.eclipse.tractusx.sde.agent.model.SftpConfigModel;
import org.eclipse.tractusx.sde.agent.repository.AutoUploadAgentConfigRepository;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.sftp.RetrieverI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.OptionalInt;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "retriever.name", havingValue="sftp")
public class SftpRetrieverFactoryImpl implements RetrieverFactory {

	@Value("${sftp.host}")
	private String host;
	@Value("${sftp.port:22}")
	private int port;
	@Value("${sftp.username}")
	private String username;
	@Value("${sftp.password}")
	private String password;
	@Value("${sftp.location.tobeprocessed}")
	private String toBeProcessed;
	@Value("${sftp.location.inprogress}")
	private String inProgress;
	@Value("${sftp.location.success}")
	private String success;
	@Value("${sftp.location.partialsucess}")
	private String partialSuccess;
	@Value("${sftp.location.failed}")
	private String failed;
	@Value("${sftp.retries:5}")
	private int numberOfRetries;
	@Value("${sftp.retryDelay.from:500}")
	private int retryDelayFrom;
	@Value("${sftp.retryDelayTo:3500}")
	private int retryDelayTo;

	private final ConfigService configService;
	private final AutoUploadAgentConfigRepository configRepository;
	private final CsvHandlerService csvHandlerService;

	@SneakyThrows
	public RetrieverI create(OptionalInt port) {
		SftpConfigModel configModel = configService.getSFTPConfiguration();
		return new SftpRetriever(csvHandlerService, 
				configModel.getHost(),
				port.orElse(configModel.getPort()),
				configModel.getUsername(), 
				configModel.getPassword(),
				configModel.getAccessKey(),
				configModel.getToBeProcessedLocation(), 
				configModel.getInProgressLocation(),
				configModel.getSuccessLocation(),
				configModel.getPartialSuccessLocation(),
				configModel.getFailedLocation(),
				numberOfRetries,
				retryDelayFrom,
				retryDelayTo
		);

	}

	@Override
	public RetrieverI create() {
		return create(OptionalInt.empty());
	}

	@SneakyThrows
	@Override
	public void saveDefaultConfig() {
		Optional<ConfigEntity> config = configRepository.findAllByType(ConfigType.SFTP.toString());
		if (config.isEmpty()) {
		SftpConfigModel sftpConfigModel = SftpConfigModel.builder()
				.host(host)
				.port(port)
				.failedLocation(failed)
				.username(username)
				.password(password)
				.toBeProcessedLocation(toBeProcessed)
				.inProgressLocation(inProgress)
				.partialSuccessLocation(partialSuccess)
				.successLocation(success).build();
		configService.saveConfiguration(ConfigType.SFTP, sftpConfigModel);
		}
	}
}
