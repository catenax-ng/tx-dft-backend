package org.eclipse.tractusx.sde.common.configuration.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@Data
public class PortalConfigurationProperties {

	@Value("${portal.backend.hostname}")
	private String hostname;
	
	@Value("${portal.backend.authentication.url}")
	private String authenticationUrl;
	
	@Value("${portal.backend.clientId}")
	private String clientId;
	
	@Value("${portal.backend.clientSecret}")
	private String clientSecret;
	
	@Value("${portal.backend.grantType}")
	private String grantType;

}
