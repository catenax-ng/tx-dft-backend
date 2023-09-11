package org.eclipse.tractusx.sde.configuration;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KeycloakLogoutHandler implements LogoutHandler {

	@Autowired
	private KeycloakLogoutProxy keycloakLogoutProxy;

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication auth) {
		logoutFromKeycloak((OidcUser) auth.getPrincipal());
	}

	@SneakyThrows
	private void logoutFromKeycloak(OidcUser user) {
		String endSessionEndpoint = user.getIssuer() + "/protocol/openid-connect/logout";
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(endSessionEndpoint)
				.queryParam("id_token_hint", user.getIdToken().getTokenValue());
		String res = keycloakLogoutProxy.logoutProxy(new URI(builder.toUriString()));

		if (res != null) {
			log.info("Successfulley logged out from Keycloak");
		} else {
			log.error("Could not propagate logout to Keycloak");
		}
	}

}
