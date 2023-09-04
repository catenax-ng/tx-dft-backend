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
package org.eclipse.tractusx.sde.submodels.pcf.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class Pcf {
	
	private String biogenicCarbonEmissionsOtherThanCO2;
	private String distributionStagePcfExcludingBiogenic;
	private String biogenicCarbonWithdrawal;
	private String distributionStageBiogenicCarbonEmissionsOtherThanCO2;
	
	@SerializedName(value = "extWBCSD_allocationRulesDescription")
	private String extWBCSDAllocationRulesDescription;
	private String exemptedEmissionsDescription;
	private String distributionStageFossilGhgEmissions;
	private String exemptedEmissionsPercent;
	private String geographyCountrySubdivision;

	@SerializedName(value = "extTFS_luGhgEmissions")
	private String extTFSLuGhgEmissions;
	private String distributionStageBiogenicCarbonWithdrawal;
	private String pcfIncludingBiogenic;
	private String aircraftGhgEmissions;
	private String productMassPerDeclaredUnit;
	
	private List<ProductOrSectorSpecificRules> productOrSectorSpecificRules;
	
	@SerializedName(value = "extTFS_allocationWasteIncineration")
	private String extTFSAllocationWasteIncineration;
	private String pcfExcludingBiogenic;
	private String referencePeriodEnd;
	
	@SerializedName(value = "extWBCSD_characterizationFactors")
	private String extWBCSDCharacterizationFactors;
	
	private List<SecondaryEmissionFactorSources> secondaryEmissionFactorSources;
	
	private String unitaryProductAmount;
	private String declaredUnit;
	private String referencePeriodStart;
	private String geographyRegionOrSubregion;
	private String fossilGhgEmissions;
	private String boundaryProcessesDescription;
	private String geographyCountry;
	
	@SerializedName(value = "extWBCSD_packagingGhgEmissions")
	private String extWBCSDPackagingGhgEmissions;
	private String dlucGhgEmissions;
	private String carbonContentTotal;
	
	@SerializedName(value = "extTFS_distributionStageLuGhgEmissions")
	private String extTFSDistributionStageLuGhgEmissions;
	private String primaryDataShare;
	
	private DataQualityRating dataQualityRating;
	
	@SerializedName(value = "extWBCSD_packagingEmissionsIncluded")
	private String extWBCSDPackagingEmissionsIncluded;
	
	@SerializedName(value = "extWBCSD_fossilCarbonContent")
	private String extWBCSDFossilCarbonContent;
	
	private List<CrossSectoralStandardsUsed> crossSectoralStandardsUsed;
	
	@SerializedName(value = "extTFS_distributionStageDlucGhgEmissions")
	private String extTFSDistributionStageDlucGhgEmissions;
	private String distributionStagePcfIncludingBiogenic;
	private String carbonContentBiogenic;

}
