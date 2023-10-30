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

package org.eclipse.tractusx.sde;


import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.Base58;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;

public class TestContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4")
    		.withAccessToHost(true)
            .withDatabaseName("sde")
            .withUsername("root")
            .withPassword("P@ssword21");

//    static public GenericContainer<?>  sftp = new GenericContainer<>("dvasunin/sftp:latest")
//            .withCopyFileToContainer(
//                    MountableFile.forClasspathResource("sftp/", 0777),
//                    "/home/foo/upload/sftp")
//            .withExposedPorts(22)
//            .withCommand("foo:pass:::upload");

    public static GenericContainer<?> minio = new GenericContainer<>("quay.io/minio/minio")
            .withEnv("MINIO_ACCESS_KEY", "minioadmin")
            .withEnv("MINIO_SECRET_KEY", "minioadmin")
            .withCommand("server", "/data")
            .withExposedPorts(9000);
            ;


    static {
        Startables.deepStart(postgres, minio).join();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        TestPropertyValues.of(
            "spring.datasource.url=" + postgres.getJdbcUrl(),
            "spring.datasource.username="+ postgres.getUsername(),
            "spring.datasource.password="+postgres.getPassword(),
            "sftp.location.tobeprocessed=/upload/sftp/tobe",
            "sftp.location.success=/upload/sftp/success",
            "sftp.location.failed=/upload/sftp/failed",
            "sftp.location.partialsucess=/upload/sftp/partial",
            "sftp.location.inprogress=/upload/sftp/inprogress",
            "sftp.username=foo",
            "sftp.password=pass",
            "sftp.host=127.0.0.1",
            "sftp.port=22",
            "minio.endpoint="+minio.getHost(),
            "minio.access-key=minioadmin",
            "minio.secret-key=minioadmin1",
            "minio.bucket-name=bucket1",
            "minio.location.tobeprocessed=/upload/sftp/tobe",
            "minio.location.inprogress=/upload/sftp/inprogress",
            "minio.location.success=/upload/sftp/success",
            "minio.location.partialsucess=/upload/sftp/partial",
            "minio.location.failed=/upload/sftp/failed"
        ).applyTo(applicationContext);
    }
}
