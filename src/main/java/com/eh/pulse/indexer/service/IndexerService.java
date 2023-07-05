package com.eh.pulse.indexer.service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexedObjectInformation;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.eh.pulse.indexer.model.Article;
import com.eh.pulse.indexer.model.Content;
import com.eh.pulse.indexer.model.Embedding;
import com.eh.pulse.indexer.repository.ContentRepository;
import com.eh.pulse.indexer.utils.JsonFileReader;

@Service
public class IndexerService {
  private static final Logger logger = LogManager.getLogger(IndexerService.class);
  
  private final ElasticsearchOperations elasticsearchOperations;
  private final JsonFileReader jsonFileReader;
  private final ContentRepository contentRepository;

  @Autowired
  private RestTemplate restTemplate;

  @Value("${embedding.url}")
  private String embeddingUrl;

  public IndexerService(
    ElasticsearchOperations elasticsearchOperations,
    JsonFileReader jsonFileReader,
    ContentRepository contentRepository) {
      this.elasticsearchOperations = elasticsearchOperations;
      this.jsonFileReader = jsonFileReader;
      this.contentRepository = contentRepository;
  }

  public void indexArticles() {
    List<Article> articles = this.jsonFileReader.readJson("medium_articles.json");
    logger.info("articles length -> {}", articles.size());

    logger.info("------------ Deleting all data ------------");
    this.contentRepository.deleteAll();
    logger.info("==> Count -> {}", this.contentRepository.count());

    List<Content> contents = articles.parallelStream().map(article -> {
      logger.info("==> Processing for article -> {}", article.getTitle());
      Content content = new Content();
      content.setId(article.get_id());
      content.setTitle(article.getTitle());
      content.setDescription(article.getDescription());
      content.setKeywords(article.getTags());
      
      List<CompletableFuture<List<Float>>> futures = Arrays.asList(article.getTags().split(",")).stream().map(tag -> {
        if(tag.trim().isEmpty()) return CompletableFuture.completedFuture((List<Float>)null);
        return CompletableFuture.supplyAsync(() -> {
          try {
            Embedding embedding = this.getEmbedding(tag).get();
            return embedding.getEmbedding();
          } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
          }
        });
      }).collect(Collectors.toList());

      List<List<Float>> embeddings = futures.stream().map(CompletableFuture::join)
        .filter(Objects::nonNull)
        .map(embedding -> (List<Float>)embedding)
        .collect(Collectors.toList());

      // List<List<Float>> embeddings = Arrays.asList(article.getTags().split(",")).parallelStream().map(tag -> {
      //   if(tag.trim().isEmpty()) return null;
      //   try {
      //     Embedding embedding = this.getEmbedding(tag).get();
      //     return embedding.getEmbedding();
      //   } catch (Exception e) {
      //     logger.error(e.getMessage());
      //     return null;
      //   }
      // })
      // .filter(Objects::nonNull)
      // .collect(Collectors.toList());

      if(!embeddings.isEmpty()) {
        List<Float> meanEmbedding = this.calculateMeanEmbedding(embeddings);
        content.setVecKeywords(meanEmbedding);
      }

      CompletableFuture<Embedding> titleEmbedding = this.getEmbedding(article.getTitle());
      CompletableFuture<Embedding> descriptionEmbedding = this.getEmbedding(article.getDescription());
      CompletableFuture.allOf(titleEmbedding, descriptionEmbedding).join();

      logger.info("==> Calculated embeddings for title, description and keywords");
      try {
        content.setVecTitle(titleEmbedding.get().getEmbedding());
        content.setVecDescription(descriptionEmbedding.get().getEmbedding());
      } catch (Exception e) {
        logger.error(e.getMessage());
      }

      return content;
    }).collect(Collectors.toList());

    List<IndexQuery> indexQueries = contents.stream().map(content -> 
      new IndexQueryBuilder()
        .withId(content.getId().toString())
        .withObject(content)
        .build()
      ).collect(Collectors.toList());
    
    logger.info("------------ Indexing data ------------");
    List<IndexedObjectInformation> result = this.elasticsearchOperations.bulkIndex(indexQueries, Content.class);
    
    logger.info(result.get(0).toString());
    logger.info("==> Count -> {}", this.contentRepository.count());
  }

  public void indexPeople() {

  }

  @Async
  private CompletableFuture<Embedding> getEmbedding(String text) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(embeddingUrl).queryParam("text", text);
    Embedding embedding = restTemplate.getForObject(builder.toUriString(), Embedding.class);
    return CompletableFuture.completedFuture(embedding);
  }

  private List<Float> calculateMeanEmbedding(List<List<Float>> embeddings) {
    int dimensions = embeddings.get(0).size();

    return IntStream.range(0, dimensions)
      .mapToObj(i -> (float)embeddings.stream()
        .mapToDouble(embedding -> embedding.get(i))
        .average()
        .orElse(0.0)
      ).collect(Collectors.toList());
  }
}
