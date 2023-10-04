package org.eclipse.tractusx.sde.core.submodel.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.tractusx.sde.sematichub.proxy.SematichubModel;
import org.mapstruct.Mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import lombok.SneakyThrows;

@Mapper(componentModel = "spring")
public interface SubmodelSematicModelMapper {

	Gson gson = new Gson();

	public SubmodelEntity mapFrom(SematichubModel sematichubModel);

	public SematichubModel mapFrom(SubmodelEntity entity);
	
	@SneakyThrows
	default JsonObject entitytoPojo(SubmodelEntity entity) {
		ObjectMapper mapper= new ObjectMapper();
		return gson.fromJson(mapper.writeValueAsString(entity), JsonObject.class).getAsJsonObject();
	}
	
	@SneakyThrows
	default JsonObject pojotoJson(SematichubModel pojo) {
		ObjectMapper mapper= new ObjectMapper();
		return gson.fromJson(mapper.writeValueAsString(pojo), JsonObject.class).getAsJsonObject();
	}

	@SneakyThrows
	@SuppressWarnings("unchecked")
	default Map<Object, Object> jsonPojoToMap(JsonObject input) {
		return gson.fromJson(input, LinkedHashMap.class);
	}

}
