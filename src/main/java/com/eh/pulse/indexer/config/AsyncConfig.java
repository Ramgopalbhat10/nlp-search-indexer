package com.eh.pulse.indexer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableAsync
public class AsyncConfig {

  @Bean
  RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
