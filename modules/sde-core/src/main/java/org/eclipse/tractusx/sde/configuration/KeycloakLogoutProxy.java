package org.eclipse.tractusx.sde.configuration;

import java.net.URI;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "KeycloakLogoutProxy", url = "placeholder")
public interface KeycloakLogoutProxy {

	@GetMapping
	public String logoutProxy(URI url);

}
