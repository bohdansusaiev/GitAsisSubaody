package com.sigmadevs.aiintegration.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OpenAIConfig {

    @Value("${openai.api.key}")
    String openAIKey;
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body,execution) -> {
            request.getHeaders().add("Authorization", "Bearer " + openAIKey);
            return execution.execute(request, body);
        });
        return restTemplate;
    }

}
