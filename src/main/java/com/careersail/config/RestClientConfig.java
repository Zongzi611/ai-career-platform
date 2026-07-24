package com.careersail.config;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Fix DashScope embedding API returning application/octet-stream instead of application/json
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClientCustomizer dashScopeContentTypeFix() {
        return restClientBuilder -> restClientBuilder.messageConverters(converters -> {
            for (var converter : converters) {
                if (converter instanceof MappingJackson2HttpMessageConverter jackson) {
                    List<MediaType> supported = new ArrayList<>(jackson.getSupportedMediaTypes());
                    supported.add(MediaType.APPLICATION_OCTET_STREAM);
                    jackson.setSupportedMediaTypes(supported);
                }
            }
        });
    }
}
