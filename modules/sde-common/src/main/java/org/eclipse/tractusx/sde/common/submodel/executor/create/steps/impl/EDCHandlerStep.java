package org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.common.configuration.properties.EDCConfigurationProperties;
import org.eclipse.tractusx.sde.common.entities.UsagePolicies;
import org.eclipse.tractusx.sde.common.enums.UsagePolicyEnum;
import org.eclipse.tractusx.sde.common.mapper.EDCAssetEntryRequestFactory;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.edc.asset.EDCAssetV2Facilator;
import org.eclipse.tractusx.sde.edc.core.model.Constraint;
import org.eclipse.tractusx.sde.edc.core.model.ConstraintOperator;
import org.eclipse.tractusx.sde.edc.core.model.EDCOffer;
import org.eclipse.tractusx.sde.edc.core.model.LogicalConstraint;
import org.eclipse.tractusx.sde.edc.core.model.LogicalOperatorType;
import org.eclipse.tractusx.sde.edc.provider.EDCProviderV2Facilator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Component
@RequiredArgsConstructor
public class EDCHandlerStep extends Step {

	private final EDCProviderV2Facilator edcProviderV2Facilator;
	private final EDCAssetV2Facilator edcAssetV2Facilator;
	private final EDCAssetEntryRequestFactory edcAssetEntryRequestFactory;
	private final EDCConfigurationProperties edcConfigurationProperties;

	@Value(value = "${dft.hostname}")
	private String dftHostname;

	@SneakyThrows
	@PostConstruct
	public void init() {

		edcProviderV2Facilator.init(edcConfigurationProperties.getProviderHost(), 
				edcConfigurationProperties.getProviderApiKey(),
				edcConfigurationProperties.getProviderApiValue(), 
				edcConfigurationProperties.getProviderManagementPath());

		edcAssetV2Facilator.init(edcConfigurationProperties.getProviderHost(),
				edcConfigurationProperties.getProviderApiKey(),
				edcConfigurationProperties.getProviderApiValue(), 
				edcConfigurationProperties.getProviderManagementPath());
	}

	@SneakyThrows
	public JsonNode getAsset(String assetId) {
		return edcAssetV2Facilator.getAsset(assetId);
	}

	public JsonNode getAllAssets(Integer offset, Integer limit, List<Constraint> filterCriteria) {
		return edcAssetV2Facilator.getAllAssets(offset, limit, filterCriteria);
	}
	
	@SneakyThrows
	public Map<String, String> createEDCOffer(String submodel, String shellId, String subModelId, String uuid,
			List<String> bpnNumbers, Map<UsagePolicyEnum, UsagePolicies> usagePolicies) {

		String assetId = shellId + "-" + subModelId;

		Map<String, Object> assetProperties = edcAssetEntryRequestFactory.getAssetProperties(assetId,
				getSubmodelShortDescriptionOfModel());

		Map<String, Object> dataAddressProps = edcAssetEntryRequestFactory
				.getDataAddressProperties(subModelPayloadUrl(submodel, uuid));

		return createAsset(bpnNumbers, usagePolicies, assetProperties, dataAddressProps);
	}

	public Map<String, String> createAsset(List<String> bpnNumbers, Map<UsagePolicyEnum, UsagePolicies> usagePolicies,
			Map<String, Object> assetProperties, Map<String, Object> dataAddressProps) {
		
		List<Constraint> accessConstraints = new ArrayList<>();
		
		bpnNumbers.stream().forEach(bpn -> 
					accessConstraints.add(Constraint.builder()
					.leftOperand("BusinessPartnerNumber")
					.operator(ConstraintOperator.EQ)
					.rightOperand(bpn).build()));
		
		LogicalConstraint accessLC = LogicalConstraint.builder()
				.type(LogicalOperatorType.OR)
				.constraints(accessConstraints)
				.build();

		List<Constraint> usageConstraints = new ArrayList<>();
		for (Map.Entry<UsagePolicyEnum, UsagePolicies> entry : usagePolicies.entrySet()) {
			usageConstraints.add(Constraint.builder()
					.leftOperand(entry.getKey().name())
					.operator(ConstraintOperator.EQ)
					.rightOperand(entry.getValue())
					.build());
		}
		LogicalConstraint usageLC = LogicalConstraint.builder()
				.type(LogicalOperatorType.OR)
				.constraints(accessConstraints)
				.build();

		EDCOffer edcOffer = EDCOffer.builder()
				.assetProperties(assetProperties)
				.dataAddressProperties(dataAddressProps)
				.accessPolicyConstraint(accessLC)
				.usagePolicyConstraint(usageLC)
				.build();

		return edcProviderV2Facilator.createEDCOffer(edcOffer);
	}

	private String subModelPayloadUrl(String submodel, String uuid) {
		return UriComponentsBuilder.fromHttpUrl(dftHostname).path("/" + submodel + "/public/").path(uuid).toUriString();
	}

}
