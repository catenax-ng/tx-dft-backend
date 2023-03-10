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
package org.eclipse.tractusx.sde.submodels.pap.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "part_as_planned")
@Entity
@Data
public class PartAsPlannedEntity implements Serializable {

    @Id
    @Column(name = "uuid")
    private String uuid;
    
    @Column(name = "process_id")
    private String processId;
    
    @Column(name = "manufacturer_part_id")
    private String manufacturerPartId;
    
    @Column(name = "classification")
    private String classification;
    
    @Column(name = "name_at_manufacturer")
    private String nameAtManufacturer;
    
    @Column(name = "customer_part_id")
    private String customerPartId;
    
    @Column(name = "valid_from")
    private String validFrom;
    
    @Column(name = "valid_to")
    private String validTo;

    @Column(name = "shell_id")
    private String shellId;
    
    @Column(name = "sub_model_id")
    private String subModelId;
    
    @Column(name = "contract_defination_id")
    private String contractDefinationId;
    
    @Column(name = "usage_policy_id")
    private String usagePolicyId;
    
    @Column(name = "access_policy_id")
    private String accessPolicyId;
    
    @Column(name = "asset_id")
    private String assetId;
    
    @Column(name = "deleted")
    private String deleted;
    
    @Column(name = "updated")
    private String updated;
}