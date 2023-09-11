package org.eclipse.tractusx.sde.sematichub.proxy;

import java.net.URI;

import org.eclipse.tractusx.sde.common.utils.TokenUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

public class SematicHubApiProxyConfiguration {

	@Bean(name = "sematicHubApiProxyInterceptor")
	public SematicHubApiProxyInterceptor appRequestInterceptor() {
		return new SematicHubApiProxyInterceptor();
	}
}

@Slf4j
class SematicHubApiProxyInterceptor implements RequestInterceptor {

	@Value(value = "${semantics.backend.authentication.url}")
	private URI semanticsAppTokenURI;

	@Value(value = "${semantics.backend.clientSecret}")
	private String semanticsAppClientSecret;

	@Value(value = "${semantics.backend.clientId}")
	private String semanticsAppClientId;

	@Value(value = "${semantics.backend.grantType}")
	private String semanticsGrantType;

	@Autowired
	private TokenUtility tokenUtilityForSemantics;

	private String accessTokenForSemantics;
	
	@Override
	public void apply(RequestTemplate template) {
		template.header("Authorization", getTokenForSemantics());
		log.debug("Bearer authentication applied for PartnerPoolExternalServiceApiInterceptor");
	}

	@SneakyThrows
	public String getTokenForSemantics(){
		if (accessTokenForSemantics != null && tokenUtilityForSemantics.isTokenValid(accessTokenForSemantics)) {
			return "Bearer " + accessTokenForSemantics;
		}
		accessTokenForSemantics = tokenUtilityForSemantics.getToken(semanticsAppTokenURI, semanticsGrantType,
				semanticsAppClientId, semanticsAppClientSecret);
		return "Bearer " + accessTokenForSemantics;
	}
}