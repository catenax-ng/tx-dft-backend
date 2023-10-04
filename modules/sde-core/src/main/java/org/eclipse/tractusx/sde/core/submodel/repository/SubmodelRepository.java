package org.eclipse.tractusx.sde.core.submodel.repository;

import org.eclipse.tractusx.sde.core.submodel.entity.SubmodelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmodelRepository extends JpaRepository<SubmodelEntity, String> {

}
