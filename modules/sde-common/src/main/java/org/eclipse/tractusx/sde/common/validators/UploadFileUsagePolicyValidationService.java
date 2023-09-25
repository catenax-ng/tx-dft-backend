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

package org.eclipse.tractusx.sde.common.validators;

import org.eclipse.tractusx.sde.common.entities.SubmodelPolicyRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.SneakyThrows;

public class UploadFileUsagePolicyValidationService implements ConstraintValidator<UsagePolicyValidation, String> {

    private ObjectMapper objectMapper = new ObjectMapper();
    private final ValidationService validationService;

    public UploadFileUsagePolicyValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public void initialize(UsagePolicyValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @SneakyThrows
    @Override
    public boolean isValid(String metadata, ConstraintValidatorContext constraintValidatorContext) {
        SubmodelPolicyRequest submodelFileRequest = objectMapper.readValue(metadata, SubmodelPolicyRequest.class);
        return validationService.isValid(submodelFileRequest.getUsagePolicies());
    }
}
