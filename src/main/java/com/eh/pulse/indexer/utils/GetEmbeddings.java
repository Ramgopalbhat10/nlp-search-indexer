package com.eh.pulse.indexer.utils;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.eh.pulse.indexer.model.Embedding;

@Service
public class GetEmbeddings {
  
  @Autowired
  private RestTemplate restTemplate;

  @Value("${embedding.url}")
  private String embeddingUrl;
  
  @Async
  public CompletableFuture<Embedding> getEmbedding(String text) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(embeddingUrl).queryParam("text", text);
    Embedding embedding = restTemplate.getForObject(builder.toUriString(), Embedding.class);
    return CompletableFuture.completedFuture(embedding);
  }
}
