package org.eclipse.tractusx.sde.common.configuration.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@Data
public class EDCConfigurationProperties {
	
	@Value("${edc.hostname}")
	private String providerHost;
	
	@Value("${edc.managementpath:/data}")
	private String providerManagementPath;
	
	@Value("${edc.apiKeyHeader}")
	private String providerApiKey;
	
	@Value("${edc.apiKey}")
	private String providerApiValue;
	
	@Value("${edc.protocol.path:/api/v1/dsp}")
	private String providerProtocolpath;
	
	
	
	
	@Value("${edc.consumer.hostname}")
	private String consumerHost;

	@Value("${edc.consumer.managementpath:/data}")
	private String consumerManagementPath;

	@Value("${edc.consumer.apikeyheader}")
	private String consumerApiKey;

	@Value("${edc.consumer.apikey}")
	private String consumerApiValue;
	
	@Value("${edc.consumer.protocol.path:/api/v1/dsp}")
	private String consumerProtocolpath;

}
