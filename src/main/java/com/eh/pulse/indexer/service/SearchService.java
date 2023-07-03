package com.eh.pulse.indexer.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import com.eh.pulse.indexer.model.Content;
import com.eh.pulse.indexer.model.Embedding;
import com.eh.pulse.indexer.utils.GetEmbeddings;

import co.elastic.clients.elasticsearch._types.KnnQuery;

@Service
public class SearchService {
  
  private final GetEmbeddings getEmbeddings;
  private final ElasticsearchOperations elasticsearchOperations;

  public SearchService(GetEmbeddings getEmbeddings, ElasticsearchOperations elasticsearchOperations) {
    this.getEmbeddings = getEmbeddings;
    this.elasticsearchOperations = elasticsearchOperations;
  }

  public List<Content> search(String query) {
    Embedding embedding = new Embedding();
    try {
      embedding = this.getEmbeddings.getEmbedding(query).get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }

    KnnQuery.Builder knnQueryBuilderTitle = getKnnQueryBuilder(query, "vec-title", embedding);
    KnnQuery.Builder knnQueryBuilderDescription = getKnnQueryBuilder(query, "vec-description", embedding);
    // KnnQuery.Builder knnQueryBuilderComments = getKnnQueryBuilder(query, "vec-comments", embedding);

    NativeQuery nativeQuery = new NativeQueryBuilder()
      .withSearchType(null)
      .withQuery(q -> q
        
        .match(m -> m
          .field("title")
          .query(query)
          .boost(0.1f)
        )
      )
      .withKnnQuery(knnQueryBuilderTitle.boost(1.2f).build())
      .withKnnQuery(knnQueryBuilderDescription.boost(1f).build())
      // .withKnnQuery(knnQueryBuilderComments.boost(0.8f).build())
      .build();

    SearchHits<Content> search = this.elasticsearchOperations.search(nativeQuery, Content.class);
    List<Content> result = new ArrayList<>();
    search.forEach(hit -> {
      Content content = hit.getContent();
      result.add(content);
    });

    return result;
  }

  private KnnQuery.Builder getKnnQueryBuilder(String query, String vector, Embedding embedding) {
    KnnQuery.Builder knnQueryBuilder = new KnnQuery.Builder();
    knnQueryBuilder.field(vector);
    knnQueryBuilder.queryVector(embedding.getEmbedding());
    knnQueryBuilder.numCandidates(10);
    knnQueryBuilder.k(10);
    return knnQueryBuilder;
  }
}
