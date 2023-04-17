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

package org.eclipse.tractusx.sde.edc.entities.request.policies.accesspolicy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tractusx.sde.edc.entities.request.policies.ConstraintRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.Expression;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccessPolicyDTO {
	private static final String DATASPACECONNECTOR_LITERALEXPRESSION = "dataspaceconnector:literalexpression";

	private List<String> bpnNumbers;

	public ConstraintRequest toConstraint() {
		if (bpnNumbers.size() > 1) {
			List<ConstraintRequest> constraints = new ArrayList<>();
			bpnNumbers.stream().forEach(bpnNumber -> constraints.add(prepareConstraint(bpnNumber)));
			
			return ConstraintRequest.builder()
					.edcType("dataspaceconnector:orconstraint")
					.constraints(constraints)
					.build();
		} else {
			return prepareConstraint(bpnNumbers.get(0));
		}

	}

	private ConstraintRequest prepareConstraint(String bpnNumber) {
		Expression lExpression = Expression.builder()
				.edcType(DATASPACECONNECTOR_LITERALEXPRESSION)
				.value("BusinessPartnerNumber")
				.build();

		String operator = "EQ";

		Expression rExpression = Expression.builder()
				.edcType(DATASPACECONNECTOR_LITERALEXPRESSION)
				.value(bpnNumber)
				.build();

		return ConstraintRequest.builder()
				.edcType("AtomicConstraint")
				.leftExpression(lExpression)
				.rightExpression(rExpression)
				.operator(operator)
				.build();
	}
}
