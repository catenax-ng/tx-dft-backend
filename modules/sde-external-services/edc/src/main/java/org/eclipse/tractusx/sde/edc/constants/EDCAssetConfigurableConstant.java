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

	@Value("${edc.policy.profile:cx-policy:}")
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

}
