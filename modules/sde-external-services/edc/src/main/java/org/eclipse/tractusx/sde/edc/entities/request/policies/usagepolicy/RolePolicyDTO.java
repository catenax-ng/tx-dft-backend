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

package org.eclipse.tractusx.sde.edc.entities.request.policies.usagepolicy;

import org.eclipse.tractusx.sde.common.entities.UsagePolicies;
import org.eclipse.tractusx.sde.common.enums.PolicyAccessEnum;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ConstraintRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.Expression;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@SuperBuilder
public class RolePolicyDTO extends UsagePolicyDTO{
    private static final String DATASPACECONNECTOR_LITERALEXPRESSION = "dataspaceconnector:literalexpression";

    public static RolePolicyDTO fromUsagePolicy(UsagePolicies usagePolicy)
    {
    	return RolePolicyDTO.builder().type(usagePolicy.getType()).typeOfAccess(usagePolicy.getTypeOfAccess())
                .value(usagePolicy.getValue()).build();

    }
    @Override
    public ConstraintRequest toConstraint() {
        if (getTypeOfAccess().equals(PolicyAccessEnum.RESTRICTED)) {
            Expression lExpression = Expression.builder()
                    .edcType(DATASPACECONNECTOR_LITERALEXPRESSION)
                    .value("idsc:ROLE")
                    .build();

            String operator = "EQ";
            Expression rExpression = null;
            rExpression = Expression.builder()
                    .edcType(DATASPACECONNECTOR_LITERALEXPRESSION)
                    .value(getValue())
                    .build();

            return ConstraintRequest.builder().edcType("AtomicConstraint")
                    .leftExpression(lExpression)
                    .rightExpression(rExpression)
                    .operator(operator)
                    .build();

        }
        return null;
    }
}
