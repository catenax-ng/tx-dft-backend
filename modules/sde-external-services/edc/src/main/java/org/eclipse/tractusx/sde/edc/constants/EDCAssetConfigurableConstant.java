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

package org.eclipse.tractusx.sde.edc.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class EDCAssetConfigurableConstant {

	@Value("${edc.asset.prop.common.version:1.0.0}")
	private String assetPropCommonVersion;

	@Value("${edc.asset.prop.dcat.version:1.0.0}")
	private String assetPropDcatVersion;

	@Value("${edc.asset.prop.type.default.value:data.core.digitalTwin.submodel}")
	private String assetPropTypeDefaultValue;

	@Value("${edc.asset.prop.type.digital-twin.value:DigitalTwinRegistry}")
	private String assetPropTypeDigitalTwin;

	@Value("${edc.asset.prop.type.pcfexchange.value:PcfExchange}")
	private String assetPropTypePCFExchangeType;

	@Value("${edc.policy.prefix:cx-policy:}")
	private String cxPolicyPrefix;
	
	@Value("${edc.policy.profile:profile2405}")
	private String cxPolicyProfile;

	@Value("${edc.policy.bpnnumber.technicalkey:BusinessPartnerNumber}")
	private String bpnNumberTechnicalKey;
	
	@Value("${edc.policy.pcf.framework.leftoperand:FrameworkAgreement}")
	private String pcfFrameworkAgreementLeftOperand;

	@Value("${edc.policy.pcf.framework.rightoperand:PCF:1.0}")
	private String pcfFrameworkAgreementRightOperand;

	@Value("${edc.policy.membership.leftoperand:Membership}")
	private String membershipAgreementLeftOperand;

	@Value("${edc.policy.membership.rightoperand:active}")
	private String membershipAgreementRightOperand;
	
	@Value("${edc.policy.usage-purpose.leftoperand:UsagePurpose}")
	private String usagePurposeLeftOperand;

	@Value("${edc.policy.usage-purpose.rightoperand:cx.pcf.base:1}")
	private String usagePurposeRightOperand;

}