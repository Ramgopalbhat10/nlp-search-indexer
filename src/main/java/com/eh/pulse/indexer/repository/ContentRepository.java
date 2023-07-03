package com.eh.pulse.indexer.repository;


import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.eh.pulse.indexer.model.Content;

public interface ContentRepository extends ElasticsearchRepository<Content, String> {
  
}
