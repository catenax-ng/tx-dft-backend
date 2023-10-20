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

package org.eclipse.tractusx.sde.configuration;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.SneakyThrows;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	private static final String[] PUBLIC_URL = { "/ping", "/cache/**", "/api-docs/**", "/swagger-ui/**",
			"*/swagger-ui/**", "/actuator/health/readiness", "/actuator/health/liveness", "/v3/api-docs/**" };

	@Value("${keycloak.clientid}")
	private String resourceName;

	public interface Jwt2AuthoritiesConverter extends Converter<Jwt, Collection<? extends GrantedAuthority>> {
	}

	@SuppressWarnings("unchecked")
	@Bean
	public Jwt2AuthoritiesConverter authoritiesConverter() {
		// This is a converter for roles as embedded in the JWT by a Keycloak server
		// Roles are taken from both realm_access.roles & resource_access.{client}.roles

		return jwt -> {
			final var realmAccess = (Map<String, Object>) jwt.getClaims().getOrDefault("realm_access", Map.of());
			final var realmRoles = (Collection<String>) realmAccess.getOrDefault("roles", List.of());

			final var resourceAccess = (Map<String, Object>) jwt.getClaims().getOrDefault("resource_access", Map.of());
			// read all roles from resource
			final var confidentialClientAccess = (Map<String, Object>) resourceAccess.getOrDefault(resourceName,
					Map.of());
			final var confidentialClientRoles = (Collection<String>) confidentialClientAccess.getOrDefault("roles",
					List.of());

			return Stream.concat(realmRoles.stream(), confidentialClientRoles.stream()).map(SimpleGrantedAuthority::new)
					.toList();
		};
	}

	public interface Jwt2AuthenticationConverter extends Converter<Jwt, AbstractAuthenticationToken> {
	}

	@Bean
	public Jwt2AuthenticationConverter authenticationConverter(Jwt2AuthoritiesConverter authoritiesConverter) {
		return jwt -> new JwtAuthenticationToken(jwt, authoritiesConverter.convert(jwt));
	}

	@SneakyThrows
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, Jwt2AuthenticationConverter authenticationConverter,
			ServerProperties serverProperties) {

		// Enable OAuth2 with custom authorities mapping
		http.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter)));

		// Enable anonymous
		http.anonymous();

		// Enable and configure CORS
		http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

		// State-less session (state in access-token only)
		http.sessionManagement(
				sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		// Disable CSRF because of state-less session-management
		http.csrf(AbstractHttpConfigurer::disable);

		// Route security: authenticated to all routes but actuator and Swagger-UI
		// @formatter:off
		        http.authorizeHttpRequests(authz -> authz
		        		.requestMatchers(PUBLIC_URL).permitAll()
		                .anyRequest().authenticated());
		            
		        // @formatter:on
		http.headers(headers -> headers
				.xssProtection(xssProtection -> xssProtection.headerValue(HeaderValue.ENABLED_MODE_BLOCK))
				.contentSecurityPolicy(policy -> policy.policyDirectives("default-src 'self'; script-src 'self'"))
				.httpStrictTransportSecurity(httStrict -> httStrict.includeSubDomains(true).maxAgeInSeconds(15724800)));

		return http.build();
	}

	@Bean
	protected CorsConfigurationSource corsConfigurationSource() {
		// Very permissive CORS config...
		final var configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("*"));
		configuration.setAllowedMethods(Arrays.asList("*"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setExposedHeaders(Arrays.asList("*"));
		// Limited to API routes (neither actuator nor Swagger-UI)
		final var source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

}
